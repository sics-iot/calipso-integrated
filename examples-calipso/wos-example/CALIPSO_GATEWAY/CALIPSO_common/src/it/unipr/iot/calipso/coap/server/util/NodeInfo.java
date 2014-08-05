package it.unipr.iot.calipso.coap.server.util;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class NodeInfo {

	private String id;
	private String treeId;
	private Integer port;
	private String ip;
	private Long arrivedAt;
	private Long parentReadyTime;

	public NodeInfo(String id, String treeId, String ip, Integer port, Long arrivedAt, Long parentReadyTime) {
		this.id = id;
		this.treeId = treeId;
		this.ip = ip;
		this.port = port;
		this.arrivedAt = arrivedAt;
		this.parentReadyTime = parentReadyTime;
	}

	public String getId() {
		return this.id;
	}
	
	public String getTreeId(){
		return this.treeId;
	}

	public String getIp() {
		return this.ip;
	}

	public Integer getPort() {
		return this.port;
	}

	public Long getArrivedAt() {
		return this.arrivedAt;
	}

	public Long getParentReadyTime() {
		return this.parentReadyTime;
	}

	public void setParentReadyTime(Long parentReadyTime) {
		this.parentReadyTime = parentReadyTime;
	}

	public String toString() {
		return this.id + " (" + this.treeId + ") - [" + this.ip + ":" + this.getPort() + "] - joined at " + this.arrivedAt + " - parent set at " + this.parentReadyTime;
	}

}
