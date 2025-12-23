package org.jandle.http;

import org.jandle.api.http.RequestMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestMethodTest {

	@Test
	void fromString_validMethods() {
		assertEquals(RequestMethod.GET, RequestMethod.fromString("GET"));
		assertEquals(RequestMethod.GET, RequestMethod.fromString("gEt"));
		assertEquals(RequestMethod.POST, RequestMethod.fromString("POST"));
		assertEquals(RequestMethod.POST, RequestMethod.fromString("post"));
		assertEquals(RequestMethod.PUT, RequestMethod.fromString("PUT"));
		assertEquals(RequestMethod.PUT, RequestMethod.fromString("puT"));
		assertEquals(RequestMethod.PATCH, RequestMethod.fromString("PATCH"));
		assertEquals(RequestMethod.PATCH, RequestMethod.fromString("PATCh"));
		assertEquals(RequestMethod.HEAD, RequestMethod.fromString("HEAD"));
		assertEquals(RequestMethod.HEAD, RequestMethod.fromString("head"));
		assertEquals(RequestMethod.OPTIONS, RequestMethod.fromString("OPTIONS"));
		assertEquals(RequestMethod.OPTIONS, RequestMethod.fromString("optIONS"));
		assertEquals(RequestMethod.DELETE, RequestMethod.fromString("DELETE"));
		assertEquals(RequestMethod.DELETE, RequestMethod.fromString("Delete"));
	}

	@Test
	void fromString_invalidMethod_throwsException() {
		assertThrows(IllegalArgumentException.class,
				() -> RequestMethod.fromString("TRACE"));
	}
}
