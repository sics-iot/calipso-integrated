package it.unipr.iot.calipso.database.time.server.process;

import it.unipr.iot.calipso.database.time.TimeDatabase;
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
		boolean saveAtExit = true;
		String loadFrom = null;
		
		if(args.length == 1){
			httpPort = Integer.parseInt(args[0]);
		}
		if(args.length == 2){
			httpPort = Integer.parseInt(args[0]);
			saveAtExit = Boolean.parseBoolean(args[1]);
		}
		if(args.length == 3){
			httpPort = Integer.parseInt(args[0]);
			saveAtExit = Boolean.parseBoolean(args[1]);
			loadFrom = args[2];
		}

		final HTTPServer server = new HTTPServer(httpPort);
		server.setHandler(new TimeDatabaseHandler());

		if(saveAtExit){
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try{
						server.stop();
					} catch (Exception e){
						logger.info("HTTP server could not stop correctly: {}", e.getMessage());
					}
					String path = "saves/timedb-" + System.currentTimeMillis() + ".txt";
					if(TimeDatabase.getInstance().saveToFile(path)){
						logger.info("Saved TimeDatabase from file {}", path);
					}
					else{
						logger.error("Failed to saved TimeDatabase on file {}", path);
					}
				}
			});
		}

		if(loadFrom != null){
			if(TimeDatabase.getInstance().loadFromFile(loadFrom)){
				logger.info("Loaded TimeDatabase from file {}", loadFrom);
			}
			else{
				logger.error("Failed to load TimeDatabase from file {}", loadFrom);
			}
		}

		try{
			logger.info("Starting TimeDatabase server on port {}...", httpPort);
			server.start();
			logger.info("TimeDatabase server started.");
		} catch (Exception e){
			e.printStackTrace();
		}

	}
}
