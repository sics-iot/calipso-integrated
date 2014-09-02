package it.unipr.iot.calipso.database.time.server;

import it.unipr.iot.calipso.database.time.TimeDatabase;
import it.unipr.iot.calipso.database.time.TimestampedResource;
import it.unipr.iot.calipso.http.server.HttpServletRequestUtil;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class TimeDatabaseHandler extends AbstractHandler {

	private static Logger logger = LoggerFactory.getLogger(TimeDatabaseHandler.class);

	public static final int DEFAULT_PAGE_SIZE = 20;
	
	private Gson gson;

	public TimeDatabaseHandler() {
		this.gson = new GsonBuilder().serializeNulls().create();
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String method = request.getMethod();
		if(method.equals("POST")){
			String body = HttpServletRequestUtil.getBody(request);
			TimeDatabaseClientData data = this.gson.fromJson(body, TimeDatabaseClientData.class);
			logger.debug("New resource for key {}", data.getUri());
			TimeDatabase.getInstance().addResource(data.getUri(), data.getResource());
			response.setStatus(201);
			baseRequest.setHandled(true);
		}
		else if(method.equals("GET")){
			String query = request.getQueryString();
			logger.debug("Fetch resource history: {}", query);
			String resource = request.getParameter("resource");
			String size = request.getParameter("size");
			if(query != null && query.length() > 0){
				List<TimestampedResource> values;
				if(size == null){
					//values = TimeDatabase.getInstance().get(resource);
					values = TimeDatabase.getInstance().getLatest(resource, DEFAULT_PAGE_SIZE);
				}
				else{
					values = TimeDatabase.getInstance().getLatest(resource, Integer.parseInt(size));
				}
				String jsonp = request.getParameter("jsonp");
				String body = this.gson.toJson(values);
				if(jsonp != null){
					body = jsonp + "(" + body + ")";
				}
				response.setStatus(201);
				response.setContentType("application/json");
				baseRequest.setHandled(true);
				response.getWriter().println(body);
			}
		}
		else{
			logger.info("Bad method {}", method);
			response.setStatus(405); // method not allowed
			baseRequest.setHandled(true);
		}

	}

}