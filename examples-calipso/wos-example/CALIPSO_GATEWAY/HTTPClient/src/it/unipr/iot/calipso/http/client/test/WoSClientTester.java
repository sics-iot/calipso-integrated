package it.unipr.iot.calipso.http.client.test;

import it.unipr.iot.calipso.coap.server.data.WorldSensingServerData;
import it.unipr.iot.calipso.http.client.dao.WorldsensingServerRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class WoSClientTester {

	private static Logger logger = LoggerFactory.getLogger(WoSClientTester.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String data = "882F280001CBFFFF";
		WorldSensingServerData wosData = new WorldSensingServerData(data);
		logger.info(wosData.isOccupationPacket() ? "Occupation packet -> post to server" : "Other packet -> discard");
		if(wosData.isOccupationPacket()){
			logger.info("Parking spot status: {}", wosData.getOccupation() ? "busy" : "free");
		}
		ResponseEntity<String> response = WorldsensingServerRequest.postData(WorldsensingServerRequest.Localhost_BASE_URL, wosData);
		if(response != null){
			logger.info("{} {}", response.getStatusCode(), response.getStatusCode().getReasonPhrase());
		}
		else{
			logger.error("An error occurred");
		}
	}
}
