package it.unipr.iot.calipso.coap.client.process;

import it.unipr.iot.calipso.coap.server.process.CalipsoSmartParkingServer;
import it.unipr.iot.calipso.coap.server.resources.CalipsoSmartParkingResource;

import java.util.Random;
import java.util.logging.Logger;

import ch.ethz.inf.vs.californium.coap.CoAP;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class CalipsoSmartParkingClientDemoContikiEmulator {

	/** The logger. */
	protected final static Logger LOGGER = Logger.getLogger(ResourceBase.class.getCanonicalName());

	public static final String path = "parking";
	private String id;
	private String serverHost;
	private int serverPort;

	public CalipsoSmartParkingClientDemoContikiEmulator(String serverHost, int serverPort) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}

	public CalipsoSmartParkingClientDemoContikiEmulator(String serverHost) {
		this(serverHost, 5683);
	}

	private boolean registerClientOnServer() throws InterruptedException {
		Request request = Request.newPost();
		request.setType(CoAP.Type.NON);
		String uri = this.getResourceURIBase();
		request.setURI(uri);
		LOGGER.info("SENT: " + request.toString());
		request.send();
		Response response = request.waitForResponse(); // request.waitForResponse(1000);
		LOGGER.info("RECV: " + response.toString() + "\n\t" + response.getOptions().getLocationPathString());
		if(response.getCode() == ResponseCode.CREATED && response.getOptions().getLocationPathString().length() > path.length() + 1){
			String id = response.getOptions().getLocationPathString().substring(path.length() + 1);
			LOGGER.info("\tAssigned id = " + id);
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
		LOGGER.info("SENT: " + request.toString());
		request.send();
		Response response = request.waitForResponse(); // request.waitForResponse(1000);
		LOGGER.info("RECV: " + response.toString() + "\n\t" + response.getOptions().getLocationPathString());
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
		LOGGER.info("SENT: " + request.toString());
		request.send();
		Response response = request.waitForResponse(); // request.waitForResponse(1000);
		LOGGER.info("RECV: " + response.toString() + "\n\t" + response.getOptions().getLocationPathString());
		if(response.getCode() == ResponseCode.CHANGED){
			return true;
		}
		else return false;
	}

	private String getResourceURIBase() {
		return this.serverHost + ":" + this.serverPort + "/" + path;
	}

	private String getResourceURI(String resource) {
		return this.getResourceURIBase() + "/" + this.id + "/" + resource;
	}

	public static void main(String[] args) {

		CalipsoSmartParkingClientDemoContikiEmulator client = new CalipsoSmartParkingClientDemoContikiEmulator("localhost");
		try{
			if(client.registerClientOnServer()){
				boolean presenceValue = true;
				int rplValue = 10;
				double energyValue = 2.1231452;
				boolean presence = client.registerResourceOnServer(CalipsoSmartParkingResource.PRESENCE_SERVICE, presenceValue ? "free" : "busy");
				Thread.sleep(1000);
				boolean rpl = client.registerResourceOnServer(CalipsoSmartParkingServer.RPL_INFO_SERVICE, new Integer(rplValue).toString());
				Thread.sleep(1000);
				boolean energy = client.registerResourceOnServer(CalipsoSmartParkingServer.ENERGY_SERVICE, new Double(energyValue).toString());
				Thread.sleep(1000);
				long seed = 666;
				Random random = new Random(seed);
				while (true){
					int service = random.nextInt(3);
					switch (service) {
						case 0: // presence
							if(presence){ // only if resource has been created before
								boolean b = random.nextBoolean();
								if(b != presenceValue){
									presenceValue = b;
									client.updateResourceOnServer(CalipsoSmartParkingResource.PRESENCE_SERVICE, presenceValue ? "free" : "busy");
								}
							}
							break;
						case 1: // RPL Info
							if(rpl){ // only if resource has been created before
								client.updateResourceOnServer(CalipsoSmartParkingServer.RPL_INFO_SERVICE, new Integer(rplValue).toString());
							}
							break;

						case 2: // energy
							if(energy){ // only if resource has been created before
								energyValue -= (random.nextDouble() / 10);
								client.updateResourceOnServer(CalipsoSmartParkingServer.ENERGY_SERVICE, new Double(energyValue).toString());
							}
							break;
						default:
							break;
					}
					long wait = (random.nextInt(10) + 1) * 1000;
					Thread.sleep(wait);
				}
			}
			else{
				LOGGER.info("Client could not be registered");
			}
		} catch (InterruptedException e){
			e.printStackTrace();
		}

	}
}
