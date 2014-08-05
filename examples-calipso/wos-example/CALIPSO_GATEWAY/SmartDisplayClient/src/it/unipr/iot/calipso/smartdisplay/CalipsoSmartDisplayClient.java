package it.unipr.iot.calipso.smartdisplay;

import it.unipr.iot.calipso.smartdisplay.http.SmartDisplayActionRequest;
import it.unipr.iot.calipso.smartdisplay.http.SmartDisplayConnectionRequest;
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

	public void updateResource(String resource, String value) {
		if(this.token == null){
			logger.error("Missing token!");
			return;
		}
		SmartDisplayResourceDTO params = new SmartDisplayResourceDTO(resource, System.currentTimeMillis(), value);
		logger.debug("Updating resource {} = {}", params.getResource(), params.getValue());
		SmartDisplayActionRequest actionRequest = new SmartDisplayActionRequest(this.proxy, this.from, "resourceUpdate", params, this.token);
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

	public void updateTree(String node, String parent, double lqi) {
		if(this.token == null){
			logger.error("Missing token!");
			return;
		}
		SmartDisplayRPLTreeDTO params = new SmartDisplayRPLTreeDTO(node, parent, lqi, System.currentTimeMillis());
		logger.debug("Updating tree link {} -> {} [lqi={}]", params.getNode(), params.getParent(), params.getLQI());
		SmartDisplayActionRequest actionRequest = new SmartDisplayActionRequest(this.proxy, this.from, "treeChange", params, this.token);
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

	public void connect(String display, String app) {
		SmartDisplayConnectionRequest connectionRequest = new SmartDisplayConnectionRequest(this.proxy, this.from, display, app);
		logger.info("Connecting to {} -> {}/{}", this.proxy, display, app);
		AgentConnectionResponse connectionResponse = connectionRequest.execute();
		if(connectionRequest.getStatus() == HttpStatus.OK){
			String token = connectionResponse.getParams().getToken();
			logger.info("Connected! [session token = {}]", token);
			this.token = token;
		}
		else logger.error("Unable to connect!");
	}

	public void disconnect() {
		logger.info("Disconnecting...");
		// SmartDisplayDisconnectionRequest disconnectionRequest = new SmartDisplayDisconnectionRequest(proxy, token);
		// AgentDisconnectionResponse disconnectionResponse = disconnectionRequest.execute();
		// if(disconnectionRequest.getStatus() == HttpStatus.OK){
		logger.info("Disconnected!");
	}

}
