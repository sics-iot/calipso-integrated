package it.unipr.iot.calipso.demo;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unipr.iot.calipso.coap.client.process.CalipsoSmartParkingClientDemoContikiEmulator;
import it.unipr.iot.calipso.coap.server.process.CalipsoSmartParkingGateway;
import it.unipr.iot.calipso.coap.server.util.ConsoleUtil;
import it.unipr.iot.calipso.coap.server.util.TimeUtil;
import it.unipr.iot.calipso.database.time.server.process.TimeDatabaseServerLauncher;
import it.unipr.iot.calipso.smartdisplay.proxy.process.SmartDisplayProxyLauncher;

public class SmartParkingDemoLauncher {

	private static final Logger logger = LoggerFactory.getLogger(SmartParkingDemoLauncher.class);
	
	public static void main(String[] args) {
		logger.info("Launching SmartParkingDemo");
		final String latest = getLatestTimeDBDump("saves");
		new Thread(){
			public void run(){
				if(latest != null){
					TimeDatabaseServerLauncher.main(new String[]{"50080","true",latest});
				}
				else{
					TimeDatabaseServerLauncher.main(new String[]{"50080","false"});
				}
			}
		}.start();
		TimeUtil.pause(2000);
		new Thread(){
			public void run(){
				SmartDisplayProxyLauncher.main(new String[]{});
			}
		}.start();
		TimeUtil.pause(2000);
		ConsoleUtil.waitForConsole("Type enter to launch CoAP Server...");
		new Thread(){
			public void run(){
				CalipsoSmartParkingGateway.main(new String[]{});
			}
		}.start();
		logger.info("SmartParkingDemo is running...");
		TimeUtil.pause(2000);
		ConsoleUtil.waitForConsole("Type enter to start CoAP clients...", '\n');
		new Thread(){
			public void run(){
				CalipsoSmartParkingClientDemoContikiEmulator.main(new String[]{});
			}
		}.start();
		TimeUtil.pause(2000);
	}
	
	private static String getLatestTimeDBDump(String dir){
		File savesDir = new File(dir);
		if(savesDir.exists() && savesDir.isDirectory()){
			File[] files = savesDir.listFiles();
			if(files.length > 0){
				return files[files.length - 1].getAbsolutePath();
			}
			else{
				return null;
			}
		}
		else return null;
	}

}
