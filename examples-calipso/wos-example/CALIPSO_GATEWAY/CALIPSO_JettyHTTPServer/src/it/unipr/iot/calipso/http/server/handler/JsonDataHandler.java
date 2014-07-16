package it.unipr.iot.calipso.http.server.handler;

import it.unipr.iot.calipso.http.server.HttpServletRequestUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class JsonDataHandler extends AbstractHandler {

	private static Logger logger = LoggerFactory.getLogger(JsonDataHandler.class);

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String body = HttpServletRequestUtil.getBody(request);
		if(body == null || body.length() == 0){
			body = "null";
		}
		logger.info("body = {}", body);
		response.setStatus(200);
		response.setContentType("application/json");
		baseRequest.setHandled(true);
		response.getWriter().println(body);
	}

}