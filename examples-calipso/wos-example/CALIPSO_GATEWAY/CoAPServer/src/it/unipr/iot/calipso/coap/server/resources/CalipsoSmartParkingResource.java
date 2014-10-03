package it.unipr.iot.calipso.coap.server.resources;

import it.unipr.iot.calipso.coap.server.CoAPEventListener;
import it.unipr.iot.calipso.coap.server.data.PDRData;
import it.unipr.iot.calipso.coap.server.data.RPLData;
import it.unipr.iot.calipso.coap.server.data.RedisStorage;
import it.unipr.iot.calipso.coap.server.data.SmartDisplayClientManager;
import it.unipr.iot.calipso.coap.server.data.TimeDatabaseStorage;
import it.unipr.iot.calipso.coap.server.process.CalipsoSmartParkingServer;
import it.unipr.iot.calipso.coap.server.util.MessageCountManager;
import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.coap.server.util.NodeManager;
import it.unipr.iot.calipso.coap.server.util.ResourceManager;
import it.unipr.iot.calipso.tools.tunslip.BorderRouterTunslipAnalyzer;
import it.unipr.iot.calipso.tools.tunslip.BorderRouterTunslipAnalyzerListener;
import it.unipr.iot.calipso.tools.tunslip.ConvergenceTimeAnalyzer;
import it.unipr.iot.calipso.tools.tunslip.HopCountAnalyzer;
import it.unipr.iot.calipso.util.IPAddress;
import it.unipr.iot.calipso.coap.server.data.NodeRegistrationData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.OptionSet;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

