package it.unipr.iot.calipso.coap.server.process;

import it.unipr.iot.calipso.coap.server.CoAPEventListener;
import it.unipr.iot.calipso.coap.server.NodeListener;
import it.unipr.iot.calipso.coap.server.PDRCoAPEventListener;
import it.unipr.iot.calipso.coap.server.ResourceListener;
import it.unipr.iot.calipso.coap.server.SmartDisplayClient;
import it.unipr.iot.calipso.coap.server.WOSFastPrkEventListener;
import it.unipr.iot.calipso.coap.server.data.TimeDatabaseStorage;
import it.unipr.iot.calipso.coap.server.resources.CalipsoSmartParkingResource;
import it.unipr.iot.calipso.http.client.dao.WorldSensingServerRequest;
import it.unipr.iot.calipso.tools.tunslip.BorderRouterTunslipAnalyzer;
import it.unipr.iot.calipso.tools.tunslip.BorderRouterTunslipAnalyzerListener;
import it.unipr.iot.calipso.tools.tunslip.ConvergenceTimeAnalyzer;
import it.unipr.iot.calipso.tools.tunslip.HopCountAnalyzer;
import it.unipr.iot.calipso.tools.tunslip.RPLAnalyzer;

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
public class CalipsoSmartParkingGateway {

	private final static Logger logger = LoggerFactory.getLogger(CalipsoSmartParkingGateway.class);

	public static void main(String[] args)  {

		/* ---------------------------------------------------------------- */
		/* 							Smart Parking Demo 						*/
		/* ---------------------------------------------------------------- */
		
		/* Initialization */
		String tunslipLogFile = "/home/tai/calipso-integrated/examples-calipso/wos-example/log.dat.RELAYED";
		
		String timeDatabaseURL = "http://localhost:50080";
		String fastPrkServerURL = WorldSensingServerRequest.Localhost_BASE_URL;

		if(args.length == 1){
			timeDatabaseURL = args[0];
		}
		else if(args.length == 2){
			timeDatabaseURL = args[0];
			fastPrkServerURL = args[1];
		}
		else if(args.length != 0){
			logger.error("usage: {} [<timeDatabaseURL>]", CalipsoSmartParkingGateway.class.getCanonicalName());
			System.exit(1);
		}

		/* Time Database */
		logger.info("TimeDatabase @ {}", timeDatabaseURL);
		TimeDatabaseStorage.getInstance().setTimeDatabase(timeDatabaseURL);

		/* Smart Display */
		String sdProxy = "http://localhost:9876";
		String displayId = "Display_1";
		String appId = "SmartParking";
		logger.info("Smart Display Proxy @ {} ({}/{})", sdProxy, displayId, appId);
		SmartDisplayClient sdClient = new SmartDisplayClient(sdProxy,CalipsoSmartParkingGateway.class.getCanonicalName(),displayId, appId);
		sdClient.connect();
		
		/* CoAP server */
		Server server = new Server();
		server.setExecutor(Executors.newScheduledThreadPool(10));
		List<CoAPEventListener> listeners = new ArrayList<CoAPEventListener>();
		NodeListener nodeListener = new NodeListener();
		listeners.add(nodeListener);
		listeners.add(new ResourceListener());
		listeners.add(sdClient);
		PDRCoAPEventListener pdrListener = new PDRCoAPEventListener();
		pdrListener.addListener(sdClient);
		listeners.add(pdrListener);
		listeners.add(new WOSFastPrkEventListener(fastPrkServerURL));
		CalipsoSmartParkingResource resource = new CalipsoSmartParkingResource("parking", listeners);
		server.add(resource);
		
		/* Border Router tunslip management */
		final BorderRouterTunslipAnalyzer analyzer = BorderRouterTunslipAnalyzer.createAnalyzerWithFile(tunslipLogFile, new BorderRouterTunslipAnalyzerListener(){
			
			public void onAnalysisStarted(BorderRouterTunslipAnalyzer analyzer) {
				logger.info("Started analyzing...");
			}
			
			public void onAnalysisTerminated(BorderRouterTunslipAnalyzer analyzer) {
				logger.info("Stopped analyzing...");
			}
			
			public void onTunslipEvent(BorderRouterTunslipAnalyzer analyzer, Long timestamp, String line){
				logger.info("{}: {}", new java.util.Date(timestamp), line);
			}
			
		});
		
		RPLAnalyzer rplAnalyzer = new RPLAnalyzer();
		analyzer.addListener(RPLAnalyzer.CTIME_PREFIX, rplAnalyzer);
		nodeListener.setRPLAnalyzer(rplAnalyzer);
		
		ConvergenceTimeAnalyzer cTimeAnalyzer = new ConvergenceTimeAnalyzer();
		analyzer.addListener(ConvergenceTimeAnalyzer.START_PREFIX, cTimeAnalyzer);
		analyzer.addListener(ConvergenceTimeAnalyzer.CTIME_PREFIX, cTimeAnalyzer);
		
		HopCountAnalyzer hopCountAnalyzer = new HopCountAnalyzer();
		analyzer.addListener(HopCountAnalyzer.HOPCOUNT_PREFIX, hopCountAnalyzer);
		
		/* Start tunslip analyzer */
		new Thread(new Runnable(){
			public void run(){
				analyzer.analyze();	
			}
		}).start();
		
		
		/* Start CoAP server */
		server.start();

	}
}
