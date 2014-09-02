package it.unipr.iot.calipso.coap.server;

import it.unipr.iot.calipso.coap.server.data.RPLData;
import it.unipr.iot.calipso.coap.server.data.TimeDatabaseStorage;
import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.coap.server.util.NodeManager;
import it.unipr.iot.calipso.tools.tunslip.RPLAnalyzer;
import it.unipr.iot.calipso.util.CoapUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

import com.google.gson.Gson;

public class NodeListener implements CoAPEventListener {
	
	private final static Logger logger = LoggerFactory.getLogger(NodeListener.class);
	
	public static final String NODE_INFO_LIST = "_nodeinfo";
	
	private RPLAnalyzer rplAnalyzer;
	
	public void setRPLAnalyzer(RPLAnalyzer rplAnalyzer){
		this.rplAnalyzer = rplAnalyzer;
	}
	
	public void onNewNode(String nodeId, NodeInfo nodeInfo) {
		logger.info("onNewNode(\"{}\") ({})", nodeId, nodeInfo);
		if(this.rplAnalyzer != null){
			String ipAddress = nodeInfo.getIp();
			Long timestamp = this.rplAnalyzer.getTimestampForNode(ipAddress);
			nodeInfo.setParentReadyTime(timestamp);
			logger.info("{} -> CTIME({}) = {}", nodeId, ipAddress, timestamp);
		}
		NodeManager.getInstance().addNode(nodeId, nodeInfo);
		TimeDatabaseStorage.getInstance().store(NODE_INFO_LIST, new Gson().toJson(NodeManager.getInstance().getNodeInfoList()));
	}

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
		if(sender != null){
			if(resource != null && resource.equals(CalipsoResources.RPL_INFO_SERVICE)){
				RPLData data = new Gson().fromJson(exchange.getRequestText(), RPLData.class);
				if(data != null){
					NodeInfo nodeInfo = NodeManager.getInstance().getNode(sender);
					nodeInfo.setParentId(data.getParentId());
					NodeManager.getInstance().addNode(sender, nodeInfo);
					TimeDatabaseStorage.getInstance().store(NODE_INFO_LIST, new Gson().toJson(NodeManager.getInstance().getNodeInfoList()));
				}
			}
		}
	}
	
}
