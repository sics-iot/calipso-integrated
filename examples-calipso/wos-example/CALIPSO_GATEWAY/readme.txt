GATEWAY
--------------------------------------------------------------------------
Import all the projects that are available under CALIPSO_GATEWAY

Create a an empty file named log.dat.RELAYED in the folder you want (be careful with the absolute path). This will allow the analyzer to process the tunslip output

In CalipsoSmartParkingServer.java and in BorderRouterTunslipAnalyzerLauncher.java set the path to the log file you just created 

Launch the BorderRouterTunslipAnalyzerLauncher before the execution of the experience


In order to collect statistics launch in the following order these classes:
TimeDatabaseServerLauncher.java (inside the TimeDatabase project)
SmartDisplayProxyLauncher.java (inside the SmartDisplayProxy project)
CalipsoSmartParkingServer.java (inside the CoapServer project)
The class PingDelayMeter.java allows to collect statistic on round trip delay (execute it after network has started up)

Once they are running you can execute cooja or start the nodes (NOTE: not launch the tunslip yet)

Launch the tunslip with output redirection to the file previously created (e.g., sudo ./tunslip6 -a localhost -p 60003 aaaa::1/64 > YOUR_PATH/log.dat.RELAYED

Once the tree is created, eventually launch the generate_msgs.sh script to assign the ids (if in Cooja)

Launch PingDelayMeter.java to periodically ping the nodes and collect the statistics on delay


COLLECT RESULTS
--------------------------------------------------------------------------

Kill all the running java processes to save statistics. The gateway will create a timedb-TIME.txt file in the ~/workspace/TimeDatabase/saves folder with all the performance indicators.
