package com.jandle.api.http;

/**
 * Represents supported HTTP request methods.
 *
 * <p>
 * This enum is used internally by Jandle to route incoming HTTP requests
 * to the appropriate handlers based on their HTTP method.
 * </p>
 */
public enum RequestMethod {
	/** HTTP GET method */
	GET,
	/** HTTP POST method */
	POST,
	/** HTTP PUT method */
	PUT,
	/** HTTP PATCH method */
	PATCH,
	/** HTTP HEAD method */
	HEAD,
	/** HTTP OPTIONS method */
	OPTIONS,
	/** HTTP DELETE method */
	DELETE;

	/**
	 * Converts a string representation of an HTTP method
	 * into a {@link RequestMethod}.
	 *
	 * @param s the HTTP method as a string (e.g. {@code "GET"})
	 * @return the corresponding {@code RequestMethod}
	 *
	 * @throws IllegalArgumentException if the method is not supported
	 */
	public static RequestMethod fromString(String s) throws IllegalArgumentException {
		return switch (s.toUpperCase()) {
			case "GET" -> GET;
			case "POST" -> POST;
			case "PUT" -> PUT;
			case "PATCH" -> PATCH;
			case "HEAD" -> HEAD;
			case "OPTIONS" -> OPTIONS;
			case "DELETE" -> DELETE;
			default -> throw new IllegalArgumentException("Invalid RequestMethod: " + s);
		};
	}
}
