package it.unipr.iot.calipso.tools.ping.util;

import it.unipr.iot.calipso.coap.server.util.NodeInfo;
import it.unipr.iot.calipso.database.time.TimestampedResource;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class RandomNodePicker {

	private static final Logger logger = LoggerFactory.getLogger(RandomNodePicker.class);

	public static NodeInfo pickRandomNode(String gateway) {
		NodeListRequest request = new NodeListRequest(gateway);
		String response = request.execute();
		if(response == null) return null;
		response = response.trim();
		if(response.length() > 0 && response.startsWith("[") && response.endsWith("]")){
			response = response.substring(1, response.length() - 1);
		}
		TimestampedResource resource = new Gson().fromJson(response, TimestampedResource.class);
		TypeToken<ArrayList<NodeInfo>> typeToken = new TypeToken<ArrayList<NodeInfo>>() {
		};
		if(resource != null){
			ArrayList<NodeInfo> nodes = new Gson().fromJson(resource.getValue(), typeToken.getType());
			for(NodeInfo node : nodes){
				logger.debug("{} - {}", node.getId(), node.getIp());
			}
			java.util.Random random = new java.util.Random(System.currentTimeMillis());
			if(nodes.size() > 0){
				NodeInfo node = nodes.get(random.nextInt(nodes.size()));
				logger.info("Selected {}", node.getId());
				return node;
			}
		}
		return null;
	}
}
