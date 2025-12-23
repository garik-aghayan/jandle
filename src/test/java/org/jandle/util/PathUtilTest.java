package org.jandle.util;

import org.jandle.api.annotations.HttpRequestFilters;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.exceptions.JandleSyntaxException;
import org.jandle.api.http.*;
import org.jandle.internal.util.PathUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PathUtilTest {

	/* ---------- isPathSegmentParam ---------- */

	@Test
	void isPathSegmentParam_valid() {
		assertTrue(PathUtil.isPathSegmentParam("{id}"));
		assertFalse(PathUtil.isPathSegmentParam("id"));
		assertFalse(PathUtil.isPathSegmentParam("{id"));
		assertFalse(PathUtil.isPathSegmentParam("id}"));
	}

	/* ---------- segmentsOfPath ---------- */

	@Test
	void segmentsOfPath_basic() {
		String[] segments = PathUtil.segmentsOfPath("/user/{id}/profile/");
		assertArrayEquals(new String[]{"user", "{id}", "profile"}, segments);
	}

	@Test
	void segmentsOfPath_invalidParam_throws() {
		assertThrows(JandleSyntaxException.class,
				() -> PathUtil.segmentsOfPath("/user/{i-d}"));
		assertThrows(JandleSyntaxException.class,
				() -> PathUtil.segmentsOfPath("/user/{i*d}"));
	}

	/* ---------- pathFromSegments ---------- */

	@Test
	void pathFromSegments_rebuildsPath() {
		String path = PathUtil.pathFromSegments(new String[]{"a", "b", "{c}"});
		assertEquals("/a/b/{c}", path);
	}

	/* ---------- getParamNameFromSegment ---------- */

	@Test
	void getParamNameFromSegment_extractsName() {
		assertEquals("id", PathUtil.getParamNameFromSegment("{id}"));
	}

	/* ---------- duplicatePathParam ---------- */

	@Test
	void duplicatePathParam_detectsDuplicate() {
		String dup = PathUtil.duplicatePathParam(
				new String[]{"user", "{id}", "profile", "{id}"});
		assertEquals("{id}", dup);
	}

	@Test
	void duplicatePathParam_none() {
		assertNull(PathUtil.duplicatePathParam(
				new String[]{"user", "{id}", "profile"}));
	}

	/* ---------- getValidatedPathSegments ---------- */

	@Test
	void getValidatedPathSegments_valid() {
		String[] segs = PathUtil.getValidatedPathSegments("/user/{id}/profile/**");
		assertEquals(4, segs.length);
	}

	@Test
	void getValidatedPathSegments_invalidSyntax() {
		assertThrows(JandleSyntaxException.class,
				() -> PathUtil.getValidatedPathSegments("user/{id}"));
	}

	@Test
	void getValidatedPathSegments_duplicateParams() {
		assertThrows(JandleSyntaxException.class,
				() -> PathUtil.getValidatedPathSegments("/a/{id}/{id}"));
	}

	/* ---------- getHandlerData ---------- */

	@Test
	void getHandlerData_extractsMetadata() throws Exception {
		Handler handler = new TestHandler();

		Map<String, Object> data =
				PathUtil.getHandlerData(handler, "/api");

		assertEquals("GET", data.get("method"));
		assertArrayEquals(
				new String[]{"api", "test"},
				(String[]) data.get("pathSegments")
		);

		Filter[] filters = (Filter[]) data.get("filters");
		assertEquals(2, filters.length);
	}

	@Test
	void getHandlerData_missingAnnotation_throws() {
		Handler handler = new NoAnnotationHandler();

		assertThrows(JandleSyntaxException.class,
				() -> PathUtil.getHandlerData(handler, "/api"));
	}

	/* ---------- test helpers ---------- */

	@HttpRequestHandler(method = RequestMethod.GET, path = "/test")
	@HttpRequestFilters({TestFilterA.class, TestFilterB.class})
	static class TestHandler implements Handler {
		@Override
		public void handle(
				Request Request,
				Response Response) {}
	}

	static class NoAnnotationHandler implements Handler {
		@Override
		public void handle(
				Request Request,
				Response Response) {}
	}

	public static class TestFilterA implements Filter {
		@Override
		public void doFilter(
				Request r,
				Response s,
				Chain c) {}
	}

	public static class TestFilterB implements Filter {
		@Override
		public void doFilter(
				Request r,
				Response s,
				Chain c) {}
	}
}
