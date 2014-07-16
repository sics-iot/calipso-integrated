package it.unipr.iot.calipso.coap.server.resources;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.OptionSet;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

/**
 * 
 * 
 * @author Simone Cirani <a href="mailto:simone.cirani@unipr.it">simone.cirani@unipr.it</a>
 * 
 */

public class CalipsoSmartParkingResource extends ResourceBase {

	public static final String PRESENCE_SERVICE = "presence";

	private int counter;
	private String content;

	public CalipsoSmartParkingResource(String name) {
		super(name);
		this.counter = 1;
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		OptionSet options = exchange.getRequestOptions();
		List<String> segments = options.getURIPaths();
		LinkedList<String> s = new LinkedList<String>(segments);
		s.removeFirst();
		String path = options.getURIPathString();
		LOGGER.info("[" + this.getName() + "]\tRECV POST " + path + " - Payload: " + exchange.getRequestText());
		if(path.equals(this.getName())){
			LOGGER.info("\tA new node has joined the network: " + this.counter);
			s.add(new Integer(this.counter).toString());
			Resource resource = create(s);
			Response response = new Response(ResponseCode.CREATED);
			response.getOptions().setLocationPath(resource.getURI());
			exchange.respond(response);
			this.counter++;
		}
		else{
			if(segments.size() == 3){
				String id = segments.get(1);
				String res = segments.get(2);
				LOGGER.info("\tNode " + id + " is adding '" + res + "' service");
				this.content = exchange.getRequestText();
				Resource resource = create(new LinkedList<String>());
				Response response = new Response(ResponseCode.CREATED);
				response.getOptions().setLocationPath(resource.getURI());
				exchange.respond(response);
			}
			else{
				Response response = new Response(ResponseCode.BAD_REQUEST);
				response.setPayload("Invalid URI");
				exchange.respond(response);
			}
		}

	}

	@Override
	public void handlePUT(CoapExchange exchange) {
		this.content = exchange.getRequestText();
		exchange.respond(ResponseCode.CHANGED);
		RedisAndTimeDatabaseResourceStorage.getInstance().store(this.getURI(), exchange.getRequestText());
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		if(content != null){
			exchange.respond(content);
		}
		else{
			// String subtree = LinkFormat.serializeTree(this);
			// exchange.respond(ResponseCode.CONTENT, subtree, MediaTypeRegistry.APPLICATION_LINK_FORMAT);
			exchange.respond(ResponseCode.NOT_FOUND);
		}
	}

	/**
	 * Find the requested child. If the child does not exist yet, create it.
	 */
	@Override
	public Resource getChild(String name) {
		Resource resource = super.getChild(name);
		if(resource == null){
			resource = new CalipsoSmartParkingResource(name);
			add(resource);
		}
		return resource;
	}

	/**
	 * Create a resource hierarchy with according to the specified path.
	 * 
	 * @param path
	 *            the path
	 * @return the lowest resource from the hierarchy
	 */
	private Resource create(LinkedList<String> path) {
		String segment;
		do{
			if(path.size() == 0) return this;
			segment = path.removeFirst();
		} while (segment.isEmpty() || segment.equals("/"));

		CalipsoSmartParkingResource resource = new CalipsoSmartParkingResource(segment);
		add(resource);
		LOGGER.info("\tCreated resource: " + resource.getURI());
		return resource.create(path);
	}
}
