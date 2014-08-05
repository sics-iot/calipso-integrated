package it.unipr.iot.calipso.coap.server.process;

import it.unipr.iot.calipso.coap.server.PDRListener;
import it.unipr.iot.calipso.coap.server.data.SmartDisplayClientManager;
import it.unipr.iot.calipso.coap.server.data.TimeDatabaseStorage;
import it.unipr.iot.calipso.coap.server.resources.CalipsoSmartParkingResource;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.server.Server;

/**
 * This is a CoAP server that contains SmartParking resources.
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 */
public class CalipsoSmartParkingServer {

	public static final String RPL_INFO_SERVICE = "rpl";
	public static final String ENERGY_SERVICE = "energy";
	public static final String SENT_MESSAGES_SERVICE = "tx";
	public static final String RECV_MESSAGES_SERVICE = "recv";
	public static final String PDR_MESSAGES_SERVICE = "pdr";
	public static final String DELAY_SERVICE = "delay";

	private final static Logger logger = LoggerFactory.getLogger(CalipsoSmartParkingServer.class);

	public static void main(String[] args) throws Exception {

		/* Calipso Smart Parking demo */

		String redisHost = "127.0.0.1";
		int redisPort = 6379;
		String timeDatabaseURL = "http://localhost:50080";

		if(args.length == 3){
			redisHost = args[0];
			redisPort = Integer.parseInt(args[1]);
			timeDatabaseURL = args[2];
		}
		else if(args.length != 0){
			logger.error("usage: {} <redisHost> <redisPort> <timeDatabaseURL>", CalipsoSmartParkingServer.class.getCanonicalName());
			System.exit(1);
		}

		logger.info("Redis @ {}:{}", redisHost, redisPort);
		logger.info("TimeDatabase @ {}", timeDatabaseURL);

		// RedisStorage.getInstance().setRedis(redisHost, redisPort);
		TimeDatabaseStorage.getInstance().setTimeDatabase(timeDatabaseURL);

		String sdProxy = "http://localhost:9876/httpdisplay";
		String displayId = "Display_1";
		String appId = "SmartParking";
		logger.info("Smart Display Proxy @ {} ({}/{})", sdProxy);
		SmartDisplayClientManager.getInstance().setProxy(sdProxy);
		SmartDisplayClientManager.getInstance().setFrom(CalipsoSmartParkingServer.class.getCanonicalName());
		SmartDisplayClientManager.getInstance().connect(displayId, appId);

		/* CoAP server */
		Server server = new Server();
		server.setExecutor(Executors.newScheduledThreadPool(10));
		CalipsoSmartParkingResource resource = new CalipsoSmartParkingResource("parking");
		resource.addListener(new PDRListener());
		server.add(resource);
		server.start();

	}
}
