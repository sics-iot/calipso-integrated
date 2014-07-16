package it.unipr.iot.calipso.http.server.process;

import it.unipr.iot.calipso.http.server.HTTPServer;
import it.unipr.iot.calipso.http.server.handler.SimpleHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class TestHTTPServerLauncher {

	private static Logger logger = LoggerFactory.getLogger(TestHTTPServerLauncher.class);

	public static void main(String[] args) {

		int httpPort = 8080;

		if(args.length == 1){
			httpPort = Integer.parseInt(args[0]);
		}

		HTTPServer server = new HTTPServer(httpPort);
		server.setHandler(new SimpleHandler());
		try{
			server.start();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
