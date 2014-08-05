package it.unipr.iot.calipso.storage.jedis.process;

import it.unipr.iot.calipso.storage.jedis.Redis;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class JedisLauncher {

	JedisLauncher() {
	}

	public static void main(String[] args) {
		JedisLauncher launcher = new JedisLauncher();
		launcher.test();
	}

	private void test() {
		Redis redis = Redis.getInstance("127.0.0.1", 6379);
		redis.set("a", "1");
		redis.set("b", "2");
		redis.set("c", "3");
		String a = redis.get("a");
		System.out.println("a = " + a);
		redis.remove("b");
		String b = redis.get("b");
		System.out.println("b = " + b);
		redis.append("a", "ciao");
		a = redis.get("a");
		System.out.println("a = " + a);
	}

}
