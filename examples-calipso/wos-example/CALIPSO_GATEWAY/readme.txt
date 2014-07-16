--- Instructions for running the Java-based Calipso Gateway ----

1. Install required software in Instant Contiki
	- Redis: you can follow the quick start guide at redis.io/topics/quickstart
	- java openjdk 6 or greater: should be already installed in Instant Contiki
	- Eclipse IDE: you can type in a terminal > sudo apt-get install eclipse


2. Import the existing Java projects into Eclipse

Launch Eclipse and select "Import... -> Existing projects into workspace", then browse to calipso-integrated/examples-calipso/wos-example/CALIPSO_GATEWAY and select all the subfolders. Click "Finish". You should now see the following projects in the workspace:
	- CALIPSO_CaliforniumCoAP18Server: it contains the CoAP java library (draft 18)
	- CALIPSO_CoAPServer: this is the coap server handling all CoAP resources from the clients
	- CALIPSO_JettyHTTPServer: it is a web server that reads the TimeDatabase
	- CALIPSO_RedisKeyValueStore: this application stores the current data in the redis database
	- CALIPSO_SpringHTTPClient: this application stores data in the TimeDatabase
	- CALIPSO_TimeDatabase: this is the actual database, which stores all the data.


3. Run the gateway

You need to run 2 applications
	- CALIPSO_CoAPServer: the package is it.unipr.iot.calipso.coap.server.process
	- CALIPSO_TimeDatabase: the package is it.unipr.iot.calipso.database.time.server.process 


4. Start Cooja simulation

run Cooja and open calipso-integrated/examples-calipso/wos-example/wos-example-client-only.csc simulation file.

5. Connect the border router and start the simulation
 > cd calipso-integrated/examples-calipso/wos-example
 > make connect-router-cooja
Now you can start the Cooja simulation. Make sure to have the gateway running first.


6. (Optional) Start Wireshark to capture on the virtual tun interface


7. Test it!
From Wireshark, you can see the CoAP messages exchanged by the nodes and the CoAP server.
Open a browser and type:
http://localhost:5080?resource=/parking/<id>/<resource name> to see the data stored in the database

Examples:
http://localhost:5080?resource=/parking/1/pdr
http://localhost:5080?resource=/parking/1/energy
http://localhost:5080?resource=/parking/1/parent&size=10
The last URL returns the last 10 readings.
