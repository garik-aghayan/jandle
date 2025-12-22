package com.jandle.internal.http;

import com.jandle.api.http.Response;
import com.jandle.api.cookies.ResponseCookie;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents an HTTP response abstraction built on top of {@link HttpExchange}.
 *
 * <p>This class provides a high-level, safe API for:
 * <ul>
 *   <li>Sending fixed-length responses</li>
 *   <li>Streaming (chunked) responses</li>
 *   <li>Managing headers, status codes, cookies, redirects</li>
 *   <li>Preventing illegal response state transitions</li>
 * </ul>
 *
 * <p>The response can be sent exactly once. Any attempt to modify headers
 * or send the response after it has been committed will result in an
 * {@link IllegalStateException}.
 */
public final class HttpResponse implements Response {
	private static final Gson gson = new GsonBuilder().serializeNulls().create();
	private final HttpExchange exchange;
	private final Headers headers;
	private boolean isSent = false;
	private int statusCode = 200;
	private long contentLength = -1;
	private OutputStream outputStream;

	/**
	 * Creates a new {@code HttpResponse} bound to the given {@link HttpExchange}.
	 *
	 * @param exchange the underlying HTTP exchange
	 */
	public HttpResponse(HttpExchange exchange) {
		this.exchange = exchange;
		this.headers = exchange.getResponseHeaders();
	}

	/**
	 * Opens the response for streaming (chunked transfer encoding).
	 *
	 * <p>This method sends the response headers immediately with
	 * {@code Content-Length = 0}, allowing an arbitrary number of
	 * {@link #stream(byte[])} calls.
	 *
	 * @throws IOException if the stream is already open or headers cannot be sent
	 */
	public void openStream() throws IOException {
		if (outputStream != null) throw new IOException("Stream is already open");
		contentLength(0);
		sendHeaders();
		outputStream = exchange.getResponseBody();
	}

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
	public void stream(byte[] chunk) throws IOException {
		if (outputStream == null) throw new IllegalStateException("Stream is not open");
		if (contentLength != 0) throw new IllegalStateException("contentLength changed after opening the stream");
		outputStream.write(chunk);
	}

