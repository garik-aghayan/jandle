package com.jandle.cors;

import com.jandle.api.cors.Cors;
import com.jandle.api.cors.CorsConfig;
import com.jandle.api.http.Filter;
import com.jandle.internal.http.FilterChain;
import com.jandle.internal.http.HttpRequest;
import com.jandle.api.http.RequestMethod;
import com.jandle.internal.http.HttpResponse;
import com.jandle.testutil.MockHttpExchange;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CorsTest {

	private HttpRequest request(
			Headers headers,
			RequestMethod method
	) {
		return new HttpRequest(
				"/",
				method,
				new InetSocketAddress("127.0.0.1", 1111),
				new InetSocketAddress("127.0.0.1", 8080),
				Map.of(),
				"",
				headers,
				new byte[0]
		);
	}

	private FilterChain chain(AtomicBoolean called) {
		return new FilterChain(
				new Filter[0],
				(req, res) -> called.set(true)
		);
	}

	/* ---------- same-origin ---------- */

	@Test
	void sameOrigin_bypassesCors() throws IOException {
		Cors cors = new Cors(new CorsConfig());
		AtomicBoolean chainCalled = new AtomicBoolean(false);

		Headers headers = new Headers();
		headers.add("Origin", "http://localhost");
		headers.add("Host", "localhost");

		HttpRequest req = request(headers, RequestMethod.GET);
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse res = new HttpResponse(exchange);

		cors.doFilter(req, res, chain(chainCalled));

		assertTrue(chainCalled.get());
		assertEquals(-1, exchange.getResponseCode());
	}

	/* ---------- origin validation ---------- */

	@Test
	void disallowedOrigin_returns403() throws IOException {
		CorsConfig config = new CorsConfig();
		config.setAllowedOrigins(Set.of("https://allowed.com"));

		Cors cors = new Cors(config);

		Headers headers = new Headers();
		headers.add("Origin", "https://evil.com");

		HttpRequest req = request(headers, RequestMethod.GET);
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse res = new HttpResponse(exchange);

		cors.doFilter(req, res, chain(new AtomicBoolean()));

		assertEquals(403, exchange.getResponseCode());
	}

	/* ---------- simple CORS ---------- */

	@Test
	void allowedOrigin_setsHeaders() throws IOException {
		CorsConfig config = new CorsConfig();
		config.setAllowedOrigins(Set.of("https://example.com"));
		config.setAllowCredentials(true);

		Cors cors = new Cors(config);

		Headers headers = new Headers();
		headers.add("Origin", "https://example.com");

		HttpRequest req = request(headers, RequestMethod.GET);
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse res = new HttpResponse(exchange);

		cors.doFilter(req, res, chain(new AtomicBoolean()));

		Headers resHeaders = exchange.getResponseHeaders();
		assertEquals(
				"https://example.com",
				resHeaders.getFirst("Access-Control-Allow-Origin")
		);
		assertEquals(
				"true",
				resHeaders.getFirst("Access-Control-Allow-Credentials")
		);
		assertTrue(resHeaders.get("Vary").contains("Origin"));
	}

	@Test
	void wildcardWithCredentials_throws() {
		CorsConfig config = new CorsConfig();
		config.setAllowedOrigins(Set.of("*"));
		config.setAllowCredentials(true);

		Cors cors = new Cors(config);

		Headers headers = new Headers();
		headers.add("Origin", "https://x.com");

		HttpRequest req = request(headers, RequestMethod.GET);
		HttpResponse res = new HttpResponse(new MockHttpExchange());

		assertThrows(IllegalStateException.class, () ->
				cors.doFilter(req, res, chain(new AtomicBoolean()))
		);
	}

	/* ---------- preflight ---------- */

	@Test
	void validPreflight_returns204() throws IOException {
		CorsConfig config = new CorsConfig();
		config.setAllowedOrigins(Set.of("https://example.com"));
		config.setAllowedMethods(Set.of(RequestMethod.GET, RequestMethod.POST));
		config.setAllowedHeaders(Set.of("*"));
		config.setMaxAgeSeconds(600);

		Cors cors = new Cors(config);

		Headers headers = new Headers();
		headers.add("Origin", "https://example.com");
		headers.add("Access-Control-HttpRequest-Method", "POST");
		headers.add("Access-Control-HttpRequest-Headers", "Authorization, X-Test");

		HttpRequest req = request(headers, RequestMethod.OPTIONS);
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse res = new HttpResponse(exchange);
		cors.doFilter(req, res, chain(new AtomicBoolean()));
		assertEquals(204, exchange.getResponseCode());
		assertTrue(
				exchange.getResponseHeaders()
						.getFirst("Access-Control-Allow-Methods")
						.contains("POST")
		);
		assertEquals(
				"600",
				exchange.getResponseHeaders()
						.getFirst("Access-Control-Max-Age")
		);
	}

	@Test
	void preflight_invalidMethod_returns403() throws IOException {
		CorsConfig config = new CorsConfig();
		config.setAllowedOrigins(Set.of("https://example.com"));
		config.setAllowedMethods(Set.of(RequestMethod.GET));

		Cors cors = new Cors(config);

		Headers headers = new Headers();
		headers.add("Origin", "https://example.com");
		headers.add("Access-Control-HttpRequest-Method", "POST");

		HttpRequest req = request(headers, RequestMethod.OPTIONS);
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse res = new HttpResponse(exchange);

		cors.doFilter(req, res, chain(new AtomicBoolean()));

		assertEquals(403, exchange.getResponseCode());
	}
}
