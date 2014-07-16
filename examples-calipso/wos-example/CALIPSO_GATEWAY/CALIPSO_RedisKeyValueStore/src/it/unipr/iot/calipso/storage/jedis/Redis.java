package it.unipr.iot.calipso.storage.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class Redis {

	private static Logger logger = LoggerFactory.getLogger(Redis.class);

	private static Redis instance = null;
	private Jedis jedis;

	private Redis(String host, int port) {
		this.jedis = new Jedis(host, port);
	}

	public static Redis getInstance(String host, int port) {
		if(instance == null) instance = new Redis(host, port);
		return instance;
	}

	public void set(String key, String value) {
		// logger.info("Setting {} -> {}", key, value);
		this.jedis.set(key, value);
	}

	public void append(String key, String value) {
		this.jedis.append(key, value);
	}

	public String get(String key) {
		return this.jedis.get(key);
	}

	public void remove(String key) {
		this.jedis.del(key);
	}

}
