package it.unipr.iot.calipso.coap.server.util;

public class TimeUtil {
	
	public static void pause(long millis) {
		try{
			Thread.sleep(millis);
		} catch (InterruptedException e){}
	}

}
