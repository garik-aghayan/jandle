package com.jandle.internal.http;

import com.google.gson.Gson;
import com.jandle.api.http.RequestMethod;
import com.jandle.api.http.Request;
import com.sun.net.httpserver.Headers;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an HTTP request and encapsulates all its data.
 * <p>
 * Provides methods to access path, HTTP method, headers, query parameters, cookies,
 * request body (both as raw bytes and JSON), and custom attributes.
 * </p>
 */

public final class HttpRequest implements Request {
	private final static Gson gson = new Gson();
	private final String path;
	private final RequestMethod method;
	private final InetSocketAddress remoteAddress;
	private final InetSocketAddress localAddress;
	private final Map<String, String> params;
	private final Map<String, List<String>> queryMap;
	private final String queryString;
	private final Map<String, List<String>> headers;
	private final Map<String, String> cookies;
	private final byte[] requestBodyBytes;
	private Map<String, Object> requestBodyJson;
	private final Map<Object, Object> attributes;

	/**
	 * Constructs a new HttpRequest object.
	 *
	 * @param path          request path
	 * @param method        HTTP method
	 * @param remoteAddress client IP and port
	 * @param localAddress  server IP and port
	 * @param params        path parameters
	 * @param query         raw query string
	 * @param headers       HTTP headers
	 * @param requestBodyBytes raw request body bytes
	 * @throws IllegalArgumentException if path is null
	 */
	public HttpRequest(
			String path,
			RequestMethod method,
			InetSocketAddress remoteAddress,
			InetSocketAddress localAddress,
			Map<String, String> params,
			String query,
			Headers headers,
			byte[] requestBodyBytes
	) throws IllegalArgumentException {
		if (path == null) throw new IllegalArgumentException("RequestData path cannot be null");
		this.path = path;
		this.method = method;
		this.remoteAddress = remoteAddress;
		this.localAddress = localAddress;
		this.params = params == null ? Map.of() : new HashMap<>(params);
		this.queryString = query == null ? "" : query;
		this.queryMap = initQueryMap();
		this.headers = getNormalizedHeaders(headers);
		this.cookies = getCookiesMapFromRawCookies(getHeaderFirst("Cookie"));
		this.requestBodyBytes = requestBodyBytes;
		this.attributes = new ConcurrentHashMap<>();
	}

	/**
	 * Returns the request path.
	 *
	 * @return {@code String}
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the HTTP method
	 *
	 * @return {@code RequestMethod}
	 */

	public RequestMethod getMethod() {
		return method;
	}

	/**
	 * Returns the remote client address.
	 *
	 * @return {@code InetSocketAddress}
	 */
	public InetSocketAddress getRemoteAddress() {
		if (remoteAddress == null) return null;
		return InetSocketAddress.createUnresolved(remoteAddress.getHostString(), remoteAddress.getPort());
	}

	/**
	 * Returns the local server address.
	 *
	 * @return {@code InetSocketAddress}
	 */
	public InetSocketAddress getLocalAddress() {
		return InetSocketAddress.createUnresolved(localAddress.getHostString(), localAddress.getPort());
	}

	/**
	 * Returns an immutable copy of path parameters.
	 *
	 * @return {@code Map<String, String>}
	 */
	public Map<String, String> getParams() {
		return Map.copyOf(params);
	}

	/**
	 * Returns a path parameter by name.
	 *
	 * @param name {@code String} name of the path parameter
	 *
	 * @return {@code String} if the param is found, otherwise {@code null}
	 */
	public String getParam(String name) {
		return params.get(name);
	}

	/**
	 * Checks if a path parameter exists.
	 *
	 * @param name {@code String} name of the path parameter
	 *
	 * @return {@code true} if path parameter exists, otherwise {@code false}
	 */
	public boolean hasParam(String name) {
		return params.containsKey(name);
	}

	/**
	 * Returns a copy of query parameters map.
	 *
	 * @return {@code Map<String, List<String>>}
	 */

	public Map<String, List<String>> getQueryMap() {
		return new HashMap<>(queryMap);
	}

	/**
	 * Returns all values for a query parameter.
	 *
	 * @param name {@code String} name of the query parameter
	 *
	 * @return {@code List<String} if the query param is found, otherwise {@code null}
	 */
	public List<String> getQueryParam(String name) {
		return queryMap.get(name);
	}

	/**
	 * Returns the first value of a query parameter.
	 *
	 * @param name {@code String} name of the query parameter
	 *
	 * @return {@code String} if the query param is a non-empty list, otherwise {@code null}
	 */
	public String getQueryParamFirst(String name) {
		var paramList = getQueryParam(name);
		if (paramList == null || paramList.isEmpty()) return null;
		return paramList.getFirst();
	}

	/**
	 * Checks if a query parameter exists.
	 *
	 * @param name {@code String} name of the query parameter
	 *
	 * @return {@code boolean}
	 */
	public boolean hasQueryParam(String name) {
		return queryMap.containsKey(name);
	}

	/**
	 * Returns the raw query string.
	 *
	 * @return {@code String}
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * Returns all values of the header if exists.
	 *
	 * @param name {@code String} name of the header
	 *
	 * @return {@code List<String} if the header is found, otherwise {@code null}
	 */
	public List<String> getHeader(String name) {
		return headers.get(name.toLowerCase());
	}

	/**
	 * Returns the first value of the header if exists.
	 *
	 * @param name {@code String} name of the header
	 *
	 * @return {@code String} if the header is a non-empty list, otherwise {@code null}
	 */
	public String getHeaderFirst(String name) {
		var headerList = getHeader(name);
		if (headerList == null || headerList.isEmpty()) return null;
		return headerList.getFirst();
	}

