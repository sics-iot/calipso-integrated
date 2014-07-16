package it.unipr.iot.calipso.http.client.dao;

import it.unipr.iot.calipso.http.client.AbstractJSONRequest;
import it.unipr.iot.calipso.http.client.dto.ParkingSensorData;

import org.springframework.http.HttpMethod;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class PostParkingSensorDataRequest extends AbstractJSONRequest<ParkingSensorData> {

	private ParkingSensorData data;

	/**
	 * @param user
	 * @param uuid
	 * @param token
	 */
	public PostParkingSensorDataRequest(String url, ParkingSensorData data) {
		super(HttpMethod.POST, url);
		this.data = data;
	}

	public ParkingSensorData execute() {
		return super.execute(this.data, ParkingSensorData.class);
	}

}
