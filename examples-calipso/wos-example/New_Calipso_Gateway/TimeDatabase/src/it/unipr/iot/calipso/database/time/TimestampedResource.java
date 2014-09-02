package it.unipr.iot.calipso.database.time;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class TimestampedResource {

	private Long timestamp;
	private String value;

	public TimestampedResource() {
	}

	public TimestampedResource(Long timestamp, String value) {
		this.timestamp = timestamp;
		this.value = value;
	}

	public Long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		return this.timestamp + "\t" + this.value;
	}

}
