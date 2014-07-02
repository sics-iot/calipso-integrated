#include "cc2420.h"
#include "dev/watchdog.h"
#include "dev/uart1.h"
#include "lib/ringbuf.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "contiki-net.h"

//#include "dev/button-sensor.h"

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


#define DEBUG 0
#if DEBUG
#define PRINTF(...) printf(__VA_ARGS__)
#define PRINT6ADDR(addr) PRINTF("[%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x]", ((uint8_t *)addr)[0], ((uint8_t *)addr)[1], ((uint8_t *)addr)[2], ((uint8_t *)addr)[3], ((uint8_t *)addr)[4], ((uint8_t *)addr)[5], ((uint8_t *)addr)[6], ((uint8_t *)addr)[7], ((uint8_t *)addr)[8], ((uint8_t *)addr)[9], ((uint8_t *)addr)[10], ((uint8_t *)addr)[11], ((uint8_t *)addr)[12], ((uint8_t *)addr)[13], ((uint8_t *)addr)[14], ((uint8_t *)addr)[15])
#define PRINTLLADDR(lladdr) PRINTF("[%02x:%02x:%02x:%02x:%02x:%02x]",(lladdr)->addr[0], (lladdr)->addr[1], (lladdr)->addr[2], (lladdr)->addr[3],(lladdr)->addr[4], (lladdr)->addr[5])
#else
#define PRINTF(...)
#define PRINT6ADDR(addr)
#define PRINTLLADDR(addr)
#endif

/* TODO: This server address is hard-coded for Cooja. */
#define SERVER_NODE(ipaddr)   uip_ip6addr(ipaddr, 0xaaaa, 0, 0, 0, 0x0212, 0x7402, 0x0002, 0x0202) /* cooja2 */

#define LOCAL_PORT      UIP_HTONS(COAP_DEFAULT_PORT+1)
#define REMOTE_PORT     UIP_HTONS(COAP_DEFAULT_PORT)

#define TOGGLE_INTERVAL 10


typedef enum {
	CMD_NONE			= 0,
	CMD_SFSEND,
	CMD_AT_GETFWVER,
	CMD_AT_SFOXCONF, 
	CMD_ATCMD
} sfox_cmd_t;

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

sfox_cmd_t match(unsigned char *str,uint16_t len) {
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
AUTOSTART_PROCESSES(&coap_client_example);


uip_ipaddr_t server_ipaddr;
static struct etimer et;

/* Example URIs that can be queried. */
#define NUMBER_OF_URLS 4
/* leading and ending slashes only for demo purposes, get cropped automatically when setting the Uri-Path */
char* service_urls[NUMBER_OF_URLS] = {".well-known/core", "/actuators/toggle", "battery/", "error/in//path"};
char* service_fastprk_url = "fastprk/";

/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void
client_chunk_handler(void *response)
{
  const uint8_t *chunk;

  int len = coap_get_payload(response, &chunk);
  printf("|%.*s", len, (char *)chunk);
}


static coap_packet_t *build_coap_msg(char* url, uint8_t *msg, uint8_t len) {
	static coap_packet_t request[1]; /* This way the packet can be treated as pointer as usual. */
	printf("--Requesting %s--\n", url);
	/* prepare request, TID is set by COAP_BLOCKING_REQUEST() */
	coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0 );
	coap_set_header_uri_path(request, url);

	coap_set_payload(request, (uint8_t *)msg, len);

	PRINT6ADDR(&server_ipaddr);
	PRINTF(" : %u\n", UIP_HTONS(REMOTE_PORT));

	//COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, client_chunk_handler);

	printf("\n--Done--\n");
	return request;
}

PROCESS_THREAD(coap_client_example, ev, data)
{
  PROCESS_BEGIN();

  //static coap_packet_t request[1]; /* This way the packet can be treated as pointer as usual. */
  SERVER_NODE(&server_ipaddr);

  /* receives all CoAP messages */
  coap_receiver_init();
  init_sigfox_com();

  etimer_set(&et, TOGGLE_INTERVAL * CLOCK_SECOND);

  while(1) {
    cmd_t *cmd;
    if ( 0 == ringbuf_elements(&ringb) )
		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_POLL);
	
    cmd = get_cmd();
	if ( CMD_SFSEND == cmd->cmd ) {
		write_sfok();
          //sigfox_event_handler(&resource_event,cmd->payload, cmd->paylen);
		  //send_coap_msg(service_fastprk_url, cmd->payload, cmd->paylen);
		  
		  COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, build_coap_msg(service_fastprk_url, cmd->payload, cmd->paylen), client_chunk_handler);

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
