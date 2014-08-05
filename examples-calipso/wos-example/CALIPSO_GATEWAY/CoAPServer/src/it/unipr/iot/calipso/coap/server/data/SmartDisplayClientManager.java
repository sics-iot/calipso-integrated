package it.unipr.iot.calipso.coap.server.data;

import it.unipr.iot.calipso.smartdisplay.CalipsoSmartDisplayClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SmartDisplayClientManager {

	protected final static Logger logger = LoggerFactory.getLogger(SmartDisplayClientManager.class);

	private static SmartDisplayClientManager instance;

	private CalipsoSmartDisplayClient client;

	private SmartDisplayClientManager() {
		this.client = new CalipsoSmartDisplayClient();
	}

	public static SmartDisplayClientManager getInstance() {
		if(instance == null) instance = new SmartDisplayClientManager();
		return instance;
	}

	public void setFrom(String from) {
		this.client.setFrom(from);
	}

	public void setProxy(String proxy) {
		this.client.setProxy(proxy);
	}

	public void connect(String displayId, String app) {
		this.client.connect(displayId, app);
	}

	public void updateResource(String key, String value) {
		logger.debug("Updating resource on Smart Display: {} -> {}", key, value);
		this.client.updateResource(key, value);
	}

	public void updateTree(String node, String parent, double lqi) {
		logger.debug("Updating RPL tree on Smart Display: {} -> {} [lqi = {}]", node, parent, lqi);
		this.client.updateTree(node, parent, lqi);
	}

}
