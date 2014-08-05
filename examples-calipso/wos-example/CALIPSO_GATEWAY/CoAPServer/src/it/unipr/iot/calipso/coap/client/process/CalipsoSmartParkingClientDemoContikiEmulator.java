package it.unipr.iot.calipso.coap.client.process;

import it.unipr.iot.calipso.coap.server.data.RPLData;
import it.unipr.iot.calipso.coap.server.data.WorldSensingServerData;
import it.unipr.iot.calipso.coap.server.process.CalipsoSmartParkingServer;
import it.unipr.iot.calipso.coap.server.resources.CalipsoSmartParkingResource;

import java.io.IOException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

import com.google.gson.Gson;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class CalipsoSmartParkingClientDemoContikiEmulator {

	private static final Logger logger = LoggerFactory.getLogger(CalipsoSmartParkingClientDemoContikiEmulator.class);

	public static final String BASE_PATH = "parking";
	private String id;
	private String serverHost;
	private int serverPort;

	private String parentId;

	private int messageSent;

	public CalipsoSmartParkingClientDemoContikiEmulator(String id, String serverHost, int serverPort) {
		this.id = id;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.messageSent = 0;
	}

	public CalipsoSmartParkingClientDemoContikiEmulator(String id, String serverHost) {
		this(id, serverHost, 5683);
	}

	private boolean registerClientOnServer() throws InterruptedException {
		Request request = Request.newPost();
		request.setType(CoAP.Type.NON);
		String uri = this.getResourceURIBase();
		request.setURI(uri);
		request.setPayload(this.id);
		logger.debug("SENT: " + request.toString());
		request.send();
		this.messageSent++;
		Response response = request.waitForResponse(); // request.waitForResponse(1000);
		logger.debug("RECV: " + response.toString() + "\n\t" + response.getOptions().getLocationPathString());
		if(response.getCode() == ResponseCode.CREATED && response.getOptions().getLocationPathString().length() > BASE_PATH.length() + 1){
			String id = response.getOptions().getLocationPathString().substring(BASE_PATH.length() + 1);
			logger.debug("\tAssigned id = " + id);
			this.id = id;
			return true;
		}
		else return false;
	}

	private boolean registerResourceOnServer(String resource, String payload) throws InterruptedException {
		Request request = Request.newPost();
		request.setType(CoAP.Type.NON);
		String uri = this.getResourceURI(resource);
		request.setURI(uri);
		request.setPayload(payload);
		logger.debug("SENT: " + request.toString());
		request.send();
		this.messageSent++;
		Response response = request.waitForResponse(); // request.waitForResponse(1000);
		logger.debug("RECV: " + response.toString() + "\n\t" + response.getOptions().getLocationPathString());
		if(response.getCode() == ResponseCode.CREATED){
			return true;
		}
		else return false;
	}

	private boolean updateResourceOnServer(String resource, String payload) throws InterruptedException {
		Request request = Request.newPut();
		request.setType(CoAP.Type.NON);
		String uri = this.getResourceURI(resource);
		request.setURI(uri);
		request.setPayload(payload);
		logger.debug("SENT: " + request.toString());
		request.send();
		this.messageSent++;
		Response response = request.waitForResponse(); // request.waitForResponse(1000);
		logger.debug("RECV: " + response.toString() + "\n\t" + response.getOptions().getLocationPathString());
		if(response.getCode() == ResponseCode.CHANGED){
			return true;
		}
		else return false;
	}

	private String getResourceURIBase() {
		return this.serverHost + ":" + this.serverPort + "/" + BASE_PATH;
	}

	private String getResourceURI(String resource) {
		return this.getResourceURIBase() + "/" + this.id + "/" + resource;
	}

	public void start() {
		new Thread() {
			public void run() {
				// long seed = 666;
				// Random random = new Random(seed);
				Random random = new Random();
				try{
					if(registerClientOnServer()){
						registerResourceOnServer(CalipsoSmartParkingServer.DELAY_SERVICE, new Gson().toJson(null));
						boolean presenceValue = true;
						double energyValue = 22.1231452;
						//boolean presence = registerResourceOnServer(CalipsoSmartParkingResource.PRESENCE_SERVICE, presenceValue ? "free" : "busy");
						boolean presence = registerResourceOnServer(CalipsoSmartParkingResource.PRESENCE_SERVICE, new WorldSensingServerData(id,presenceValue).getValue());
						Thread.sleep(1000);
						RPLData rplData = new RPLData(parentId, random.nextDouble() * 10);
						// String rplValue = parentId;
						boolean rpl = registerResourceOnServer(CalipsoSmartParkingServer.RPL_INFO_SERVICE, new Gson().toJson(rplData));
						Thread.sleep(1000);
						boolean energy = registerResourceOnServer(CalipsoSmartParkingServer.ENERGY_SERVICE, new Double(energyValue).toString());
						Thread.sleep(1000);
						boolean pdr = registerResourceOnServer(CalipsoSmartParkingServer.SENT_MESSAGES_SERVICE, new Integer(messageSent).toString());
						Thread.sleep(1000);
						while (true){
							// while (messageSent < 200){ // simulation lasts for 200 messages
							int service = random.nextInt(4);
							switch (service) {
								case 0: // presence
									if(presence){ // only if resource has been created before
										boolean b = random.nextBoolean();
										if(b != presenceValue){
											presenceValue = b;
											//updateResourceOnServer(CalipsoSmartParkingResource.PRESENCE_SERVICE, presenceValue ? "free" : "busy");
											updateResourceOnServer(CalipsoSmartParkingResource.PRESENCE_SERVICE, new WorldSensingServerData(id,presenceValue).getValue());
										}
									}
									break;
								case 1: // RPL Info
									if(rpl){ // only if resource has been created before
										double lqi = random.nextDouble() * 10;
										RPLData updatedRplData = new RPLData(parentId, lqi);
										updateResourceOnServer(CalipsoSmartParkingServer.RPL_INFO_SERVICE, new Gson().toJson(updatedRplData));
									}
									break;

								case 2: // energy
									if(energy){ // only if resource has been created before
										energyValue -= (random.nextDouble() / 10);
										updateResourceOnServer(CalipsoSmartParkingServer.ENERGY_SERVICE, new Double(energyValue).toString());
									}
									break;
								case 3: // sent messages
									if(pdr){ // only if resource has been created before
										updateResourceOnServer(CalipsoSmartParkingServer.SENT_MESSAGES_SERVICE, new Integer(messageSent).toString());
									}
									break;
								default:
									break;
							}
							long wait = (random.nextInt(5) + 5) * 1000;
							pause(wait);
						}
					}
					else{
						logger.info("Client could not be registered");
					}
				} catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}.start();
	}

	public String getParentId() {
		return this.parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getId() {
		return this.id;
	}

	public static void main(String[] args) {

		String[] nodeIds = { "12680", "12704", "12713", "12707", "12717", "12719" };
		CalipsoSmartParkingClientDemoContikiEmulator[] clients = new CalipsoSmartParkingClientDemoContikiEmulator[nodeIds.length];

		//Random r = new Random();
		for(int i = 0; i < nodeIds.length; i++){
			clients[i] = new CalipsoSmartParkingClientDemoContikiEmulator(nodeIds[i], "localhost");
			// clients[i].setParentId("/" + BASE_PATH + "/" + (i / 2));
			if(i < 2) clients[i].setParentId("/" + BASE_PATH + "/0");
			else clients[i].setParentId("/" + BASE_PATH + "/" + clients[i / 2 - 1].getId());
			logger.info("New Node /{}/{} with parent {}", BASE_PATH, clients[i].getId(), clients[i].getParentId());
			clients[i].start();
			//pause((r.nextInt(5) + 5) * 1000);
			logger.info("type to continue...");
			waitForConsole();
		}

	}

	private static void pause(long millis) {
		try{
			Thread.sleep(millis);
		} catch (InterruptedException e){
		}
	}

	private static void waitForConsole() {
		try{
			System.in.read();
		} catch (IOException e){
		}
	}

}
