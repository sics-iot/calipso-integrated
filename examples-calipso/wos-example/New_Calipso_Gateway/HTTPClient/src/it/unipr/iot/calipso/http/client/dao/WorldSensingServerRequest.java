package it.unipr.iot.calipso.http.client.dao;

import it.unipr.iot.calipso.coap.server.data.WorldSensingServerData;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class WorldSensingServerRequest {

	private static Logger logger = LoggerFactory.getLogger(WorldSensingServerRequest.class);

	public static final String WorldSensing_BASE_URL = "http://incoming3.wocs3.com/sensorinfo/msg";
	public static final String Localhost_BASE_URL = "http://localhost:8080/sensorinfo/msg";

	public static ResponseEntity<String> postData(String baseURL, WorldSensingServerData data) {
		try{
			if(!data.isValidData()) return null;
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
			long id = 1000;
			long seconds = System.currentTimeMillis() / 1000l;
			boolean duplicate = false;
			long signal = 100;
			String station = "54C";
			URI targetUrl = UriComponentsBuilder.fromUriString(baseURL).queryParam("id", id).queryParam("time", seconds).queryParam("duplicate", duplicate).queryParam("signal", signal).queryParam("station", station).queryParam("data", data.getValue()).build().toUri();
			logger.info("GET {}", targetUrl.toString());
			ResponseEntity<String> result = restTemplate.getForEntity(targetUrl, String.class);
			logger.info("{} {} - {}", result.getStatusCode(), result.getStatusCode().getReasonPhrase(), result.getBody());
			return result;
		} catch (RestClientException e){
			logger.error(e.getMessage());
			return null;
		}
	}
}
