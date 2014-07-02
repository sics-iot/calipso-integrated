all: udp-client udp-server
#APPS=servreg-hack
CONTIKI= ../..

WITH_UIP6=1
UIP_CONF_IPV6=1
WITH_COMPOWER=1

CFLAGS+= -DUIP_CONF_IPV6_RPL

ifdef RAWMAC
CFLAGS+=-DRAWMAC=$(RAWMAC)
endif

CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"
SMALL = 1

ifdef WITH_COMPOWER
APPS+=powertrace
CFLAGS+= -DCONTIKIMAC_CONF_COMPOWER=1 -DWITH_COMPOWER=1
endif

ifdef SERVER_REPLY
CFLAGS+=-DSERVER_REPLY=$(SERVER_REPLY)
endif

include $(CONTIKI)/Makefile.include