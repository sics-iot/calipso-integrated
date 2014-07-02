/*
 * WOS smart parking application
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "contiki-net.h"

#include "dev/button-sensor.h"
#ifdef WITH_COMPOWER
#include "energest.h"
#endif

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

/* TODO: This server address is hard-coded for Cooja. */
#define SERVER_NODE(ipaddr)   uip_ip6addr(ipaddr, 0xaaaa, 0, 0, 0, 0x0212, 0x7402, 0x0002, 0x0202) /* cooja2 */

#define LOCAL_PORT      UIP_HTONS(COAP_DEFAULT_PORT+1)
#define REMOTE_PORT     UIP_HTONS(COAP_DEFAULT_PORT)

#define TOGGLE_INTERVAL 10

PROCESS(coap_client_example, "COAP Client Example");
AUTOSTART_PROCESSES(&coap_client_example);


uip_ipaddr_t server_ipaddr;
static struct etimer et;

/* Example URIs that can be queried. */
#define NUMBER_OF_URLS 4
/* leading and ending slashes only for demo purposes, get cropped automatically when setting the Uri-Path */
char* service_urls[NUMBER_OF_URLS] = {".well-known/core", "/actuators/toggle", "battery/", "error/in//path"};
#if PLATFORM_HAS_BUTTON
static int uri_switch = 0;
#endif

/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void
client_chunk_handler(void *response)
{
  const uint8_t *chunk;

  int len = coap_get_payload(response, &chunk);
  printf("|%.*s", len, (char *)chunk);
}


PROCESS_THREAD(coap_client_example, ev, data)
{

#ifdef WITH_COMPOWER
  static unsigned long rx_start_duration;
  static unsigned long tx_start_duration;
  static unsigned long all_cpu;
  static unsigned long all_lpm;
#endif
  PROCESS_BEGIN();

  static coap_packet_t request[1]; /* This way the packet can be treated as pointer as usual. */
  SERVER_NODE(&server_ipaddr);

#ifdef WITH_COMPOWER
  rx_start_duration = energest_type_time(ENERGEST_TYPE_LISTEN);
  tx_start_duration = energest_type_time(ENERGEST_TYPE_TRANSMIT);
  //cpu_start_duration = energest_type_time(ENERGEST_TYPE_CPU);
  //lpm_start_duration = energest_type_time(ENERGEST_TYPE_LPM);
#endif
  /* receives all CoAP messages */
  coap_receiver_init();

  etimer_set(&et, TOGGLE_INTERVAL * CLOCK_SECOND);

#if PLATFORM_HAS_BUTTON
  SENSORS_ACTIVATE(button_sensor);
  printf("Press a button to request %s\n", service_urls[uri_switch]);
#endif

  while(1) {
    PROCESS_YIELD();

    if (etimer_expired(&et)) {
/*      printf("--Toggle timer--\n");

       prepare request, TID is set by COAP_BLOCKING_REQUEST()
      coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0 );
      coap_set_header_uri_path(request, service_urls[1]);

      const char msg[] = "Toggle!";
      coap_set_payload(request, (uint8_t *)msg, sizeof(msg)-1);


      PRINT6ADDR(&server_ipaddr);
      PRINTF(" : %u\n", UIP_HTONS(REMOTE_PORT));

      COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, client_chunk_handler);

      printf("\n--Done--\n");*/
#ifdef WITH_COMPOWER
      uint32_t all_transmit = energest_type_time(ENERGEST_TYPE_TRANSMIT) - tx_start_duration;
      uint32_t all_listen = energest_type_time(ENERGEST_TYPE_LISTEN) - rx_start_duration;
      uint32_t all_time = energest_type_time(ENERGEST_TYPE_CPU) + energest_type_time(ENERGEST_TYPE_LPM);

      PRINTF("total-radio: (mJ) %lu (%d.%02d%%) listen: (mJ) %lu (%d.%02d%%) transmit: (mJ) %lu (%d.%02d%%)\n",
    		  20 * 3 * (all_transmit + all_listen) / RTIMER_ARCH_SECOND,
    		  (int)((100L * (all_transmit + all_listen)) / all_time),
    		  (int)((10000L * (all_transmit + all_listen) / all_time) - ((100L * (all_transmit + all_listen) / all_time) * 100)),
    		  20 * 3 * (all_listen) / RTIMER_ARCH_SECOND,
    		  (int)((100L * all_listen) / all_time),
    		  (int)((10000L * all_listen) / all_time - ((100L * all_listen / all_time) * 100)),
    		  20 * 3 * (all_transmit) / RTIMER_ARCH_SECOND,
    	      (int)((100L * all_transmit) / all_time),
    	      (int)((10000L * all_transmit) / all_time - ((100L * all_transmit / all_time) * 100)));

      //printf("energy tx: %lu mJ\n", (energest_type_time(ENERGEST_TYPE_TRANSMIT) - tx_start_duration)*20*3/RTIMER_ARCH_SECOND);
      //printf("total time: %lu s\n", (energest_type_time(ENERGEST_TYPE_CPU) + energest_type_time(ENERGEST_TYPE_LPM))/RTIMER_ARCH_SECOND);
      //printf("total time radio: %lu s\n", (energest_type_time(ENERGEST_TYPE_LISTEN) + energest_type_time(ENERGEST_TYPE_TRANSMIT))/RTIMER_ARCH_SECOND);
#endif

      etimer_reset(&et);

#if PLATFORM_HAS_BUTTON
    } else if (ev == sensors_event && data == &button_sensor) {

      /* send a request to notify the end of the process */

      coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
      coap_set_header_uri_path(request, service_urls[uri_switch]);

      printf("--Requesting %s--\n", service_urls[uri_switch]);

      PRINT6ADDR(&server_ipaddr);
      PRINTF(" : %u\n", UIP_HTONS(REMOTE_PORT));

      COAP_BLOCKING_REQUEST(&server_ipaddr, REMOTE_PORT, request, client_chunk_handler);

      printf("\n--Done--\n");

      uri_switch = (uri_switch+1) % NUMBER_OF_URLS;
#endif

    }
  }

  PROCESS_END();
}
