package it.unipr.iot.calipso.coap.server.process;

import it.unipr.iot.calipso.coap.server.CoAPEventListener;
import it.unipr.iot.calipso.coap.server.NodeListener;
import it.unipr.iot.calipso.coap.server.PDRCoAPEventListener;
import it.unipr.iot.calipso.coap.server.ResourceListener;
import it.unipr.iot.calipso.coap.server.SmartDisplayClient;
import it.unipr.iot.calipso.coap.server.WOSFastPrkEventListener;
import it.unipr.iot.calipso.coap.server.data.RedisStorage;
import it.unipr.iot.calipso.coap.server.data.TimeDatabaseStorage;
import it.unipr.iot.calipso.coap.server.resources.CalipsoSmartParkingResource;
import it.unipr.iot.calipso.http.client.dao.WorldSensingServerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.server.Server;

/**
 * This is a CoAP server that contains SmartParking resources.
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 */
public class CalipsoSmartParkingGatewayWithRedis {

	private final static Logger logger = LoggerFactory.getLogger(CalipsoSmartParkingGatewayWithRedis.class);

	public static void main(String[] args)  {

		/* Calipso Smart Parking demo */

		String redisHost = "127.0.0.1";
		int redisPort = 6379;
		String timeDatabaseURL = "http://localhost:50080";
		String fastPrkServerURL = WorldSensingServerRequest.Localhost_BASE_URL;

		if(args.length == 3){
			redisHost = args[0];
			redisPort = Integer.parseInt(args[1]);
			timeDatabaseURL = args[2];
		}
		else if(args.length == 4){
			redisHost = args[0];
			redisPort = Integer.parseInt(args[1]);
			timeDatabaseURL = args[2];
			fastPrkServerURL = args[3];
		}
		else if(args.length != 0){
			logger.error("usage: {} <redisHost> <redisPort> <timeDatabaseURL>", CalipsoSmartParkingGatewayWithRedis.class.getCanonicalName());
			System.exit(1);
		}

		logger.info("Redis @ {}:{}", redisHost, redisPort);
		logger.info("TimeDatabase @ {}", timeDatabaseURL);

		RedisStorage.getInstance().setRedis(redisHost, redisPort);
		TimeDatabaseStorage.getInstance().setTimeDatabase(timeDatabaseURL);

		String sdProxy = "http://localhost:9876";
		String displayId = "Display_1";
		String appId = "SmartParking";
		logger.info("Smart Display Proxy @ {} ({}/{})", sdProxy, displayId, appId);
		SmartDisplayClient sdClient = new SmartDisplayClient(sdProxy,CalipsoSmartParkingGatewayWithRedis.class.getCanonicalName(),displayId, appId);
		sdClient.connect();
		
		/* CoAP server */
		Server server = new Server();
		server.setExecutor(Executors.newScheduledThreadPool(10));
		List<CoAPEventListener> listeners = new ArrayList<CoAPEventListener>();
		listeners.add(new NodeListener());
		listeners.add(new ResourceListener());
		listeners.add(sdClient);
		PDRCoAPEventListener pdrListener = new PDRCoAPEventListener();
		pdrListener.addListener(sdClient);
		listeners.add(pdrListener);
		listeners.add(new WOSFastPrkEventListener(fastPrkServerURL));
		CalipsoSmartParkingResource resource = new CalipsoSmartParkingResource("parking", listeners);
		server.add(resource);
		server.start();

	}
}
