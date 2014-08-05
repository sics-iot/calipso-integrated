package it.unipr.iot.calipso.tools.ping;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class PingResults {

	private int exitValue;
	private int packetTX;
	private int packetRX;
	private double packetLoss;
	private double rttMin;
	private double rttAvg;
	private double rttMax;
	private double rttStdDev;

	public PingResults() {
	}

	public PingResults(int exitValue, int packetTX, int packetRX, double packetLoss, double rttMin, double rttAvg, double rttMax, double rttStdDev) {
		this.exitValue = exitValue;
		this.packetTX = packetTX;
		this.packetRX = packetRX;
		this.packetLoss = packetLoss;
		this.rttMin = rttMin;
		this.rttAvg = rttAvg;
		this.rttMax = rttMax;
		this.rttStdDev = rttStdDev;
	}

	public int getExitValue() {
		return this.exitValue;
	}

	public int getPacketTX() {
		return this.packetTX;
	}

	public int getPacketRX() {
		return this.packetRX;
	}

	public double getPacketLoss() {
		return this.packetLoss;
	}

	public double getRttMin() {
		return this.rttMin;
	}

	public double getRttAvg() {
		return this.rttAvg;
	}

	public double getRttMax() {
		return this.rttMax;
	}

	public double getRttStdDev() {
		return this.rttStdDev;
	}

	public void setExitValue(int exitValue) {
		this.exitValue = exitValue;
	}

	public void setPacketTX(int packetTX) {
		this.packetTX = packetTX;
	}

	public void setPacketRX(int packetRX) {
		this.packetRX = packetRX;
	}

	public void setPacketLoss(double packetLoss) {
		this.packetLoss = packetLoss;
	}

	public void setRttMin(double rttMin) {
		this.rttMin = rttMin;
	}

	public void setRttAvg(double rttAvg) {
		this.rttAvg = rttAvg;
	}

	public void setRttMax(double rttMax) {
		this.rttMax = rttMax;
	}

	public void setRttStdDev(double rttStdDev) {
		this.rttStdDev = rttStdDev;
	}

}
