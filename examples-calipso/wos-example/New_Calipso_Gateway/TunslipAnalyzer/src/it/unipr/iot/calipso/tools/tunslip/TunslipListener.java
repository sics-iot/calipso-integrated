package it.unipr.iot.calipso.tools.tunslip;

public interface TunslipListener {
	
	public void onTunslipStarted(BorderRouterTunslipAnalyzer analyzer);
	public void onTunslipTerminated(BorderRouterTunslipAnalyzer analyzer);

}
