package org.jandle.ratelimiter;

import org.jandle.api.http.Filter;
import org.jandle.internal.http.FilterChain;
import org.jandle.internal.http.HttpRequest;
import org.jandle.api.http.RequestMethod;
import org.jandle.internal.http.HttpResponse;
import org.jandle.api.ratelimiter.RateLimiter;
import org.jandle.testutil.MockHttpExchange;
import org.jandle.testutil.RequestFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

	private FilterChain chain(AtomicBoolean called) {
		return new FilterChain(
				new Filter[0],
				(req, res) -> called.set(true)
		);
	}

	@Test
	void requestWithoutRemoteAddress_isAllowed() throws IOException {
		RateLimiter limiter = new RateLimiter(1, 1);

		HttpRequest req = RequestFactory.withoutRemoteAddress(RequestMethod.GET);
		HttpResponse res = new HttpResponse(new MockHttpExchange());

		AtomicBoolean chainCalled = new AtomicBoolean(false);

		limiter.doFilter(req, res, chain(chainCalled));

		assertTrue(chainCalled.get());
	}

	@Test
	void allowsRequestsWithinCapacity() throws IOException {
		RateLimiter limiter = new RateLimiter(2, 1);

		HttpRequest req = RequestFactory.withIp("10.0.0.1", RequestMethod.GET);
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse res = new HttpResponse(exchange);

		AtomicBoolean chainCalled = new AtomicBoolean(false);

		limiter.doFilter(req, res, chain(chainCalled));

		assertTrue(chainCalled.get());
		assertEquals("-1", String.valueOf(exchange.getResponseCode()));
	}

	@Test
	void rejectsWhenTokensExhausted() throws IOException {
		RateLimiter limiter = new RateLimiter(1, 0);

		HttpRequest req = RequestFactory.withIp("10.0.0.2", RequestMethod.GET);

		// first request
		limiter.doFilter(req, new HttpResponse(new MockHttpExchange()), chain(new AtomicBoolean()));

		// second request
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse res = new HttpResponse(exchange);

		limiter.doFilter(req, res, chain(new AtomicBoolean()));

		assertEquals(429, exchange.getResponseCode());
		assertNotNull(exchange.getResponseHeaders().getFirst("Retry-After"));
	}

	@Test
	void setsRateLimitHeadersOnSuccess() throws IOException {
		RateLimiter limiter = new RateLimiter(5, 1);

		HttpRequest req = RequestFactory.withIp("192.168.1.1", RequestMethod.GET);
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse res = new HttpResponse(exchange);

		limiter.doFilter(req, res, chain(new AtomicBoolean()));

		assertEquals("5", exchange.getResponseHeaders().getFirst("RateLimit-Limit"));
		assertNotNull(exchange.getResponseHeaders().getFirst("RateLimit-Remaining"));
		assertNotNull(exchange.getResponseHeaders().getFirst("RateLimit-Reset"));
		assertNotNull(exchange.getResponseHeaders().getFirst("RateLimit-Policy"));
	}

	@Test
	void releaseDelegatesToStorage() {
		RateLimiter limiter = new RateLimiter(1, 1);

		assertDoesNotThrow(limiter::release);
	}
}
