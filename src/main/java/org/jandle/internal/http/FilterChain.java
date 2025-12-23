package org.jandle.internal.http;

import org.jandle.api.http.*;

import java.io.IOException;

/**
 * Represents the execution chain of filters leading to a final handler.
 *
 * <p>The chain invokes filters sequentially. Each filter must call
 * {@link #next(Request, Response)} to continue execution.
 * When all filters have been executed, the target handler is invoked.</p>
 *
 * <p>This class is not thread-safe and is created per request.</p>
 */
public final class FilterChain implements Chain {
	/**
	 * Ordered list of filters to be executed before the handler.
	 * May be empty but is never {@code null}.
	 */
	private final Filter[] filters;
	/**
	 * The final request handler invoked after all filters have run.
	 */
	private final Handler handler;
	/**
	 * Current position in the filter chain.
	 */
	private int index = 0;

	/**
	 * Creates a new execution chain.
	 *
	 * <p>
	 * If the provided filter array is {@code null}, an empty filter list
	 * is used, and the request is immediately passed to the handler.
	 * </p>
	 *
	 * @param filters the filters to execute in order
	 * @param handler the handler to invoke after all filters have executed
	 */
	public FilterChain(Filter[] filters, Handler handler) {
		this.filters = filters == null ? new Filter[]{} : filters;
		this.handler = handler;
	}

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
	public void next(Request request, Response response) throws IOException {
		if (index < filters.length) {
			Filter filter = filters[index++];
			filter.doFilter(request, response, this);
		}
		else {
			handler.handle(request, response);
		}
	}
}
