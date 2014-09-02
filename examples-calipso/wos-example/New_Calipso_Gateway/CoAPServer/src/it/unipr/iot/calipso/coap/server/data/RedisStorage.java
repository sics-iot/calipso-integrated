package it.unipr.iot.calipso.coap.server.data;

import it.unipr.iot.calipso.storage.jedis.Redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class RedisStorage implements IResourceStorage {

	protected final static Logger logger = LoggerFactory.getLogger(RedisStorage.class);

	private static RedisStorage instance;

	private Redis redis;

	private RedisStorage() {
	}

	public static RedisStorage getInstance() {
		if(instance == null) instance = new RedisStorage();
		return instance;
	}

	public void setRedis(String redisHost, int redisPort) {
		this.redis = Redis.getInstance(redisHost, redisPort);
	}

	public void store(String key, String value) {
		logger.debug("Storing resource");
		if(this.redis != null){
			logger.debug("Adding resource '{}' to Redis database", key);
			this.redis.set(key, value);
			logger.debug("Added resource to Redis database");
		}
		else{
			logger.debug("Redis database is not configured");
		}
	}
}
