package org.jandle.internal.router;

import org.jandle.api.exceptions.JandleSyntaxException;
import org.jandle.api.http.Filter;
import org.jandle.api.http.Handler;
import org.jandle.internal.util.PathUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Represents a node in the routing tree.
 * <p>
 * Each node corresponds to one path segment and may have:
 * - Static children (literal path segments)
 * - A parameter child (e.g. "{id}")
 * - A single wildcard child ("*") matching exactly one segment
 * - A double wildcard child ("**") matching zero or more segments (only at the end)
 * <p>
 * A node may optionally contain a handler and filters if it represents
 * a fully registered route.
 */
public final class RouteNode {
	/** Static (literal) child segments mapped to their corresponding nodes */
	private final Map<String, RouteNode> staticChildren = new ConcurrentHashMap<>();
	/** Name of the path parameter if this node represents a parameter segment */
	private String paramName;
	/** Child node for a path parameter segment */
	private RouteNode paramChild;
	/** Child node for a single wildcard segment ("*") */
	private RouteNode singleWildCardChild;
	/** Child node for a double wildcard segment ("**") */
	private RouteNode doubleWildcardChild;
	/** Handler associated with this route (if this node is terminal) */
	private Handler handler;
	/** Filters associated with this route */
	private Filter[] filers;

	/**
	 * Creates an empty route node.
	 */
	public RouteNode() {}

