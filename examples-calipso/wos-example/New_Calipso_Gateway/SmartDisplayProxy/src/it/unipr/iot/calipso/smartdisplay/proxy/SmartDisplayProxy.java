package it.unipr.iot.calipso.smartdisplay.proxy;

import it.unipr.iot.calipso.http.server.HTTPServer;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayProxy {

	private static final Logger logger = LoggerFactory.getLogger(SmartDisplayProxy.class);

	private HTTPServer server;
	private WebSocketHandler webSocketHandler;
	private ConcurrentLinkedQueue<SimpleWebSocket> broadcast = new ConcurrentLinkedQueue<SimpleWebSocket>();

	public SmartDisplayProxy(int port) {
		this(port, 0);
	}

	public SmartDisplayProxy(int port, final int max_ws_connections) {
		this.server = new HTTPServer(port);
		this.webSocketHandler = new WebSocketHandler() {
			public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
				logger.info("doWebSocketConnect({}): Request from {}", protocol, request.getRemoteHost());
				if(max_ws_connections > 0){
					return (broadcast.size() < 1 ? new SimpleWebSocket(broadcast) : null);
				}
				else return new SimpleWebSocket(broadcast);
			}
		};
		this.server.setHandler(this.webSocketHandler);
		this.webSocketHandler.setHandler(new BroadcastHttpHandler(this.broadcast));
	}

	public void start() {
		try{
			this.server.start();
		} catch (Exception e){
			logger.error(e.getMessage());
		}
	}

	public void stop() {
		try{
			this.server.stop();
		} catch (Exception e){
			logger.error(e.getMessage());
		}
	}
}
