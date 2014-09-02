package it.unipr.iot.calipso.coap.server.util;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class ResourceManager {

	private static ResourceManager instance = null;

	private Map<String, String> map;

	private ResourceManager() {
		this.map = new TreeMap<String, String>();
	}

	public static ResourceManager getInstance() {
		if(instance == null) instance = new ResourceManager();
		return instance;
	}

	public void addResource(String resource) {
		synchronized (this.map){
			this.map.put(resource, resource);
		}
	}

	public void removeResource(String resource) {
		synchronized (this.map){
			this.map.remove(resource);
		}
	}

	public Set<String> getResources() {
		synchronized (this.map){
			return this.map.keySet();
		}
	}

}
