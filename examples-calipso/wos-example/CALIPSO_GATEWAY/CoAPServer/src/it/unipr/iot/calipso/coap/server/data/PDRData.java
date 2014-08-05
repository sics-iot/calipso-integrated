package it.unipr.iot.calipso.coap.server.data;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class PDRData {

	private int TX;
	private int RX;
	private double PDR;

	public PDRData() {
	}

	public PDRData(int tx, int rx) {
		super();
		this.TX = tx;
		this.RX = rx;
		this.PDR = (double) tx / (double) rx;
	}

	public int getTX() {
		return this.TX;
	}

	public void setTX(int tX) {
		this.TX = tX;
	}

	public int getRX() {
		return this.RX;
	}

	public void setRX(int rX) {
		this.RX = rX;
	}

	public double getPDR() {
		return this.PDR;
	}

	public void setPDR(double pDR) {
		this.PDR = pDR;
	}

}
