package it.unipr.iot.calipso.tools.ping;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class Ping6Client extends PingClient {

	public String getPingCommand(String ip, int count) {
		return "ping6 -c " + count + " " + ip;
	}

}
