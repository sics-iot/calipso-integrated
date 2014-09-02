package it.unipr.iot.calipso.coap.server.util;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class MessageCountManager {

	private static final Logger logger = LoggerFactory.getLogger(MessageCountManager.class);

	private static MessageCountManager instance = null;

	private Map<String, Integer> map;

	private MessageCountManager() {
		this.map = new TreeMap<String, Integer>();
	}

	public static MessageCountManager getInstance() {
		if(instance == null) instance = new MessageCountManager();
		return instance;
	}

	public int incrementMessageCountForNode(String node) {
		synchronized (this.map){
			if(this.getMessageCountForNode(node) == null) this.map.put(node, new Integer(0));
			this.map.put(node, new Integer(this.map.get(node) + 1));
			logger.debug("{} -> {} messages RX", node, this.getMessageCountForNode(node));
			return this.map.get(node);
		}
	}

	public Integer getMessageCountForNode(String node) {
		synchronized (this.map){
			return this.map.get(node);
		}
	}

	public void removeNode(String node) {
		synchronized (this.map){
			this.map.remove(node);
		}
	}

}
