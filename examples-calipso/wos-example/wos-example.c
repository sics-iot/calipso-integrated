#include "cc2420.h"
#include "dev/watchdog.h"
#include "dev/uart1.h"
#include "lib/ringbuf.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "contiki-net.h"

#include "energest.h"

#if WITH_COAP == 3
#include "er-coap-03-engine.h"
#elif WITH_COAP == 6
#include "er-coap-06-engine.h"
#elif WITH_COAP == 7
#include "er-coap-07-engine.h"
#elif WITH_COAP == 12
#include "er-coap-12-engine.h"
#elif WITH_COAP == 13
#include "er-coap-13-engine.h"
#else
#error "CoAP version defined by WITH_COAP not implemented"
#endif


#define DEBUG 1
#if DEBUG
#define PRINTF(...) printf(__VA_ARGS__)
#define PRINT6ADDR(addr) PRINTF("[%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x]", ((uint8_t *)addr)[0], ((uint8_t *)addr)[1], ((uint8_t *)addr)[2], ((uint8_t *)addr)[3], ((uint8_t *)addr)[4], ((uint8_t *)addr)[5], ((uint8_t *)addr)[6], ((uint8_t *)addr)[7], ((uint8_t *)addr)[8], ((uint8_t *)addr)[9], ((uint8_t *)addr)[10], ((uint8_t *)addr)[11], ((uint8_t *)addr)[12], ((uint8_t *)addr)[13], ((uint8_t *)addr)[14], ((uint8_t *)addr)[15])
#define PRINTLLADDR(lladdr) PRINTF("[%02x:%02x:%02x:%02x:%02x:%02x]",(lladdr)->addr[0], (lladdr)->addr[1], (lladdr)->addr[2], (lladdr)->addr[3],(lladdr)->addr[4], (lladdr)->addr[5])
#else
#define PRINTF(...)
#define PRINT6ADDR(addr)
#define PRINTLLADDR(addr)
#endif

/* TODO: This server address is hard-coded as we do not have discovery. */
#define SERVER_NODE(ipaddr)   uip_ip6addr(ipaddr, 0xaaaa, 0, 0, 0, 0x0212, 0x7401, 0x0001, 0x0101)

#define LOCAL_PORT      UIP_HTONS(COAP_DEFAULT_PORT+1)
#define REMOTE_PORT     UIP_HTONS(COAP_DEFAULT_PORT)

#define PUSH_INTERVAL   600
#define MAX_URL_SIZE	128
#define MAX_ID_SIZE		8
static char url_str_buf[MAX_URL_SIZE];
static char id_str_buf[MAX_ID_SIZE];

typedef enum {
	CMD_NONE			= 0,
	CMD_SFSEND,
	CMD_AT_GETFWVER,
	CMD_AT_SFOXCONF, 
	CMD_ATCMD
} sfox_cmd_t;

typedef struct str_buf_t {
	char *str;
	uint8_t len;
	uint8_t max_len;
} str_buf_t ;

static void init_strbuf( str_buf_t *url, char *buf, uint8_t max_len ) {
	url->len = 0;
	url->max_len = max_len;
	url->str = buf;
	url->str[url->len] = 0x00;
	url->str[url->max_len-1] = 0x00;
}

static void concat_strbuf( str_buf_t *url, char *buf, uint8_t len ) {
	int i;
	for (i=0; ((i<len&&len>0) || (0==len && 0!=buf[i])) && (url->len<(url->max_len-1));++i)
		url->str[url->len++] = buf[i];
	url->str[url->len] = 0x00;
}

static void concat_formated( str_buf_t *url, const char *format, ...) {
  int res;
  va_list ap;
  va_start(ap, format);
  res = vsnprintf(url->str, url->max_len-1, format, ap);
  va_end(ap);
  url->len = res;
  url->str[url->len] = 0x00;
}

typedef struct cmd_t {
	sfox_cmd_t cmd;
	char payload[24];
	uint8_t paylen;
	uint8_t tx_pow;
	uint8_t cca_thres;
} cmd_t;

static int sigofx_input_byte(unsigned char c);

