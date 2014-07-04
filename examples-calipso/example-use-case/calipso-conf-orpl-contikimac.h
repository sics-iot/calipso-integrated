#ifndef __CALIPSO_CONF_H__
#define __CALIPSO_CONF_H__

#undef CONTIKIMAC_CONF_CYCLE_TIME
#define CONTIKIMAC_CONF_CYCLE_TIME (RTIMER_ARCH_SECOND / 8)

/* For detailled logs */
/* #define ORPL_LOG(...) printf(__VA_ARGS__) */

#include "orpl-contiki-conf.h"

#endif /* __CALIPSO_CONF_H__ */
