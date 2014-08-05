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
			if(rawBytes.length == 4){
				return getIPv4Address(rawBytes);
			}
			else if(rawBytes.length == 16){
				return getIPv6Address(rawBytes);
			}
			else return null;
		}
	}
	
	private static String getIPv4Address(byte[] bytes){
		String ipAddress = "";
		for(byte raw : bytes){
			ipAddress += Integer.toString(raw & 0xFF);
			ipAddress += ".";
		}
		ipAddress = ipAddress.substring(0, ipAddress.length() - 1);
		return ipAddress;
	}
	
	private static String getIPv6Address(byte[] bytes){
		String ipAddress = "";
		for(int i = 0; i < bytes.length; ){
			byte a = bytes[i++];
			byte b = bytes[i++];
			String sa = Integer.toHexString(a & 0xFF);
			if(sa.length() == 1) sa = "0" + sa;
			String sb = Integer.toHexString(b & 0xFF);
			if(sb.length() == 1) sb = "0" + sb;
			ipAddress += (sa+sb);
			ipAddress += ":";
		}
		ipAddress = ipAddress.substring(0, ipAddress.length() - 1);
		return ipAddress;
	}
	

	public static void main(String[] args) throws Exception{
		byte[] bytes = new byte[]{(byte) 170,(byte) 170,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 2,(byte) 18,(byte) 116,(byte) 2,(byte) 0,(byte) 2,(byte) 2,(byte) 2};
		System.out.println(getIPv6Address(bytes));
		bytes = new byte[]{(byte) 192, (byte) 168, (byte) 0, (byte) 1};
		System.out.println(getIPv4Address(bytes));
	}
	
}
