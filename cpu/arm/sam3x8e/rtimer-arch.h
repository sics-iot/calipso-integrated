/**
 * Copyright (c) 2013, Calipso project consortium
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or
 * other materials provided with the distribution.
 * 
 * 3. Neither the name of the Calipso nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific
 * prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
*/
#include "platform-conf.h"



#ifndef RTIMER_ARCH_H_
#define RTIMER_ARCH_H_


#define		RES_95NS	1

#ifdef	RT_CONF_RESOLUTION
#define RT_RESOLUTION	RT_CONF_RESOLUTION
#else
#define RT_RESOLUTION	RES_95NS
#endif

// If it were possible to define a custom size for the rtimer_clock_t type:
// typedef unsigned long long rtimer_clock_t; // Only 48 bits are used. It is enough for hundrends of years

#if RT_RESOLUTION == RES_95NS
#define		RT_PRESCALER	1
#define		RTIMER_ARCH_SECOND 10500000    // One tick: 95.238 ns. CPU Clock is 84MHz. 
//Note that this needs to comply with the system clock setting! TODO
#else
#warning	Clock resolution not implemented!
#endif /* RT_RESOLUTION == RES_95NS */

rtimer_clock_t rtime_arch_now(void);

void rtimer_arch_disable_irq(void);
void rtimer_arch_enable_irq(void);
rtimer_clock_t rtimer_arch_now(void);

#endif /* RTIMER-ARCH_H_ */