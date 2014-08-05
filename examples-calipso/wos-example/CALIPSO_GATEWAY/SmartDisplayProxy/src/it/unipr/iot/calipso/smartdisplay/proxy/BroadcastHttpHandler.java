package it.unipr.iot.calipso.smartdisplay.proxy;

import it.unipr.iot.calipso.http.server.HttpServletRequestUtil;
import it.unipr.smartdisplay.messages.BaseMessage;
import it.unipr.smartdisplay.messages.BaseRequest;
import it.unipr.smartdisplay.messages.BaseResponse;
import it.unipr.smartdisplay.messages.RequestType;
import it.unipr.smartdisplay.messages.agent.AgentActionRequest;
import it.unipr.smartdisplay.messages.agent.AgentActionResponse;
import it.unipr.smartdisplay.messages.agent.AgentConnectionRequest;
import it.unipr.smartdisplay.messages.agent.AgentConnectionResponse;
import it.unipr.smartdisplay.messages.agent.AgentConnectionResponse.AgentConnectionResponseParameters;
import it.unipr.smartdisplay.messages.agent.AgentDisconnectionRequest;
import it.unipr.smartdisplay.messages.json.BaseMessageWithoutPayloadDeserializer;
import it.unipr.smartdisplay.util.TokenGenerator;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

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

public class BroadcastHttpHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(BroadcastHttpHandler.class);

	private ConcurrentLinkedQueue<SimpleWebSocket> broadcast = new ConcurrentLinkedQueue<SimpleWebSocket>();
	private TokenGenerator tokenGenerator;

	public BroadcastHttpHandler(ConcurrentLinkedQueue<SimpleWebSocket> broadcast) {
		this.broadcast = broadcast;
		this.tokenGenerator = new TokenGenerator();
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String responseBody = null;
		if(request.getMethod().equals("POST")){
			String body = HttpServletRequestUtil.getBody(request);
			logger.debug("RECV\t{}", body);
			BaseRequest req = parseRequest(body);
			if(req != null){
				logger.debug("Deserialized {} request from {}", req.getType(), req.getFrom());
				if(req.getType().equals(RequestType.AGENT_CONNECT)){
					AgentConnectionRequest acreq = (AgentConnectionRequest) req;
					String token = this.generateToken();
					String displayId = acreq.getParams().getDisplayId();
					String appId = acreq.getParams().getAppId();
					AgentConnectionResponse resp = new AgentConnectionResponse(BaseResponse.OK, "Connected", req.getRequestId(), System.currentTimeMillis(), new AgentConnectionResponseParameters(displayId, appId, token));
					resp.setContentType("application/json");
					resp.setContentEncoding("UTF-8");
					responseBody = new Gson().toJson(resp);
					response.setContentType("application/json");
					response.setStatus(200);
				}
				else if(req.getType().equals(RequestType.AGENT_ACTION)){
					sendMessageOnWebSocket(body);
					AgentActionResponse resp = new AgentActionResponse(BaseResponse.OK, null, req.getRequestId(), System.currentTimeMillis(), null, null, null);
					responseBody = new Gson().toJson(resp);
					response.setContentType("application/json");
					response.setStatus(200);
				}
				else if(req.getType().equals(RequestType.AGENT_DISCONNECT)){
					response.setStatus(501);
				}
				else{
					response.setStatus(400);
				}
			}
			else{
				logger.debug("Unknown message");
				responseBody = "Bad request";
				response.setStatus(400);
			}
		}
		else{
			response.setStatus(405);
		}
		baseRequest.setHandled(true);
		if(responseBody != null) response.getWriter().println(responseBody);
	}

	private void sendMessageOnWebSocket(String msg) {
		for(SimpleWebSocket ws : this.broadcast){
			try{
				logger.debug("sending data to {}", ws.getConnection());
				ws.getConnection().sendMessage(msg);
			} catch (IOException e){
				this.broadcast.remove(ws);
				e.printStackTrace();
			}
		}
	}

	private String generateToken() {
		return this.tokenGenerator.nextToken();
	}

	private static BaseRequest parseRequest(String body) {
		Gson gson = new GsonBuilder().registerTypeAdapter(BaseMessage.class, new BaseMessageWithoutPayloadDeserializer()).serializeNulls().create();
		AgentConnectionRequest acr = gson.fromJson(body, AgentConnectionRequest.class);
		if(acr.getType() != null && acr.getType().equals(RequestType.AGENT_CONNECT)){
			return acr;
		}
		AgentActionRequest aar = gson.fromJson(body, AgentActionRequest.class);
		if(aar.getType() != null && aar.getType().equals(RequestType.AGENT_ACTION)){
			return aar;
		}
		AgentDisconnectionRequest adr = gson.fromJson(body, AgentDisconnectionRequest.class);
		if(adr.getType() != null && adr.getType().equals(RequestType.AGENT_DISCONNECT)){
			return adr;
		}
		else return null;
	}

}
