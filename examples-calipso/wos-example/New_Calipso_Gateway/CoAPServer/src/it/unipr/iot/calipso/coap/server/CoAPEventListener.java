package it.unipr.iot.calipso.coap.server;

import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public interface CoAPEventListener {

	public void onNewNode(String id, NodeInfo nodeInfo);

	public void onNewResource(String resource);
	
	public void onNewResourceWithValue(String resource,String value);
	
	public void onResourceChanged(String resource, String value);

	public void onMessageReceived(CoapExchange exchange);

}
