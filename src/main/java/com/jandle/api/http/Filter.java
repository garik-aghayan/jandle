package com.jandle.api.http;

import java.io.IOException;

/**
 * Represents a request/response interceptor in the Jandle processing pipeline.
 *
 * <p>
 * A {@code Filter} is executed before the final {@link Handler} and can:
 * </p>
 * <ul>
 *   <li>Inspect or modify the {@link Request} and {@link Response}</li>
 *   <li>Perform cross-cutting concerns (authentication, CORS, rate limiting, logging, etc.)</li>
 *   <li>Decide whether request processing should continue</li>
 * </ul>
 *
 * <p>
 * Filters are executed sequentially in the order they are registered.
 * To continue the chain, the filter must explicitly call
 * {@link Chain#next(Request, Response)}.
 * </p>
 *
 * <p>
 * If {@code chain.next(...)} is not called, request processing stops
 * and the handler will not be executed.
 * </p>
 */
public interface Filter {
	/**
	 * Processes the incoming request and response.
	 *
	 * @param request  the incoming HTTP request
	 * @param response the HTTP response being built
	 * @param filterChain    the filter filterChain used to continue processing
	 * @throws IOException if an I/O error occurs while handling the request
	 */
	void doFilter(Request request, Response response, Chain filterChain) throws IOException;
}
