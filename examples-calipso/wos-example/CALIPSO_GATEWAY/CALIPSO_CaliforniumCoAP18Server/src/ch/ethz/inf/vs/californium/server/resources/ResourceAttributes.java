/*******************************************************************************
 * Copyright (c) 2014, Institute for Pervasive Computing, ETH Zurich.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * This file is part of the Californium (Cf) CoAP framework.
 ******************************************************************************/
package ch.ethz.inf.vs.californium.server.resources;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ch.ethz.inf.vs.californium.coap.LinkFormat;

/**
 * ResourceAttributes wraps different attributes that the CoAP protocol defines
 * such as title, resource type or interface description. These attributes will
 * also be included in the link description of the resource they belong to. For
 * example, if a title was specified, the link description for a sensor resource
 * might look like this <code>&lt;/sensors&gt;;title="Sensor Index"</code>.
 */
public class ResourceAttributes {
	
	/** Contains the resource's attributes specified in the CoRE Link Format. */
	private final ConcurrentMap<String, AttributeValues> attributes;
	
	/**
	 * Instantiates a new resource attributes.
	 */
	public ResourceAttributes() {
		attributes = new ConcurrentHashMap<String, AttributeValues>();
	}
	
	/**
	 * Gets the number of attributes.
	 *
	 * @return the number of attributes
	 */
	public int getCount() {
		return attributes.size();
	}
	
	/**
	 * Gets the resource title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		if (containsAttribute(LinkFormat.TITLE)) {
			return getAttributeValues(LinkFormat.TITLE).get(0);
		} else {
			return null;
		}
	}

	/**
	 * Sets the resource title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		findAttributeValues(LinkFormat.TITLE).setOnly(title);
	}
	
	/**
	 * Adds a resource type.
	 *
	 * @param type the type
	 */
	public void addResourceType(String type) {
		findAttributeValues(LinkFormat.RESOURCE_TYPE).add(type);
	}
	
	/**
	 * Clear all resource types.
	 */
	public void clearResourceType() {
		attributes.remove(LinkFormat.RESOURCE_TYPE);
	}
	
	/**
	 * Gets all resource types.
	 *
	 * @return the resource types
	 */
	public List<String> getResourceTypes() {
		return getAttributeValues(LinkFormat.RESOURCE_TYPE);
	}
	
	/**
	 * Adds an interface description.
	 *
	 * @param description the description
	 */
	public void addInterfaceDescription(String description) {
		findAttributeValues(LinkFormat.INTERFACE_DESCRIPTION).add(description);
	}
	
	/**
	 * Gets all interface descriptions.
	 *
	 * @return the interface descriptions
	 */
	public List<String> getInterfaceDescriptions() {
		return getAttributeValues(LinkFormat.INTERFACE_DESCRIPTION);
	}
	
	/**
	 * Sets the maximum size estimate.
	 *
	 * @param size the new maximum size estimate
	 */
	public void setMaximumSizeEstimate(String size) {
		findAttributeValues(LinkFormat.MAX_SIZE_ESTIMATE).setOnly(size);
	}
	
	/**
	 * Sets the maximum size estimate.
	 *
	 * @param size the new maximum size estimate
	 */
	public void setMaximumSizeEstimate(int size) {
		findAttributeValues(LinkFormat.MAX_SIZE_ESTIMATE).setOnly(Integer.toString(size));
	}
	
	/**
	 * Gets the maximum size estimate.
	 *
	 * @return the maximum size estimate
	 */
	public String getMaximumSizeEstimate() {
		return findAttributeValues(LinkFormat.MAX_SIZE_ESTIMATE).getFirst();
	}
	
	/**
	 * Adds a content type specified by an integer.
	 *
	 * @param type the type
	 */
	public void addContentType(int type) {
		findAttributeValues(LinkFormat.CONTENT_TYPE).add(Integer.toString(type));
	}
	
	/**
	 * Gets all content types as list.
	 *
	 * @return the content types
	 */
	public List<String> getContentTypes() {
		return getAttributeValues(LinkFormat.CONTENT_TYPE);
	}
	
