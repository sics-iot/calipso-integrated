#include "cc2420.h"
#include "dev/watchdog.h"
#include "dev/uart1.h"
#include "lib/ringbuf.h"
#include "pt-sem.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "contiki-net.h"

#include "simple-energest.h"
#include "net/rpl/rpl.h"
#include "net/uip-ds6-nbr.h"

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

// TODO: This server address is hard-coded as we do not have discovery.
//#define SERVER_NODE(ipaddr)   uip_ip6addr(ipaddr, 0xaaaa, 0, 0, 0, 0x0212, 0x7403, 0x0003, 0x0303)
#define SERVER_NODE(ipaddr)   uip_ip6addr(ipaddr, 0xaaaa, 0, 0, 0, 0, 0, 0, 0x0001)

#define LOCAL_PORT      UIP_HTONS(COAP_DEFAULT_PORT+1)
#define REMOTE_PORT     UIP_HTONS(COAP_DEFAULT_PORT)

#define PUSH_INTERVAL   30
#define MAX_URL_SIZE	128
#define MAX_ID_SIZE	15
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
static unsigned char serial_buf[32]; // ringbuffer internal buffer, may be oversized
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
				rxBuf[rxLen++] = c;
				sigfox_cmd.cmd = match(rxBuf,rxLen);
				if ( CMD_SFSEND == sigfox_cmd.cmd ) {
					uint8_t len = rxBuf[3];
					if ( (5+len) <= rxLen ) {
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

PROCESS(main_wos_process, "COAP Client Example");
PROCESS(sigfox_process, "radio stack stats");
PROCESS(simple_energest_process, "Simple Energest");
AUTOSTART_PROCESSES(&main_wos_process);

uip_ipaddr_t server_ipaddr;

#define NUMBER_OF_RES 4
#define STATS_DATA_SIZE 16
// Example URIs that can be queried.
// leading and ending slashes only for demo purposes, get cropped automatically when setting the Uri-Path
enum {
	RPL_RESOURCE_IDX = 0,
	FASTPRK_RESOURCE_IDX,
	DUTYCYCLE_RESOURCE_IDX,
	PDR_RESOURCE_IDX
};
static char* service_urls[NUMBER_OF_RES] = {"/rpl", "/presence", "/dc", "/pdr"};
static uint8_t reg_resource_idx;
static uint8_t reg_resource_status[NUMBER_OF_RES];

static str_buf_t request_url;
static str_buf_t url_id;
static str_buf_t stats;
static char stats_buf[STATS_DATA_SIZE];
static struct pt_sem mutex;

/*	Statistics CoAP	*/
static uint32_t last_tx, last_rx, last_time;
static uint32_t delta_tx, delta_rx, delta_time;
static uint32_t curr_tx, curr_rx, curr_time;

static uint32_t tx_pkts;

static uint8_t rpl_parentId;
static uint16_t rpl_parentLinkMetric;

// These functions will be passed to COAP_BLOCKING_REQUEST() to handle responses.
void
client_id_handler(void *response)
{
  const uint8_t *buf;
  coap_packet_t* packet = response;

  if ( REST.status.CREATED == packet->code ) {
	uint8_t len = coap_get_header_location_path(response, &buf); // the identifier is passed in the location path
	init_strbuf(&url_id,id_str_buf,MAX_ID_SIZE);
	concat_strbuf(&url_id,(char *)buf,len);
	printf("CODE=%d\n", packet->code);
	printf("ID: %s\n", id_str_buf);
  }
}

void
register_res_reply_handler(void *response) {
	coap_packet_t* packet = response;
	if ( REST.status.CREATED == packet->code ) {
		reg_resource_status[reg_resource_idx] = 1;
	}
	printf("IDX=%d CODE=%d\n",reg_resource_idx, packet->code);
}

void
client_dummy_handler(void *response) {}

// Request builing auxiliary functions
static coap_packet_t *build_coap_msg(str_buf_t* url, coap_method_t method, char *msg, uint8_t len, char* query, uint8_t query_len ) {
	static coap_packet_t request[1]; // This way the packet can be treated as pointer as usual.
	//printf("--Requesting %s--\n", url->str);
	// prepare request, TID is set by COAP_BLOCKING_REQUEST()
	coap_init_message(request, COAP_TYPE_CON, method, 0 );
	coap_set_header_uri_path(request, url->str);
	if ( query_len > 0 )
		coap_set_header_uri_query(request,query);
	coap_set_payload(request, (uint8_t *)msg, len);

	//PRINT6ADDR(&server_ipaddr);
	//PRINTF(" : %u\n", UIP_HTONS(REMOTE_PORT));
	//printf("\n--Done--\n");
	tx_pkts++;
	return request;
}

static void build_url( str_buf_t *url, char *res_name ) {
	init_strbuf(&request_url,url_str_buf,MAX_URL_SIZE);
	concat_strbuf(&request_url,"",0);
	concat_strbuf(&request_url,url_id.str,url_id.len);
	concat_strbuf(&request_url,res_name,0);
}

// Theses functions retrieve the radio stack information to send to the server
/*static void get_dutycycle_str( str_buf_t *strbuf ) {
	uint32_t all_transmit = energest_type_time(ENERGEST_TYPE_TRANSMIT) - tx_start_duration;
	uint32_t all_listen = energest_type_time(ENERGEST_TYPE_LISTEN) - rx_start_duration;
	//uint32_t all_time = energest_type_time(ENERGEST_TYPE_CPU) + energest_type_time(ENERGEST_TYPE_LPM);
	uint32_t energy_mj = 20 * 3 * (all_transmit + all_listen) / RTIMER_ARCH_SECOND;
	//uint16_t dc_int = (int)((100L * (all_transmit + all_listen)) / all_time);
	//uint16_t dc_dec = (int)((10000L * (all_transmit + all_listen) / all_time) - ((100L * (all_transmit + all_listen) / all_time) * 100));

	// stats
	init_strbuf(strbuf,stats_buf,STATS_DATA_SIZE);
	concat_formated( strbuf, "%lu", energy_mj );
}*/

static void get_rplstats_str( str_buf_t *strbuf ) {
	rpl_parentId = (uint8_t) rpl_get_parent_ipaddr((rpl_parent_t *) rpl_get_any_dag()->preferred_parent)->u8[sizeof(uip_ipaddr_t)-1];
	rpl_parentLinkMetric = (uint16_t) rpl_get_parent_link_metric((uip_lladdr_t *)
		uip_ds6_nbr_lladdr_from_ipaddr((uip_ipaddr_t *) rpl_get_any_dag()->preferred_parent));
	init_strbuf(strbuf,stats_buf,STATS_DATA_SIZE);
	concat_formated( strbuf, "%u %u", rpl_parentId , rpl_parentLinkMetric);
	//concat_formated( strbuf, "%u", rpl_parentId);
}

static void get_pdrstats_str( str_buf_t *strbuf ) {
	init_strbuf(strbuf,stats_buf,STATS_DATA_SIZE);
	concat_formated( strbuf, "%lu", tx_pkts );
}

PROCESS_THREAD(sigfox_process, ev, data) {
	static coap_packet_t *pkt;
	//static struct etimer et;
	PROCESS_BEGIN();

	init_sigfox_com();
	// wait for sensor commands
	while(1) {
		cmd_t *cmd;
		if ( 0 == ringbuf_elements(&ringb) )
			PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_POLL);

		cmd = get_cmd();
		if ( CMD_SFSEND == cmd->cmd ) { // sensor message received
			write_sfok();
			PT_SEM_WAIT(process_pt, &mutex);
			printf("\n--PARKING event START--\n");
			if ( 1 == reg_resource_status[FASTPRK_RESOURCE_IDX] ) {
				build_url( &request_url, service_urls[FASTPRK_RESOURCE_IDX] );
				pkt = build_coap_msg(&request_url, COAP_PUT, cmd->payload, cmd->paylen,NULL,0);
				COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);
			}
			// introduce a delay to emulate SF behaviour (up to 5 secs)
			//etimer_set(&et, 1 * CLOCK_SECOND);
			//do PROCESS_WAIT_EVENT(); while (ev != PROCESS_EVENT_TIMER);

			write_sfsent();
			cmd->cmd = CMD_NONE;
			printf("\n--PARKING event END--\n");
			PT_SEM_SIGNAL(process_pt, &mutex);
		}

		switch ( cmd->cmd ) {
			case CMD_AT_GETFWVER: {
				// reset
				watchdog_reboot();
			} break;
			case CMD_AT_SFOXCONF: {
				// radio settings
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

PROCESS_THREAD(simple_energest_process, ev, data)
{
  static struct etimer periodic;
  static coap_packet_t *pkt;
  static uint32_t fraction;
  PROCESS_BEGIN();
  //coap_receiver_init();
  etimer_set(&periodic, 60 * CLOCK_SECOND);

  while(1) {
    PROCESS_WAIT_UNTIL(etimer_expired(&periodic));
    etimer_reset(&periodic);
    fraction = simple_energest_step();
    printf("Duty Cycle: %lu\%\n", fraction);
    init_strbuf(&stats,stats_buf,STATS_DATA_SIZE);
    concat_formated( &stats, "%lu", fraction );
    build_url( &request_url, service_urls[DUTYCYCLE_RESOURCE_IDX] );
    pkt = build_coap_msg(&request_url, COAP_PUT, stats.str, stats.len,NULL,0);
    COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
void simple_energest_start() {
  energest_flush();
  last_tx = energest_type_time(ENERGEST_TYPE_TRANSMIT);
  last_rx = energest_type_time(ENERGEST_TYPE_LISTEN);
  last_time = energest_type_time(ENERGEST_TYPE_CPU) + energest_type_time(ENERGEST_TYPE_LPM);
  process_start(&simple_energest_process, NULL);
}

/*---------------------------------------------------------------------------*/
static uint32_t simple_energest_step() {
  static uint16_t cnt;
  uint32_t fraction;

  energest_flush();

  curr_tx = energest_type_time(ENERGEST_TYPE_TRANSMIT);
  curr_rx = energest_type_time(ENERGEST_TYPE_LISTEN);
  curr_time = energest_type_time(ENERGEST_TYPE_CPU) + energest_type_time(ENERGEST_TYPE_LPM);

  delta_tx = curr_tx - last_tx;
  delta_rx = curr_rx - last_rx;
  delta_time = curr_time - last_time;

  last_tx = curr_tx;
  last_rx = curr_rx;
  last_time = curr_time;

  //return (1000ul*(delta_tx+delta_rx))/delta_time;
  return (100ul*(curr_tx+curr_rx))/curr_time;
}

PROCESS_THREAD(main_wos_process, ev, data)
{
	static coap_packet_t *pkt;
	static struct etimer et;

	PROCESS_BEGIN();
	memset( reg_resource_status, 0, NUMBER_OF_RES);
	PT_SEM_INIT(&mutex, 1);
	tx_pkts = 0;
	init_strbuf(&url_id,id_str_buf,MAX_ID_SIZE);
	SERVER_NODE(&server_ipaddr);
	process_start(&sigfox_process,NULL);
	simple_energest_start();

	/* receives all CoAP messages */
	coap_receiver_init();
	etimer_set(&et, 30 * CLOCK_SECOND);

	// register the node and obtain identifier
	while (1) {
		PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et));
		printf("Trying node registration at the server...");
		init_strbuf(&request_url,url_str_buf,MAX_URL_SIZE);
		concat_strbuf(&request_url,"/parking/",0);
		pkt = build_coap_msg(&request_url, COAP_POST, NULL, 0, NULL, 0);
		COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_id_handler);
		// retry until successful
		if ( 0==url_id.len ) {
			etimer_restart(&et);
		} else {
			printf("Done\n");
			break;
		}
	}

	// register to each resource
	for (reg_resource_idx=0;reg_resource_idx<NUMBER_OF_RES;) {
	  build_url( &request_url, service_urls[reg_resource_idx] );
	  pkt = build_coap_msg(&request_url, COAP_POST, NULL, 0,NULL,0);
	  COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, register_res_reply_handler);
	  // retry until successful
	  if ( 0==reg_resource_status[reg_resource_idx] ) {
		  etimer_restart(&et);
		  do PROCESS_WAIT_EVENT(); while (ev != PROCESS_EVENT_TIMER);
	  } else reg_resource_idx++;
	}

	etimer_set(&et, PUSH_INTERVAL * CLOCK_SECOND);
	printf("\n--start put process--\n");
	while (1) {
		PROCESS_WAIT_EVENT();
		if (ev == PROCESS_EVENT_TIMER) {
			PT_SEM_WAIT(process_pt, &mutex);
			printf("--Stats put timer--\n");
	
			// RPL STATS
			get_rplstats_str( &stats );
			build_url( &request_url, service_urls[RPL_RESOURCE_IDX] );
			pkt = build_coap_msg(&request_url, COAP_PUT, stats.str, stats.len,NULL,0);
			COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);
	
			// DUTY CYCLE is handled by the simple-energest process
			//get_dutycycle_str( &stats );
			//build_url( &request_url, service_urls[DUTYCYCLE_RESOURCE_IDX] );
			//pkt = build_coap_msg(&request_url, COAP_PUT, stats.str, stats.len,NULL,0);
			//COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);

			// PDR
			get_pdrstats_str( &stats );
			build_url( &request_url, service_urls[PDR_RESOURCE_IDX] );
			pkt = build_coap_msg(&request_url, COAP_PUT, stats.str, stats.len,NULL,0);
			COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);

			printf("\n--Stats put Done--\n");
			etimer_reset(&et);
			PT_SEM_SIGNAL(process_pt, &mutex);
		}
	}

	PROCESS_END();
}

static int sigofx_input_byte(unsigned char c) {
	ringbuf_put(&ringb,c);
	process_poll(&sigfox_process);
	return 1;
}
