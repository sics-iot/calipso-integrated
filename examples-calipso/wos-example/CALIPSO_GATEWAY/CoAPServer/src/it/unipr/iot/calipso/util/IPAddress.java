package it.unipr.iot.calipso.util;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class IPAddress {

	public static String getIpAddress(byte[] rawBytes) throws Exception {
		if(rawBytes.length != 4 && rawBytes.length != 16){
			throw new Exception("Invalid IP address (" + rawBytes.length + " bytes)");
		}
		else{
			String ipAddress = "";
			int i = 0;
			for(byte raw : rawBytes){
				i++;
				ipAddress += (raw & 0xFF);
				String delim = "";
				if(rawBytes.length == 4) delim = ".";
				else if(rawBytes.length == 16) delim = ":";
				if(i < rawBytes.length){
					ipAddress += delim;
				}
			}
			return ipAddress;
		}
	}

}