	/**
	 * Creates a route node representing a path parameter.
	 *
	 * @param paramName name of the parameter (without curly braces)
	 */
	private RouteNode(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * Returns filters associated with this route node
	 * @return {@code Filter[]} filters associated with this route node
	 */
	public Filter[] getFilers() {
		return filers;
	}

	/**
	 * Assigns filters to this route node.
	 *
	 * @param filers filters to apply before handler execution
	 */
	private void setFilers(Filter[] filers) {
		this.filers = filers;
	}

	/**
	 * Returns handler associated with this route node
	 *
	 * @return {@code Handler} handler associated with this route node
	 */
	public Handler getHandler() {
		return handler;
	}

	/**
	 * Assigns a handler to this route node.
	 *
	 * @param handler handler to execute for this route
	 */
	private void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * Creates or replaces the double wildcard child ("**").
	 *
	 * @return the created double wildcard child node
	 */
	private RouteNode setDoubleWildcardChild() {
		this.doubleWildcardChild = new RouteNode();
		return this.doubleWildcardChild;
	}

	/**
	 * Creates or replaces the single wildcard child ("*").
	 *
	 * @return the created single wildcard child node
	 */
	private RouteNode setSingleWildCardChild() {
		this.singleWildCardChild = new RouteNode();
		return this.singleWildCardChild;
	}

	/**
	 * Creates or replaces the parameter child node.
	 *
	 * @param paramName name of the parameter
	 * @return the created parameter child node
	 */
	private RouteNode setParamChild(String paramName) {
		this.paramChild = new RouteNode(paramName);
		return this.paramChild;
	}

	/**
	 * Adds or retrieves a static child node for a literal path segment.
	 *
	 * @param segment static path segment
	 * @return the corresponding child node
	 */
	private RouteNode addStaticChild(String segment) {
		return staticChildren.computeIfAbsent(segment, k -> new RouteNode());
	}

	/**
	 * Retrieves a static child node.
	 *
	 * @param segment static path segment
	 * @return the corresponding child node or null if not present
	 */
	public RouteNode getStaticChild(String segment) {
		return staticChildren.get(segment);
	}

	/**
	 * Return the param name ff the node represents a param segment
	 *
	 * @return {@code String} name of the path parameter associated with this node. {@code null} if the node does not represent a param segment
	 */
	public String getParamName() {
		return this.paramName;
	}

	/**
	 * Registers a route by traversing or creating nodes for each path segment.
	 * <p>
	 * Supported segments:
	 * - Literal segments
	 * - Path parameters ("{param}")
	 * - Single wildcard ("*") — matches exactly one segment
	 * - Double wildcard ("**") — matches zero or more segments
	 *
	 * @param pathSegments path split into segments
	 * @param handler handler to associate with the route
	 * @param filters filters to apply before the handler
	 * @throws JandleSyntaxException if a duplicate route is detected
	 */
	public void registerNode(String[] pathSegments, Handler handler, Filter... filters) {
		RouteNode node = this;

		for (var segment : pathSegments) {
			if (segment.equals("**")) {
				node = node.setDoubleWildcardChild();
				continue;
			}
			if (segment.equals("*")) {
				node = node.setSingleWildCardChild();
				continue;
			}

			if (PathUtil.isPathSegmentParam(segment)) {
				node = node.setParamChild(PathUtil.getParamNameFromSegment(segment));
				continue;
			}

			node = node.addStaticChild(segment);
		}

		if (node.getHandler() != null) throw new JandleSyntaxException("Duplicate route: " + PathUtil.pathFromSegments(pathSegments));

		node.setHandler(handler);
		node.setFilers(filters);
	}

	/**
	 * Recursively searches for a matching route node based on path segments.
	 * <p>
	 * Matching priority:
	 * 1. Static segment
	 * 2. Path parameter
	 * 3. Single wildcard ("*")
	 * 4. Double wildcard ("**")
	 * <p>
	 * Parameter values are collected during traversal.
	 *
	 * @param pathSegments incoming request path segments
	 * @param index current index in the path
	 * @param params collected path parameters
	 * @return a {@link SearchResult} if a matching route is found, otherwise null
	 */

	public SearchResult getNodeAtPath(String[] pathSegments, int index, Map<String, String> params) {
		var paramsCopy = new HashMap<>(params);
		if (index == pathSegments.length) {
			if (handler != null) return new SearchResult(this, paramsCopy);
			return doubleWildCardNodeAtPath(pathSegments, index, paramsCopy);
		}
		SearchResult staticNode = staticNodeAtPath(pathSegments, index, paramsCopy);
		if (staticNode != null) return staticNode;

		SearchResult paramNode = paramNodeAtPath(pathSegments, index, paramsCopy);
		if (paramNode != null) return paramNode;

		SearchResult singleWildcardNode = singleWildCardNodeAtPath(pathSegments, index, paramsCopy);
		if (singleWildcardNode != null) return singleWildcardNode;

		return doubleWildCardNodeAtPath(pathSegments, index, paramsCopy);
	}


	/**
	 * Attempts to resolve the next route node using a static (literal) path segment.
	 *
	 * @param pathSegments full request path split into segments
	 * @param index current segment index
	 * @param paramsCopy current collected path parameters
	 * @return a {@link SearchResult} if a matching static route is found, otherwise null
	 */
	private SearchResult staticNodeAtPath(String[] pathSegments, int index, Map<String, String> paramsCopy) {
		RouteNode staticNode = getStaticChild(pathSegments[index]);
		if (staticNode == null) return null;
		return staticNode.getNodeAtPath(pathSegments, index + 1, paramsCopy);
	}

	/**
	 * Attempts to resolve the next route node using a path parameter.
	 * <p>
	 * If the parameter match fails, the parameter is removed from the map
	 * to preserve correctness during backtracking.
	 *
	 * @param pathSegments full request path split into segments
	 * @param index current segment index
	 * @param paramsCopy current collected path parameters
	 * @return a {@link SearchResult} if a matching parameter route is found, otherwise null
	 */
	private SearchResult paramNodeAtPath(String[] pathSegments, int index, Map<String, String> paramsCopy) {
		if (paramChild == null) return null;

		paramsCopy.put(paramChild.getParamName(), pathSegments[index]);

		var res = paramChild.getNodeAtPath(pathSegments, index + 1, paramsCopy);
		if (res == null) paramsCopy.remove(paramChild.getParamName());
		return res;
	}

	/**
	 * Attempts to resolve the next route node using a single wildcard ("*").
	 * <p>
	 * The single wildcard matches exactly one path segment.
	 *
	 * @param pathSegments full request path split into segments
	 * @param index current segment index
	 * @param paramsCopy current collected path parameters
	 * @return a {@link SearchResult} if a matching wildcard route is found, otherwise null
	 */
	private SearchResult singleWildCardNodeAtPath(String[] pathSegments, int index, Map<String, String> paramsCopy) {
		if (singleWildCardChild == null) return null;
		return singleWildCardChild.getNodeAtPath(pathSegments, index + 1, paramsCopy);
	}

	/**
	 * Attempts to resolve the next route node using a double wildcard ("**").
	 * <p>
	 * The double wildcard matches zero or more path segments and is evaluated
	 * by progressively advancing the path index until a match is found or
	 * all possibilities are exhausted.
	 *
	 * @param pathSegments full request path split into segments
	 * @param index starting segment index
	 * @param paramsCopy current collected path parameters
	 * @return a {@link SearchResult} if a matching wildcard route is found, otherwise null
	 */

	private SearchResult doubleWildCardNodeAtPath(String[] pathSegments, int index, Map<String, String> paramsCopy) {
		if (doubleWildcardChild == null) return null;
		for (int i = index; i <= pathSegments.length; i++) {
			var res = doubleWildcardChild.getNodeAtPath(pathSegments, i, paramsCopy);
			if (res != null) return res;
		}

		return new SearchResult(doubleWildcardChild, paramsCopy);
	}
}
