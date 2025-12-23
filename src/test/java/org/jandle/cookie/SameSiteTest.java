package org.jandle.cookie;

import org.jandle.api.cookies.SameSite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SameSiteTest {

	@Test
	void toString_values() {
		assertEquals("None", SameSite.NONE.toString());
		assertEquals("Strict", SameSite.STRICT.toString());
		assertEquals("Lax", SameSite.LAX.toString());
	}
}
