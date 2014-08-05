package it.unipr.iot.calipso.http.server.test.handler;

import java.io.File;
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

public class SimpleFileHandler extends AbstractHandler {

	private String baseURLPath;
	private String fileSystemBase;

	public SimpleFileHandler(String fileSystemBase, String baseURLPath) {
		this.fileSystemBase = fileSystemBase;
		this.baseURLPath = baseURLPath;
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String[] parts = target.split("/");
		if(parts.length > 0){
			if(("/" + parts[1]).equals(this.baseURLPath)){
				File file = new File(this.fileSystemBase + target.substring(this.baseURLPath.length()));

				response.setStatus(200);
				baseRequest.setHandled(true);
				response.getWriter().println(file.getAbsolutePath());
			}
			else{
				response.setStatus(404);
				baseRequest.setHandled(true);
				response.getWriter().println("Not found!\n" + target + "\t'" + parts[1] + "'\t" + "/" + this.baseURLPath);
			}
		}
		// String body = HttpServletRequestUtil.getBody(request);

	}

}