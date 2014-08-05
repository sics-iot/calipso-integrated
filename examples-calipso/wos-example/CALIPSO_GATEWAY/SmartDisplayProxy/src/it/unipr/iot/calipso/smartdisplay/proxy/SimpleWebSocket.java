package it.unipr.iot.calipso.smartdisplay.proxy;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SimpleWebSocket implements WebSocket {

	private static final Logger logger = LoggerFactory.getLogger(SimpleWebSocket.class);

	protected Connection connection;

	private ConcurrentLinkedQueue<SimpleWebSocket> broadcast = new ConcurrentLinkedQueue<SimpleWebSocket>();

	public SimpleWebSocket(ConcurrentLinkedQueue<SimpleWebSocket> broadcast) {
		this.broadcast = broadcast;
	}

	public void onClose(int code, String message) {
		logger.info("onClose(): {} - {}", code, message);
		this.broadcast.remove(this);
	}

	public void onOpen(Connection connection) {
		logger.info("onOpen()");
		connection.setMaxIdleTime(3600 * 1000); // 1 hour IDLE before closing
		this.connection = connection;
		this.broadcast.add(this);
	}

	public Connection getConnection() {
		return this.connection;
	}
}
