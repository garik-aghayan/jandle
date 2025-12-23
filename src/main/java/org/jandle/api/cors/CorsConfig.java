package org.jandle.api.cors;

import org.jandle.api.http.RequestMethod;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration holder for Cross-Origin Resource Sharing (CORS) behavior.
 * <p>
 * This class defines all policy-level decisions used by {@link Cors}
 * to validate requests and generate appropriate CORS response headers.
 * <p>
 * All values are applied to requests passing through the filter.
 */
public final class CorsConfig {

	/**
	 * Creates a new {@code CorsConfig} instance with default settings.
	 *
	 * <p>No CORS rules are enabled by default. All options must be
	 * explicitly configured using the provided setters.</p>
	 */
	public CorsConfig(){}

	/**
	 * Set of allowed origins.
	 * <p>
	 * Use {@code "*"} to allow all origins.
	 * <p>
	 * Note: If {@code allowCredentials} is set to {@code true},
	 * wildcard origins are not permitted and must be validated explicitly.
	 */
	private Set<String> allowedOrigins = Set.of("*");
	/**
	 * Set of allowed HTTP methods for cross-origin requests.
	 * <p>
	 * These methods are used to validate preflight requests (OPTIONS) and
	 * to populate the {@code Access-Control-Allow-Methods} response header.
	 * <p>
	 * Default includes the most common HTTP methods: GET, POST, PUT, DELETE, and OPTIONS.
	 */
	private Set<String> allowedMethods = Set.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
	/**
	 * Set of allowed request headers.
	 * <p>
	 * Use {@code "*"} to allow all headers requested by the client.
	 * This value controls {@code Access-Control-Allow-Headers}.
	 */
	private Set<String> allowedHeaders = Set.of("*");
	/**
	 * Set of response headers that are exposed to the browser.
	 * <p>
	 * By default, browsers expose only a limited set of headers.
	 * This configuration controls {@code Access-Control-Expose-Headers}.
	 */
	private Set<String> exposedHeaders = Set.of();
	/**
	 * Whether cross-origin requests may include credentials
	 * such as cookies, authorization headers, or TLS client certificates.
	 * <p>
	 * When enabled, wildcard origins ({@code "*"}) are not allowed.
	 */
	private boolean allowCredentials = false;
	/**
	 * Maximum time (in seconds) that a preflight response may be cached
	 * by the browser.
	 * <p>
	 * This value is sent via {@code Access-Control-Max-Age}.
	 */
	private long maxAgeSeconds = 3600;
	/**
	 * Returns allowed origins for cross-origin requests
	 * @return {@code Set<String>} of origins for cross-origin requests
	 */

	public Set<String> getAllowedOrigins() {
		return allowedOrigins;
	}

	/**
	 * Sets the allowed origins.
	 *
	 * @param allowedOrigins set of allowed origin strings or {@code "*"}
	 *
	 * @return {@code this} {@code CorsConfig} for chaining
	 */
	public CorsConfig setAllowedOrigins(Set<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
		return this;
	}

	/**
	 * Returns allowed HTTP methods for cross-origin requests
	 * @return {@code Set<String>} of allowed HTTP methods for cross-origin requests
	 */
	public Set<String> getAllowedMethods() {
		return allowedMethods;
	}

	/**
	 * Sets the allowed HTTP methods.
	 *
	 * @param allowedMethods set of allowed method names (e.g. GET, POST)
	 *
	 * @return {@code this} {@code CorsConfig} for chaining
	 */
	public CorsConfig setAllowedMethods(Set<RequestMethod> allowedMethods) {
		this.allowedMethods = allowedMethods.stream().map(m -> m.toString().toUpperCase()).collect(Collectors.toSet());
		return this;
	}

	/**
	 * Returns allowed request headers
	 * @return {@code Set<String>} of allowed request headers
	 */
	public Set<String> getAllowedHeaders() {
		return allowedHeaders;
	}

	/**
	 * Sets the allowed request headers.
	 *
	 * @param allowedHeaders set of allowed header names or {@code "*"}
	 *
	 * @return {@code this} {@code CorsConfig} for chaining
	 */
	public CorsConfig setAllowedHeaders(Set<String> allowedHeaders) {
		this.allowedHeaders = allowedHeaders;
		return this;
	}

	/**
	 * Returns headers exposed to the browser
	 * @return {@code Set<String>} of headers exposed to the browser
	 */

	public Set<String> getExposedHeaders() {
		return exposedHeaders;
	}

	/**
	 * Sets the response headers that should be exposed to the browser.
	 *
	 * @param exposedHeaders set of response header names
	 *
	 * @return {@code this} {@code CorsConfig} for chaining
	 */
	public CorsConfig setExposedHeaders(Set<String> exposedHeaders) {
		this.exposedHeaders = exposedHeaders;
		return this;
	}

	/**
	 * Returns whether credentials are allowed or not in cross-origin requests
	 * @return {@code true} if credentials are allowed in cross-origin requests, otherwise {@code false}
	 */

	public boolean isAllowCredentials() {
		return allowCredentials;
	}

	/**
	 * Enables or disables credentialed cross-origin requests.
	 *
	 * @param allowCredentials whether credentials are allowed
	 *
	 * @return {@code this} {@code CorsConfig} for chaining
	 */
	public CorsConfig setAllowCredentials(boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
		return this;
	}

	/**
	 * Returns maximum preflight cache duration in seconds
	 * @return maximum preflight cache duration in seconds as {@code long}
	 */
	public long getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	/**
	 * Sets the preflight cache duration.
	 *
	 * @param maxAgeSeconds number of seconds the browser may cache preflight responses
	 *
	 * @return {@code this} {@code CorsConfig} for chaining
	 */

	public CorsConfig setMaxAgeSeconds(long maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
		return this;
	}
}
