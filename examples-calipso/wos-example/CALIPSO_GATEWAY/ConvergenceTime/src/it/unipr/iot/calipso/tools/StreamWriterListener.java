package it.unipr.iot.calipso.tools;

import java.io.IOException;

public interface StreamWriterListener {
	
	public void onLineWritten(String line);
	public void onWriteError(IOException e);

}
