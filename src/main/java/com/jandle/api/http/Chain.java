package com.jandle.api.http;

import java.io.IOException;

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
