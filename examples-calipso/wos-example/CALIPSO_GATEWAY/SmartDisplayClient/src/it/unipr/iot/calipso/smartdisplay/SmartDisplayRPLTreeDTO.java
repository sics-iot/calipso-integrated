package it.unipr.iot.calipso.smartdisplay;

import it.unipr.smartdisplay.messages.agent.AgentActionRequest.AgentActionRequestParameters;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayRPLTreeDTO extends AgentActionRequestParameters {

	private static final long serialVersionUID = -3040342011026645877L;

	public static final String NODE = "node";
	public static final String PARENT = "parent";
	public static final String LQI = "lqi";
	public static final String TIMESTAMP = "timestamp";

	public SmartDisplayRPLTreeDTO(String node, String parent, double lqi, long timestamp) {
		this.addParameter(NODE, node);
		this.addParameter(PARENT, parent);
		this.addParameter(LQI, lqi);
		this.addParameter(TIMESTAMP, timestamp);
	}

	public String getNode() {
		return (String) this.getParameter(NODE);
	}

	public String getParent() {
		return (String) this.getParameter(PARENT);
	}

	public Double getLQI() {
		return (Double) this.getParameter(LQI);
	}

	public Long getTimestamp() {
		return (Long) this.getParameter(TIMESTAMP);
	}

}
