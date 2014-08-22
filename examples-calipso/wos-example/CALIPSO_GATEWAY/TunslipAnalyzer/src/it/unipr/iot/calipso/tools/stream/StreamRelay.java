package it.unipr.iot.calipso.tools.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamRelay implements StreamReaderListener, StreamWriterListener{

	private static final Logger logger = LoggerFactory.getLogger(StreamRelay.class);
	
	protected StreamReader reader;
	protected StreamWriter writer;
	
	public StreamRelay(InputStream is, OutputStream os){
		this(is, os, false);
	}
	
	public StreamRelay(InputStream is, OutputStream os,boolean delay){
		this.reader = new StreamReader(is, delay);
		this.writer = new StreamWriter(os);
		this.reader.setListener(this);
		this.writer.setListener(this);
	}
	
	public void relay(){
		this.reader.readLines();
	}
	
	public void onLineRead(StreamReader reader, String line) {
		logger.debug("READ: {}", line);
		if(this.writer != null){
			this.writer.writeLine(line);
		}
	}

	public void onReadError(StreamReader reader, IOException e) {
		logger.error("READ ERROR: {}", e.getMessage());
	}
	
	public void onLineWritten(String line) {
		logger.debug("WRITE: {}", line);
	}

	public void onWriteError(IOException e) {
		logger.error("WRITE ERROR: {}", e.getMessage());
	}

}
