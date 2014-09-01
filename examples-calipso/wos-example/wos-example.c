#include "cc2420.h"
#include "dev/watchdog.h"
#include "dev/uart1.h"
#include "lib/ringbuf.h"
#include "sys/pt.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "net/uip.h"
#include "net/uip-ds6.h"
#include "contiki.h"
#include "contiki-net.h"

#include "net/rpl/rpl.h"
#include "net/uip-ds6-nbr.h"

#if WITH_RRPL
#include "net/rrpl/rrpl.h"
#endif

#if WITH_ORPL
#include "net/orpl/orpl.h"
#endif /* WITH_ORPL */

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


// TODO: This server address is hard-coded as we do not have discovery.
//#define SERVER_NODE(ipaddr)   uip_ip6addr(ipaddr, 0xaaaa, 0, 0, 0, 0x0212, 0x7403, 0x0003, 0x0303)
#define SERVER_NODE(ipaddr)   uip_ip6addr(ipaddr, 0xaaaa, 0, 0, 0, 0, 0, 0, 0x0001)

#define LOCAL_PORT      UIP_HTONS(COAP_DEFAULT_PORT+1)
#define REMOTE_PORT     UIP_HTONS(COAP_DEFAULT_PORT)

#define PUSH_INTERVAL   			75 // interval at which to send radio statistics (seconds)
#define ENERGY_UPDATE_INTERVAL  	60 // simple ENERGEST step interval (seconds)
#define MAX_URL_SIZE	32		// URL request max size (bytes)
#define MAX_ID_SIZE		16		// max length server generated ID string (bytes)
static char url_str_buf[MAX_URL_SIZE];
static char id_str_buf[MAX_ID_SIZE];

typedef enum {
	CMD_NONE			= 0,
	CMD_SFSEND,
	CMD_AT_GETFWVER,
	CMD_AT_SFOXCONF, 
	CMD_ATCMD
} sfox_cmd_t;

// struct to store a null terminated string
typedef struct str_buf_t {
	char *str;
	uint8_t len;
	uint8_t max_len;
} str_buf_t ;

static void init_strbuf( str_buf_t *url, char *buf, uint8_t max_len ) {
	url->len = 0;
	url->max_len = max_len;
	url->str = buf;
	url->str[0] = 0x00;
}

static void reset_strbuf( str_buf_t *url ) {
	url->len = 0;
	url->str[0] = 0x00;
}

// concatenates the first 'len' chars of 'buf' to the string stored in the str_buf_t.
// if provided length is 0, it is assumed that 'buf' is a null terminated string
static void concat_strbuf( str_buf_t *url, char *buf, uint8_t len ) {
	int i;
	for (i=0; ((i<len&&len>0) || (0==len && 0!=buf[i])) && (url->len<(url->max_len-1));++i)
		url->str[url->len++] = buf[i];
	url->str[url->len] = 0x00;
}

// concatenates a formated string to the string stored in the str_buf_t.
// the formated string follows the sprintf definition
static void concat_formatted( str_buf_t *url, const char *format, ...) {
  int res;
  va_list ap;
  va_start(ap, format);
  res = vsnprintf(&(url->str[url->len]), url->max_len-url->len-1, format, ap);
  va_end(ap);
  url->len += res;
  url->str[url->len] = 0x00;
}
#define SIGFOX_MSG_BUFFER_LEN 26
typedef struct cmd_t {
	sfox_cmd_t cmd;
	char payload[SIGFOX_MSG_BUFFER_LEN];
	uint8_t paylen;
	uint16_t nodeid;
	uint8_t tx_pow;
	uint8_t cca_thres;
} cmd_t;

#define clear_cmd_msg_buffer(_cmd_ptr)  memset((_cmd_ptr)->payload,0x00,SIGFOX_MSG_BUFFER_LEN);

static int sigofx_input_byte(unsigned char c);



