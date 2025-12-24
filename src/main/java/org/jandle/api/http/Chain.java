package org.jandle.api.http;

import java.io.IOException;

/**
 * Represents a filter execution chain for processing an HTTP request.
 *
 * <p>A {@code Chain} controls the sequential invocation of request filters
 * and the final request handler. Each filter receives the same
 * {@link Request} and {@link Response} instances and may decide whether
 * to continue processing by invoking {@link #next(Request, Response)}.
 *
 * <h2>Execution Model</h2>
 * <ul>
 *   <li>Filters are executed in the order they were registered</li>
 *   <li>Each filter must explicitly call {@link #next(Request, Response)}
 *       to pass control to the next filter or handler</li>
 * </ul>
 *
 * <h2>Short-Circuiting</h2>
 * A filter may terminate processing early by:
 * <ul>
 *   <li>Sending a response directly</li>
 *   <li>Not invoking {@link #next(Request, Response)}</li>
 * </ul>
 *
 * <p>This enables common cross-cutting concerns such as authentication,
 * logging, rate limiting, and request validation.
 *
 * <h2>Error Handling</h2>
 * Implementations may propagate {@link IOException} if an error occurs
 * while advancing the chain or writing the response.
 *
 * <h2>Thread Safety</h2>
 * {@code Chain} instances are not required to be thread-safe and are
 * typically scoped to a single request execution.
 *
 * @see Request
 * @see Response
 */

public interface Chain {
	/**
	 * Continues execution of the chain.
	 *
	 * <p>
	 * If there are remaining filters, the next filter’s
	 * {@code doFilter(...)} method is invoked. Otherwise, control
	 * is passed to the final handler.
	 * </p>
	 *
	 * <p>
	 * Filters are responsible for explicitly calling this method
	 * if they wish to allow the request to proceed.
	 * </p>
	 *
	 * @param request  the current HTTP request
	 * @param response the HTTP response being built
	 *
	 * @throws IOException if an I/O error occurs while processing
	 *                     the request or response
	 */
	void next(Request request, Response response) throws IOException;
}
