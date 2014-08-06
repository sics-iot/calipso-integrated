package it.unipr.iot.calipso.tools.ping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public abstract class PingClient {

	private static final Logger logger = LoggerFactory.getLogger(PingClient.class);

	public static final double NO_VALUE = -1;

	private String result;
	private double average;
	private String statistics;

	protected PingClient() {
		this.average = NO_VALUE;
	}

	public String getResult() {
		return this.result;
	}

	public String getStatistics() {
		return this.statistics;
	}

	public abstract String getPingCommand(String ip, int count);

	public final PingResults ping(String ip) {
		return this.ping(ip, 1);
	}

	public final PingResults ping(String ip, int count) {
		String pingResult = "";
		String command = getPingCommand(ip, count);
		int exitValue = 0;
		try{
			logger.debug("Executing command: {}", command);
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			int counter = 0;
			StringBuilder sb = new StringBuilder();
			StringBuilder statsb = new StringBuilder();
			double sum = 0;
			int rows = 0;
			try{
				p.waitFor();
				exitValue = p.exitValue();
				if(exitValue != 0){
					if(exitValue == 2) logger.error("Exit value = {} ({})", exitValue, "The transmission was successful but no responses were received.");
					else logger.error("Exit value = {}", exitValue);
					return null;
				}
			} catch (InterruptedException e){
				logger.error(e.getMessage());
			}
			boolean stats = false;
			while ((line = in.readLine()) != null){
				logger.debug("{}", line);
				sb.append(line);
				pingResult += line;
				/*if(counter < count){
					sb.append("\n");
				}
				if(line.indexOf("time=") >= 0){
					int start = line.indexOf("time=") + "time=".length();
					String time = line.substring(start, start + 5);
					sum += Double.parseDouble(time);
					rows++;
				}*/
				if(stats == true){
					statsb.append(line + "\n");
				}
				else if(line.startsWith("---")){
					stats = true;
				}
				counter++;
			}
			pingResult = new String(sb);
			this.statistics = new String(statsb);
			if(rows > 0){
				this.average = sum / rows;
			}
		} catch (IOException e){
			logger.error(e.getMessage());
		}
		this.result = pingResult;
		if(this.statistics != null){
			return new PingResults(exitValue, this.getTransmitted(), this.getReceived(), this.getPacketLoss(), this.getMin(), this.getAvg(), this.getMax(), this.getStdDev());
		}
		else return null;
	}

	public double getAverage() {
		return this.average;
	}

	public int getTransmitted() {
		if(this.statistics != null){
			Scanner scanner = new Scanner(this.statistics);
			return scanner.nextInt();
		}
		return 0;
	}

	public int getReceived() {
		if(this.statistics != null){
			Scanner scanner = new Scanner(this.statistics);
			scanner.nextInt();
			scanner.next();
			scanner.next();
			return scanner.nextInt();
		}
		return 0;
	}

	public double getPacketLoss() {
		  if(this.statistics != null){
		   Scanner scanner = new Scanner(this.statistics);
		   scanner.nextInt(); // read {number}
		   scanner.next();  // read "packets"
		   scanner.next();  // read "transmitted,"
		   scanner.nextInt(); // read {number}
		   //scanner.next();  // read "packets" -> comment this line if running on Linux
		   scanner.next();  // read "received," 
		   String res = scanner.next(); // read "{number}%"
		   return Double.parseDouble(res.substring(0, res.indexOf("%")));
		  }
		  return 0;
		 }

	public double getMin() {
		if(this.statistics != null){
			Scanner scanner = new Scanner(this.statistics);
			scanner.nextLine();
			scanner.next();
			scanner.next();
			scanner.next();
			String res = scanner.next();
			String[] parts = res.split("/");
			return Double.parseDouble(parts[0]);
		}
		return 0;
	}

	public double getAvg() {
		if(this.statistics != null){
			Scanner scanner = new Scanner(this.statistics);
			scanner.nextLine();
			scanner.next();
			scanner.next();
			scanner.next();
			String res = scanner.next();
			String[] parts = res.split("/");
			return Double.parseDouble(parts[1]);
		}
		return 0;
	}

	public double getMax() {
		if(this.statistics != null){
			Scanner scanner = new Scanner(this.statistics);
			scanner.nextLine();
			scanner.next();
			scanner.next();
			scanner.next();
			String res = scanner.next();
			String[] parts = res.split("/");
			return Double.parseDouble(parts[2]);
		}
		return 0;
	}

	public double getStdDev() {
		if(this.statistics != null){
			Scanner scanner = new Scanner(this.statistics);
			scanner.nextLine();
			scanner.next();
			scanner.next();
			scanner.next();
			String res = scanner.next();
			String[] parts = res.split("/");
			return Double.parseDouble(parts[3]);
		}
		return 0;
	}

}
