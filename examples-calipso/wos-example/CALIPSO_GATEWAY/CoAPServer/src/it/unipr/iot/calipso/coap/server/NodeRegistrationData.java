package it.unipr.iot.calipso.coap.server.data;

public class NodeRegistrationData {
	
	private String id;
	private String treeId;
	
	public NodeRegistrationData(){}
	
	public NodeRegistrationData(String id, String treeId){
		this.id = id;
		this.treeId = treeId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTreeId() {
		return treeId;
	}

	public void setTreeId(String treeId) {
		this.treeId = treeId;
	}

}
