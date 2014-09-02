package it.unipr.iot.calipso.coap.server.resources;

import it.unipr.iot.calipso.coap.server.CalipsoResources;
import it.unipr.iot.calipso.coap.server.CoAPEventListener;
import it.unipr.iot.calipso.coap.server.data.NodeRegistrationData;
import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.util.IPAddress;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.OptionSet;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class CalipsoSmartParkingResource extends ResourceBase implements CoAPEventListener{

	private final static Logger logger = LoggerFactory.getLogger(CalipsoSmartParkingResource.class);

	public final static String LBR_RPL_TREE_ID = "1";
	
	private String content;
	private boolean isParent;
	private List<CoAPEventListener> listeners;

	public CalipsoSmartParkingResource(String name) {
		this(name, true, new ArrayList<CoAPEventListener>());
	}
	
	public CalipsoSmartParkingResource(String name, List<CoAPEventListener> listeners) {
		this(name, true, listeners);
	}
	
	private CalipsoSmartParkingResource(String name, boolean isParent, List<CoAPEventListener> listeners) {
		super(name);
		this.isParent = isParent;
		this.listeners = listeners;
		if(isParent){ // only the main resource holds the node list
			String res = "/" + name + "/0";
			String localIP = "::1";
			int localPort = 5683;
			NodeInfo nodeInfo = new NodeInfo(res, LBR_RPL_TREE_ID, localIP, localPort, System.currentTimeMillis(), null, null);
			this.onNewNode(LBR_RPL_TREE_ID, nodeInfo); // the CoAP server is the first node in the network
		}
	}
	
	public void addListener(CoAPEventListener listener){
		this.listeners.add(listener);
	}

	public void onNewNode(String id, NodeInfo nodeInfo) {
		logger.debug("New NODE: {}", id);
		String nodeId = "/" + this.getName() + "/" + id;
		for(CoAPEventListener listener : this.listeners){
			listener.onNewNode(nodeId, nodeInfo);
		}
		if(id.equals("0") == false){
			this.onNewResource(nodeId + "/" + CalipsoResources.PDR_MESSAGES_SERVICE);
		}
	}

	public void onNewResource(String resource) {
		logger.info("New RESOURCE: {}", resource);
		for(CoAPEventListener listener : this.listeners){
			listener.onNewResource(resource);
		}
	}
	
	public void onNewResourceWithValue(String resource, String value) {
		if(value == null){
			this.onNewResource(resource);
		}
		else{
			logger.info("New RESOURCE: {} = {}", resource);
			for(CoAPEventListener listener : this.listeners){
				listener.onNewResourceWithValue(resource,value);
			}
		}
	}
	
	public void onResourceChanged(String resource, String value){
		logger.debug("Changed RESOURCE: {}", resource);
		for(CoAPEventListener listener : this.listeners){
			listener.onResourceChanged(resource, value);
		}
	}

	public void onMessageReceived(CoapExchange exchange) {
		logger.debug("RECV message");
		for(CoAPEventListener listener : this.listeners){
			listener.onMessageReceived(exchange);
		}
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		this.onMessageReceived(exchange);
		OptionSet options = exchange.getRequestOptions();
		List<String> segments = options.getURIPaths();
		LinkedList<String> s = new LinkedList<String>(segments);
		s.removeFirst();
		String path = options.getURIPathString();
		if(path.equals(this.getName())){
			String payload = exchange.getRequestText();
			NodeRegistrationData regData = new Gson().fromJson(payload, NodeRegistrationData.class);
			s.add(regData.getId());
			CalipsoSmartParkingResource resource = create(s);
			resource.content = exchange.getRequestText();
			resource.listeners = this.listeners;
			Response response = new Response(ResponseCode.CREATED);
			response.getOptions().setLocationPath(resource.getURI());
			exchange.respond(response);
			if(this.isParent){
				String nodeIp = "";
				try{
					nodeIp = IPAddress.getIpAddress(exchange.getSourceAddress().getAddress());
					long now = System.currentTimeMillis();
					NodeInfo nodeInfo = new NodeInfo(resource.getURI(), regData.getTreeId(), nodeIp, exchange.getSourcePort(), now, null, null);
					logger.debug(now + ":\tA new node has joined the network: " + regData.getId() + " with IP " + nodeIp);
					this.onNewNode(regData.getId(), nodeInfo);
				} catch (Exception e){
					logger.error("Invalid IP - {}", e.getMessage());
				}
			}
		}
		else{
			if(segments.size() == 3){
				this.content = exchange.getRequestText();
				CalipsoSmartParkingResource resource = create(new LinkedList<String>());
				resource.content = exchange.getRequestText();
				resource.listeners = this.listeners;
				Response response = new Response(ResponseCode.CREATED);
				response.getOptions().setLocationPath(resource.getURI());
				exchange.respond(response);
				if(!this.isParent){
					if(this.content != null && this.content.length() > 0){
						this.onNewResourceWithValue(resource.getURI(), this.content);
					}
					else{
						this.onNewResource(resource.getURI());
					}
				}
			}
			else{
				Response response = new Response(ResponseCode.BAD_REQUEST);
				response.setPayload("Invalid URI");
				exchange.respond(response);
			}
		}

	}

	@Override
	public void handlePUT(CoapExchange exchange) {
		this.onMessageReceived(exchange);
		this.content = exchange.getRequestText();
		exchange.respond(ResponseCode.CHANGED);
		this.onResourceChanged(this.getURI(), this.content);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		this.onMessageReceived(exchange);
		if(content != null){
			exchange.respond(content);
		}
		else{
			// String subtree = LinkFormat.serializeTree(this);
			// exchange.respond(ResponseCode.CONTENT, subtree, MediaTypeRegistry.APPLICATION_LINK_FORMAT);
			exchange.respond(ResponseCode.NOT_FOUND);
		}
	}

	public void handleDELETE(CoapExchange exchange) {
		this.onMessageReceived(exchange);
	}

	/**
	 * Find the requested child. If the child does not exist yet, create it.
	 */
	@Override
	public Resource getChild(String name) {
		Resource resource = super.getChild(name);
		if(resource == null){
			resource = new CalipsoSmartParkingResource(name, false, this.listeners);
			add(resource);
		}
		return resource;
	}

	/**
	 * Create a resource hierarchy with according to the specified path.
	 * 
	 * @param path
	 *            the path
	 * @return the lowest resource from the hierarchy
	 */
	private CalipsoSmartParkingResource create(LinkedList<String> path) {
		String segment;
		do{
			if(path.size() == 0) return this;
			segment = path.removeFirst();
		} while (segment.isEmpty() || segment.equals("/"));

		CalipsoSmartParkingResource resource = new CalipsoSmartParkingResource(segment, false, this.listeners);
		add(resource);
		logger.debug("\tCreated resource: " + resource.getURI());
		return resource.create(path);
	}
}