static unsigned char rxBuf[32];
static uint16_t rxLen = 0; 
static unsigned char serial_buf[32]; // ringbuffer internal buffer, may be oversized
static struct ringbuf ringb;
static cmd_t sigfox_cmd;

/*---------------------------------------------------------------------------*/
#define write_atok() write_sf("\r\nOK\r\n",6)
#define write_sfsent() write_sf("\nOK;SENT;\n",10)

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
static const char hextable[16] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
#define H2C(_h) 		hextable[_h]
#define HEX2CHAR_L(_h)	H2C((_h)&0x0F)
#define HEX2CHAR_H(_h)	H2C(((unsigned char)(_h))>>4)

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
						sigfox_cmd.nodeid = ((uint16_t)rxBuf[5]<<8) | ((uint16_t)rxBuf[6]&0xFF);
						clear_cmd_msg_buffer(&sigfox_cmd);
						sigfox_cmd.paylen = hex2str(sigfox_cmd.payload, &(rxBuf[4]), len);
					} else {
						sigfox_cmd.cmd = CMD_NONE;
						continue;
					}
				} else if ( CMD_AT_SFOXCONF == sigfox_cmd.cmd ) {
					// start parsing at rxBuf[16] 2 comma separated integers
					uint8_t idx = parse_user_value(&(rxBuf[16]),5,&(sigfox_cmd.cca_thres));
					parse_user_value(&(rxBuf[idx+17]),3,&(sigfox_cmd.tx_pow));
				}
			}
			rxLen = 0;
		} else if (0==rxLen && (c<'A' || c>'Z') ) {}//ignore
		else rxBuf[rxLen++] = c;
	}
	
	return &sigfox_cmd;
}

/*************************************************************************************/

PROCESS(main_wos_process, "COAP Client Example");
PROCESS(sigfox_process, "radio stack stats");
PROCESS(simple_energest_process, "Simple Energest");
AUTOSTART_PROCESSES(&main_wos_process);

static void get_rplstats_str( str_buf_t *strbuf );
static void get_dutycycle_str( str_buf_t *strbuf );
static void get_pdrstats_str( str_buf_t *strbuf );
static void get_overhead_str( str_buf_t *strbuf );

uip_ipaddr_t server_ipaddr;

// maximum size of the statistic data payload sent to the COAP server
#define STATS_DATA_SIZE 48

// new resources should be declared here first in the index list
// then the resource URL suffixes should be declared in the 'service_urls'
// and the function to extract the data to a string should be declared in the 'service_str_builder'
// This way registration and periodic sending of the data is handled by the main process
// If the transmission must be handled apart, the 'service_str_builder' entry for the resource index must be NULL
enum {
	FASTPRK_RESOURCE_IDX = 0,
	ENERGY_RESOURCE_IDX,
	PARENT_RESOURCE_IDX,
	OVERHEAD_RESOURCE_IDX,
	PDR_RESOURCE_IDX,
	NUMBER_OF_RES	// must be always the last entry in the enum
};
// URI suffixes of the available resources
// leading and ending slashes only for demo purposes, get cropped automatically when setting the Uri-Path
static char* service_urls[NUMBER_OF_RES] = {
		"/presence",
		"/energy",
		"/rpl", // was /parent
		"/overhead", // was /pdr
		"/tx"};
static void (*service_str_builder[NUMBER_OF_RES])(str_buf_t*) = {
		NULL, // handled by the sigfox proc
		get_dutycycle_str,
		get_rplstats_str,
		get_overhead_str,
		get_pdrstats_str
};
static uint8_t reg_resource_idx;
static uint8_t reg_resource_status[NUMBER_OF_RES]; // stores 1 iff the corresponding resource has been successfully registered
// auxiliary string buffers
static str_buf_t request_url;
static str_buf_t url_id;
static str_buf_t stats;
static str_buf_t sfmsg;
static char stats_buf[STATS_DATA_SIZE];
static char sfmsg_buf[SIGFOX_MSG_BUFFER_LEN];

