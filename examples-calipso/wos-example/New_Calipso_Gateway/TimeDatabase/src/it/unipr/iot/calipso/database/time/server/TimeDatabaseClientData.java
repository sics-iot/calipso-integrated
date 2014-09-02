package it.unipr.iot.calipso.database.time.server;

import it.unipr.iot.calipso.database.time.TimestampedResource;

/**
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 */
public class TimeDatabaseClientData {
	public String uri;
	public TimestampedResource resource;

	public TimeDatabaseClientData() {
	}

	public TimeDatabaseClientData(String uri, TimestampedResource resource) {
		this.uri = uri;
		this.resource = resource;
	}

	public TimeDatabaseClientData(String uri, Long timestamp, String value) {
		this.uri = uri;
		this.resource = new TimestampedResource(timestamp, value);
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public TimestampedResource getResource() {
		return this.resource;
	}

	public void setResource(TimestampedResource resource) {
		this.resource = resource;
	}
}