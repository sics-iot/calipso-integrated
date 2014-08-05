package it.unipr.iot.calipso.smartdisplay.http;

import it.unipr.iot.calipso.http.client.AbstractJSONRequest;
import it.unipr.smartdisplay.messages.agent.AgentDisconnectionRequest;
import it.unipr.smartdisplay.messages.agent.AgentDisconnectionRequest.AgentDisconnectionRequestParameters;
import it.unipr.smartdisplay.messages.agent.AgentDisconnectionResponse;

import org.springframework.http.HttpMethod;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayDisconnectionRequest extends AbstractJSONRequest<AgentDisconnectionResponse> {

	private String from;
	private String token;

	public SmartDisplayDisconnectionRequest(String url, String from, String token) {
		super(HttpMethod.POST, url);
		this.from = from;
		this.token = token;
	}

	public AgentDisconnectionResponse execute() {
		AgentDisconnectionRequest disconnectionRequest = new AgentDisconnectionRequest("disconnection-" + System.currentTimeMillis(), System.currentTimeMillis(), new AgentDisconnectionRequestParameters(this.token), this.from, this.token);
		return super.execute(disconnectionRequest, AgentDisconnectionResponse.class);
	}
}
