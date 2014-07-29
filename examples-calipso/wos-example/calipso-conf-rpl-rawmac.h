#ifndef __CALIPSO_CONF_H__
#define __CALIPSO_CONF_H__

#ifndef WITH_RAWMAC
#define WITH_RAWMAC 1
#endif

//#include "net/mac/rawmac.h"
#undef NETSTACK_CONF_RDC
#define NETSTACK_CONF_RDC rawmac_driver

/* The phase offset Po of RAWMAC */
#undef PHASE_OFFSET
#define PHASE_OFFSET	40 * ((RTIMER_ARCH_SECOND / 1000) + 1)

/* The delta phase offset dPo of RAWMAC */
#undef DELTA_PHASE_OFFSET
#define DELTA_PHASE_OFFSET	9 * ((RTIMER_ARCH_SECOND / 1000) + 1)

/* Channel check rate in Hz */
#undef NETSTACK_RDC_CHANNEL_CHECK_RATE
#define NETSTACK_RDC_CHANNEL_CHECK_RATE 8

#endif /* __CALIPSO_CONF_H__ */
