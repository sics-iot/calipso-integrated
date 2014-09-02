package it.unipr.iot.calipso.smartdisplay;

import it.unipr.iot.calipso.database.time.TimestampedResource;
import it.unipr.smartdisplay.messages.agent.AgentActionRequest.AgentActionRequestParameters;

import com.google.gson.Gson;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayResourceDTO extends AgentActionRequestParameters {

	private static final long serialVersionUID = -3040342011026645877L;

	public static final String RESOURCE = "resource";
	public static final String DATA = "data";
	public static final String TIMESTAMP = "timestamp";
	public static final String VALUE = "value";

	public SmartDisplayResourceDTO(String resource, long timestamp, String value) {
		this.addParameter(RESOURCE, resource);
		TimestampedResource res = new TimestampedResource(timestamp, value);
		this.addParameter(DATA, new Gson().toJson(res));
	}

	public String getResource() {
		return (String) this.getParameter(RESOURCE);
	}

	public String getData() {
		return (String) this.getParameter(DATA);
	}

	public TimestampedResource getTimestampedResource() {
		String data = this.getData();
		if(data != null){
			TimestampedResource res = new Gson().fromJson(data, TimestampedResource.class);
			return res;
		}
		else return null;
	}

	public Long getTimestamp() {
		TimestampedResource res = this.getTimestampedResource();
		if(res != null) return res.getTimestamp();
		else return null;
	}

	public String getValue() {
		TimestampedResource res = this.getTimestampedResource();
		if(res != null) return res.getValue();
		else return null;
	}

}
