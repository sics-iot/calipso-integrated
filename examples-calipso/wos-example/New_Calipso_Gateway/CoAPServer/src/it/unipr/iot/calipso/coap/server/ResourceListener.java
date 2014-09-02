package it.unipr.iot.calipso.coap.server;

import java.util.Set;
import java.util.TreeSet;

import it.unipr.iot.calipso.coap.server.data.TimeDatabaseStorage;
import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.coap.server.util.ResourceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class ResourceListener implements CoAPEventListener {
	
	private final static Logger logger = LoggerFactory.getLogger(ResourceListener.class);
	
	public static final String RESOURCE_LIST = "_resources";

	public void onNewNode(String id, NodeInfo nodeInfo) {}

	public void onNewResource(String resource) {
		logger.info("onNewResource(\"{}\")", resource);
		ResourceManager.getInstance().addResource(resource);
		Set<String> resources = ResourceManager.getInstance().getResources();
		TimeDatabaseStorage.getInstance().store(RESOURCE_LIST, new Gson().toJson(new TreeSet<String>(resources)));
	}
	
	public void onNewResourceWithValue(String resource, String value) {
		logger.debug("onNewResourceWithValue(\"{}\",\"{}\")", resource, value);
		this.onNewResource(resource);
		if(value != null) this.onResourceChanged(resource, value);	
	}

	public void onResourceChanged(String resource, String value) {
		logger.info("onResourceChanged(\"{}\",\"{}\") @ {}", resource, value, new java.util.Date());
		TimeDatabaseStorage.getInstance().store(resource,value);
	}

	public void onMessageReceived(CoapExchange exchange) {}
	
	

}
