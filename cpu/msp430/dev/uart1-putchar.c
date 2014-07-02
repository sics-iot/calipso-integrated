#include <stdio.h>
#include "dev/uart1.h"

int
putchar(int c)
{
#ifndef SIGFOX_SERIAL_ENABLED
  uart1_writeb((char)c);
#endif
  return c;
}
