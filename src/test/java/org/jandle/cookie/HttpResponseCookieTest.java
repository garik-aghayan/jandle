package org.jandle.cookie;

import org.jandle.api.cookies.ResponseCookie;
import org.jandle.api.cookies.SameSite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseCookieTest {

	@Test
	void basicCookie_serialization() {
		ResponseCookie cookie = new ResponseCookie("a", "b");
		assertEquals("a=b; Path=/; SameSite=Lax", cookie.toString());
	}

	@Test
	void secureHttpOnlyCookie() {
		ResponseCookie cookie = new ResponseCookie("session", "123");
		cookie.setSecure(true);
		cookie.setHttpOnly(true);

		String out = cookie.toString();
		assertTrue(out.contains("Secure"));
		assertTrue(out.contains("HttpOnly"));
	}

	@Test
	void sameSiteNone_requiresSecure() {
		ResponseCookie cookie = new ResponseCookie("a", "b");

		assertThrows(IllegalArgumentException.class,
				() -> cookie.setSameSite(SameSite.NONE));
	}

	@Test
	void sameSiteNone_withSecure_allowed() {
		ResponseCookie cookie = new ResponseCookie("a", "b");
		cookie.setSecure(true);
		cookie.setSameSite(SameSite.NONE);

		assertTrue(cookie.toString().contains("SameSite=None"));
	}

	@Test
	void partitioned_requiresSecure() {
		ResponseCookie cookie = new ResponseCookie("a", "b");

		assertThrows(IllegalArgumentException.class,
				() -> cookie.setPartitioned(true));
	}

	@Test
	void partitioned_withSecure_allowed() {
		ResponseCookie cookie = new ResponseCookie("a", "b");
		cookie.setSecure(true);
		cookie.setPartitioned(true);

		assertTrue(cookie.toString().contains("Partitioned"));
	}

	@Test
	void maxAge_and_expires_rendered() {
		ResponseCookie cookie = new ResponseCookie("a", "b");
		cookie.setMaxAgeSeconds(3600);
		cookie.setExpiresMillis(1_700_000_000_000L);

		String out = cookie.toString();
		assertTrue(out.contains("Max-Age=3600"));
		assertTrue(out.contains("Expires="));
	}

	@Test
	void domain_and_path_rendered() {
		ResponseCookie cookie = new ResponseCookie("a", "b");
		cookie.setDomain("example.com");
		cookie.setPath("/api");

		String out = cookie.toString();
		assertTrue(out.contains("Domain=example.com"));
		assertTrue(out.contains("Path=/api"));
	}
}
