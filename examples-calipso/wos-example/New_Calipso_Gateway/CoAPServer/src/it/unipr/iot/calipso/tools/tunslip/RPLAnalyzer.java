package it.unipr.iot.calipso.tools.tunslip;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPLAnalyzer implements BorderRouterTunslipAnalyzerListener {
	
	public static final String CTIME_PREFIX = "CTIME";
	
	private static final Logger logger = LoggerFactory.getLogger(RPLAnalyzer.class); 

	private Map<String,Long> cTimeEventForNode;
	
	public RPLAnalyzer(){
		this.cTimeEventForNode = new HashMap<String,Long>();
	}
	
	public void onAnalysisStarted(BorderRouterTunslipAnalyzer analyzer) {
		logger.info("Ready for tunslip log analysis");
	}

	public void onAnalysisTerminated(BorderRouterTunslipAnalyzer analyzer) {
		logger.info("Tunslip log analysis terminated");
	}

	public void onTunslipEvent(BorderRouterTunslipAnalyzer analyzer, Long timestamp, String line) {
		if(line.startsWith(CTIME_PREFIX)){
			Scanner scanner = new Scanner(line);
			scanner.next();
			String node = scanner.next();
			//String parent = scanner.next();
			logger.info("{} CTIME = {}", node, timestamp);
			synchronized(this.cTimeEventForNode){
				if(this.cTimeEventForNode.get(node) == null){
					this.cTimeEventForNode.put(node, timestamp);
				}
			}
		}
	}
	
	public Long getTimestampForNode(String nodeIpAddress){
		synchronized(this.cTimeEventForNode){
			return this.cTimeEventForNode.get(nodeIpAddress);
		}
	}

}
