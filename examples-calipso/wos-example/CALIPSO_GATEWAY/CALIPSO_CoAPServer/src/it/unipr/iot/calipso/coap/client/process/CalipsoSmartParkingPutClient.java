package it.unipr.iot.calipso.coap.client.process;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class CalipsoSmartParkingPutClient {

	public static void main(String[] args) {

		try{

			try{
				Request request = Request.newPut();
				request.setURI("localhost:5683/parking");
				request.send();
				Response response = request.waitForResponse(1000);
				System.out.println("received " + response);
			} catch (Exception e){
				e.printStackTrace();
			}

		} catch (Exception e){
			e.printStackTrace();
		}

	}

}