package it.unipr.iot.calipso.coap.server.data;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class WorldSensingServerData {
	
	public static final String WOS_OCCUPATION_TYPE = "88";

	/*
	 * Sensor message to be posted :
	 * 
	 * example "882F280001CBFFFF"
	 * 
	 * struct byte: | 0 | 1 - 2 | 3 | 4 | 5 | 6 - 7 | field: | typeID | nodeID | seqno | occupation | eventNum | eventDelay |
	 * 
	 * typeID: message type identifier. Occupation message has typeID=0x88 
	 * nodeID: WS node identifier ( 2 bytes integer, little endian ) 
	 * seqno: sensor message sequence number 
	 * occupation: 0 - free spot / 1 - spot is taken 
	 * eventNum: occupation event sequence number 
	 * eventDelay: time since last occupation event in minutes or 0xFFFF if no previous timestamp (2 bytes integer, little endian)
	 */

	private String value;

	public WorldSensingServerData(String value) {
		this.value = value;
	}
	
	public WorldSensingServerData(String id, boolean occupation){
		this(WOS_OCCUPATION_TYPE, getIdFromString(id), "01", getOccupationAsString(occupation), "CB", "FFFF");
	}
	
	private WorldSensingServerData(String typeID, String nodeID, String seqno, String occupation, String eventNum, String eventDelay){
		this.value = new StringBuilder()
							.append(typeID)
							.append(nodeID)
							.append(seqno)
							.append(occupation)
							.append(eventNum)
							.append(eventDelay)
							.toString();
	}

	public String getValue() {
		return this.value;
	}
	
	public static String getIdFromString(String id){
		Integer i = Integer.parseInt(id);
		String s = Integer.toHexString(i).toUpperCase();
		return s;
	}

	public boolean isValidData() {
		for(char c : this.value.toCharArray()){
			if(-1 == Character.digit(c, 16)) return false;
		}
		return true;
	}

	public boolean isOccupationPacket() {
		return this.value.startsWith(WOS_OCCUPATION_TYPE);
	}

	public boolean getOccupation() {
		return this.value.substring(8, 10).equals("01");
	}
	
	private static String getOccupationAsString(boolean occupation){
		return (occupation == true) ? "00" : "01";
	}

	public static void main(String[] args){
		WorldSensingServerData data = new WorldSensingServerData("12680", true);
		System.out.println(data.value);
	}
	
}
