package it.unipr.iot.calipso.coap.server;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import it.unipr.iot.calipso.coap.server.data.PDRData;
import it.unipr.iot.calipso.coap.server.data.TimeDatabaseStorage;
import it.unipr.iot.calipso.coap.server.util.MessageCountManager;
import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.util.CoapUtil;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class PDRCoAPEventListener implements CoAPEventListener {
	
	private final static Logger logger = LoggerFactory.getLogger(PDRCoAPEventListener.class);
	
	private List<PDRListener> listeners;
	
	public PDRCoAPEventListener(){
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
		if(sender != null && (resource == null || (resource != null && !resource.equals(CalipsoResources.DELAY_SERVICE)))){	// do not count /delay resource as it is not sent by nodes
			int rx = MessageCountManager.getInstance().incrementMessageCountForNode(sender);
			logger.debug("{}/recv = {}", sender, rx);
			if(resource != null && resource.equals(CalipsoResources.SENT_MESSAGES_SERVICE)){
				String payload = exchange.getRequestText();
				if(payload == null || payload.length() == 0){
					logger.warn("No payload was set");
					return;
				}
				else{
					try{
						int tx = Integer.parseInt(payload);
						if(tx < rx) logger.error("PRECONDITION FAILED: {} - {} < {}", sender, tx, rx);
						PDRData data = new PDRData(tx, rx);
						String resourceURI = sender + "/" + CalipsoResources.PDR_MESSAGES_SERVICE;
						logger.debug("{}: rx = {}; tx = {}; PDR = rx/tx = {}", resourceURI, rx, tx, data.getPDR());	
						TimeDatabaseStorage.getInstance().store(resourceURI, new Gson().toJson(data));
						for(PDRListener listener : this.listeners){
							listener.onPDRData(resourceURI, data);
						}
					}catch(NumberFormatException e){
						logger.error(e.getMessage());
					}
				}
			}
		}
	}

}
