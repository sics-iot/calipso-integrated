package it.unipr.iot.calipso.tools.test;


import it.unipr.iot.calipso.tools.StreamReader;
import it.unipr.iot.calipso.tools.StreamRelay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvergenceTimeDataSource {
	
	public static final String T0_PREFIX = "Server IPv6 addresses:";
	public static final String CTIME_PREFIX = "CTIME";
	
	private static final Logger logger = LoggerFactory.getLogger(ConvergenceTimeDataSource.class); 
	
	private StreamRelay delayedStreamRelay;
	
	public ConvergenceTimeDataSource(InputStream fis,String outFile) throws IOException{
		File output = new File(outFile);
		if(output.exists() == false){
			output.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(output);
		this.delayedStreamRelay = new StreamRelay(fis,fos,StreamReader.WITH_DELAY);
	}
	
	public void run(){
		logger.info("Starting relaying data...");
		this.delayedStreamRelay.relay();
		logger.info("Finished relaying data...");
	}
	
	public static void main(String[] args) throws IOException {
		String inFile = "input/log2.dat";
		String outFile = "input/log.dat.RELAYED";
		FileInputStream fis = new FileInputStream(inFile);
		ConvergenceTimeDataSource analyzer = new ConvergenceTimeDataSource(fis,outFile);
		System.out.println("type to start...");
		waitForConsole();
		analyzer.run();
	}
	
	private static void waitForConsole() {
		try{
			System.in.read();
		} catch (IOException e){}
	}

}
