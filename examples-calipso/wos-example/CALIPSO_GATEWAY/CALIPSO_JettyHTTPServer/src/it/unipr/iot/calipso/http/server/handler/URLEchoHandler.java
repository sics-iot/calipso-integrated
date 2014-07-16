package it.unipr.iot.calipso.http.server.handler;

import it.unipr.iot.calipso.http.server.process.URLEchoHTTPServerLauncher;

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

public class URLEchoHandler extends AbstractHandler {

	private static Logger logger = LoggerFactory.getLogger(URLEchoHandler.class);

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setStatus(200);
		response.setContentType("text/plain");
		baseRequest.setHandled(true);
		String url = baseRequest.getUri().toString();
		logger.info("RECV: {}", url);
		response.getWriter().println(url);
	}
}
