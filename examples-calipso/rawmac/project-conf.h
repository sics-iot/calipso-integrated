#ifndef __PROJECT_H__
#define __PROJECT_H__

#ifndef WITH_RAWMAC
#define WITH_RAWMAC 1
#endif

// the rdc layer to be used: contikimac is the default
//#if WITH_RAWMAC
//#undef NETSTACK_CONF_RDC
//#define NETSTACK_CONF_RDC rawmac_driver
//#define NETSTACK_CONF_RDC contikimac_driver
//#endif /* WITH_RAWMAC */

/***** FIXED PARAMETERS ******/

/* UIP_CONF_MAX_ROUTES specifies the maximum number of routes that each
   node will be able to handle. */
//#undef UIP_CONF_MAX_ROUTES
//#define UIP_CONF_MAX_ROUTES 50

// max number of missed acks to assume the node gone or rebooted.
#undef MAX_NOACKS
#define MAX_NOACKS 4

// channel check rate in Hz
#undef NETSTACK_RDC_CHANNEL_CHECK_RATE
#define NETSTACK_RDC_CHANNEL_CHECK_RATE 4
#undef CYCLE_TIME
#define CYCLE_TIME (RTIMER_ARCH_SECOND / NETSTACK_RDC_CHANNEL_CHECK_RATE)

// phase optimization should be set to 1. it keeps track of phases of the neighbors
#undef CONTIKIMAC_CONF_WITH_PHASE_OPTIMIZATION
#define CONTIKIMAC_CONF_WITH_PHASE_OPTIMIZATION 1
#undef WITH_PHASE_OPTIMIZATION
#define WITH_PHASE_OPTIMIZATION 1



/*****	CONFIGURABLE PARAMETERS	*****/
#if WITH_RAWMAC
/* The phase offset Po of RAWMAC */
#undef PHASE_OFFSET
#define PHASE_OFFSET	40 * ((RTIMER_ARCH_SECOND / 1000) + 1)

/* The delta phase offset dPo of RAWMAC */
#undef DELTA_PHASE_OFFSET
#define DELTA_PHASE_OFFSET	9 * ((RTIMER_ARCH_SECOND / 1000) + 1)
#endif /* WITH_RAWMAC */

/* The total number of queuebuf */
//#undef QUEUEBUF_CONF_NUM
//#define QUEUEBUF_CONF_NUM               8

// set to 1 to estimate energy consumption on the node
#define WITH_COMPOWER 1

// set to 1 to enable downward traffic from the sink to the nodes. data is sent by the sink periodically
#define SERVER_REPLY 1

#endif /* __PROJECT_H__ */
