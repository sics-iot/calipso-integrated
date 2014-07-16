package it.unipr.iot.calipso.http.client.dto;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class WorldSensingServerData {

	/*
	 * Sensor message to be posted :
	 * 
	 * example "882F280001CBFFFF"
	 * 
	 * struct byte: | 0 | 1 - 2 | 3 | 4 | 5 | 6 - 7 | field: | typeID | nodeID | seqno | occupation | eventNum | eventDelay |
	 * 
	 * typeID: message type identifier. Occupation message has typeID=0x88 nodeID: WS node identifier ( 2 bytes integer, little endian ) seqno: sensor message sequence number occupation: 0 - free spot
	 * / 1 - spot is taken eventNum: occupation event sequence number eventDelay: time since last occupation event in minutes or 0xFFFF if no previous timestamp (2 bytes integer, little endian)
	 */

	private String data;

	public WorldSensingServerData(String data) {
		this.data = data;
	}

	public String getData() {
		return this.data;
	}

	public boolean isValidData() {
		for(char c : this.data.toCharArray()){
			if(-1 == Character.digit(c, 16)) return false;
		}
		return true;
	}

	public boolean isOccupationPacket() {
		return this.data.startsWith("88");
	}

	public boolean getOccupation() {
		return this.data.substring(8, 10).equals("01");
	}

}