	/**
	 * Flushes the underlying output stream if streaming is active.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void flushStream() throws IOException {
		if (outputStream != null) outputStream.flush();
	}

	/**
	 * Closes the streaming response.
	 *
	 * <p>After this method is called, no further data may be written.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public void closeStream() throws IOException {
		contentLength(-1);
		if (outputStream != null) {
			outputStream.close();
			outputStream = null;
		}
	}

	/**
	 * Sends a fixed-length binary response.
	 *
	 * <p>This method automatically sets the correct content length and
	 * commits the response.
	 *
	 * @param bytes the response body
	 * @throws IOException if the response has already been sent or an I/O error occurs
	 */
	public void sendBytes(byte[] bytes) throws IOException {
		sendHeaders(bytes.length);
		try(OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}

	/**
	 * Sends the HTTP response headers using the provided content length.
	 *
	 * <p>This method commits the response and must be called exactly once.
	 *
	 * @param contentLength the content length
	 * @throws IOException if the response was already sent
	 */
	public void sendHeaders(long contentLength) throws IOException {
		contentLength(contentLength);
		assertNotSent();
		markSent();
		exchange.sendResponseHeaders(statusCode, contentLength);
	}

	/**
	 * Sends the HTTP response headers using the internally configured content length.
	 *
	 * @throws IOException if the response was already sent
	 */
	public void sendHeaders() throws IOException {
		sendHeaders(contentLength);
	}

	/**
	 * Sends a plain-text response using UTF-8 encoding.
	 *
	 * @param message the response body
	 * @throws IOException if the response was already sent or an I/O error occurs
	 */
	public void sendText(String message) throws IOException {
		assertNotSent();
		contentType("text/plain; charset=utf-8");
		sendBytes(message.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Sends a JSON response serialized using Gson.
	 *
	 * @param src the object to serialize
	 * @throws IOException if the response was already sent or an I/O error occurs
	 */
	public void sendJson(Object src) throws IOException {
		assertNotSent();
		contentType("application/json; charset=utf-8");
		sendBytes(gson.toJson(src).getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Sends a response with no body and the given HTTP status code.
	 *
	 * @param statusCode the HTTP status code
	 * @throws IOException if the response was already sent
	 */
	public void sendStatus(int statusCode) throws IOException {
		status(statusCode);
		contentType(null);
		sendHeaders();
	}

	/**
	 * Sends an HTTP redirect with the specified status code and location.
	 *
	 * @param statusCode the redirect status code (e.g. 301, 302, 307)
	 * @param location the target location
	 * @throws IOException if the response was already sent
	 */
	public void redirect(int statusCode, String location) throws IOException {
		status(statusCode);
		header("Location", location);
		sendHeaders();
	}

	/**
	 * Sends a temporary (302) redirect to the given location.
	 *
	 * @param location the target location
	 * @throws IOException if the response was already sent
	 */
	public void redirect(String location) throws IOException {
		redirect(302, location);
	}

	/**
	 * Sets or replaces a response header.
	 *
	 * @param key the header name
	 * @param valueList the header values
	 * @return this response for chaining
	 */
	public HttpResponse header(String key, String... valueList) {
		headers.put(key, Arrays.stream(valueList).toList());
		return this;
	}

	/**
	 * Returns a copy of the current response headers.
	 * <p>
	 * Modifications to the returned {@link Headers} object will not affect
	 * the actual headers sent in the HTTP response.
	 *
	 * @return a {@link Headers} object containing all the current headers.
	 */
	public Headers getHeaders() {
		Headers headersCopy = new Headers();
		headersCopy.putAll(headers);
		return headersCopy;
	}

	/**
	 * Sets or removes the {@code Content-Type} header.
	 *
	 * @param contentType the content type, or {@code null} to remove it
	 * @return this response for chaining
	 */
	public HttpResponse contentType(String contentType) {
		if (contentType == null) headers.remove("Content-Type");
		else header("Content-Type", contentType);

		return this;
	}

	/**
	 * Sets the content length for the response.
	 *
	 * <p>Passing a 0 switches the response to chunked mode.
	 * <p>Passing a negative value removes the header "Content-Length".
	 *
	 * @param contentLength the content length
	 * @return this response for chaining
	 */
	public HttpResponse contentLength(long contentLength) {
		this.contentLength = contentLength;
		if (contentLength >= 0) {
			header("Content-Length", String.valueOf(contentLength));
		}
		else {
			headers.remove("Content-Length");
		}
		return this;
	}

	/**
	 * Sets the HTTP status code for the response.
	 *
	 * @param statusCode the HTTP status code
	 * @return this response for chaining
	 */
	public HttpResponse status(int statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	/**
	 * Adds a {@code Set-Cookie} header to the response.
	 *
	 * @param cookie the response cookie
	 * @return this response for chaining
	 */
	public HttpResponse cookie(ResponseCookie cookie) {
		headers.computeIfAbsent("Set-Cookie", k -> new ArrayList<>()).add(cookie.toString());
		return this;
	}

	/**
	 * Adds a value to the {@code Vary} response header if not already present.
	 *
	 * @param value the header value to add
	 * @return this response for chaining
	 */
	public HttpResponse vary(String value) {
		String existing = headers.getFirst("Vary");
		if (existing == null) {
			headers.add("Vary", value);
			return this;
		}

		for (String v : existing.split(",")) {
			if (v.trim().equalsIgnoreCase(value)) return this;
		}

		headers.set("Vary", existing + ", " + value);

		return this;
	}

	/**
	 * Ensures the response has not yet been sent.
	 *
	 * @throws IllegalStateException if the response was already committed
	 */
	private void assertNotSent() {
		if (isSent) throw new IllegalStateException("HttpResponse already sent");
	}

	/**
	 * Marks the response as committed.
	 *
	 * <p>Once called, headers and body may no longer be modified.
	 */
	private void markSent() {
		isSent = true;
	}
}
