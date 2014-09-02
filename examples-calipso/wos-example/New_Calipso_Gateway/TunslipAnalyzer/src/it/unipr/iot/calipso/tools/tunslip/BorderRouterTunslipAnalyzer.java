package it.unipr.iot.calipso.tools.tunslip;

import it.unipr.iot.calipso.tools.stream.StreamReader;
import it.unipr.iot.calipso.tools.stream.StreamReaderListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BorderRouterTunslipAnalyzer implements StreamReaderListener{
	
	public static final String END_PREFIX = "ifconfig tun0 down";
	
	private static final Logger logger = LoggerFactory.getLogger(BorderRouterTunslipAnalyzer.class); 
	
	private StreamReader reader;
	private TunslipListener listener;
	private Map<String,List<BorderRouterTunslipAnalyzerListener>> listeners;
	
	public BorderRouterTunslipAnalyzer(InputStream is) throws IOException{
		this.reader = new StreamReader(is);
		this.reader.setListener(this);
		this.listeners = new HashMap<String,List<BorderRouterTunslipAnalyzerListener>>();
	}
	
	public void setTunslipListener(TunslipListener listener){
		this.listener = listener;
	}
	
	public void addListener(String linePrefix, BorderRouterTunslipAnalyzerListener listener){
		synchronized(this.listeners){
			if(this.listeners.get(linePrefix) == null){
				this.listeners.put(linePrefix, new ArrayList<BorderRouterTunslipAnalyzerListener>());
			}
			this.listeners.get(linePrefix).add(listener);
		}
	}
	
	public Set<BorderRouterTunslipAnalyzerListener> getListenersForLine(String line){
		Set<BorderRouterTunslipAnalyzerListener> set = new HashSet<BorderRouterTunslipAnalyzerListener>();
		for(String prefix : this.listeners.keySet()){
			if(line.startsWith(prefix)){
				set.addAll(this.listeners.get(prefix));
			}
		}
		return set;
	}
	
	public void analyze(){
		if(this.listener != null) this.listener.onTunslipStarted(this);
		for(BorderRouterTunslipAnalyzerListener l : this.getAllListeners()){
			l.onAnalysisStarted(this);
		}
		reader.readLines();
	}
	
	private Set<BorderRouterTunslipAnalyzerListener> getAllListeners(){
		Set<BorderRouterTunslipAnalyzerListener> set = new HashSet<BorderRouterTunslipAnalyzerListener>();
		for(String prefix : this.listeners.keySet()){
			set.addAll(this.listeners.get(prefix));
		}
		return set;
	}
	
	public void onLineRead(StreamReader reader, String line) {
		logger.debug("READ: {}", line);
		Long now = System.currentTimeMillis();
		Set<BorderRouterTunslipAnalyzerListener> set = this.getListenersForLine(line);
		for(BorderRouterTunslipAnalyzerListener l : set){
			l.onTunslipEvent(this, now, line);
		}
		if(line.startsWith(END_PREFIX)){
			reader.stopReading();
			if(this.listener != null) this.listener.onTunslipTerminated(this);
			for(BorderRouterTunslipAnalyzerListener l : this.getAllListeners()){
				l.onAnalysisTerminated(this);
			}
		}
	}

	public void onReadError(StreamReader reader, IOException e) {
		logger.error("ERROR: {}", e.getMessage());
	}
	
	public static BorderRouterTunslipAnalyzer createAnalyzerWithFile(String logFile, final BorderRouterTunslipAnalyzerListener listener){
		File f = new File(logFile);
		if(f.exists() == false){
			logger.error("The specified file ({}) does not exist...", f.getAbsolutePath());
			return null;
		}
		logger.info("Reading from file {} ({})", logFile, f.getAbsolutePath());
		Runtime r = Runtime.getRuntime();
		try {
			final Process p = r.exec("tail -f " + logFile);
			BorderRouterTunslipAnalyzer analyzer = new BorderRouterTunslipAnalyzer(p.getInputStream());
			analyzer.setTunslipListener(new TunslipListener(){
				public void onTunslipStarted(BorderRouterTunslipAnalyzer analyzer) {
					if(listener != null){
						listener.onAnalysisStarted(analyzer);
					}
				}
				public void onTunslipTerminated(BorderRouterTunslipAnalyzer analyzer) {
					if(listener != null){
						listener.onAnalysisTerminated(analyzer);
					}
					p.destroy();
				}
			});
			return analyzer;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
}
