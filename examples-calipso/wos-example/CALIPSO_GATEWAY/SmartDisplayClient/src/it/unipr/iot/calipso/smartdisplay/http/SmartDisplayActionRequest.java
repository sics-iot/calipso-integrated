package it.unipr.iot.calipso.smartdisplay.http;

import it.unipr.iot.calipso.http.client.AbstractJSONRequest;
import it.unipr.smartdisplay.messages.agent.AgentActionRequest;
import it.unipr.smartdisplay.messages.agent.AgentActionRequest.AgentActionRequestParameters;
import it.unipr.smartdisplay.messages.agent.AgentActionResponse;

import org.springframework.http.HttpMethod;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayActionRequest extends AbstractJSONRequest<AgentActionResponse> {

	private AgentActionRequest action;

	public SmartDisplayActionRequest(String url, AgentActionRequest action) {
		super(HttpMethod.POST, url);
		this.action = action;
	}

	public SmartDisplayActionRequest(String url, String from, String action, AgentActionRequestParameters params, String token) {
		super(HttpMethod.POST, url);
		AgentActionRequest actionRequest = new AgentActionRequest("action-" + System.currentTimeMillis(), System.currentTimeMillis(), null, null, null, params, from, token, action);
		this.action = actionRequest;
	}

	public AgentActionResponse execute() {
		return super.execute(this.action, AgentActionResponse.class);
	}
}
