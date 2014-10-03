#ifndef __PROJECT_CONF_H__
#define __PROJECT_CONF_H__

#define CC2420_CCA_USRMODE 0x01
#define BR_TXPOWER 0x0D
//#define BR_RXTHRES 211 // -90
//#define BR_RXTHRES 226 // -75
//#define BR_RXTHRES 227 // -74
//#define BR_RXTHRES 231 // -70
//#define BR_RXTHRES 236 // -65
//#define BR_RXTHRES 241 // -60
//#define BR_RXTHRES 246 // -55
#define BR_RXTHRES 253 // -50
//#define BR_RXTHRES 255 // -54

#define CALIPSO_RPL_NULLRDC 0
#define CALIPSO_RPL_CONTIKIMAC 1
#define CALIPSO_ORPL_CONTIKIMAC 2
#define CALIPSO_RPL_RAWMAC 3
#define CALIPSO_RRPL_CONTIKIMAC 4

/* Set to one of the above values to configure the CALIPSO stack */
//#define CALIPSO_CONFIG CALIPSO_RPL_NULLRDC
//#define CALIPSO_CONFIG CALIPSO_RPL_CONTIKIMAC
//#define CALIPSO_CONFIG CALIPSO_ORPL_CONTIKIMAC
#define CALIPSO_CONFIG CALIPSO_RPL_RAWMAC
//#define CALIPSO_CONFIG CALIPSO_RRPL_CONTIKIMAC
/* Some platforms have weird includes. */
#undef IEEE802154_CONF_PANID

/* Increase rpl-border-router IP-buffer when using more than 64. */
#undef REST_MAX_CHUNK_SIZE
#define REST_MAX_CHUNK_SIZE    64

/* The IP buffer size must fit all other hops, in particular the border router. */
/*
#undef UIP_CONF_BUFFER_SIZE
#define UIP_CONF_BUFFER_SIZE    1280
*/

/* Multiplies with chunk size, be aware of memory constraints. */
#undef COAP_MAX_OPEN_TRANSACTIONS
#define COAP_MAX_OPEN_TRANSACTIONS   1

/* Must be <= open transaction number, default is COAP_MAX_OPEN_TRANSACTIONS-1. */
#undef COAP_MAX_OBSERVERS
#define COAP_MAX_OBSERVERS      0

/* Filtering .well-known/core per query can be disabled to save space. */
#undef COAP_LINK_FORMAT_FILTERING
#define COAP_LINK_FORMAT_FILTERING      0

/* Reduce 802.15.4 frame queue to save RAM. */
#undef QUEUEBUF_CONF_NUM
#define QUEUEBUF_CONF_NUM       2

/* UIP_CONF_MAX_ROUTES specifies the maximum number of routes that each
   node will be able to handle. */
#undef UIP_CONF_MAX_ROUTES
#define UIP_CONF_MAX_ROUTES 6

/* We do not use this feature */
#undef UIP_CONF_UIP_DS6_NOTIFICATIONS
#define UIP_CONF_UIP_DS6_NOTIFICATIONS 1

/* Save some memory for the sky platform. */
#undef NBR_TABLE_CONF_MAX_NEIGHBORS
#define NBR_TABLE_CONF_MAX_NEIGHBORS     4

/* Reduce 802.15.4 frame queue to save RAM. */
#undef QUEUEBUF_CONF_NUM
#define QUEUEBUF_CONF_NUM       4

#undef SICSLOWPAN_CONF_FRAG
#define SICSLOWPAN_CONF_FRAG	1

#undef UIP_CONF_ND6_SEND_NA
#define UIP_CONF_ND6_SEND_NA 	0

#undef UIP_CONF_ND6_SEND_RA
#define UIP_CONF_ND6_SEND_RA 	0

#undef UIP_CONF_TCP
#define UIP_CONF_TCP 0

#include "wos-conf-sigfox.h"

#if CALIPSO_CONFIG == CALIPSO_RPL_NULLRDC
#include "calipso-conf-rpl-nullrdc.h"
#elif CALIPSO_CONFIG == CALIPSO_RPL_CONTIKIMAC
#include "calipso-conf-rpl-contikimac.h"
#elif CALIPSO_CONFIG == CALIPSO_ORPL_CONTIKIMAC
#include "calipso-conf-orpl-contikimac.h"
#elif CALIPSO_CONFIG == CALIPSO_RPL_RAWMAC
#include "calipso-conf-rpl-rawmac.h"
#elif CALIPSO_CONFIG == CALIPSO_RRPL_CONTIKIMAC
#include "calipso-conf-rrpl-contikimac.h"
#else
#error Unsupported CALIPSO configuration. Set the CALIPSO_CONFIG flag.
#endif /* CALIPSO_CONFIG */

#endif /* __PROJECT_CONF_H__ */
