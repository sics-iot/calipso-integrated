package it.unipr.iot.calipso.smartdisplay.http;

import it.unipr.iot.calipso.http.client.AbstractJSONRequest;
import it.unipr.smartdisplay.messages.agent.AgentConnectionRequest;
import it.unipr.smartdisplay.messages.agent.AgentConnectionRequest.AgentConnectionRequestParameters;
import it.unipr.smartdisplay.messages.agent.AgentConnectionResponse;

import org.springframework.http.HttpMethod;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayConnectionRequest extends AbstractJSONRequest<AgentConnectionResponse> {

	private String from;
	private String display;
	private String application;

	public SmartDisplayConnectionRequest(String url, String from, String display, String application) {
		super(HttpMethod.POST, url);
		this.from = from;
		this.display = display;
		this.application = application;
	}

	public AgentConnectionResponse execute() {
		AgentConnectionRequest connectionRequest = new AgentConnectionRequest("connection-" + System.currentTimeMillis(), System.currentTimeMillis(), new AgentConnectionRequestParameters(this.display, this.application), this.from);
		return super.execute(connectionRequest, AgentConnectionResponse.class);
	}
}
