package it.unipr.iot.calipso.tools.tunslip;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvergenceTimeAnalyzer implements BorderRouterTunslipAnalyzerListener {
	
	public static final String START_PREFIX = "Server IPv6 addresses:";
	public static final String CTIME_PREFIX = "CTIME";
	
	private static final Logger logger = LoggerFactory.getLogger(ConvergenceTimeAnalyzer.class); 

	private Long t0;
	private Map<String, Long> cTime;
	
	public ConvergenceTimeAnalyzer(){
		this.cTime = new HashMap<String,Long>();
	}
	
	public void onAnalysisStarted(BorderRouterTunslipAnalyzer analyzer) {
		logger.info("Ready for tunslip log analysis");
	}

	public void onAnalysisTerminated(BorderRouterTunslipAnalyzer analyzer) {
		logger.info("Tunslip log analysis terminated");
		this.printStatistics();
	}

	public void onTunslipEvent(BorderRouterTunslipAnalyzer analyzer, Long timestamp, String line) {
		Long now = System.currentTimeMillis();
		if(line.startsWith(START_PREFIX)){
			this.t0 = now;
			logger.debug("t_0 = {}", this.t0);
		}
		else if(line.startsWith(CTIME_PREFIX)){
			Scanner scanner = new Scanner(line);
			scanner.next();
			String node = scanner.next();
			String parent = scanner.next();
			logger.debug("'{}' -> '{}'", node, parent);
			synchronized(this.cTime){
				if(this.t0 == null){
					logger.error("Invalid t0 has not been set yet!");
				}
				else{
					if(this.cTime.get(node) == null){
						Long delta = now - this.t0;
						logger.info("CTIME({}) = {} ms", node, delta);
						this.cTime.put(node, delta);
					}
					else{
						logger.debug("Discard! Node {} has converged already", node);
					}
				}
			}
		}
	}
	
	public String getStatistics(){
		StringBuilder sb = new StringBuilder();
		sb.append("Convergence Time [ms]" + "\n");
		synchronized(this.cTime){
			sb.append("Node\tCTIME" + "\n");
			for(String node : this.cTime.keySet()){
				sb.append(node + "\t" + this.cTime.get(node) + "\n");
			}
		}
		return new String(sb);
	}
	
	public void printStatistics(){
		String stats = this.getStatistics();
		logger.info(stats);
		System.out.println(stats);	
	}
	
}
