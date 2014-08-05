package it.unipr.iot.calipso.coap.server.data;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class RPLData {

	private String parentId;
	private double LQI;

	public RPLData() {
	}

	public RPLData(String parentId, double lqi) {
		super();
		this.parentId = parentId;
		this.LQI = lqi;
	}

	public String getParentId() {
		return this.parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public double getLQI() {
		return this.LQI;
	}

	public void setLQI(double lQI) {
		this.LQI = lQI;
	}

}
