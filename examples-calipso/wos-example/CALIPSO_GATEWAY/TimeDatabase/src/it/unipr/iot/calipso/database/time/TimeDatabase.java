package it.unipr.iot.calipso.database.time;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
		logger.debug("Adding {} -> {}", key, resource);
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

	public synchronized boolean saveToFile(String path) {
		logger.info("Saving contents to file {}", path);
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(path, "UTF-8");
			writer.println("# TimeDatabase saved on " + new java.util.Date());
			writer.println("# " + path);
			writer.println("# ");
			synchronized (map){
				for(String key : map.keySet()){
					List<TimestampedResource> values = map.get(key);
					for(TimestampedResource value : values){
						if(value.getValue() != null){
							writer.println(key + "\t" + value.getTimestamp() + "\t" + value.getValue());
						}
						else{
							writer.println(key + "\t" + value.getTimestamp());
						}
					}
				}
			}
		} catch (FileNotFoundException e){
			logger.error("Could not find file {} : {}", path, e.getMessage());
			return false;
		} catch (UnsupportedEncodingException e){
			logger.error("Could not write to file {} : {}", path, e.getMessage());
			return false;
		} finally{
			if(writer != null) writer.close();
		}
		logger.info("Saved contents to file {}", path);
		return true;
	}

	public synchronized boolean loadFromFile(String path) {
		logger.info("Loading contents from file {}", path);
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(path));
			synchronized (map){
				String line;
				while ((line = reader.readLine()) != null){
					if(!line.startsWith("#")){
						String[] tokens = line.split("\t");
						String key = tokens[0];
						Long timestamp = Long.parseLong(tokens[1]);
						String value = null;
						if(tokens.length == 3) value = tokens[2];
						this.addResource(key, new TimestampedResource(timestamp, value));
					}
				}
			}
		} catch (FileNotFoundException e){
			logger.error("Could not find file {} : {}", path, e.getMessage());
			return false;
		} catch (IOException e){
			logger.error("Could not read from file {} : {}", path, e.getMessage());
			return false;
		} finally{
			if(reader != null) try{
				reader.close();
			} catch (IOException e){
				logger.error("Could not close file {} : {}", path, e.getMessage());
			}
		}
		logger.info("Loaded contents from file {}", path);
		return true;
	}
}
