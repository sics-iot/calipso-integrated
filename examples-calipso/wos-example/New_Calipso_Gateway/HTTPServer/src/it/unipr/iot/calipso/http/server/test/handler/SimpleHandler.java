package it.unipr.iot.calipso.http.server.test.handler;

import it.unipr.iot.calipso.http.server.HttpServletRequestUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class SimpleHandler extends AbstractHandler {

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String body = HttpServletRequestUtil.getBody(request);
		response.setStatus(200);
		baseRequest.setHandled(true);
		response.getWriter().println(body);
	}

}