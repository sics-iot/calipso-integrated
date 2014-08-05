package it.unipr.iot.calipso.tools.ping.util;

import it.unipr.iot.calipso.http.client.AbstractJSONRequest;

import org.springframework.http.HttpMethod;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class NodeListRequest extends AbstractJSONRequest<String> {

	public NodeListRequest(String url) {
		super(HttpMethod.GET, url);
	}

	public String execute() {
		return super.execute(null, String.class);
	}

}
