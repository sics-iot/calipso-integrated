package it.unipr.iot.calipso.coap.server;

import it.unipr.iot.calipso.coap.server.data.PDRData;

public interface PDRListener {
	
	public void onPDRData(String resourceURI, PDRData data);

}
