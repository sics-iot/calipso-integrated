package it.unipr.iot.calipso.http.client.test;

import it.unipr.iot.calipso.http.client.dao.PostParkingSensorDataRequest;
import it.unipr.iot.calipso.http.client.dto.ParkingSensorData;

import org.springframework.http.HttpStatus;

import com.google.gson.Gson;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class BasicHttpClientTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ParkingSensorData data = new ParkingSensorData(ParkingSensorData.FREE);

		PostParkingSensorDataRequest request = new PostParkingSensorDataRequest("http://localhost:8080", data);
		ParkingSensorData response = request.execute();
		System.out.println(request.getStatus());
		if(request.getStatus() == HttpStatus.OK){
			System.out.println(new Gson().toJson(response));
		}

	}

}
