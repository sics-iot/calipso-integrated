package it.unipr.iot.calipso.tools.tunslip;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HopCountAnalyzer implements BorderRouterTunslipAnalyzerListener {
	
	public static final String HOPCOUNT_PREFIX = "HOPCOUNT";
	
	private static final Logger logger = LoggerFactory.getLogger(HopCountAnalyzer.class);
	
	private Map<String, HopCountData> hopCountForNode;
	
	public HopCountAnalyzer(){
		this.hopCountForNode = new HashMap<String,HopCountData>();
	}

	public void onAnalysisStarted(BorderRouterTunslipAnalyzer analyzer) {
		logger.info("{} is ready for tunslip log analysis", this.getClass().getCanonicalName());
	}

	public void onAnalysisTerminated(BorderRouterTunslipAnalyzer analyzer) {
		logger.info("{} tunslip log analysis terminated", this.getClass().getCanonicalName());
		this.printStatistics();
	}

	public void onTunslipEvent(BorderRouterTunslipAnalyzer analyzer, Long timestamp, String line) {
		if(line.startsWith(HOPCOUNT_PREFIX)){
			Scanner scanner = new Scanner(line);
			scanner.next();
			String node = scanner.next();
			int count = scanner.nextInt();
			logger.info("HOPCOUNT({}) = {}", node, count);
			logger.debug("{} -> {}", node, count);
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

	}
	
	public String getStatistics(){
		StringBuilder sb = new StringBuilder();
		long sum = 0;
		long count = 0;
		synchronized(this.hopCountForNode){
			sb.append("Average Hop Count [adimensional]" + "\n");
			for(String node : this.hopCountForNode.keySet()){
				HopCountData data = this.hopCountForNode.get(node);
				sb.append(node + " = " + data.getAverage() + "\n");	
				sum += data.getSum();
				count += data.getCount();
			}
		}
		HopCountData data = new HopCountData(sum,count);
		sb.append("Overall Hop Count [adimensional] = " + data.getAverage() + "\n");
		return new String(sb);
	}
	
	public void printStatistics(){
		String stats = this.getStatistics();
		logger.info(stats);
		System.out.println(stats);
	}
	
	/* -------------------------------------------------------------------- */
	
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

}
