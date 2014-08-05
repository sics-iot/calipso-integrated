package it.unipr.iot.calipso.util;

import java.util.List;

import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class CoapUtil {
	
	public static String getSender(CoapExchange exchange){
		if(exchange.getRequestOptions().getURIPathCount() > 1){
			List<String> parts = exchange.getRequestOptions().getURIPaths();
			String sender = "/" + parts.get(0) + "/" + parts.get(1);
			return sender;
		}
		else return null;
	}
	
	public static String getResource(CoapExchange exchange){
		if(exchange.getRequestOptions().getURIPathCount() == 3){
			List<String> parts = exchange.getRequestOptions().getURIPaths();
			return parts.get(2);
		}
		else return null;
	}

}