static unsigned char rxBuf[32];
static uint16_t rxLen = 0; 
static unsigned char serial_buf[32];
static struct ringbuf ringb;
static cmd_t sigfox_cmd;

/*---------------------------------------------------------------------------*/
#define write_atok() write_sf("\r\nOK\r\n",6)
#define write_sfok() write_sf("OK;",3)
#define write_sfsent() write_sf("SENT;",5)

static const char *sf_send_str = "SFM";
static const char *at_fwver_str = "ATI13";
static const char *at_cnf_str = "AT$IF=";
static const char *at_gen_str = "AT";

static const char *str_table[4];
static const uint8_t str_len_table[4] = { 3,5,6,2 };

static void init_sigfox_com() {
	str_table[0] = sf_send_str;
	str_table[1] = at_fwver_str;
	str_table[2] = at_cnf_str;	
	str_table[3] = at_gen_str;
	rxLen = 0;
	ringbuf_init(&ringb,serial_buf,32);
	uart1_set_input(sigofx_input_byte);
}

static sfox_cmd_t match(unsigned char *str,uint16_t len) {
	int i,j;
	sfox_cmd_t cmd = CMD_NONE;
	for (i=0;i<4 && CMD_NONE==cmd;++i) {
		if (str_len_table[i]<=len) cmd=i+1;
		for (j=0;(j<str_len_table[i]) && ((i+1)==cmd);++j) 
			if ( str_table[i][j] != str[j] ) cmd = CMD_NONE;
	}
	return cmd;
}

static void write_sf(char *data, uint8_t len) {
	int i;
	for (i=0;i<len;++i) 
		uart1_writeb((unsigned char)data[i]);
}

//#define H2C(_h) 		( ((_h)>9)?((_h)+'A'-10):((_h)+'0') )
#define H2C(_h) 		hextable[_h]
#define HEX2CHAR_L(_h)	H2C((_h)&0x0F)
#define HEX2CHAR_H(_h)	H2C(((unsigned char)(_h))>>4)
static const char hextable[16] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'}; 

static int hex2str(char *str_buf, unsigned char *data, uint8_t len) {
	int i,j;
	for (i=0,j=0;i<len&&i<12;++i) {
		str_buf[j++]=HEX2CHAR_H(data[i]);
		str_buf[j++]=HEX2CHAR_L(data[i]);
	}
	return j;
}

static uint8_t parse_user_value(unsigned char *buf, uint8_t len, uint8_t *result) {
	uint16_t tmp = 0;
	uint8_t i;
	
	for (i=0;i<len;++i) {
		if ( '0' <= buf[i] && buf[i] <= '9' ) {
			tmp = tmp*10 + ( buf[i] - '0' );
		} else break;
	}
	*result = (uint8_t)tmp; 
	return i;
}

static cmd_t *get_cmd() {
	int res;
	
	sigfox_cmd.cmd = CMD_NONE;
    while ( (CMD_NONE==sigfox_cmd.cmd) && (res = ringbuf_get(&ringb)) != -1 ) {
		char c =  ((char)res);
		if ( '\n' == c || ';' == c ) { // break char
			if ( 0 < rxLen ) {
				sigfox_cmd.cmd = match(rxBuf,rxLen);
				if ( CMD_SFSEND == sigfox_cmd.cmd ) {
					uint8_t len = rxBuf[3];
					if ( (4+len) <= rxLen ) {
						sigfox_cmd.paylen = hex2str(sigfox_cmd.payload, &(rxBuf[4]), len);
					} else {
						sigfox_cmd.cmd = CMD_NONE;
						continue;
					}
				} else if ( CMD_AT_SFOXCONF == sigfox_cmd.cmd ) {
					// AT$IF=868200000,200,20
					// start parsing at rxBuf[16] = "200,20"
					uint8_t idx = parse_user_value(&(rxBuf[16]),5,&(sigfox_cmd.cca_thres));
					parse_user_value(&(rxBuf[idx+17]),3,&(sigfox_cmd.tx_pow));
				}
			}
			rxLen = 0;
		} else if (0==rxLen && (c<'A' || c>'Z') ) ;//ignore 
		else rxBuf[rxLen++] = c;
	}
	
	return &sigfox_cmd;
}

