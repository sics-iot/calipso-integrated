package it.unipr.iot.calipso.coap.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unipr.iot.calipso.coap.server.util.MessageCountManager;
import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.util.CoapUtil;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class PDRListener implements CoAPEventListener {
	
	private final static Logger logger = LoggerFactory.getLogger(PDRListener.class);
	
	public PDRListener(){
	}

	@Override
	public void onNewNode(String id, NodeInfo nodeInfo) {
		logger.info("new node: {} {}", id, nodeInfo.getIp());
	}

	@Override
	public void onNewResource(String resource) {
		logger.info("new resource: {}", resource);
	}

	@Override
	public void onMessageReceived(CoapExchange exchange) {
		String sender = CoapUtil.getSender(exchange);
		MessageCountManager.getInstance().incrementMessageCountForNode(sender);
		Integer rx = MessageCountManager.getInstance().getMessageCountForNode(sender);
		logger.info("New MESSAGE from {} ({}) - {}", sender, rx, exchange.getRequestOptions().getURIPathString());
		
	}

}
