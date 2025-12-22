package com.jandle.http;

import com.jandle.internal.http.HttpResponse;
import com.jandle.testutil.MockHttpExchange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {

	@Test
	void sendText_setsHeadersAndBody() throws Exception {
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse httpResponse = new HttpResponse(exchange);

		httpResponse.sendText("hello");

		assertEquals(200, exchange.getStatus());
		assertEquals("text/plain; charset=utf-8",
				exchange.getResponseHeaders().getFirst("Content-Type"));
		assertEquals("hello", exchange.getBodyAsString());
	}

	@Test
	void responseSentTwice_throwsException() throws Exception {
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse httpResponse = new HttpResponse(exchange);

		httpResponse.sendText("ok");

		assertThrows(IllegalStateException.class,
				() -> httpResponse.sendText("again"));
	}

	@Test
	void redirect_setsLocationHeader() throws Exception {
		MockHttpExchange exchange = new MockHttpExchange();
		HttpResponse httpResponse = new HttpResponse(exchange);

		httpResponse.redirect("/login");

		assertEquals(302, exchange.getStatus());
		assertEquals("/login",
				exchange.getResponseHeaders().getFirst("Location"));
	}
}