/*	Statistics CoAP	*/
static uint32_t curr_tx, curr_rx, curr_time;
static uint32_t dutycycle_per10k;

static uint32_t tx_pkts;

#if !WITH_ORPL && !WITH_RRPL
static uint8_t rpl_parentId;
static uint16_t rpl_parentLinkMetric;
#endif
// These functions will be passed to COAP_BLOCKING_REQUEST() to handle responses.
void
client_id_handler(void *response)
{
  const uint8_t *buf;
  coap_packet_t* packet = response;

  if ( REST.status.CREATED == packet->code ) {
	uint8_t len = coap_get_header_location_path(response, (const char **)&buf); // the identifier is passed in the location path
	reset_strbuf(&url_id);
	concat_strbuf(&url_id,(char *)buf,len);
	PRINTF("CODE=%d  ID: %s\n", packet->code, id_str_buf);
  }
}

void
register_res_reply_handler(void *response) {
	coap_packet_t* packet = response;
	if ( REST.status.CREATED == packet->code ) {
		reg_resource_status[reg_resource_idx] = 1;
	}
	PRINTF("IDX=%d CODE=%d\n",reg_resource_idx, packet->code);
}

void
client_dummy_handler(void *response) {
	PRINTF("RES CODE=%d\n",((coap_packet_t*)response)->code);
}

// Request builing auxiliary functions
static void build_coap_msg(coap_packet_t *request, str_buf_t* url, coap_method_t method, char *msg, uint8_t len, char* query, uint8_t query_len ) {
	PRINTF("--Requesting %s-- payload: %s\n", url->str, msg);
	// prepare request, TID is set by COAP_BLOCKING_REQUEST()
	coap_init_message(request, COAP_TYPE_CON, method, 0 );
	coap_set_header_uri_path(request, url->str);
	if ( query_len > 0 )
		coap_set_header_uri_query(request,query);
	coap_set_payload(request, (uint8_t *)msg, len);
	//if ( COAP_PUT == method || COAP_POST==method )
	tx_pkts++;
	printf ("when incremented is %lu\n", tx_pkts);
}

static void build_url( str_buf_t *url, char *res_name ) {
	reset_strbuf(url);
	concat_strbuf(url,url_id.str,url_id.len);
	concat_strbuf(url,res_name,0);
}

// statistic data extraction functions
// each function must store in the string buffer given as parameter 'strbuf' the payload to send
// to the COAP server for the corresponding resource
static void get_rplstats_str( str_buf_t *strbuf ) {
#if WITH_ORPL
	concat_formatted( strbuf, "{\"parentId\":\"%u\"}", 0);
#elif WITH_RRPL
	concat_formatted( strbuf, "{\"parentId\":\"%u\"}", 1);
#else
	rpl_parentId = (uint8_t) rpl_get_parent_ipaddr((rpl_parent_t *) rpl_get_any_dag()->preferred_parent)->u8[sizeof(uip_ipaddr_t)-1];
	rpl_parentLinkMetric = (uint16_t) rpl_get_parent_link_metric((uip_lladdr_t *)
		uip_ds6_nbr_lladdr_from_ipaddr((uip_ipaddr_t *) rpl_get_any_dag()->preferred_parent));
	//concat_formatted( strbuf, "{\"parentId\":\"%u\",\"LQI\":%u}", rpl_parentId , rpl_parentLinkMetric );
	concat_formatted( strbuf, "{\"parentId\":\"%u\"}", rpl_parentId);
#endif
}

static void get_dutycycle_str( str_buf_t *strbuf ) {
	concat_formatted( strbuf, "%u.%02u", (uint16_t)(dutycycle_per10k/100), (uint16_t)(dutycycle_per10k%100));
}

static void get_pdrstats_str( str_buf_t *strbuf ) {
	printf("tx packets %lu\n", tx_pkts);
	concat_formatted( strbuf, "%lu", tx_pkts+1);
}

