package it.unipr.iot.calipso.coap.server.util;

import com.google.gson.Gson;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class NodeManager {

	private static NodeManager instance = null;

	private Map<String, NodeInfo> map;

	private NodeManager() {
		this.map = new TreeMap<String, NodeInfo>();
	}

	public static NodeManager getInstance() {
		if(instance == null) instance = new NodeManager();
		return instance;
	}

	public void addNode(String node, NodeInfo info) {
		synchronized (this.map){
			this.map.put(node, info);
		}
	}

	public NodeInfo getNode(String node) {
		return this.map.get(node);
	}

	public void removeNode(String node) {
		synchronized (this.map){
			this.map.remove(node);
		}
	}

	public Set<String> getNodes() {
		synchronized (this.map){
			TreeSet<String> res = new TreeSet<>(this.map.keySet());
			return res;
		}
	}

	public java.util.Collection<NodeInfo> getNodeInfoList() {
		synchronized (this.map){
			TreeSet<NodeInfo> res = new TreeSet<>(this.map.values());
			return res;
		}
	}
	
	public String getNodesAsJson() {
		synchronized (this.map){
			return new Gson().toJson(this.map.keySet());
		}
	}

	public String getNodeInfoListAsJson() {
		synchronized (this.map){
			return new Gson().toJson(this.map.values());
		}
	}

}
