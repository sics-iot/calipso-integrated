package it.unipr.iot.calipso.coap.server;

import it.unipr.iot.calipso.coap.server.data.WorldSensingServerData;
import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.http.client.dao.WorldSensingServerRequest;
import it.unipr.iot.calipso.util.CoapUtil;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class WOSFastPrkEventListener implements CoAPEventListener {
	
	private final static Logger logger = LoggerFactory.getLogger(WOSFastPrkEventListener.class);
	
	private String serverURL;
	private List<PDRListener> listeners;
	
	public WOSFastPrkEventListener(String serverURL){
		this.serverURL = serverURL;
		this.listeners = new ArrayList<PDRListener>();
	}
	
	public void addListener(PDRListener listener){
		this.listeners.add(listener);
	}

	public void onNewNode(String id, NodeInfo nodeInfo) {}

	public void onNewResource(String resource) {}
	
	public void onNewResourceWithValue(String resource, String value) {}
	
	public void onResourceChanged(String resource, String value) {}

	public void onMessageReceived(CoapExchange exchange) {
		String sender = CoapUtil.getSender(exchange);
		String resource = CoapUtil.getResource(exchange);
		if(resource != null){
			logger.debug("onMessageReceived() - {} {}/{} - [payload: {}]", exchange.getRequestCode().name(), sender, resource, exchange.getRequestText());
		}
		else{
			logger.debug("onMessageReceived() - {} {} - [payload: {}]", exchange.getRequestCode().name(), sender, exchange.getRequestText());
		}
		if(resource != null && resource.equals(CalipsoResources.PRESENCE_SERVICE)){
			logger.info("FASTPRK EVENT: {} -> {}", resource, exchange.getRequestText());
			String payload = exchange.getRequestText();
			if(payload != null){
				WorldSensingServerData wosData = new WorldSensingServerData(payload);
				WorldSensingServerRequest.postData(serverURL, wosData);
			}
		}
	}

}
