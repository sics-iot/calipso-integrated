#ifndef __CALIPSO_CONF_H__
#define __CALIPSO_CONF_H__

#undef NETSTACK_CONF_RDC
#define NETSTACK_CONF_RDC     nullrdc_driver

#undef NETSTACK_CONF_MAC
#define NETSTACK_CONF_MAC     csma_driver

#undef NULLRDC_802154_AUTOACK
#define NULLRDC_802154_AUTOACK 1

/* Channel check rate in Hz */
#undef NETSTACK_RDC_CHANNEL_CHECK_RATE
#define NETSTACK_RDC_CHANNEL_CHECK_RATE 8

#endif /* __CALIPSO_CONF_H__ */
