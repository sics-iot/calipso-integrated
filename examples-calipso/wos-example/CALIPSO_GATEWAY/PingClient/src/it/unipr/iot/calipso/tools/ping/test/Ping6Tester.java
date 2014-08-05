package it.unipr.iot.calipso.tools.ping.test;

import it.unipr.iot.calipso.tools.ping.Ping6Client;
import it.unipr.iot.calipso.tools.ping.PingClient;
import it.unipr.iot.calipso.tools.ping.PingResults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class Ping6Tester {

	private static final Logger logger = LoggerFactory.getLogger(Ping6Tester.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PingClient client = new Ping6Client();
		String ip = "::1";
		PingResults result = client.ping(ip, 4);
		// client.ping(ip);
		// logger.info("Average Ping to " + ip + " = " + client.getAverage() + " ms");
		// logger.info("Stats:\n{}", client.getStatistics());
		if(result != null){
			logger.info("packets transmitted = {}", result.getPacketTX());
			logger.info("packets received = {}", result.getPacketRX());
			logger.info("packet loss = {}%", result.getPacketLoss());
			logger.info("round-trip min = {} ms", result.getRttMin());
			logger.info("round-trip avg = {} ms", result.getRttAvg());
			logger.info("round-trip max = {} ms", result.getRttMax());
			logger.info("round-trip stddev = {} ms", result.getRttStdDev());
		}
		else{
			logger.info("Ping failed");
		}
	}

}