/*************************************************************************************/

PROCESS(coap_client_example, "COAP Client Example");
PROCESS(push_stats_thread, "radio stack stats");
AUTOSTART_PROCESSES(&coap_client_example);


uip_ipaddr_t server_ipaddr;


/* Example URIs that can be queried. */
#define NUMBER_OF_URLS 4
#define STATS_DATA_SIZE 16
/* leading and ending slashes only for demo purposes, get cropped automatically when setting the Uri-Path */
static char* service_urls[NUMBER_OF_URLS] = {"/rpl", "/presence", "/dc", "/error/in//path"};
static char* service_fastprk_url = "/parking/";
//static char* service_fastprk_url = "/leds";

static str_buf_t request_url;
static str_buf_t url_id;
static str_buf_t stats;
static char stats_buf[STATS_DATA_SIZE];
static unsigned long rx_start_duration;
static unsigned long tx_start_duration;
//static unsigned long all_cpu;
//static unsigned long all_lpm;
/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void
client_id_handler(void *response)
{
  const uint8_t *buf;
  coap_packet_t* packet = response;
  if ( REST.status.OK == packet->code ) {
	  uint8_t len = coap_get_payload(response, &buf);
	  init_strbuf(&url_id,id_str_buf,MAX_ID_SIZE);
	  concat_strbuf(&url_id,(char *)buf,len);
  }
  printf("ID: len=%d %s\n", url_id.len, (char *)(url_id.str));
  printf("CODE: %d\n", packet->code);
}

void
client_dummy_handler(void *response) {}

static coap_packet_t *build_coap_msg(str_buf_t* url, coap_method_t method, char *msg, uint8_t len, char* query, uint8_t query_len ) {
	static coap_packet_t request[1]; /* This way the packet can be treated as pointer as usual. */
	printf("--Requesting %s--\n", url->str);
	/* prepare request, TID is set by COAP_BLOCKING_REQUEST() */
	coap_init_message(request, COAP_TYPE_CON, method, 0 );
	coap_set_header_uri_path(request, url->str);
	if ( query_len > 0 )
		coap_set_header_uri_query(request,query);
	coap_set_payload(request, (uint8_t *)msg, len);

	PRINT6ADDR(&server_ipaddr);
	PRINTF(" : %u\n", UIP_HTONS(REMOTE_PORT));

	printf("\n--Done--\n");
	return request;
}

static void build_url( str_buf_t *url, char *res_name ) {
	init_strbuf(&request_url,url_str_buf,MAX_URL_SIZE);
	concat_strbuf(&request_url,service_fastprk_url,0);
	concat_strbuf(&request_url,url_id.str,url_id.len);
	concat_strbuf(&request_url,res_name,0);
}

static void get_dutycycle_str( str_buf_t *strbuf ) {
	uint32_t all_transmit = energest_type_time(ENERGEST_TYPE_TRANSMIT) - tx_start_duration;
	uint32_t all_listen = energest_type_time(ENERGEST_TYPE_LISTEN) - rx_start_duration;
	//uint32_t all_time = energest_type_time(ENERGEST_TYPE_CPU) + energest_type_time(ENERGEST_TYPE_LPM);
	uint32_t energy_mj = 20 * 3 * (all_transmit + all_listen) / RTIMER_ARCH_SECOND;
	//uint16_t dc_int = (int)((100L * (all_transmit + all_listen)) / all_time);
	//uint16_t dc_dec = (int)((10000L * (all_transmit + all_listen) / all_time) - ((100L * (all_transmit + all_listen) / all_time) * 100));

	// stats
	init_strbuf(strbuf,stats_buf,STATS_DATA_SIZE);
	//int snprintf ( char * s, size_t n, const char * format, ... );
	concat_formated( strbuf, "%lu", energy_mj );
}

