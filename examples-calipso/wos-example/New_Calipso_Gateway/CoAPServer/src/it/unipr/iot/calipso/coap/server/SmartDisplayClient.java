package it.unipr.iot.calipso.coap.server;

import it.unipr.iot.calipso.coap.server.data.PDRData;
import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.smartdisplay.CalipsoSmartDisplayClient;
import it.unipr.iot.calipso.smartdisplay.SmartDisplayNewNodeRPLTreeDTO;
import it.unipr.iot.calipso.smartdisplay.SmartDisplayNewResourceRPLTreeDTO;
import it.unipr.iot.calipso.smartdisplay.SmartDisplayResourceDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayClient implements CoAPEventListener, PDRListener {

	protected final static Logger logger = LoggerFactory.getLogger(SmartDisplayClient.class);
	
	public static final String NEW_NODE = "newNode";
	public static final String NEW_RESOURCE = "newResource";
	public static final String UPDATE_RESOURCE = "updateResource";

	private CalipsoSmartDisplayClient client;
	private String displayId;
	private String app;

	public SmartDisplayClient(String proxy, String from, String displayId, String app) {
		this.client = new CalipsoSmartDisplayClient();
		this.client.setFrom(from);
		this.client.setProxy(proxy);
		this.displayId = displayId;
		this.app = app;
	}

	public void connect() {
		this.client.connect(this.displayId, this.app);
	}
	
	public void onNewNode(String id, NodeInfo nodeInfo) {
		logger.debug("New Node: {} - {}", id, nodeInfo);
		SmartDisplayNewNodeRPLTreeDTO params = new SmartDisplayNewNodeRPLTreeDTO(id, nodeInfo, System.currentTimeMillis());
		this.client.doSmartDisplayAction(NEW_NODE, params);
	}

	public void onNewResource(String resource) {
		logger.debug("New Resource: {}", resource);
		SmartDisplayNewResourceRPLTreeDTO params = new SmartDisplayNewResourceRPLTreeDTO(resource, System.currentTimeMillis());
		this.client.doSmartDisplayAction(NEW_RESOURCE, params);
	}
	
	public void onNewResourceWithValue(String resource, String value){
		logger.debug("New Resource: {} = {}", resource, value);
		this.onNewResource(resource);
		if(value != null) this.onResourceChanged(resource, value);
	}
	
	public void onResourceChanged(String resource, String value) {
		logger.debug("Changed Resource: {} = {}", resource, value);
		SmartDisplayResourceDTO params = new SmartDisplayResourceDTO(resource, System.currentTimeMillis(), value);
		this.client.doSmartDisplayAction(UPDATE_RESOURCE, params);
	}

	public void onMessageReceived(CoapExchange exchange) {}

	public void onPDRData(String resourceURI, PDRData data) {
		String value = new Gson().toJson(data);
		logger.debug("PDR data: {} = {}", resourceURI, value);
		this.onResourceChanged(resourceURI, value);
	}

}
