package com.jandle.cors;

import com.jandle.api.http.RequestMethod;
import com.jandle.api.cors.CorsConfig;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

	@Test
	void defaultValues_areCorrect() {
		CorsConfig config = new CorsConfig();

		assertEquals(Set.of("*"), config.getAllowedOrigins());
		assertTrue(config.getAllowedMethods().contains("GET"));
		assertTrue(config.getAllowedMethods().contains("OPTIONS"));
		assertEquals(Set.of("*"), config.getAllowedHeaders());
		assertEquals(Set.of(), config.getExposedHeaders());
		assertFalse(config.isAllowCredentials());
		assertEquals(3600, config.getMaxAgeSeconds());
	}

	@Test
	void setAllowedOrigins() {
		CorsConfig config = new CorsConfig();
		config.setAllowedOrigins(Set.of("https://example.com"));

		assertEquals(Set.of("https://example.com"), config.getAllowedOrigins());
	}

	@Test
	void setAllowedMethods_convertsEnumToUppercaseStrings() {
		CorsConfig config = new CorsConfig();
		config.setAllowedMethods(Set.of(RequestMethod.GET, RequestMethod.POST));

		assertEquals(Set.of("GET", "POST"), config.getAllowedMethods());
	}

	@Test
	void setAllowedHeaders() {
		CorsConfig config = new CorsConfig();
		config.setAllowedHeaders(Set.of("X-Test", "Authorization"));

		assertEquals(Set.of("X-Test", "Authorization"), config.getAllowedHeaders());
	}

	@Test
	void setExposedHeaders() {
		CorsConfig config = new CorsConfig();
		config.setExposedHeaders(Set.of("X-Total-Count"));

		assertEquals(Set.of("X-Total-Count"), config.getExposedHeaders());
	}

	@Test
	void allowCredentials_toggle() {
		CorsConfig config = new CorsConfig();

		config.setAllowCredentials(true);
		assertTrue(config.isAllowCredentials());

		config.setAllowCredentials(false);
		assertFalse(config.isAllowCredentials());
	}

	@Test
	void setMaxAgeSeconds() {
		CorsConfig config = new CorsConfig();
		config.setMaxAgeSeconds(600);

		assertEquals(600, config.getMaxAgeSeconds());
	}
}
