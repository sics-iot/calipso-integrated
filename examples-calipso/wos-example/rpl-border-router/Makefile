CONTIKI_PROJECT=border-router
all: $(CONTIKI_PROJECT) 

CONTIKI=../../..

RRPL = 1
WITH_UIP6=1
UIP_CONF_IPV6=1

ifeq ($(RRPL), 1)
CFLAGS += -D'RRPL_CONF_IS_COORDINATOR()=1'
CFLAGS+= -DUIP_CONF_IPV6_RPL=0
CFLAGS+= -DWITH_IPV6_RRPL=1 
CFLAGS+= -DUSE_OPT=1 
CFLAGS+= -DRRPL_CONF_SND_QRY=1 
#CFLAGS += -DUIP_CONF_ND6_SEND_NA=0 
CFLAGS+= -DRRPL_RANDOM_WAIT=1 
CFLAGS+= -DRESOLV_CONF_SUPPORTS_MDNS=0
CFLAGS += -DRRPL_CONF_IS_SINK=1 

CFLAGS += -DUIP_CONF_ROUTER=1  

else
CFLAGS+= -DUIP_CONF_IPV6_RPL
endif
#linker optimizations
SMALL=1

CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"
PROJECT_SOURCEFILES += slip-bridge.c

ifeq ($(PREFIX),)
 PREFIX = aaaa::1/64
endif


include $(CONTIKI)/Makefile.include

$(CONTIKI)/tools/tunslip6:	$(CONTIKI)/tools/tunslip6.c
	(cd $(CONTIKI)/tools && $(MAKE) tunslip6)

connect-router:	$(CONTIKI)/tools/tunslip6
	sudo $(CONTIKI)/tools/tunslip6 $(PREFIX)

connect-router-cooja:	$(CONTIKI)/tools/tunslip6
	sudo $(CONTIKI)/tools/tunslip6 -a 127.0.0.1 $(PREFIX)
