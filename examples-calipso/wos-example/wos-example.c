#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "dev/uart1.h"
#include "lib/ringbuf.h"
#include "contiki.h"
#include "contiki-net.h"
#include "cc2420.h"
#include "dev/watchdog.h"
#include "erbium.h"

/* For CoAP-specific example: not required for normal RESTful Web service. */
#if WITH_COAP == 3
#include "er-coap-03.h"
#elif WITH_COAP == 7
#include "er-coap-07.h"
#elif WITH_COAP == 12
#include "er-coap-12.h"
#elif WITH_COAP == 13
#include "er-coap-13.h"
#else
#warning "Erbium example without CoAP-specifc functionality"
#endif /* CoAP-specific example */

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

static void
sigfox_event_handler(resource_t *r, char *msg, uint8_t len)
{
  static uint16_t event_counter = 0;

  ++event_counter;
  /* Build notification. */
  coap_packet_t notification[1]; /* This way the packet can be treated as pointer as usual. */
  coap_init_message(notification, COAP_TYPE_CON, REST.status.OK, 0 );
  coap_set_payload(notification, msg, len);
  
  /* Notify the registered observers with the given message type, observe option, and payload. */
  REST.notify_subscribers(r, event_counter, notification);
}

/*
 * Example for an event resource.
 * Additionally takes a period parameter that defines the interval to call [name]_periodic_handler().
 * A default post_handler takes care of subscriptions and manages a list of subscribers to notify.
 */
EVENT_RESOURCE(event, METHOD_GET, "button", "title=\"Event demo\";obs");

void
event_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  REST.set_header_content_type(response, REST.type.TEXT_PLAIN);
  /* Usually, a CoAP server would response with the current resource representation. */
  const char *msg = "It's eventful!";
  REST.set_response_payload(response, (uint8_t *)msg, strlen(msg));

  /* A post_handler that handles subscriptions/observing will be called for periodic resources by the framework. */
}

PROCESS(rest_server_example, "Erbium Example Server");
AUTOSTART_PROCESSES(&rest_server_example);

PROCESS_THREAD(rest_server_example, ev, data)
{
  PROCESS_BEGIN();

  PRINTF("Starting Erbium Example Server\n");

#ifdef RF_CHANNEL
  PRINTF("RF channel: %u\n", RF_CHANNEL);
#endif
#ifdef IEEE802154_PANID
  PRINTF("PAN ID: 0x%04X\n", IEEE802154_PANID);
#endif

  PRINTF("uIP buffer: %u\n", UIP_BUFSIZE);
  PRINTF("LL header: %u\n", UIP_LLH_LEN);
  PRINTF("IP+UDP header: %u\n", UIP_IPUDPH_LEN);
  PRINTF("REST max chunk: %u\n", REST_MAX_CHUNK_SIZE);

  /* Initialize the REST engine. */
  rest_init_engine();

  /* Activate the application-specific resources. */
  rest_activate_event_resource(&resource_event);

  /* Define application-specific events here. */
  init_sigfox_com();

  while(1) {
    cmd_t *cmd;
    if ( 0 == ringbuf_elements(&ringb) )
		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_POLL);
	
    cmd = get_cmd();
    switch ( cmd->cmd ) {
		case CMD_SFSEND: {
          write_sfok();
          sigfox_event_handler(&resource_event,cmd->payload, cmd->paylen);
#if REST_RES_SEPARATE && WITH_COAP>3
		  // Also call the separate response example handler.
		  separate_finalize_handler();
#endif
		  //etimer_set(&et, 3 * CLOCK_SECOND);
		  //PROCESS_YIELD();
		  //if (etimer_expired(&et)) {}
		  
          write_sfsent();
		} break;
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
	int res = ringbuf_put(&ringb,c);
	process_poll(&rest_server_example);
	return 1;
}
