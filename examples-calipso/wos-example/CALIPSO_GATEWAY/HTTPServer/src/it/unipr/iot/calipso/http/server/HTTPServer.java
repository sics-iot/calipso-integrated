package it.unipr.iot.calipso.http.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class HTTPServer {

	private static Logger logger = LoggerFactory.getLogger(HTTPServer.class);

	private Server httpServer;

	public HTTPServer(int httpPort) {
		this.httpServer = new Server(httpPort);
	}

	public void setHandler(AbstractHandler handler) {
		this.httpServer.setHandler(handler);
	}

	public void start() throws Exception {
		logger.info("Starting HTTP interface");
		if(!this.httpServer.isStarted()){
			this.httpServer.start();
			this.httpServer.join();
		}
		logger.info("Started HTTP interface");
	}

	public void stop() throws Exception {
		logger.info("Stopping HTTP interface");
		if(!this.httpServer.isStopped()){
			this.httpServer.stop();
			this.httpServer.join();
		}
		logger.info("Stopped HTTP interface");
	}

}
