package it.unipr.iot.calipso.database.time.test;

import it.unipr.iot.calipso.database.time.TimeDatabase;
import it.unipr.iot.calipso.database.time.TimestampedResource;

import java.util.List;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class TimeDatabaseTester {

	private static void pause(int millis) {
		try{
			Thread.sleep(millis);
		} catch (InterruptedException e){
		}
	}

	public static void main(String[] args) {
		TimeDatabase tdb = TimeDatabase.getInstance();
		tdb.addResource("a", "a1");
		pause(200);
		tdb.addResource("a", "a2");
		pause(200);
		tdb.addResource("a", "a3");
		pause(200);
		tdb.addResource("a", "a4");
		pause(200);
		tdb.addResource("a", "a5");
		pause(200);
		tdb.addResource("a", "a6");
		pause(200);
		tdb.addResource("a", "a5");
		pause(200);
		tdb.addResource("a", "a4");
		pause(200);
		tdb.addResource("a", "a3");
		pause(200);
		tdb.addResource("a", "a2");
		pause(200);
		tdb.addResource("a", "a1");
		pause(200);
		tdb.addResource("b", "b1");
		pause(200);
		tdb.addResource("b", "b2");
		pause(200);
		tdb.addResource("b", "b3");
		pause(200);
		tdb.addResource("b", "b4");
		pause(200);
		tdb.addResource("b", "b5");
		pause(200);

		List<TimestampedResource> list = tdb.get("a");
		for(TimestampedResource res : list){
			System.out.println(res);
		}
		System.out.println();
		List<TimestampedResource> sublist = tdb.getLatest("a", 5);
		for(TimestampedResource res : sublist){
			System.out.println(res);
		}
		System.out.println();
		List<TimestampedResource> sublist2 = tdb.getLatest("b", 2);
		for(TimestampedResource res : sublist2){
			System.out.println(res);
		}

	}

}
