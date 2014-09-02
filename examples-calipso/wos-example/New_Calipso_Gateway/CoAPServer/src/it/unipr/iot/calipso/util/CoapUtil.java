package it.unipr.iot.calipso.util;

import it.unipr.iot.calipso.coap.server.data.NodeRegistrationData;

import java.util.List;

import com.google.gson.Gson;

import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class CoapUtil {
	
	public static String getSender(CoapExchange exchange){
		List<String> parts = exchange.getRequestOptions().getURIPaths();
		if(exchange.getRequestOptions().getURIPathCount() > 1){
			String sender = "/" + parts.get(0) + "/" + parts.get(1);
			return sender;
		}
		else if(exchange.getRequestText() != null){
			//String sender = "/" + parts.get(0) + "/" + exchange.getRequestText();
			NodeRegistrationData data = new Gson().fromJson(exchange.getRequestText(), NodeRegistrationData.class);
			if(data != null){
				return "/" + parts.get(0) + "/" + data.getId();
			}
			else {
				String sender = "/" + parts.get(0);
				return sender;
			}
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
