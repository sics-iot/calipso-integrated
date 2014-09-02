package it.unipr.iot.calipso.smartdisplay;

import it.unipr.smartdisplay.messages.agent.AgentActionRequest.AgentActionRequestParameters;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayNewResourceRPLTreeDTO extends AgentActionRequestParameters {

	private static final long serialVersionUID = -3040342011026645877L;

	public static final String RESOURCE = "resource";
	public static final String TIMESTAMP = "timestamp";
	

	public SmartDisplayNewResourceRPLTreeDTO(String resource, long timestamp) {
		this.addParameter(RESOURCE, resource);
		this.addParameter(TIMESTAMP, timestamp);
	}

	public String getResource(){
		return (String) this.getParameter(RESOURCE);
	}

	public Long getTimestamp() {
		return (Long) this.getParameter(TIMESTAMP);
	}

}
