package it.unipr.iot.calipso.smartdisplay;

import it.unipr.iot.calipso.smartdisplay.http.SmartDisplayActionRequest;
import it.unipr.iot.calipso.smartdisplay.http.SmartDisplayConnectionRequest;
import it.unipr.smartdisplay.messages.agent.AgentActionRequest.AgentActionRequestParameters;
import it.unipr.smartdisplay.messages.agent.AgentActionResponse;
import it.unipr.smartdisplay.messages.agent.AgentConnectionResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class CalipsoSmartDisplayClient {

	private static final Logger logger = LoggerFactory.getLogger(CalipsoSmartDisplayClient.class);

	private String proxy;
	private String from;
	private String token;

	public CalipsoSmartDisplayClient() {
		this(null, null, null);
	}

	public CalipsoSmartDisplayClient(String proxy, String from, String token) {
		this.proxy = proxy;
		this.from = from;
		this.token = token;
	}

	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void connect(String display, String app) {
		SmartDisplayConnectionRequest connectionRequest = new SmartDisplayConnectionRequest(this.proxy, this.from, display, app);
		logger.info("Connecting to {} -> {}/{}", this.proxy, display, app);
		AgentConnectionResponse connectionResponse = connectionRequest.execute();
		if(connectionRequest.getStatus() == HttpStatus.OK){
			String token = connectionResponse.getParams().getToken();
			this.token = token;
			logger.info("Connected! [session token = {}]", this.token);
		}
		else logger.error("Unable to connect!");
	}
	
	public void doSmartDisplayAction(String action, AgentActionRequestParameters params){
		if(this.token == null){
			logger.error("Missing token!");
			return;
		}
		SmartDisplayActionRequest actionRequest = new SmartDisplayActionRequest(this.proxy, this.from, action, params, this.token);
		AgentActionResponse actionResponse = actionRequest.execute();
		if(actionRequest.getStatus() == HttpStatus.OK){
			if(actionResponse.getStatus() == 200){
				logger.debug("Smart Display updated");
			}
			else{
				logger.debug("Smart Display not updated [{}]", actionResponse.getStatus());
			}
		}
	}
	
	public void disconnect() {
		logger.info("Disconnecting...");
		// SmartDisplayDisconnectionRequest disconnectionRequest = new SmartDisplayDisconnectionRequest(proxy, token);
		// AgentDisconnectionResponse disconnectionResponse = disconnectionRequest.execute();
		// if(disconnectionRequest.getStatus() == HttpStatus.OK){
		logger.info("Disconnected!");
	}

}
