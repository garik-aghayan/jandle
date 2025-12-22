package com.jandle.internal.router;

import java.util.Map;

/**
 * Represents the result of a route lookup in the router.
 * <p>
 * Contains the matched {@link RouteNode} and a map of extracted
 * path parameters from the request URL.
 *
 * @param routeNode the route node that matched the request path
 * @param params    a map of path parameter names to their resolved values
 *                  (e.g. {@code "id" -> "42"})
 */
public record SearchResult(RouteNode routeNode, Map<String, String> params) {
}
