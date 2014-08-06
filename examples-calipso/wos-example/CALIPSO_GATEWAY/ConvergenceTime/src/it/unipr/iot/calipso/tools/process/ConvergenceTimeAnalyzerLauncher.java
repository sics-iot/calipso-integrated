package it.unipr.iot.calipso.tools.process;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvergenceTimeAnalyzerLauncher {
	
	private static final Logger logger = LoggerFactory.getLogger(ConvergenceTimeAnalyzerLauncher.class);

	public static void main(String[] args) throws IOException {
		String logFile = "/home/paolomeda/calipso-integrated/examples-calipso/wos-example/CALIPSO_GATEWAY/ConvergenceTime/input/log.dat.RELAYED";
		File f = new File(logFile);
		if(args.length > 0){
			logFile = args[0];
			f = new File(logFile);
			if(f.exists() == false){
				logger.error("The specified file ({}) does not exist...", f.getAbsolutePath());
				System.exit(1);
			}
		}
		logger.info("ConvergenceTimeAnalyzer started");
		logger.info("Reading from file {} ({})", logFile, f.getAbsolutePath());
		Runtime r = Runtime.getRuntime();
		final Process p = r.exec("tail -f " + logFile);
		ConvergenceTimeAnalyzer analyzer = new ConvergenceTimeAnalyzer(p.getInputStream());
		analyzer.setListener(new ConvergenceTimeAnalyzerListener(){
			
			public void onAnalysisTerminated(ConvergenceTimeAnalyzer analyzer) {
				logger.info("Stopped analyzing...");
				analyzer.printStatistics();
				p.destroy();
			}
			
			public void onAnalysisStarted(ConvergenceTimeAnalyzer analyzer) {
				logger.info("Started analyzing...");
			}
		});
		analyzer.analyze();
		logger.info("ConvergenceTimeAnalyzer exited");
	}

}
