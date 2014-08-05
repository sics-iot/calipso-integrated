package it.unipr.iot.calipso.tools;

import java.io.*;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamReader {
	
	private static final Logger logger = LoggerFactory.getLogger(StreamReader.class);
	
	public static final boolean WITH_DELAY = true;
	public static final boolean WITHOUT_DELAY = false;
	
	protected InputStream is;
	protected Boolean isReading;
	protected StreamReaderListener listener;
	private boolean delay;
	private Random random;
	
	public StreamReader(InputStream is){
		this(is,false);
	}
	
	public StreamReader(InputStream is, boolean delay){
		this.is = is;
		this.isReading = false;
		this.delay = delay;
		this.random = new Random();
	}

	public void setListener(StreamReaderListener listener){
		this.listener = listener;
	}
	
	public void readLines(){
		synchronized(this.isReading){
			this.isReading = true;
		}
		if(this.is != null){
			BufferedReader br = new BufferedReader(new InputStreamReader(this.is));
			String line;
		    try {
				while(this.isReading == true && (line = br.readLine()) != null) {
				    logger.debug("READ: {}", line);
				    if(this.listener != null){
				    	this.listener.onLineRead(this, line);
				    }
				    if(this.delay){
				    	long pause = (long)(this.random.nextDouble() * 1000);
				    	logger.debug("wait for {} ms", pause);
				    	pause(pause);
				    }
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
				if(this.listener != null) this.listener.onReadError(this, e);
			}
		}
		else{
			logger.error("There is no input stream to read from");
		}
	}
	
	public void stopReading(){
		synchronized(this.isReading){
			this.isReading = false;
		}
	}
	
	private static final void pause(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.error("ERROR: {}", e.getMessage());
		}
	}

}
