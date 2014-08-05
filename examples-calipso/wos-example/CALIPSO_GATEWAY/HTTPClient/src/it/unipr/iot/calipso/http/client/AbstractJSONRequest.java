/**
 * 
 *
 * @author	Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 * Created on 28/mar/2014 
 *
 */

package it.unipr.iot.calipso.http.client;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractJSONRequest<T> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractJSONRequest.class);

	protected String url;
	protected HttpMethod method;
	protected HttpStatus status;
	private String responseBody;

	private static final MediaType APPLICATION_JSON = new MediaType("application", "json");

	protected AbstractJSONRequest(HttpMethod method, String url) {
		this.method = method;
		this.url = url;
	}

	protected AbstractJSONRequest(HttpMethod method, String url, String uuid, String token) {
		this(method, url);
		if(uuid != null && token != null){
			this.url = this.url + "?uuid=" + uuid + "&token=" + token;
		}
	}

	protected AbstractJSONRequest(HttpMethod method, String url, String uuid, String token, Map<String, Object> params) {
		this(method, url, uuid, token);
		if(params != null){
			if(uuid != null && token != null){
				for(String key : params.keySet()){
					this.url = this.url + "&" + key + "=" + params.get(key);
				}
			}
		}
	}

	public abstract T execute();

	protected T execute(Object entity, Class<T> returnType) {
		return this.execute(entity, returnType, new HttpMessageConverter<?>[] {});
	}

	protected T execute(Object entity, Class<T> returnType, HttpMessageConverter<?>... converters) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		for(HttpMessageConverter<?> converter : converters){
			restTemplate.getMessageConverters().add(converter);
		}
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAccept(Collections.singletonList(APPLICATION_JSON));
		if(entity != null){
			requestHeaders.setContentType(APPLICATION_JSON);
		}
		HttpEntity<?> requestEntity;
		if(entity == null){
			requestEntity = new HttpEntity<Object>(requestHeaders);
		}
		else{
			requestEntity = new HttpEntity<Object>(entity, requestHeaders);
		}
		ResponseEntity<T> responseEntity;
		T t = null;
		try{
			logger.debug("{} {}", this.method, this.url);
			responseEntity = restTemplate.exchange(this.url, this.method, requestEntity, returnType);
			this.status = responseEntity.getStatusCode();
			t = responseEntity.getBody();
		} catch (RestClientException e){
			if(e instanceof HttpStatusCodeException){
				HttpStatusCodeException ex = (HttpStatusCodeException) e;
				this.status = ex.getStatusCode();
				this.responseBody = ex.getResponseBodyAsString();
			}
			else if(e instanceof ResourceAccessException){
				this.status = null;
				this.responseBody = null;
			}
			return null;
		}
		return t;
	}

	public HttpMethod getMethod() {
		return this.method;
	}

	public String getURL() {
		return this.url;
	}

	public HttpStatus getStatus() {
		return this.status;
	}

	public String getResponseBodyAsString() {
		return this.responseBody;
	}

}