import com.google.gson.Gson;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class CalipsoSmartParkingResource extends ResourceBase implements CoAPEventListener, BorderRouterTunslipAnalyzerListener {

	private final static Logger logger = LoggerFactory.getLogger(CalipsoSmartParkingResource.class);

	public static final String PRESENCE_SERVICE = "presence";
	public static final String NODE_LIST = "_nodes";
	public static final String NODE_INFO_LIST = "_nodeinfo";
	public static final String RESOURCE_LIST = "_resources";

	private String content;
	private boolean isParent;
	private List<CoAPEventListener> listeners;
	
	private Map<String,Long> cTimeForNodes;

	public CalipsoSmartParkingResource(String name) {
		this(name, true);
	}

	private CalipsoSmartParkingResource(String name, boolean isParent) {
		super(name);
		this.isParent = isParent;
		this.listeners = new ArrayList<CoAPEventListener>();
		if(isParent){ // only the main resource holds the node list
			String res = "/" + name + "/0";
			String localIP = "::1";
			int localPort = 5683;
			NodeInfo nodeInfo = new NodeInfo(res, "0", localIP, localPort, System.currentTimeMillis(), null);
			this.onNewNode("0", nodeInfo); // the CoAP server is the first node in the network
		}
		this.cTimeForNodes = new HashMap<String,Long>();
	}
	
	public void startConvergenceTimeAnalyzer(final String cTimeLogFile, final BorderRouterTunslipAnalyzerListener l){
		new Thread(new Runnable(){
			public void run(){
				try {
					BorderRouterTunslipAnalyzer analyzer = BorderRouterTunslipAnalyzer.createAnalyzerWithFile(cTimeLogFile, l);
					analyzer.addListener("CTIME", l);
					ConvergenceTimeAnalyzer cTimeAnalyzer = new ConvergenceTimeAnalyzer();
					analyzer.addListener(ConvergenceTimeAnalyzer.START_PREFIX, cTimeAnalyzer);
					analyzer.addListener(ConvergenceTimeAnalyzer.CTIME_PREFIX, cTimeAnalyzer);
					HopCountAnalyzer hopCountAnalyzer = new HopCountAnalyzer();
					analyzer.addListener(HopCountAnalyzer.HOPCOUNT_PREFIX, hopCountAnalyzer);
					analyzer.analyze();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}).start();
	}
	
	public void addListener(CoAPEventListener listener){
		this.listeners.add(listener);
	}

	public void onNewNode(String id, NodeInfo nodeInfo) {
		logger.info("New NODE: " + nodeInfo);
		String nodeId = "/" + this.getName() + "/" + id;
		NodeManager.getInstance().addNode(nodeId, nodeInfo);
		RedisStorage.getInstance().store(NODE_LIST, NodeManager.getInstance().getNodesAsJson());
		TimeDatabaseStorage.getInstance().store(NODE_LIST, NodeManager.getInstance().getNodesAsJson());
		TimeDatabaseStorage.getInstance().store(NODE_INFO_LIST, NodeManager.getInstance().getNodeInfoListAsJson());
		for(CoAPEventListener listener : this.listeners){
			listener.onNewNode(nodeId, nodeInfo);
		}
		if(id.equals("0") == false){
			this.onNewResource(nodeId + "/" + CalipsoSmartParkingServer.RECV_MESSAGES_SERVICE);
			this.onNewResource(nodeId + "/" + CalipsoSmartParkingServer.PDR_MESSAGES_SERVICE);
		}
	}

	public void onNewResource(String resource) {
		logger.info("New RESOURCE: " + resource);
		ResourceManager.getInstance().addResource(resource);
		RedisStorage.getInstance().store(RESOURCE_LIST, ResourceManager.getInstance().getResourcesAsJson());
		TimeDatabaseStorage.getInstance().store(RESOURCE_LIST, ResourceManager.getInstance().getResourcesAsJson());
		for(CoAPEventListener listener : this.listeners){
			listener.onNewResource(resource);
		}
	}

	public void onMessageReceived(CoapExchange exchange) {
		if(exchange.getRequestOptions().getURIPathCount() == 3){
			List<String> parts = exchange.getRequestOptions().getURIPaths();
			String sender = "/" + parts.get(0) + "/" + parts.get(1);
			if(!this.getName().equals("delay")){
				MessageCountManager.getInstance().incrementMessageCountForNode(sender);
			}
			Integer rx = MessageCountManager.getInstance().getMessageCountForNode(sender);
			logger.info("New MESSAGE from {} ({}) - {}", sender, rx, exchange.getRequestOptions().getURIPathString());
			if(this.getName().equals(CalipsoSmartParkingServer.SENT_MESSAGES_SERVICE)){
				logger.info("*** New PDR data for {}", sender);
				logger.info("*** tx = {}; rx ={};", exchange.getRequestText(), rx);
				String payload = exchange.getRequestText();
				int tx = 0;
			    if(payload != null && payload.length() > 0) tx = Integer.parseInt(payload);
				PDRData data = new PDRData(tx, rx);
				RedisStorage.getInstance().store(sender + "/" + CalipsoSmartParkingServer.PDR_MESSAGES_SERVICE, new Gson().toJson(data));
				TimeDatabaseStorage.getInstance().store(sender + "/" + CalipsoSmartParkingServer.PDR_MESSAGES_SERVICE, new Gson().toJson(data));
			}
			else if(this.getName().equals(CalipsoSmartParkingServer.RPL_INFO_SERVICE)){
			    String payload = exchange.getRequestText();
			    
			    if(payload != null && payload.length() > 0){
			    	logger.info("RECEIVED PAYLOAD: {}",payload);
			     RPLData data = new Gson().fromJson(payload, RPLData.class);
			     if(data != null){
			      SmartDisplayClientManager.getInstance().updateTree(sender, data.getParentId(), data.getLQI());
			     }
			    }
			   }
			else{
				String payload = exchange.getRequestText();
				SmartDisplayClientManager.getInstance().updateResource(sender + "/" + this.getName(), payload);
			}
			RedisStorage.getInstance().store(sender + "/" + CalipsoSmartParkingServer.RECV_MESSAGES_SERVICE, rx.toString());
			TimeDatabaseStorage.getInstance().store(sender + "/" + CalipsoSmartParkingServer.RECV_MESSAGES_SERVICE, rx.toString());
			for(CoAPEventListener listener : this.listeners){
				listener.onMessageReceived(exchange);
			}
		}
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		OptionSet options = exchange.getRequestOptions();
		List<String> segments = options.getURIPaths();
		LinkedList<String> s = new LinkedList<String>(segments);
		s.removeFirst();
		String path = options.getURIPathString();
		logger.debug("[" + this.getName() + "]\tRECV POST " + path + " - Payload: " + exchange.getRequestText());
		if(path.equals(this.getName())){
			String payload = exchange.getRequestText();
			NodeRegistrationData regData = new Gson().fromJson(payload, NodeRegistrationData.class);
			   logger.debug("\tA new node has joined the network: " + regData.getId());
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
				     logger.info("Sender IP address: {}", exchange.getSourceAddress());
				     nodeIp = IPAddress.getIpAddress(exchange.getSourceAddress().getAddress());
				    } catch (Exception e){
				     logger.error("Invalid IP - {}", e.getMessage());
				    }
				long now = System.currentTimeMillis();
				Long cTimeEventForNode = this.cTimeForNodes.get(nodeIp);
				if(cTimeEventForNode == null) cTimeEventForNode = now;				
				NodeInfo nodeInfo = new NodeInfo(resource.getURI(), regData.getTreeId(), nodeIp, exchange.getSourcePort(), now, cTimeEventForNode);
			    logger.debug(now + ":\tA new node has joined the network: " + regData.getId() + " with IP " + nodeIp + " @ " + new java.util.Date(cTimeEventForNode));
			    this.onNewNode(regData.getId(), nodeInfo);
				MessageCountManager.getInstance().incrementMessageCountForNode(resource.getURI());
			}

		}
		else{
			if(segments.size() == 3){
				this.content = exchange.getRequestText();
				// Resource resource = create(new LinkedList<String>());
				CalipsoSmartParkingResource resource = create(new LinkedList<String>());
				resource.content = exchange.getRequestText();
				resource.listeners = this.listeners;
				Response response = new Response(ResponseCode.CREATED);
				response.getOptions().setLocationPath(resource.getURI());
				exchange.respond(response);
				if(!this.isParent){
					this.onNewResource(resource.getURI());
					this.onMessageReceived(exchange);
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
		// logger.debug("PUT " + this.getURI() + "\t" + exchange.getRequestText());
		RedisStorage.getInstance().store(this.getURI(), exchange.getRequestText());
		TimeDatabaseStorage.getInstance().store(this.getURI(), exchange.getRequestText());
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
			resource = new CalipsoSmartParkingResource(name, false);
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

		CalipsoSmartParkingResource resource = new CalipsoSmartParkingResource(segment, false);
		add(resource);
		logger.debug("\tCreated resource: " + resource.getURI());
		return resource.create(path);
	}
	
	public void onAnalysisTerminated(BorderRouterTunslipAnalyzer analyzer) {
		logger.info("****Stopped analyzing...");
	}
	
	public void onAnalysisStarted(BorderRouterTunslipAnalyzer analyzer) {
		logger.info("****Started analyzing...");
	}

	public void onTunslipEvent(BorderRouterTunslipAnalyzer analyzer, Long timestamp, String line) {
		if(line.startsWith("CTIME")){
			Scanner scanner = new Scanner(line);
			scanner.next();
			String node = scanner.next();
			synchronized(this.cTimeForNodes){
				if(this.cTimeForNodes.get(node) == null){
					logger.info("****CTIME: {} joined at {}", node, timestamp);
					this.cTimeForNodes.put(node, timestamp);
				}
			}
		}
	}
	
	
	
}
