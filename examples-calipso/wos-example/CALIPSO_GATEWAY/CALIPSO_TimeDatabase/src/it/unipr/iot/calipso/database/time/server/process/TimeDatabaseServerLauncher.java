package it.unipr.iot.calipso.database.time.server.process;

import it.unipr.iot.calipso.database.time.server.TimeDatabaseHandler;
import it.unipr.iot.calipso.http.server.HTTPServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class TimeDatabaseServerLauncher {

	private static Logger logger = LoggerFactory.getLogger(TimeDatabaseServerLauncher.class);

	public static void main(String[] args) {

		int httpPort = 50080;

		HTTPServer server = new HTTPServer(httpPort);
		server.setHandler(new TimeDatabaseHandler());
		try{
			logger.info("Starting TimeDatabase server on port {}...", httpPort);
			server.start();
			logger.info("TimeDatabase server started.");
		} catch (Exception e){
			e.printStackTrace();
		}

	}

}
