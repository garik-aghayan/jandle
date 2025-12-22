package com.jandle.router;

import com.jandle.api.http.Handler;
import com.jandle.internal.router.RouteNode;
import com.jandle.internal.router.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class RouteNodeTest {

	@Test
	void staticRouteMatch() {
		RouteNode root = new RouteNode();
		Handler h = (req, res) -> {};

		root.registerNode(new String[]{"users"}, h);

		SearchResult res = root.getNodeAtPath(new String[]{"users"}, 0, new HashMap<>());

		assertNotNull(res);
		assertEquals(h, res.routeNode().getHandler());
	}

	@Test
	void paramRouteMatch_extractsParams() {
		RouteNode root = new RouteNode();
		Handler h = (req, res) -> {};

		root.registerNode(new String[]{"users", "{id}"}, h);

		SearchResult res = root.getNodeAtPath(new String[]{"users", "42"}, 0, new HashMap<>());

		assertEquals("42", res.params().get("id"));
	}

	@Test
	void wildcardRouteMatch() {
		RouteNode root = new RouteNode();
		Handler h = (req, res) -> {};

		root.registerNode(new String[]{"files", "*"}, h);

		assertNotNull(root.getNodeAtPath(
				new String[]{"files", "x"}, 0, new HashMap<>()));
	}

	@Test
	void doubleWildcardMatchesMultipleSegments() {
		RouteNode root = new RouteNode();
		Handler h = (req, res) -> {};

		root.registerNode(new String[]{"assets", "**"}, h);

		assertNotNull(root.getNodeAtPath(
				new String[]{"assets", "a", "b", "c"}, 0, new HashMap<>()));
	}
}
