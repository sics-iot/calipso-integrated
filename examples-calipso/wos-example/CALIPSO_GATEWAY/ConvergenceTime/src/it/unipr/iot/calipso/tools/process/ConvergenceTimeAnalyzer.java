package it.unipr.iot.calipso.tools.process;

import it.unipr.iot.calipso.tools.StreamReader;
import it.unipr.iot.calipso.tools.StreamReaderListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvergenceTimeAnalyzer implements StreamReaderListener{
	
	public static final String T0_PREFIX = "Server IPv6 addresses:";
	public static final String CTIME_PREFIX = "CTIME";
	public static final String END_PREFIX = "ifconfig tun0 down";
	
	private static final Logger logger = LoggerFactory.getLogger(ConvergenceTimeAnalyzer.class); 
	
	private StreamReader reader;
	
	private Long t0;
	private Map<String, Long> cTime;
	private ConvergenceTimeAnalyzerListener listener;
	
	public ConvergenceTimeAnalyzer(InputStream is) throws IOException{
		cTime = new HashMap<String,Long>();
		this.reader = new StreamReader(is);
		this.reader.setListener(this);
	}
	
	public void setListener(ConvergenceTimeAnalyzerListener listener){
		this.listener = listener;
	}
	
	public void analyze(){
		if(this.listener != null) this.listener.onAnalysisStarted(this);
		reader.readLines();
	}
	
	public void onLineRead(StreamReader reader, String line) {
		logger.debug("READ: {}", line);
		if(line.startsWith(T0_PREFIX)){
			this.t0 = System.currentTimeMillis();
			logger.debug("t_0 = {}", this.t0);
		}
		else if(line.startsWith(CTIME_PREFIX)){
			Scanner scanner = new Scanner(line);
			scanner.next();
			String node = scanner.next();
			String parent = scanner.next();
			logger.debug("'{}' -> '{}'", node, parent);
			if(this.cTime.get(node) == null){
				Long now = System.currentTimeMillis();
				Long delta = now - this.t0;
				logger.debug("CTIME({}) = {} ms", node, delta);
				this.cTime.put(node, delta);
			}
			else{
				logger.debug("Discard! Node {} has converged already", node);
			}
		}
		else if(line.startsWith(END_PREFIX)){
			reader.stopReading();
			if(this.listener != null) this.listener.onAnalysisTerminated(this);
		}
	}

	public void onReadError(StreamReader reader, IOException e) {
		logger.error("ERROR: {}", e.getMessage());
	}
	
	public void printStatistics(){
		logger.info("Convergence Time [ms]");
		synchronized(this.cTime){
			logger.debug("Node\tCTIME");
			System.out.println("Node\tCTIME");
			for(String node : this.cTime.keySet()){
				logger.debug("{}\t{}",node, this.cTime.get(node));
				System.out.println(node + "\t" + this.cTime.get(node));
			}
		}
	}

}