static void get_rplstats_str( str_buf_t *strbuf ) {
	uint32_t parentId = 10uL;
	uint32_t metric = 30uL;
	init_strbuf(strbuf,stats_buf,STATS_DATA_SIZE);
	concat_formated( strbuf, "%lu %lu", parentId , metric );
}

PROCESS_THREAD(push_stats_thread, ev, data) {
	static coap_packet_t *pkt;
	static struct etimer et;
	PROCESS_BEGIN();
	etimer_set(&et, PUSH_INTERVAL * CLOCK_SECOND);
	printf("\n--start push process--\n");
	while (1) {
		PROCESS_WAIT_EVENT();
		if(ev == PROCESS_EVENT_TIMER) {
			printf("--Stats push timer--\n");

			// RPL STATS
			get_rplstats_str( &stats );
			build_url( &request_url, service_urls[0] );
			pkt = build_coap_msg(&request_url, COAP_PUT, stats.str, stats.len,NULL,0);
			COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);

			// DUTY CYCLE
			get_dutycycle_str( &stats );
			build_url( &request_url, service_urls[2] );
			pkt = build_coap_msg(&request_url, COAP_PUT, stats.str, stats.len,NULL,0);
			COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);
			printf("\n--Stats push Done--\n");
			etimer_reset(&et);
		}
	}
	PROCESS_END();
}

PROCESS_THREAD(coap_client_example, ev, data)
{
  static int i = 0;
  static coap_packet_t *pkt;
  static struct etimer et;
  PROCESS_BEGIN();
  rx_start_duration = energest_type_time(ENERGEST_TYPE_LISTEN);
  tx_start_duration = energest_type_time(ENERGEST_TYPE_TRANSMIT);
  init_strbuf(&url_id,id_str_buf,MAX_ID_SIZE);
  //static coap_packet_t request[1]; /* This way the packet can be treated as pointer as usual. */
  SERVER_NODE(&server_ipaddr);

  /* receives all CoAP messages */
  coap_receiver_init();

  etimer_set(&et, 30 * CLOCK_SECOND);
  while (0==url_id.len) {
	PROCESS_WAIT_EVENT();
	if(ev == PROCESS_EVENT_TIMER) {
		init_strbuf(&request_url,url_str_buf,MAX_URL_SIZE);
		concat_strbuf(&request_url,service_fastprk_url,0);

		pkt = build_coap_msg(&request_url, COAP_POST, NULL, 0,NULL,0);
		COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_id_handler);
		etimer_reset(&et);
	}
  }

  for (i=0;i<3;++i) {
	  build_url( &request_url, service_urls[i] );
	  pkt = build_coap_msg(&request_url, COAP_POST, NULL, 0,NULL,0);
	  COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);
  }

  init_sigfox_com();
  process_start(&push_stats_thread,NULL);// */

  while(1) {
    cmd_t *cmd;
    if ( 0 == ringbuf_elements(&ringb) )
		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_POLL);
	
    cmd = get_cmd();
	if ( CMD_SFSEND == cmd->cmd ) {
		write_sfok();
		//sigfox_event_handler(&resource_event,cmd->payload, cmd->paylen);
		//send_coap_msg(service_fastprk_url, cmd->payload, cmd->paylen);
		printf("\n--PARKING event--\n");
		build_url( &request_url, service_urls[1] );
		pkt = build_coap_msg(&request_url, COAP_PUT, cmd->payload, cmd->paylen,NULL,0);
		COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);

		write_sfsent();
		cmd->cmd = CMD_NONE;
	}
	
    switch ( cmd->cmd ) {
		case CMD_AT_GETFWVER: {
			// reset
			watchdog_reboot();
		} break;
		case CMD_AT_SFOXCONF: {
			// readio settings
			cc2420_set_cca_threshold(cmd->cca_thres);
			cc2420_set_txpower(cmd->tx_pow);
		} // FALLTHROUGH
		case CMD_ATCMD: write_atok(); break;
		default:
        case CMD_NONE: break;
	}

  } /* while (1) */

  PROCESS_END();
}

static int sigofx_input_byte(unsigned char c) {
	ringbuf_put(&ringb,c);
	process_poll(&coap_client_example);
	return 1;
}
