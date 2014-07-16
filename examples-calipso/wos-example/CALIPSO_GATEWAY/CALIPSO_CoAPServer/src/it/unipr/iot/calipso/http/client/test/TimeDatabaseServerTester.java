package it.unipr.iot.calipso.http.client.test;

import it.unipr.iot.calipso.database.time.server.TimeDatabaseClientData;
import it.unipr.iot.calipso.http.client.dao.PostTimeDatabaseRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class TimeDatabaseServerTester {

	private static Logger logger = LoggerFactory.getLogger(TimeDatabaseServerTester.class);

	public static void main(String[] args) {
		for(int i = 0; i < 100; i++){
			TimeDatabaseClientData data = new TimeDatabaseClientData("/parking/1/parent", System.currentTimeMillis(), "10");
			PostTimeDatabaseRequest request = new PostTimeDatabaseRequest("http://localhost:50080", data);
			request.execute();
			logger.info("{}: {} {} - {}", new java.util.Date(), request.getStatus(), request.getStatus().getReasonPhrase(), data);
			try{
				Thread.sleep(1000);
			} catch (InterruptedException e){
				e.printStackTrace();
			}
		}
	}

}
