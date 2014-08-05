package it.unipr.iot.calipso.smartdisplay.test;

import it.unipr.iot.calipso.smartdisplay.SmartDisplayRPLTreeDTO;
import it.unipr.iot.calipso.smartdisplay.SmartDisplayResourceDTO;
import it.unipr.iot.calipso.smartdisplay.http.SmartDisplayActionRequest;
import it.unipr.iot.calipso.smartdisplay.http.SmartDisplayConnectionRequest;
import it.unipr.smartdisplay.messages.agent.AgentActionResponse;
import it.unipr.smartdisplay.messages.agent.AgentConnectionResponse;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class CalipsoSmartDisplayTester {

	private static final Logger logger = LoggerFactory.getLogger(CalipsoSmartDisplayTester.class);

	public static void main(String[] args) {

		String proxy = "http://172.30.0.234:9876/httpdisplay";
		String display = "Display_1";
		String app = "App_1";
		String from = CalipsoSmartDisplayTester.class.getCanonicalName();

		String[] nodeIds = { "12680", "12704", "12713", "12707", "12717", "12719" };
		String[] parentIds = { "0", "0", "12680", "12680", "12704", "12704" };

		String[] resources = { "presence", "energy" };

		java.util.Random random = new java.util.Random(1000);
		HashMap<String, String> presenceMap = new HashMap<String, String>();
		for(int i = 0; i < nodeIds.length; i++){
			String presence = (random.nextBoolean() ? "busy" : "free");
			presenceMap.put(nodeIds[i], presence);
		}
		double energy = 18;
		String token = null;

		if(token == null){
			token = connect(proxy, from, display, app);

		}

		if(token != null){
			for(int i = 0; i >= 0; i++){
				int index = random.nextInt(nodeIds.length);
				String node = nodeIds[index];

				String res = resources[random.nextInt(resources.length)];
				String resource = "/parking/" + node + "/" + res;
				String value;
				if(res.equals("presence")){
					String presence = presenceMap.get(node);
					if(presence.equals("busy")) presence = "free";
					else presence = "busy";
					presenceMap.put(node, presence);
					value = presence;
				}
				else{
					energy -= (random.nextDouble() / 10);
					value = new Double(energy).toString();
				}
				updateResource(proxy, from, token, resource, value);

				pause((random.nextInt(10) + 1) * 1000);

				String parentId = parentIds[index];
				updateTree(proxy, from, token, "/parking/" + node, "/parking/" + parentId, random.nextDouble() * 10);

				pause((random.nextInt(10) + 1) * 1000);
			}

		}
		disconnect();
	}

	private static void updateResource(String proxy, String from, String token, String resource, String value) {
		SmartDisplayResourceDTO params = new SmartDisplayResourceDTO(resource, System.currentTimeMillis(), value);
		logger.info("Updating resource {} = {}", params.getResource(), params.getValue());
		SmartDisplayActionRequest actionRequest = new SmartDisplayActionRequest(proxy, from, "resourceUpdate", params, token);
		AgentActionResponse actionResponse = actionRequest.execute();
		if(actionRequest.getStatus() == HttpStatus.OK){
			if(actionResponse.getStatus() == 200){
				logger.info("Smart Display updated");
			}
			else{
				logger.info("Smart Display not updated [{}]", actionResponse.getStatus());
			}
		}
	}

	private static void updateTree(String proxy, String from, String token, String node, String parent, double lqi) {
		SmartDisplayRPLTreeDTO params = new SmartDisplayRPLTreeDTO(node, parent, lqi, System.currentTimeMillis());
		logger.info("Updating tree link {} -> {} [lqi={}]", params.getNode(), params.getParent(), params.getLQI());
		SmartDisplayActionRequest actionRequest = new SmartDisplayActionRequest(proxy, from, "treeChange", params, token);
		AgentActionResponse actionResponse = actionRequest.execute();
		if(actionRequest.getStatus() == HttpStatus.OK){
			if(actionResponse.getStatus() == 200){
				logger.info("Smart Display updated");
			}
			else{
				logger.info("Smart Display not updated [{}]", actionResponse.getStatus());
			}
		}
	}

	private static String connect(String proxy, String from, String display, String app) {
		SmartDisplayConnectionRequest connectionRequest = new SmartDisplayConnectionRequest(proxy, from, display, app);
		logger.info("Connecting...");
		AgentConnectionResponse connectionResponse = connectionRequest.execute();
		if(connectionRequest.getStatus() == HttpStatus.OK){
			String token = connectionResponse.getParams().getToken();
			logger.info("Connected! [session token = {}]", token);
			return token;
		}
		logger.info("Unable to connect!");
		return null;
	}

	private static void disconnect() {
		logger.info("Disconnecting...");
		// SmartDisplayDisconnectionRequest disconnectionRequest = new SmartDisplayDisconnectionRequest(proxy, token);
		// AgentDisconnectionResponse disconnectionResponse = disconnectionRequest.execute();
		// if(disconnectionRequest.getStatus() == HttpStatus.OK){
		logger.info("Disconnected!");
	}

	private static void pause(long millis) {
		try{
			Thread.sleep(millis);
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}

}
