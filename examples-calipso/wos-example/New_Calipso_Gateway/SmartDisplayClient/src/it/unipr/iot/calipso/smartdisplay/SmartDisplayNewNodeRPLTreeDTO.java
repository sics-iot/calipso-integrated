package it.unipr.iot.calipso.smartdisplay;

import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.smartdisplay.messages.agent.AgentActionRequest.AgentActionRequestParameters;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayNewNodeRPLTreeDTO extends AgentActionRequestParameters {

	private static final long serialVersionUID = -3040342011026645877L;

	public static final String NODE = "node";
	public static final String INFO = "info";
	public static final String TIMESTAMP = "timestamp";
	

	public SmartDisplayNewNodeRPLTreeDTO(String node, NodeInfo info, long timestamp) {
		this.addParameter(NODE, node);
		this.addParameter(INFO, info);
		this.addParameter(TIMESTAMP, timestamp);
	}

	public String getNode() {
		return (String) this.getParameter(NODE);
	}
	
	public NodeInfo getInfo(){
		return (NodeInfo) this.getInfo();
	}

	public Long getTimestamp() {
		return (Long) this.getParameter(TIMESTAMP);
	}

}
