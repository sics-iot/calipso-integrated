package it.unipr.iot.calipso.http.server.test;

import it.unipr.iot.calipso.http.server.HTTPServer;
import it.unipr.iot.calipso.http.server.test.handler.URLEchoHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class URLEchoHTTPServerLauncher {

	private static Logger logger = LoggerFactory.getLogger(URLEchoHTTPServerLauncher.class);

	public static void main(String[] args) {

		int httpPort = 8080;

		if(args.length == 1){
			httpPort = Integer.parseInt(args[0]);
		}
		logger.info("Running HTTP server on port {}", httpPort);
		HTTPServer server = new HTTPServer(httpPort);
		server.setHandler(new URLEchoHandler());
		try{
			server.start();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
