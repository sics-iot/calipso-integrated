package it.unipr.iot.calipso.coap.server.resources;

import it.unipr.iot.calipso.http.client.dao.PostTimeDatabaseRequest;
import it.unipr.iot.calipso.storage.jedis.Redis;

import java.util.logging.Logger;

import org.springframework.http.HttpStatus;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class RedisAndTimeDatabaseResourceStorage implements IResourceStorage {

	protected final static Logger LOGGER = Logger.getLogger(RedisAndTimeDatabaseResourceStorage.class.getCanonicalName());

	private static RedisAndTimeDatabaseResourceStorage instance;

	private Redis redis;
	private String timeDatabaseURL;

	private RedisAndTimeDatabaseResourceStorage() {
	}

	public static RedisAndTimeDatabaseResourceStorage getInstance() {
		if(instance == null) instance = new RedisAndTimeDatabaseResourceStorage();
		return instance;
	}

	public void setRedis(String redisHost, int redisPort) {
		this.redis = Redis.getInstance(redisHost, redisPort);
	}

	public void setTimeDatabase(String timeDatabaseURL) {
		this.timeDatabaseURL = timeDatabaseURL;
	}

	public void store(String key, String value) {
		// LOGGER.info("Storing resource");
		if(this.redis != null){
			// LOGGER.info("Adding resource '" + key + " 'to Redis database");
			this.redis.set(key, value);
			// LOGGER.info("Added resource to Redis database");
		}
		else{
			// LOGGER.info("Redis database is not configured");
		}
		if(this.timeDatabaseURL != null){
			// LOGGER.info("Adding resource to TimeDatabase " + this.timeDatabaseURL);
			PostTimeDatabaseRequest request = new PostTimeDatabaseRequest(this.timeDatabaseURL, key, value);
			request.execute();
			// LOGGER.info(new java.util.Date() + ": " + request.getStatus() + " " + request.getStatus().getReasonPhrase());
			if(request.getStatus() == HttpStatus.CREATED){
				// LOGGER.info("Added resource to TimeDatabase " + this.timeDatabaseURL);
			}
			else{
				// LOGGER.info("An error occurred while adding resource to TimeDatabase " + this.timeDatabaseURL);
			}
		}
		else{
			// LOGGER.info("TimeDatabase database is not configured");
		}
	}
}
