package it.unipr.iot.calipso.tools.ping.process;

import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.tools.ping.Ping4Client;
import it.unipr.iot.calipso.tools.ping.Ping6Client;
import it.unipr.iot.calipso.tools.ping.PingClient;
import it.unipr.iot.calipso.tools.ping.PingResults;
import it.unipr.iot.calipso.tools.ping.util.RandomNodePicker;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.inf.vs.californium.coap.CoAP;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

import com.google.gson.Gson;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class PingDelayMeter {

	private static final Logger logger = LoggerFactory.getLogger(PingDelayMeter.class);

	public static void main(String[] args) {

		int seconds = 20;
		if(args.length == 1){
			seconds = Integer.parseInt(args[0]);
		}

		logger.info("Starting Ping process - probing every {} seconds", seconds);

		while (true){

			PingDelayMeter.run();
			try{
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException e){
				logger.error(e.getMessage());
			}
		}

	}

	private static void publishOnServer(String uri, String data) throws InterruptedException {
		Request request = Request.newPut();
		request.setType(CoAP.Type.NON);
		request.setURI(uri + "/delay");
		request.setPayload(data);
		logger.info("SENT: " + request.toString());
		request.send();
		Response response = request.waitForResponse(); // request.waitForResponse(1000);
		logger.info("RECV: " + response.toString());
	}

	public static boolean run() {
		NodeInfo node = RandomNodePicker.pickRandomNode("http://localhost:50080/?resource=_nodeinfo&size=1");
		if(node != null){
			logger.info("Found node - {}", node.getId());
		}
		else{
			logger.info("No node found");
		}
		if(node != null && node.getId().equals("/parking/0") == false){
			logger.info("Pinging {} - {}", node.getId(), node.getIp());
			InetAddress address;
			try{
				address = InetAddress.getByName(node.getIp());
				logger.debug("{}", address);
				PingClient client = null;
				if(address.getAddress().length == 16){
					client = new Ping6Client();
				}
				else if(address.getAddress().length == 4){
					client = new Ping4Client();
				}
				if(client != null){
					PingResults result = client.ping(node.getIp(), 1);
					if(result != null){
						logger.debug("packets transmitted = {}", result.getPacketTX());
						logger.debug("packets received = {}", result.getPacketRX());
						logger.debug("packet loss = {}%", result.getPacketLoss());
						logger.debug("round-trip min = {} ms", result.getRttMin());
						logger.debug("round-trip avg = {} ms", result.getRttAvg());
						logger.debug("round-trip max = {} ms", result.getRttMax());
						logger.debug("round-trip stddev = {} ms", result.getRttStdDev());
						logger.info("Publishing delay on gateway {}", new Gson().toJson(result));
						publishOnServer(node.getId(), new Gson().toJson(result));
					}
					else{
						logger.info("Ping failed");
					}
					return true;
				}
			} catch (UnknownHostException e){
				logger.error("{}", e.getMessage());
			} catch (InterruptedException e){
				logger.error("{}", e.getMessage());
			}
			return false;
		}
		return false;
	}

}
