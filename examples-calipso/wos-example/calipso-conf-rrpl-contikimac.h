#ifndef __CALIPSO_CONF_H__
#define __CALIPSO_CONF_H__

#ifndef WITH_RRPL
#define WITH_RRPL 1
#endif

#undef NETSTACK_CONF_RDC
#define NETSTACK_CONF_RDC     contikimac_driver

#undef NETSTACK_CONF_MAC
#define NETSTACK_CONF_MAC     csma_driver

/* Channel check rate in Hz */
#undef NETSTACK_RDC_CHANNEL_CHECK_RATE
#define NETSTACK_RDC_CHANNEL_CHECK_RATE 8



#endif /* __CALIPSO_CONF_H__ */
