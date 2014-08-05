package it.unipr.iot.calipso.http.client.dto;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class ParkingSensorData {

	public static final boolean FREE = true;
	public static final boolean BUSY = false;

	private boolean free;

	public ParkingSensorData() {
		this.free = FREE;
	}

	public ParkingSensorData(boolean free) {
		this.free = free;
	}

	public boolean isFree() {
		return this.free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

}