	/**
	 * Checks if the header exists.
	 *
	 * @param name {@code String} name of the header
	 *
	 * @return {@code boolean}
	 */
	public boolean hasHeader(String name) {
		return headers.containsKey(name.toLowerCase());
	}

	/**
	 * Returns the value of a cookie.
	 *
	 * @param name {@code String} name of the cookie
	 *
	 * @return {@code String} if the cookie is found, otherwise {@code null}
	 */
	public String getCookie(String name) {return cookies.get(name);}

	/**
	 * Checks if a cookie exists.
	 *
	 * @param name {@code String} name of the cookie
	 *
	 * @return {@code boolean}
	 */
	public boolean hasCookie(String name) {
		return cookies.containsKey(name);
	}

	/**
	 * Returns a custom request attribute.
	 *
	 * @param name {@code String} name of the attribute
	 *
	 * @return {@code Object} if the attribute is found, otherwise {@code null}
	 */
	public Object getAttribute(Object name) {
		return attributes.get(name);
	}

	/**
	 * Sets a custom request attribute.
	 * @param name {@code String} name of the attribute
	 * @param value {@code Object} value associated with the {@code name}
	 */
	public void setAttribute(Object name, Object value) {
		this.attributes.put(name, value);
	}

	/**
	 * Returns a clone of the request body bytes.
	 *
	 * @return {@code byte[]}
	 */
	public byte[] getBodyBytes() {
		if (requestBodyBytes == null) return null;
		return requestBodyBytes.clone();
	}

	/**
	 * Returns the request body as parsed JSON.
	 * Returns an empty Map if Content-Type is not JSON.
	 *
	 * @return {@code Map<String, Object>}
	 */
	public Map<String, Object> getBodyJson() {
		if (requestBodyBytes == null) return null;
		if (requestBodyJson != null) return requestBodyJson;
		String contentType = getHeaderFirst("Content-Type");
		if (contentType == null || !contentType.toLowerCase().startsWith("application/json")) {
			return Map.of();
		}
		try {
			requestBodyJson = gson.fromJson(new String(requestBodyBytes, StandardCharsets.UTF_8), Map.class);
			return requestBodyJson;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid JSON request body", e);
		}
	}

	/**
	 * Returns a string representation of the request.
	 *
	 * @return {@code String}
	 */
	@Override
	public String toString() {
		return "{\n\tURI: " + path + "?" + queryString +
				",\n\t HttpRequest Method: " + method +
				",\n\t Params: " + params +
				",\n\t Remote Address: " + (remoteAddress == null ? "null" : remoteAddress.getHostString()) +
				"\n\t}";
	}

	/**
	 * Parses the raw query string and returns a map of query parameter names to their values.
	 * Multiple values for the same parameter are stored as a list.
	 * <p>
	 * If {@code queryString} is null or a blank string returns an empty map.
	 *
	 * @return {@code Map<String, List<String>>} where the keys are decoded parameter names and the values are lists of decoded parameter values.
	 */
	private Map<String, List<String>> initQueryMap() {
		Map<String, List<String>> queryMap = new LinkedHashMap<>();
		if (queryString == null || queryString.isBlank()) return queryMap;
		String[] entries = queryString.split("&");
		for (var entry : entries) {
			entry = entry.trim();
			int indexOfSeparator = entry.indexOf('=');
			String key = indexOfSeparator >= 0 ? entry.substring(0, indexOfSeparator) : entry;
			String value = indexOfSeparator >= 0 ? entry.substring(indexOfSeparator + 1) : null;
			value = value != null ? URLDecoder.decode(value, StandardCharsets.UTF_8) : null;
			queryMap.computeIfAbsent(URLDecoder.decode(key, StandardCharsets.UTF_8), k -> new ArrayList<>()).add(value);
		}
		return queryMap;
	}

	/**
	 * Parses the "Cookie" header into a map of cookie names to their values.
	 * Decodes both names and values using UTF-8.
	 * <p>
	 * If {@code rawCookies} is null or a blank string returns an empty map.
	 *
	 * @param rawCookies the raw cookie header string
	 * @return {@code Map<String, String>} of cookie names to values
	 */
	private Map<String, String> getCookiesMapFromRawCookies(String rawCookies) {
		Map<String, String> map = new HashMap<>();
		if (rawCookies == null || rawCookies.isBlank()) return map;
		for (String cookie : rawCookies.split(";")) {
			cookie = cookie.trim();
			int indexOfSeparator = cookie.indexOf('=');
			if (indexOfSeparator > 0) {
				String key = URLDecoder.decode(cookie.substring(0, indexOfSeparator), StandardCharsets.UTF_8);
				String value = indexOfSeparator < cookie.length() - 1 ? URLDecoder.decode(cookie.substring(indexOfSeparator + 1), StandardCharsets.UTF_8) : null;
				map.put(key, value);
			}
		}
		return map;
	}

	/**
	 * Converts the provided Headers object into a normalized map with lowercase keys.
	 * This ensures case-insensitive access to HTTP headers.
	 *<p>
	 * If {@code headers} is null returns an empty map.
	 *
	 * @param headers the original Headers object from the HTTP request
	 * @return {@code Map<String, List<String>>} where keys are lowercase header names and values are lists of header values
	 */
	private Map<String, List<String>> getNormalizedHeaders(Headers headers) {
		Map<String, List<String>> normalizedHeaders = new HashMap<>();
		if (headers == null) return normalizedHeaders;
		for (var entry : headers.entrySet()) {
			normalizedHeaders.put(entry.getKey().toLowerCase(), entry.getValue());
		}
		return normalizedHeaders;
	}
}
