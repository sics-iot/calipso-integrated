package it.unipr.iot.calipso.tools.stream;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamWriter {
	
	private static final Logger logger = LoggerFactory.getLogger(StreamWriter.class);
	
	private PrintStream ps;
	private StreamWriterListener listener;
	
	public StreamWriter(OutputStream os){
		this.ps = new PrintStream(os);
	}

	public void setListener(StreamWriterListener listener){
		this.listener = listener;
	}
	
	public void writeLine(String line){
		if(this.ps != null){
			this.ps.println(line);
			logger.debug("WRITE: {}", line);
			if(this.listener != null) this.listener.onLineWritten(line);
		}
		else{
			logger.error("There is no output stream to write to");
		}
	}
	
}
