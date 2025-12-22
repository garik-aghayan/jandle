package com.jandle.cookie;

import com.jandle.internal.cookie.SetCookie;
import com.jandle.api.cookies.SameSite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpSetCookieTest {

	@Test
	void basicCookie_serialization() {
		SetCookie cookie = new SetCookie("a", "b");
		assertEquals("a=b; Path=/; SameSite=Lax", cookie.toString());
	}

	@Test
	void secureHttpOnlyCookie() {
		SetCookie cookie = new SetCookie("session", "123");
		cookie.setSecure(true);
		cookie.setHttpOnly(true);

		String out = cookie.toString();
		assertTrue(out.contains("Secure"));
		assertTrue(out.contains("HttpOnly"));
	}

	@Test
	void sameSiteNone_requiresSecure() {
		SetCookie cookie = new SetCookie("a", "b");

		assertThrows(IllegalArgumentException.class,
				() -> cookie.setSameSite(SameSite.NONE));
	}

	@Test
	void sameSiteNone_withSecure_allowed() {
		SetCookie cookie = new SetCookie("a", "b");
		cookie.setSecure(true);
		cookie.setSameSite(SameSite.NONE);

		assertTrue(cookie.toString().contains("SameSite=None"));
	}

	@Test
	void partitioned_requiresSecure() {
		SetCookie cookie = new SetCookie("a", "b");

		assertThrows(IllegalArgumentException.class,
				() -> cookie.setPartitioned(true));
	}

	@Test
	void partitioned_withSecure_allowed() {
		SetCookie cookie = new SetCookie("a", "b");
		cookie.setSecure(true);
		cookie.setPartitioned(true);

		assertTrue(cookie.toString().contains("Partitioned"));
	}

	@Test
	void maxAge_and_expires_rendered() {
		SetCookie cookie = new SetCookie("a", "b");
		cookie.setMaxAgeSeconds(3600);
		cookie.setExpiresMillis(1_700_000_000_000L);

		String out = cookie.toString();
		assertTrue(out.contains("Max-Age=3600"));
		assertTrue(out.contains("Expires="));
	}

	@Test
	void domain_and_path_rendered() {
		SetCookie cookie = new SetCookie("a", "b");
		cookie.setDomain("example.com");
		cookie.setPath("/api");

		String out = cookie.toString();
		assertTrue(out.contains("Domain=example.com"));
		assertTrue(out.contains("Path=/api"));
	}
}
