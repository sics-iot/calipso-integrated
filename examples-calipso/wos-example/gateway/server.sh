#!/bin/bash

java -Djava.util.logging.config.file=jlog.properties -cp SmartParkingDemo.jar:./lib/californium-0.18.7-final.jar:./lib/cf-server-0.18.7-final.jar:./lib/element-connector-0.2-final.jar it.unipr.iot.calipso.coap.server.process.CalipsoSmartParkingServer
