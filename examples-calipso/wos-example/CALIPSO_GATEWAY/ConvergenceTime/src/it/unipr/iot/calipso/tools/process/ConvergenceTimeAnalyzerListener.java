package it.unipr.iot.calipso.tools.process;

public interface ConvergenceTimeAnalyzerListener {
	
	public void onAnalysisStarted(ConvergenceTimeAnalyzer analyzer);
	public void onAnalysisTerminated(ConvergenceTimeAnalyzer analyzer);

}
