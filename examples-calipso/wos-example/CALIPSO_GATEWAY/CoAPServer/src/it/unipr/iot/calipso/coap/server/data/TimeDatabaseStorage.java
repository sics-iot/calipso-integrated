package it.unipr.iot.calipso.coap.server.data;

import it.unipr.iot.calipso.http.client.dao.PostTimeDatabaseRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class TimeDatabaseStorage implements IResourceStorage {

	protected final static Logger logger = LoggerFactory.getLogger(TimeDatabaseStorage.class);

	private static TimeDatabaseStorage instance;

	private String timeDatabaseURL;

	private TimeDatabaseStorage() {
	}

	public static TimeDatabaseStorage getInstance() {
		if(instance == null) instance = new TimeDatabaseStorage();
		return instance;
	}

	public void setTimeDatabase(String timeDatabaseURL) {
		this.timeDatabaseURL = timeDatabaseURL;
	}

	public void store(String key, String value) {
		logger.debug("Storing resource");
		if(this.timeDatabaseURL != null){
			logger.debug("Adding resource to TimeDatabase {}", this.timeDatabaseURL);
			PostTimeDatabaseRequest request = new PostTimeDatabaseRequest(this.timeDatabaseURL, key, value);
			request.execute();
			logger.debug("PostTimeDatabaseRequest: {}", request.getStatus());
			if(request.getStatus() == HttpStatus.CREATED){
				logger.debug("Added resource {} to TimeDatabase {}", request.getData(), this.timeDatabaseURL);
			}
			else{
				logger.error("An error occurred while adding resource {} to TimeDatabase {}", request.getData(), this.timeDatabaseURL);
			}
		}
		else{
			logger.debug("TimeDatabase database is not configured");
		}
	}
}
