#ifndef __PROJECT_WOS_INTEGRATED_CONF_H__
#define __PROJECT_WOS_INTEGRATED_CONF_H__

#define PROCESS_CONF_NO_PROCESS_NAMES 1
#define LOG_CONF_ENABLED 0

//#define RAWMAC 1

/* Some platforms have weird includes. */
#undef IEEE802154_CONF_PANID

/* Disabling RDC for demo purposes. Core updates often require more memory. */
/* For projects, optimize memory and enable RDC again. */
#undef NETSTACK_CONF_RDC
#if RAWMAC
#define NETSTACK_CONF_RDC	rawmac_driver
#else
#define NETSTACK_CONF_RDC   nullrdc_driver
#endif /* RAWMAC */

/* Increase rpl-border-router IP-buffer when using more than 64. */
#undef REST_MAX_CHUNK_SIZE
#define REST_MAX_CHUNK_SIZE    64

/* Estimate your header size, especially when using Proxy-Uri. */
/*
#undef COAP_MAX_HEADER_SIZE
#define COAP_MAX_HEADER_SIZE    70
*/

/* The IP buffer size must fit all other hops, in particular the border router. */
/*
#undef UIP_CONF_BUFFER_SIZE
#define UIP_CONF_BUFFER_SIZE    1280
*/

/* Multiplies with chunk size, be aware of memory constraints. */
#undef COAP_MAX_OPEN_TRANSACTIONS
#define COAP_MAX_OPEN_TRANSACTIONS   1

/* Must be <= open transaction number, default is COAP_MAX_OPEN_TRANSACTIONS-1. */
/*
#undef COAP_MAX_OBSERVERS
#define COAP_MAX_OBSERVERS      2
*/

/* Filtering .well-known/core per query can be disabled to save space. */
/*
#undef COAP_LINK_FORMAT_FILTERING
#define COAP_LINK_FORMAT_FILTERING      0
*/

/* UIP_CONF_MAX_ROUTES specifies the maximum number of routes that each
   node will be able to handle. */
#undef UIP_CONF_MAX_ROUTES
#define UIP_CONF_MAX_ROUTES 6

/* Save some memory for the sky platform. */
#undef NBR_TABLE_CONF_MAX_NEIGHBORS
#define NBR_TABLE_CONF_MAX_NEIGHBORS     5

/* Reduce 802.15.4 frame queue to save RAM. */
#undef QUEUEBUF_CONF_NUM
#define QUEUEBUF_CONF_NUM       4

#undef SICSLOWPAN_CONF_FRAG
#define SICSLOWPAN_CONF_FRAG	0

// channel check rate in Hz
#undef NETSTACK_RDC_CHANNEL_CHECK_RATE
#define NETSTACK_RDC_CHANNEL_CHECK_RATE 8

/*****	CONFIGURABLE PARAMETERS	*****/
#if RAWMAC
/* The phase offset Po of RAWMAC */
#undef PHASE_OFFSET
#define PHASE_OFFSET	40 * ((RTIMER_ARCH_SECOND / 1000) + 1)

/* The delta phase offset dPo of RAWMAC */
#undef DELTA_PHASE_OFFSET
#define DELTA_PHASE_OFFSET	9 * ((RTIMER_ARCH_SECOND / 1000) + 1)
#endif /* RAWMAC */

#endif /* __PROJECT_WOS_INTEGRATED_CONF_H__ */
