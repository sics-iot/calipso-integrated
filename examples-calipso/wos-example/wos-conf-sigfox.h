#ifndef __CALIPSO_CONF_H__
#define __CALIPSO_CONF_H__

#define PROCESS_CONF_NO_PROCESS_NAMES 1
#define LOG_CONF_ENABLED 0
// In deployments with real FastPark sensors, this flag needs to be defined
//#define SIGFOX_SERIAL_ENABLED 1
// To test with real HW, fake sensors, and debug messages enabled
#define SIGFOX_SERIAL_TEST_EN 1

#ifdef SIGFOX_SERIAL_ENABLED
#undef printf
#define printf(...)
#endif

#endif /* __CALIPSO_CONF_H__ */

