#!/bin/bash

TRANSMISSION_PERIOD=30

while [ 1 ] ; do

	for PORT in $@ ; do

	echo "sending to port ${PORT}"

	echo "ATE0" | nc localhost "${PORT}"
	sleep 1 
	echo "AT\$SF=868200000,246,31" | nc localhost "${PORT}"
	sleep 1
	# 882F280001CBFFFF
	echo -en "SFM\x08\x88\x2F\x28\x00\x01\xCB\xFF\xFF;" | nc localhost "${PORT}"
	sleep 4

	done

	sleep ${TRANSMISSION_PERIOD}

done