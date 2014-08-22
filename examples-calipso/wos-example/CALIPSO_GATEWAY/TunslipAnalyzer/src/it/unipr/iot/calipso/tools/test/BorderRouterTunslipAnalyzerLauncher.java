package it.unipr.iot.calipso.tools.test;

import it.unipr.iot.calipso.tools.tunslip.BorderRouterTunslipAnalyzer;
import it.unipr.iot.calipso.tools.tunslip.BorderRouterTunslipAnalyzerListener;
import it.unipr.iot.calipso.tools.tunslip.ConvergenceTimeAnalyzer;
import it.unipr.iot.calipso.tools.tunslip.HopCountAnalyzer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BorderRouterTunslipAnalyzerLauncher {
	
	private static final Logger logger = LoggerFactory.getLogger(BorderRouterTunslipAnalyzerLauncher.class);

	public static void main(String[] args) throws IOException {
		String logFile = "input/log.dat.RELAYED";
		if(args.length > 0){
			logFile = args[0];
		}
		
		BorderRouterTunslipAnalyzer analyzer = BorderRouterTunslipAnalyzer.createAnalyzerWithFile(logFile, new BorderRouterTunslipAnalyzerListener(){
			
			public void onAnalysisStarted(BorderRouterTunslipAnalyzer analyzer) {
				logger.info("Started analyzing...");
			}
			
			public void onAnalysisTerminated(BorderRouterTunslipAnalyzer analyzer) {
				logger.info("Stopped analyzing...");
			}
			
			public void onTunslipEvent(BorderRouterTunslipAnalyzer analyzer, Long timestamp, String line){}
			
		});
		
		ConvergenceTimeAnalyzer cTimeAnalyzer = new ConvergenceTimeAnalyzer();
		analyzer.addListener(ConvergenceTimeAnalyzer.START_PREFIX, cTimeAnalyzer);
		analyzer.addListener(ConvergenceTimeAnalyzer.CTIME_PREFIX, cTimeAnalyzer);
		HopCountAnalyzer hopCountAnalyzer = new HopCountAnalyzer();
		analyzer.addListener(HopCountAnalyzer.HOPCOUNT_PREFIX, hopCountAnalyzer);
		analyzer.analyze();
		logger.info("BorderRouterTunslipAnalyzer exited");
		
		
	}
	
	
	
}
