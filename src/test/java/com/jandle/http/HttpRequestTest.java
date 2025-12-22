package com.jandle.http;

import com.jandle.internal.http.HttpRequest;
import com.jandle.api.http.RequestMethod;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

	@Test
	void constructor_nullPath_throwsException() {
		assertThrows(IllegalArgumentException.class, () ->
				new HttpRequest(null, RequestMethod.GET, null, null,
						null, null, null, null)
		);
	}

	@Test
	void queryParsing_multipleValues() {
		HttpRequest req = new HttpRequest(
				"/test",
				RequestMethod.GET,
				null,
				null,
				Map.of(),
				"a=1&a=2&b=hello",
				new Headers(),
				null
		);

		assertEquals(List.of("1", "2"), req.getQueryParam("a"));
		assertEquals("hello", req.getQueryParamFirst("b"));
	}

	@Test
	void headers_areCaseInsensitive() {
		Headers headers = new Headers();
		headers.add("Content-Type", "application/json");

		HttpRequest req = new HttpRequest(
				"/test",
				RequestMethod.POST,
				null,
				null,
				Map.of(),
				null,
				headers,
				null
		);

		assertEquals("application/json", req.getHeaderFirst("content-type"));
	}

	@Test
	void cookies_areParsedCorrectly() {
		Headers headers = new Headers();
		headers.add("Cookie", "a=1; b=hello");

		HttpRequest req = new HttpRequest(
				"/test",
				RequestMethod.GET,
				null,
				null,
				Map.of(),
				null,
				headers,
				null
		);

		assertEquals("1", req.getCookie("a"));
		assertEquals("hello", req.getCookie("b"));
	}

	@Test
	void jsonBody_parsedCorrectly() {
		Headers headers = new Headers();
		headers.add("Content-Type", "application/json");

		HttpRequest req = new HttpRequest(
				"/test",
				RequestMethod.POST,
				null,
				null,
				Map.of(),
				null,
				headers,
				"{\"x\":5}".getBytes()
		);

		assertEquals(5.0, req.getBodyJson().get("x"));
	}

	@Test
	void invalidJson_throwsException() {
		Headers headers = new Headers();
		headers.add("Content-Type", "application/json");

		HttpRequest req = new HttpRequest(
				"/test",
				RequestMethod.POST,
				null,
				null,
				Map.of(),
				null,
				headers,
				"{invalid}".getBytes()
		);

		assertThrows(IllegalArgumentException.class, req::getBodyJson);
	}
}
