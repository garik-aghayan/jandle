package org.jandle.internal.util;

import org.jandle.api.http.Filter;
import org.jandle.api.http.Handler;
import org.jandle.api.annotations.HttpRequestFilters;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.exceptions.JandleSyntaxException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility class providing helper methods for HTTP path parsing
 * and route metadata extraction.
 *
 * <p>This class contains only static utility methods and is not
 * intended to be instantiated.</p>
 *
 * <p>The default constructor exists only to satisfy
 * Java language requirements.</p>
 */
public abstract class PathUtil {
	/**
	 * Prevents instantiation of this utility class.
	 *
	 * <p>{@code PathUtil} contains only static helper methods and
	 * should not be instantiated.</p>
	 */
	private PathUtil() {}

	/**
	 * Checks if a given path segment is a path parameter.
	 * A segment is considered a parameter if it starts with '{' and ends with '}'.
	 *
	 * @param pathSegment the path segment to check
	 * @return true if the segment is a parameter, false otherwise
	 */
	public static boolean isPathSegmentParam(String pathSegment) {
		return pathSegment.startsWith("{") && pathSegment.endsWith("}");
	}

	/**
	 * Splits a path string into its individual segments and validates
	 * that any path parameters follow the allowed pattern.
	 * <p>
	 * Example: "/user/{id}/profile" -> ["user", "{id}", "profile"]
	 *
	 * @param path the HTTP path to split
	 * @return array of path segments
	 * @throws JandleSyntaxException if a path parameter has invalid format
	 */
	public static String[] segmentsOfPath(String path) {
		final Pattern INVALID_PARAM_PATTERN =
				Pattern.compile("\\{(?![A-Za-z0-9_]+\\})[^}]*\\}");

		if (path == null || path.isEmpty()) return new String[0];
		var segments = Arrays.stream(path.split("/"))
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);
		for (var seg : segments) {
			if (isPathSegmentParam(seg) && seg.matches(INVALID_PARAM_PATTERN.pattern())) {
				throw new JandleSyntaxException("Invalid path param: " + getParamNameFromSegment(seg) + " | Path: " + path);
			}
		}
		return segments;
	}

	/**
	 * Reconstructs a path string from an array of segments.
	 * <p>
	 * Example: ["user", "{id}", "profile"] -> "/user/{id}/profile"
	 *
	 * @param segments the path segments
	 * @return the reconstructed path string
	 */
	public static String pathFromSegments(String[] segments) {
		StringBuilder sb = new StringBuilder();
		for (String seg: segments) {
			sb.append("/");
			sb.append(seg);
		}
		return sb.toString();
	}

	/**
	 * Extracts the name of a path parameter from its segment.
	 * <p>
	 * Example: "{id}" -> "id"
	 *
	 * @param segment the path segment representing a parameter
	 * @return the parameter name without the curly braces
	 */

	public static String getParamNameFromSegment(String segment) {
		return segment.substring(1, segment.length() - 1);
	}

	/**
	 * Validates a full HTTP path string and returns its segments.
	 * Checks for:
	 * - Correct HTTP path syntax
	 * - Proper placement of path parameters
	 * - No duplicate path parameters
	 * <p>
	 * Example: "/user/{id}/profile/**" -> ["user", "{id}", "profile", "**"]
	 *
	 * @param path the full HTTP path
	 * @return validated array of path segments
	 * @throws JandleSyntaxException if the path is invalid or contains duplicate parameters
	 */
	public static String[] getValidatedPathSegments(String path) {
		final Pattern HTTP_PATH_PATTERN = Pattern.compile("^/(?:[A-Za-z0-9._-]+|\\{[A-Za-z0-9_]+\\}|\\*|\\*\\*)(?:/(?:[A-Za-z0-9._-]+|\\{[A-Za-z0-9_]+\\}|\\*|\\*\\*))*$");

		if (!path.matches(HTTP_PATH_PATTERN.pattern())) {
			throw new JandleSyntaxException("Http request wrong syntax. Provided '" + path + "'");
		}

		String[] pathParts = segmentsOfPath(path);

		String duplicateParam = duplicatePathParam(pathParts);
		if (duplicateParam != null) {
			throw new JandleSyntaxException("Duplicate path param: " + duplicateParam + ". Path provided: " + path);
		}

		return pathParts;
	}

	/**
	 * Checks if a list of path segments contains duplicate path parameters.
	 *
	 * @param pathSegments array of path segments
	 * @return the first duplicate segment found, or null if no duplicates exist
	 */
	public static String duplicatePathParam(String[] pathSegments) {
		Set<String> seen = new HashSet<>();
		for (var segment : pathSegments) {
			if (isPathSegmentParam(segment) && !seen.add(getParamNameFromSegment(segment))) {
				return segment;
			}
		}
		return null;
	}

	/**
	 * Extracts and validates all routing metadata from a {@link Handler} class.
	 * <p>
	 * This method inspects the handler's class-level annotations to determine:
	 * <ul>
	 *   <li>The HTTP method and path defined by {@link HttpRequestHandler}</li>
	 *   <li>Any request filters defined by {@link HttpRequestFilters}</li>
	 * </ul>
	 *
	 * <p>
	 * The resulting path is combined with the provided base path and fully validated:
	 * <ul>
	 *   <li>Ensures correct HTTP path syntax</li>
	 *   <li>Validates path parameters</li>
	 *   <li>Rejects duplicate path parameters</li>
	 * </ul>
	 *
	 * <p>
	 * Exactly one {@link HttpRequestHandler} annotation must be present.
	 * If none or more than one are found, a {@link JandleSyntaxException} is thrown.
	 *
	 * @param handler  the handler instance whose class annotations will be analyzed
	 * @param basePath the global base path applied to all handlers
	 * @return a map containing:
	 *         <ul>
	 *           <li>{@code "method"} – the HTTP method as a string</li>
	 *           <li>{@code "pathSegments"} – validated path segments</li>
	 *           <li>{@code "filters"} – instantiated filters applied to this handler</li>
	 *         </ul>
	 *
	 * @throws JandleSyntaxException if annotation usage or path syntax is invalid
	 * @throws NoSuchMethodException if a filter does not expose a public no-arg constructor
	 * @throws InvocationTargetException if filter instantiation fails
	 * @throws InstantiationException if a filter class cannot be instantiated
	 * @throws IllegalAccessException if a filter constructor is not accessible
	 */
	public static Map<String, Object> getHandlerData(Handler handler, String basePath) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		HttpRequestHandler annotation = null;
		Filter[] filters = new Filter[0];
		for (Annotation ann : handler.getClass().getAnnotations()) {
			if (ann instanceof HttpRequestHandler) {
				if (annotation != null) throw new JandleSyntaxException("More than one request method provided for the class " + handler.getClass().getName());
				annotation = (HttpRequestHandler) ann;
			}
			else if (ann instanceof HttpRequestFilters) {
				filters = getFiltersFromAnnotation((HttpRequestFilters) ann);
			}

		}
		if (annotation == null) throw new JandleSyntaxException("No request method provided for the class " + handler.getClass().getName());
		String path = annotation.path();
		String[] pathSegments = PathUtil.getValidatedPathSegments(basePath + path);
		return Map.of("method", annotation.method().toString(), "pathSegments", pathSegments, "filters", filters);
	}

	/**
	 * Instantiates all {@link Filter} implementations declared in a
	 * {@link HttpRequestFilters} annotation.
	 * <p>
	 * Each filter class is instantiated via its public no-argument constructor.
	 * The order of filters in the returned array strictly follows the order
	 * declared in the annotation.
	 *
	 * <p>
	 * Filter instances created by this method are bound to a specific handler
	 * and are resolved once during handler registration, not per request.
	 *
	 * @param ann the {@link HttpRequestFilters} annotation containing filter classes
	 * @return an array of instantiated {@link Filter} objects
	 *
	 * @throws NoSuchMethodException if a filter class does not define a public no-arg constructor
	 * @throws InvocationTargetException if filter construction throws an exception
	 * @throws InstantiationException if a filter class cannot be instantiated
	 * @throws IllegalAccessException if the filter constructor is not accessible
	 */
	private static Filter[] getFiltersFromAnnotation(HttpRequestFilters ann) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		Filter[] filters = new Filter[ann.value().length];
		for (int i = 0; i < filters.length; i++) {
			var filterClass = ann.value()[i];
			Filter filter = filterClass.getConstructor().newInstance();
			filters[i] = filter;
		}
		return filters;
	}
}
