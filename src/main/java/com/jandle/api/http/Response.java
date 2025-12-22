package com.jandle.api.http;

import com.jandle.api.cookies.ResponseCookie;
import com.sun.net.httpserver.Headers;

import java.io.IOException;

public interface Response {
	/**
	 * Opens the response for streaming (chunked transfer encoding).
	 *
	 * <p>This method sends the response headers immediately with
	 * {@code Content-Length = 0}, allowing an arbitrary number of
	 * {@link #stream(byte[])} calls.
	 *
	 * @throws IOException if the stream is already open or headers cannot be sent
	 */
	void openStream() throws IOException;

	/**
	 * Writes a chunk of data to the open response stream.
	 *
	 * <p>This method may only be used after {@link #openStream()} and before
	 * {@link #closeStream()}.
	 *
	 * @param chunk the byte array to write
	 * @throws IOException if an I/O error occurs
	 * @throws IllegalStateException if the stream is not open or a fixed content length was set
	 */
	void stream(byte[] chunk) throws IOException;

	/**
	 * Flushes the underlying output stream if streaming is active.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	void flushStream() throws IOException;

	/**
	 * Closes the streaming response.
	 *
	 * <p>After this method is called, no further data may be written.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	void closeStream() throws IOException;

	/**
	 * Sends a fixed-length binary response.
	 *
	 * <p>This method automatically sets the correct content length and
	 * commits the response.
	 *
	 * @param bytes the response body
	 * @throws IOException if the response has already been sent or an I/O error occurs
	 */
	void sendBytes(byte[] bytes) throws IOException;

	/**
	 * Sends the HTTP response headers using the provided content length.
	 *
	 * <p>This method commits the response and must be called exactly once.
	 *
	 * @param contentLength the content length
	 * @throws IOException if the response was already sent
	 */
	void sendHeaders(long contentLength) throws IOException;

	/**
	 * Sends the HTTP response headers using the internally configured content length.
	 *
	 * @throws IOException if the response was already sent
	 */
	void sendHeaders() throws IOException;

	/**
	 * Sends a plain-text response using UTF-8 encoding.
	 *
	 * @param message the response body
	 * @throws IOException if the response was already sent or an I/O error occurs
	 */
	void sendText(String message) throws IOException;

	/**
	 * Sends a JSON response serialized using Gson.
	 *
	 * @param src the object to serialize
	 * @throws IOException if the response was already sent or an I/O error occurs
	 */
	void sendJson(Object src) throws IOException;

	/**
	 * Sends a response with no body and the given HTTP status code.
	 *
	 * @param statusCode the HTTP status code
	 * @throws IOException if the response was already sent
	 */
	void sendStatus(int statusCode) throws IOException;

	/**
	 * Sends an HTTP redirect with the specified status code and location.
	 *
	 * @param statusCode the redirect status code (e.g. 301, 302, 307)
	 * @param location the target location
	 * @throws IOException if the response was already sent
	 */
	void redirect(int statusCode, String location) throws IOException;

	/**
	 * Sends a temporary (302) redirect to the given location.
	 *
	 * @param location the target location
	 * @throws IOException if the response was already sent
	 */
	void redirect(String location) throws IOException;

	/**
	 * Sets or replaces a response header.
	 *
	 * @param key the header name
	 * @param valueList the header values
	 * @return this response for chaining
	 */
	Response header(String key, String... valueList);

	/**
	 * Returns a copy of the current response headers.
	 * <p>
	 * Modifications to the returned {@link Headers} object will not affect
	 * the actual headers sent in the HTTP response.
	 *
	 * @return a {@link Headers} object containing all the current headers.
	 */
	Headers getHeaders();

	/**
	 * Sets or removes the {@code Content-Type} header.
	 *
	 * @param contentType the content type, or {@code null} to remove it
	 * @return this response for chaining
	 */
	Response contentType(String contentType);

	/**
	 * Sets the content length for the response.
	 *
	 * <p>Passing a 0 switches the response to chunked mode.
	 * <p>Passing a negative value removes the header "Content-Length".
	 *
	 * @param contentLength the content length
	 * @return this response for chaining
	 */
	Response contentLength(long contentLength);

	/**
	 * Sets the HTTP status code for the response.
	 *
	 * @param statusCode the HTTP status code
	 * @return this response for chaining
	 */
	Response status(int statusCode);

	/**
	 * Adds a {@code Set-Cookie} header to the response.
	 *
	 * @param cookie the response cookie
	 * @return this response for chaining
	 */
	Response cookie(ResponseCookie cookie);

	/**
	 * Adds a value to the {@code Vary} response header if not already present.
	 *
	 * @param value the header value to add
	 * @return this response for chaining
	 */
	Response vary(String value);
}
