package it.unipr.iot.calipso.tools.stream;

import java.io.IOException;

public interface StreamReaderListener {
	
	public void onLineRead(StreamReader reader, String line);
	public void onReadError(StreamReader reader, IOException e);

}
