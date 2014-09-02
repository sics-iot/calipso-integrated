package it.unipr.iot.calipso.http.client.dao;

import it.unipr.iot.calipso.database.time.TimestampedResource;
import it.unipr.iot.calipso.database.time.server.TimeDatabaseClientData;
import it.unipr.iot.calipso.http.client.AbstractJSONRequest;

import org.springframework.http.HttpMethod;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class PostTimeDatabaseRequest extends AbstractJSONRequest<TimeDatabaseClientData> {

	private TimeDatabaseClientData data;

	public PostTimeDatabaseRequest(String url, String key, String value) {
		super(HttpMethod.POST, url);
		TimestampedResource resource = new TimestampedResource(System.currentTimeMillis(), value);
		TimeDatabaseClientData data = new TimeDatabaseClientData(key, resource);
		this.data = data;
	}

	public PostTimeDatabaseRequest(String url, TimeDatabaseClientData data) {
		super(HttpMethod.POST, url);
		this.data = data;
	}

	public TimeDatabaseClientData execute() {
		return super.execute(this.data, TimeDatabaseClientData.class);
	}

	public TimeDatabaseClientData getData() {
		return this.data;
	}
}
