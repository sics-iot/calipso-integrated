package it.unipr.iot.calipso.database.time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class TimeDatabase {

	private static Logger logger = LoggerFactory.getLogger(TimeDatabase.class);

	private static TimeDatabase instance;

	private Map<String, List<TimestampedResource>> map;

	private TimeDatabase() {
		this.map = new HashMap<String, List<TimestampedResource>>();
	}

	public static TimeDatabase getInstance() {
		if(instance == null) instance = new TimeDatabase();
		return instance;
	}

	public synchronized void addResource(String key, String value) {
		this.addResource(key, new TimestampedResource(System.currentTimeMillis(), value));
	}

	public synchronized void addResource(String key, TimestampedResource resource) {
		logger.info("Adding {} -> {}", key, resource);
		synchronized (map){
			if(this.map.get(key) == null){
				this.map.put(key, new ArrayList<TimestampedResource>());
			}
			List<TimestampedResource> list = this.map.get(key);
			synchronized (list){
				list.add(resource);
			}
		}
	}

	public synchronized List<TimestampedResource> get(String key) {
		synchronized (map){
			if(this.map.get(key) != null){
				return new ArrayList<TimestampedResource>(this.map.get(key));
			}
			else return new ArrayList<TimestampedResource>();
		}
	}

	public synchronized List<TimestampedResource> getLatest(String key, int size) {
		synchronized (map){
			if(this.map.get(key) != null){
				int from = 0;
				if(this.map.get(key).size() - size > 0) from = this.map.get(key).size() - size;
				int to = this.map.get(key).size();
				return new ArrayList<TimestampedResource>(this.map.get(key).subList(from, to));
			}
			else return new ArrayList<TimestampedResource>();
		}
	}

	public void saveToFile(String path) {

	}

	public void loadFromFile(String path) {

	}

}
