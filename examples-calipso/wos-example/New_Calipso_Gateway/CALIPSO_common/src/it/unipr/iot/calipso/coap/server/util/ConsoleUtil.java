package it.unipr.iot.calipso.coap.server.util;

import java.io.IOException;

public class ConsoleUtil {

	public static void waitForConsole(String prompt, char c) {
		try{
			System.out.println(prompt);
			int type = 0;
			do{
				type = System.in.read();
			} while(type != c);
			
		} catch (IOException e){}
	}
	
	public static void waitForConsole(String prompt) {
		try{
			System.out.println(prompt);
			System.in.read();
		} catch (IOException e){}
	}
	
	public static void waitForConsole() {
		try{
			System.in.read();
		} catch (IOException e){}
	}
	
}
