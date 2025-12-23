package org.jandle.api.http;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public interface Request {
	/**
	 * Returns the request path.
	 *
	 * @return {@code String}
	 */
	String getPath();

	/**
	 * Returns the HTTP method
	 *
	 * @return {@code RequestMethod}
	 */

	RequestMethod getMethod();

	/**
	 * Returns the remote client address.
	 *
	 * @return {@code InetSocketAddress}
	 */
	InetSocketAddress getRemoteAddress();

	/**
	 * Returns the local server address.
	 *
	 * @return {@code InetSocketAddress}
	 */
	InetSocketAddress getLocalAddress();

	/**
	 * Returns an immutable copy of path parameters.
	 *
	 * @return {@code Map<String, String>}
	 */
	Map<String, String> getParams();

	/**
	 * Returns a path parameter by name.
	 *
	 * @param name {@code String} name of the path parameter
	 *
	 * @return {@code String} if the param is found, otherwise {@code null}
	 */
	String getParam(String name);

	/**
	 * Checks if a path parameter exists.
	 *
	 * @param name {@code String} name of the path parameter
	 *
	 * @return {@code true} if path parameter exists, otherwise {@code false}
	 */
	boolean hasParam(String name);

	/**
	 * Returns a copy of query parameters map.
	 *
	 * @return {@code Map<String, List<String>>}
	 */

	Map<String, List<String>> getQueryMap();

	/**
	 * Returns all values for a query parameter.
	 *
	 * @param name {@code String} name of the query parameter
	 *
	 * @return {@code List<String} if the query param is found, otherwise {@code null}
	 */
	List<String> getQueryParam(String name);

	/**
	 * Returns the first value of a query parameter.
	 *
	 * @param name {@code String} name of the query parameter
	 *
	 * @return {@code String} if the query param is a non-empty list, otherwise {@code null}
	 */
	String getQueryParamFirst(String name);

	/**
	 * Checks if a query parameter exists.
	 *
	 * @param name {@code String} name of the query parameter
	 *
	 * @return {@code boolean}
	 */
	boolean hasQueryParam(String name);

	/**
	 * Returns the raw query string.
	 *
	 * @return {@code String}
	 */
	String getQueryString();

	/**
	 * Returns all values of the header if exists.
	 *
	 * @param name {@code String} name of the header
	 *
	 * @return {@code List<String} if the header is found, otherwise {@code null}
	 */
	List<String> getHeader(String name);

	/**
	 * Returns the first value of the header if exists.
	 *
	 * @param name {@code String} name of the header
	 *
	 * @return {@code String} if the header is a non-empty list, otherwise {@code null}
	 */
	String getHeaderFirst(String name);

	/**
	 * Checks if the header exists.
	 *
	 * @param name {@code String} name of the header
	 *
	 * @return {@code boolean}
	 */
	boolean hasHeader(String name);

	/**
	 * Returns the value of a cookie.
	 *
	 * @param name {@code String} name of the cookie
	 *
	 * @return {@code String} if the cookie is found, otherwise {@code null}
	 */
	String getCookie(String name);

	/**
	 * Checks if a cookie exists.
	 *
	 * @param name {@code String} name of the cookie
	 *
	 * @return {@code boolean}
	 */
	boolean hasCookie(String name);

	/**
	 * Returns a custom request attribute.
	 *
	 * @param name {@code String} name of the attribute
	 *
	 * @return {@code Object} if the attribute is found, otherwise {@code null}
	 */
	Object getAttribute(Object name);

	/**
	 * Sets a custom request attribute.
	 * @param name {@code String} name of the attribute
	 * @param value {@code Object} value associated with the {@code name}
	 */
	void setAttribute(Object name, Object value);

	/**
	 * Returns a clone of the request body bytes.
	 *
	 * @return {@code byte[]}
	 */
	byte[] getBodyBytes();

	/**
	 * Returns the request body as parsed JSON.
	 * Returns an empty Map if Content-Type is not JSON.
	 *
	 * @return {@code Map<String, Object>}
	 */
	Map<String, Object> getBodyJson();
}