static void get_overhead_str( str_buf_t *strbuf ) {
	//printf("Overhead: %u %u %u\n", dio_count, dao_count, dis_count);
#if WITH_ORPL
	concat_formatted( strbuf, "%u %u", dio_count, orpl_broadcast_count);
#elif WITH_RRPL
	concat_formatted( strbuf, "ciao");
#else
	concat_formatted( strbuf, "%u %u %u", dio_count, dao_count, dis_count);
#endif
}

// procs
PROCESS_THREAD(sigfox_process, ev, data) {
	PROCESS_BEGIN();

	init_sigfox_com();
	// wait for sensor commands
	while(1) {
		cmd_t *cmd;
		if ( 0 == ringbuf_elements(&ringb) )
			PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_POLL);

		cmd = get_cmd();
		if ( CMD_SFSEND == cmd->cmd ) { // sensor message received
			write_sfsent();
			if ( 0==url_id.len ) {
				uip_ipaddr_t ipaddr;
				get_global_addr(&ipaddr);
				reset_strbuf(&stats);
				concat_formatted( &stats, "{\"id\":\"%u\",\"treeId\":\"%u\"}", cmd->nodeid, ipaddr.u8[sizeof(uip_ipaddr_t)-1] );
			}
			if ( 1 == reg_resource_status[FASTPRK_RESOURCE_IDX] && sfmsg.len==0 ) {
				PRINTF("Posting FP event\n");
				concat_strbuf( &sfmsg, cmd->payload, cmd->paylen );
				process_post(&main_wos_process, PROCESS_EVENT_MSG, NULL);
			}
			cmd->cmd = CMD_NONE;
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
			case CMD_ATCMD:
				write_atok();
				break;
			default:
			case CMD_NONE: break;
		}
	} /* while (1) */

	PROCESS_END();
}

/*---------------------------------------------------------------------------*/
static void simple_energest_start() {
  energest_flush();
  process_start(&simple_energest_process, NULL);
}

/*---------------------------------------------------------------------------*/
static uint32_t simple_energest_step() {
  uint32_t temp;
  energest_flush();

  curr_tx = energest_type_time(ENERGEST_TYPE_TRANSMIT);
  curr_rx = energest_type_time(ENERGEST_TYPE_LISTEN);
  curr_time = energest_type_time(ENERGEST_TYPE_CPU) + energest_type_time(ENERGEST_TYPE_LPM);

  temp = (curr_tx+curr_rx)/(curr_time/10000uL);
  if ( temp > 10000uL ) return 10000uL;
  return temp;
}

/*---------------------------------------------------------------------------*/

typedef struct missed_ev_t {
	process_event_t evs[4];
	uint8_t idx;
	uint8_t blockedFlag;
	//struct process *p;
} missed_ev_t;
#define INIT_MISSED_EVS(_e) do { memset((_e),0,sizeof(missed_ev_t)); /*(_e)->p = PROCESS_CURRENT();*/ } while(0)

#define BLOCK_EVENTS(_e) (_e)->blockedFlag = 1;

void unblock_missed_events(missed_ev_t *evs) {
	uint8_t i;
	evs->blockedFlag = 0;
	for (i=0;i<evs->idx;++i)
		process_post(/*evs->p*/&main_wos_process, evs->evs[i], NULL);
	evs->idx = 0;
}
#define UNBLOCK_EVENTS(_e) unblock_missed_events(_e)

#define ADD_EVENT(_e, _ev) do { if ((_e)->blockedFlag  && ((_ev)==PROCESS_EVENT_MSG || (_ev)==PROCESS_EVENT_TIMER)) (_e)->evs[(_e)->idx++] = (_ev); } while(0)


/*---------------------------------------------------------------------------*/
PROCESS_THREAD(simple_energest_process, ev, data)
{
  static struct etimer periodic;
  PROCESS_BEGIN();
  etimer_set(&periodic, ENERGY_UPDATE_INTERVAL * CLOCK_SECOND);

  while(1) {
    PROCESS_WAIT_UNTIL(etimer_expired(&periodic));
    etimer_reset(&periodic);
    dutycycle_per10k = simple_energest_step();
  }

  PROCESS_END();
}

