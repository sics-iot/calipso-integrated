package it.unipr.iot.calipso.tools.tunslip;

public interface BorderRouterTunslipAnalyzerListener {
	
	public void onAnalysisStarted(BorderRouterTunslipAnalyzer analyzer);
	public void onAnalysisTerminated(BorderRouterTunslipAnalyzer analyzer);
	public void onTunslipEvent(BorderRouterTunslipAnalyzer analyzer, Long timestamp, String line);

}
