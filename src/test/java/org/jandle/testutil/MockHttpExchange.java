package org.jandle.testutil;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class MockHttpExchange extends HttpExchange {

	private final Headers requestHeaders = new Headers();
	private final Headers responseHeaders = new Headers();

	private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
	private InputStream requestBody = InputStream.nullInputStream();

	private int statusCode = -1;
	private final String method;
	private final URI uri;

	/* ---------- Constructors ---------- */

	public MockHttpExchange() {
		this("GET", "/");
	}

	public MockHttpExchange(String method, String path) {
		this.method = method;
		this.uri = URI.create(path);
	}

	public MockHttpExchange(String method, String path, byte[] body) {
		this(method, path);
		this.requestBody = new ByteArrayInputStream(body);
	}

	/* ---------- HttpExchange overrides ---------- */

	@Override
	public Headers getRequestHeaders() {
		return requestHeaders;
	}

	@Override
	public Headers getResponseHeaders() {
		return responseHeaders;
	}

	@Override
	public URI getRequestURI() {
		return uri;
	}

	@Override
	public String getRequestMethod() {
		return method;
	}

	@Override
	public InputStream getRequestBody() {
		return requestBody;
	}

	@Override
	public OutputStream getResponseBody() {
		return responseBody;
	}

	@Override
	public void sendResponseHeaders(int rCode, long responseLength) {
		this.statusCode = rCode;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return new InetSocketAddress("localhost", 12345);
	}

	@Override
	public int getResponseCode() {
		return statusCode;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return new InetSocketAddress("localhost", 8000);
	}

	@Override
	public void close() {
		// no-op
	}

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	public void setAttribute(String name, Object value) {
		// no-op
	}

	@Override
	public void setStreams(InputStream i, OutputStream o) {
		this.requestBody = i;
	}

	@Override
	public HttpPrincipal getPrincipal() {
		return null;
	}

	@Override
	public String getProtocol() {
		return "HTTP/1.1";
	}

	@Override
	public com.sun.net.httpserver.HttpContext getHttpContext() {
		return null;
	}

	/* ---------- Test helpers ---------- */

	public int getStatus() {
		return statusCode;
	}

	public String getBodyAsString() {
		return responseBody.toString(StandardCharsets.UTF_8);
	}

	public byte[] getBodyAsBytes() {
		return responseBody.toByteArray();
	}
}
