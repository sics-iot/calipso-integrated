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
	public static final String HOPCOUNT_PREFIX = "HOPCOUNT";
	public static final String END_PREFIX = "ifconfig tun0 down";
	
	private static final Logger logger = LoggerFactory.getLogger(ConvergenceTimeAnalyzer.class); 
	
	public class HopCountData {
		
		private long sum;
		private long count;
		
		public HopCountData(){
			this(0,0);
		}
		
		public HopCountData(long sum, long count) {
			this.sum = sum;
			this.count = count;
		}

		public long getSum() {
			return sum;
		}

		public void setSum(long sum) {
			this.sum = sum;
		}

		public long getCount() {
			return count;
		}

		public void setCount(long count) {
			this.count = count;
		}
		
		public double getAverage(){
			return ((double)this.sum / (double)this.count);
		}
		
	}
	
	private StreamReader reader;
	private Long t0;
	private Map<String, Long> cTime;
	private ConvergenceTimeAnalyzerListener listener;
	private Map<String, HopCountData> hopCountForNode;
	
	public ConvergenceTimeAnalyzer(InputStream is) throws IOException{
		this.cTime = new HashMap<String,Long>();
		this.hopCountForNode = new HashMap<String,HopCountData>();
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
			synchronized(this.cTime){
				if(this.cTime.get(node) == null){
					Long now = System.currentTimeMillis();
					Long delta = now - this.t0;
					logger.info("CTIME({}) = {} ms", node, delta);
					this.cTime.put(node, delta);
				}
				else{
					logger.debug("Discard! Node {} has converged already", node);
				}
			}
		}
		else if(line.startsWith(HOPCOUNT_PREFIX)){
			Scanner scanner = new Scanner(line);
			scanner.next();
			String node = scanner.next();
			int count = scanner.nextInt();
			logger.info("HOPCOUNT({}) = {}", node, count);
			logger.info("{} -> {}", node, count);
			synchronized(this.hopCountForNode){
				if(this.hopCountForNode.get(node) == null){
					HopCountData data = new HopCountData(0,0);
					this.hopCountForNode.put(node, data);
				}
				HopCountData data = this.hopCountForNode.get(node);
				data.setSum(data.getSum() + count);
				data.setCount(data.getCount() + 1);
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
		long sum = 0;
		long count = 0;
		synchronized(this.hopCountForNode){
			logger.info("Average Hop Count [adimensional]");
			System.out.println("Average Hop Count [adimensional]");
			for(String node : this.hopCountForNode.keySet()){
				HopCountData data = this.hopCountForNode.get(node);
				logger.info("{} = {}", node, data.getAverage());
				System.out.println(node + " = " + data.getAverage());	
				sum += data.getSum();
				count += data.getCount();
			}
		}
		HopCountData data = new HopCountData(sum,count);
		logger.info("Overall Hop Count [adimensional] = {}", data.getAverage());
		System.out.println("Overall Hop Count [adimensional] = " + data.getAverage());	
	}

}
