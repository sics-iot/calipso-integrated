package it.unipr.iot.calipso.smartdisplay.proxy.process;

import it.unipr.iot.calipso.smartdisplay.proxy.SmartDisplayProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayProxyLauncher {

	private static final Logger logger = LoggerFactory.getLogger(SmartDisplayProxyLauncher.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int port = 9876;
		if(args.length == 1){
			port = Integer.parseInt(args[0]);
		}
		final SmartDisplayProxy proxy = new SmartDisplayProxy(port);
		proxy.start();
		logger.info("Proxy started on port {}", port);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				proxy.stop();
				logger.info("Proxy stopped");
			}
		});
	}

}
