package org.jandle.api.http;

import java.io.IOException;

/**
 * Represents a final request handler responsible for producing a response.
 *
 * <p>
 * A {@code Handler} is invoked after all registered {@link Filter}s
 * have been executed.
 * </p>
 *
 * <p>
 * It is responsible for:
 * </p>
 * <ul>
 *   <li>Processing the {@link Request}</li>
 *   <li>Writing the response using {@link Response}</li>
 *   <li>Determining status codes, headers, and response body</li>
 * </ul>
 *
 * <p>
 * Each handler is typically mapped to a specific HTTP method and path
 * using annotations such as {@code @HttpRequestHandler}.
 * </p>
 */
public interface Handler {
	/**
	 * Handles the incoming request and generates a response.
	 *
	 * @param request  the incoming HTTP request
	 * @param response the HTTP response to send
	 * @throws IOException if an I/O error occurs while writing the response
	 */
	void handle(Request request, Response response) throws IOException;
}
