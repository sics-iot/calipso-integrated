package it.unipr.iot.calipso.coap.server.process;

import it.unipr.iot.calipso.coap.server.resources.CalipsoSmartParkingResource;
import it.unipr.iot.calipso.coap.server.resources.RedisAndTimeDatabaseResourceStorage;

import java.util.concurrent.Executors;

import ch.ethz.inf.vs.californium.server.Server;

/**
 * This is a CoAP server that contains SmartParking resources.
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 */
public class CalipsoSmartParkingServer {

	public static final String RPL_INFO_SERVICE = "rpl";
	public static final String ENERGY_SERVICE = "energy";

	public static void main(String[] args) throws Exception {

		Server server = new Server();
		server.setExecutor(Executors.newScheduledThreadPool(10));

		/* Calipso Smart Parking demo */
		RedisAndTimeDatabaseResourceStorage storage = RedisAndTimeDatabaseResourceStorage.getInstance();
		storage.setRedis("127.0.0.1", 6379);
		storage.setTimeDatabase("http://localhost:50080");

		CalipsoSmartParkingResource resource = new CalipsoSmartParkingResource("parking");
		server.add(resource);

		server.start();

	}

}