PROCESS_THREAD(main_wos_process, ev, data)
{
	static coap_packet_t pkt[1];
	static struct etimer et;
	static missed_ev_t missed_evs;

	ADD_EVENT(&missed_evs, ev);

	PROCESS_BEGIN();
//	process_start(&rrpl_process, NULL);
	memset( reg_resource_status, 0, NUMBER_OF_RES);
	memset( &sigfox_cmd, 0x00, sizeof(cmd_t) );
	INIT_MISSED_EVS(&missed_evs);

	init_strbuf(&request_url,url_str_buf,MAX_URL_SIZE);
	init_strbuf(&stats,stats_buf,STATS_DATA_SIZE);
	init_strbuf(&url_id,id_str_buf,MAX_ID_SIZE);
	init_strbuf(&sfmsg,sfmsg_buf,SIGFOX_MSG_BUFFER_LEN);

	tx_pkts = 0;
	SERVER_NODE(&server_ipaddr);
	process_start(&sigfox_process,NULL);

	simple_energest_start();

	/* receives all CoAP messages */
	coap_receiver_init();

	etimer_set(&et, (30) * CLOCK_SECOND);

#if WITH_ORPL
  orpl_init(0, 0);
#endif /* WITH_ORPL */


	// register the node and obtain identifier
	while (1) {
		PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et));
		if ( 0 != stats.len ) {
			printf("Trying node registration at the server...");
			reset_strbuf(&request_url);
			concat_strbuf(&request_url,"/parking/",0);
			build_coap_msg(pkt, &request_url, COAP_POST, stats.str, stats.len, NULL, 0);
			COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_id_handler);
		}
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

	  build_coap_msg(pkt, &request_url, COAP_POST, NULL, 0,NULL,0);
	  COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, register_res_reply_handler);
	  // retry until successful
	  if ( 0==reg_resource_status[reg_resource_idx] ) {
		  etimer_restart(&et);
		  PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et));
	  } else reg_resource_idx++;
	}

	etimer_set(&et, PUSH_INTERVAL * CLOCK_SECOND);
	printf("\nstart put process\n");
	while (1) {
		PROCESS_WAIT_EVENT();
		if (ev == PROCESS_EVENT_TIMER) {
			static uint8_t i;
			printf("\n--Stats put START--\n");
			// periodic sending of declared statistic data
			for (i=0;i<NUMBER_OF_RES;++i) {
				// resources with NULL 'service_str_builder' entry are skipped
				if ( NULL == service_str_builder[i] ) continue;
				reset_strbuf(&stats);
				(*service_str_builder[i])(&stats);
				build_url( &request_url, service_urls[i] );
				build_coap_msg(pkt, &request_url, COAP_PUT, stats.str, stats.len,NULL,0);
				BLOCK_EVENTS(&missed_evs);				
				COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);
				UNBLOCK_EVENTS(&missed_evs);
			}
			PRINTF("\n--Stats put END--\n");
			etimer_reset(&et);
		} else if (ev == PROCESS_EVENT_MSG && sfmsg.len>0) { // asynchronous Fastpark sensor messages
			PRINTF("\n--PARKING event START--\n");
			build_url( &request_url, service_urls[FASTPRK_RESOURCE_IDX] );
			build_coap_msg(pkt, &request_url, COAP_PUT, sfmsg.str, sfmsg.len,NULL,0);
			BLOCK_EVENTS(&missed_evs);
			COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, pkt, client_dummy_handler);
			UNBLOCK_EVENTS(&missed_evs);
			reset_strbuf(&sfmsg);
			PRINTF("\n--PARKING event END--\n");
		}
	}
	PROCESS_END();
}

static int sigofx_input_byte(unsigned char c) {
	ringbuf_put(&ringb,c);
	process_poll(&sigfox_process);
	return 1;
}
