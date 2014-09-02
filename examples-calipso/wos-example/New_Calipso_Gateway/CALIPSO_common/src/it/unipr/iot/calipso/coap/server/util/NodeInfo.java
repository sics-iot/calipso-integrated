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
	private String parentId;
	private Integer port;
	private String ip;
	private Long arrivedAt;
	private Long parentReadyTime;

	public NodeInfo(String id, String treeId, String ip, Integer port, Long arrivedAt, String parentId, Long parentReadyTime) {
		this.id = id;
		this.treeId = treeId;
		this.ip = ip;
		this.port = port;
		this.arrivedAt = arrivedAt;
		this.parentId = parentId;
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
	
	public String getParentId() {
		return this.parentId;
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
	
	public void setId(String id) {
		this.id = id;
	}

	public void setTreeId(String treeId) {
		this.treeId = treeId;
	}
	
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setArrivedAt(Long arrivedAt) {
		this.arrivedAt = arrivedAt;
	}

	public void setParentReadyTime(Long parentReadyTime) {
		this.parentReadyTime = parentReadyTime;
	}

	public String toString() {
		return this.id + " (" + this.treeId + ") - [" + this.ip + ":" + this.getPort() + "] - joined at " + this.arrivedAt + " - parent set at " + this.parentReadyTime;
	}

}