	/**
	 * Clear all content types.
	 */
	public void clearContentType() {
		attributes.remove(LinkFormat.CONTENT_TYPE);
	}
	
	/**
	 * Marks the resource as observable.
	 */
	public void setObservable() {
		findAttributeValues(LinkFormat.OBSERVABLE).setOnly("");
	}
	
	/**
	 * Checks if the resource is observable.
	 *
	 * @return true, if observable
	 */
	public boolean hasObservable() {
		return !getAttributeValues(LinkFormat.OBSERVABLE).isEmpty();
	}
	
	/**
	 * Replaces the value for the specified attribute with the specified value.
	 * If another value has been set for the attribute name, it will be removed.
	 * 
	 * @param attr the attribute name
	 * @param value the value
	 */
	public void setAttribute(String attr, String value) {
		findAttributeValues(attr).setOnly(value);
	}
	
	/**
	 * Adds an arbitrary attribute with no value.
	 *
	 * @param attr the attribute name
	 */
	public void addAttribute(String attr) {
		addAttribute(attr, "");
	}
	
	/**
	 * Adds the specified value to the other values of the specified attribute
	 * name.
	 * 
	 * @param attr the attribute
	 * @param value the value
	 */
	public void addAttribute(String attr, String value) {
		findAttributeValues(attr).add(value);
	}
	
	/**
	 * Removes all values for the specified attribute
	 *
	 * @param attr the attribute
	 */
	public void clearAttribute(String attr) {
		attributes.remove(attr);
	}
	
	/**
	 * Returns <tt>true</tt> if this object contains the specified attribute.
	 *
	 * @param attr the attribute
	 * @return true, if successful
	 */
	public boolean containsAttribute(String attr) {
		return attributes.containsKey(attr);
	}
	
	/**
	 * Returns a {@link Set} view of the attribute names. If the map is modified
	 * while an iteration over the set is in progress (except through the
	 * iterator's own <tt>remove</tt> operation), the results of the iteration
	 * are undefined. The set supports element removal, which removes the
	 * corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
	 * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
	 * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
	 * <tt>addAll</tt> operations.
	 * 
	 * @return a set view of the attribute names
	 */
	public Set<String> getAttributeKeySet() {
		return attributes.keySet();
	}
	
	/**
	 * Gets all values for the specified attribute.
	 *
	 * @param attr the atrribute
	 * @return the attribute values
	 */
	public List<String> getAttributeValues(String attr) {
		AttributeValues list = attributes.get(attr);
		if (list != null) return list.getAll();
		else return Collections.emptyList();
	}
	
	/**
	 * Find the attribute values for the specified attribute.
	 *
	 * @param attr the attribute
	 * @return the attribute values
	 */
	private AttributeValues findAttributeValues(String attr) {
		AttributeValues list = attributes.get(attr);
		if (list == null) {
			list = new AttributeValues();
			AttributeValues prev = attributes.putIfAbsent(attr, list);
			if (prev != null) return prev;
		}
		return list;
	}
	
	/**
	 * The class AttributeValues contains a list of all values for a specific
	 * attribute.
	 */
	private final static class AttributeValues {
		 
		/** The list. */
		private final List<String> list = 
				Collections.synchronizedList(new LinkedList<String>());
		
		/**
		 * Gets all values.
		 *
		 * @return all values
		 */
		private List<String> getAll() {
			return list;
		}
		
		/**
		 * Adds the specified value to the list.
		 *
		 * @param value the value
		 */
		private void add(String value) {
			list.add(value);
		}
		
		/**
		 * Gets the first value of the list.
		 *
		 * @return the first value
		 */
		private synchronized String getFirst() {
			if (list.isEmpty()) return "";
			else return list.get(0);
		}
		
		/**
		 * Adds the specified value but removes all others.
		 *
		 * @param value the value
		 */
		private synchronized void setOnly(String value) {
			list.clear();
			if (value != null)
				list.add(value);
		}
	}
}