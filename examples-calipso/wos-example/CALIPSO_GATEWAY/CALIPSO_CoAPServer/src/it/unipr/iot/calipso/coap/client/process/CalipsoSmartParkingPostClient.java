package it.unipr.iot.calipso.coap.client.process;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class CalipsoSmartParkingPostClient {

	public static void main(String[] args) {

		try{
			Request request = Request.newPost();
			// request.setURI("localhost:5683/parking");
			request.setURI("localhost:5683/parking/1/rpl");
			request.setPayload("10");
			request.send();
			Response response = request.waitForResponse(1000);
			System.out.println("received: " + response.getOptions().getLocationPathString());
		} catch (Exception e){
			e.printStackTrace();
		}

	}

}
