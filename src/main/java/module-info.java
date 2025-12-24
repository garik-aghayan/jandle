/**
 * The single module of the Jandle HTTP framework.
 *
 * <p>This module provides a lightweight, modular HTTP server framework
 * built on top of the JDK {@code HttpServer}. It exposes a clean public API
 * for handling HTTP requests and responses, middleware-style filter chains,
 * cookies, CORS handling, rate limiting, lifecycle hooks, and structured
 * logging.
 *
 * <h2>Design Goals</h2>
 * <ul>
 *   <li>Minimal dependencies (JDK + Gson)</li>
 *   <li>Explicit, stable public API surface</li>
 * </ul>
 *
 * <h2>Module Dependencies</h2>
 * <ul>
 *   <li>{@code com.google.gson} — JSON serialization and deserialization</li>
 *   <li>{@code jdk.httpserver} — underlying HTTP server implementation</li>
 * </ul>
 *
 * <h2>Exported Packages</h2>
 * <ul>
 *   <li>{@code org.jandle.api} — core public API entry points</li>
 *   <li>{@code org.jandle.api.annotations} — routing and framework annotations</li>
 *   <li>{@code org.jandle.api.http} — HTTP request/response abstractions</li>
 *   <li>{@code org.jandle.api.cookies} — cookie handling utilities</li>
 *   <li>{@code org.jandle.api.cors} — Cross-Origin Resource Sharing support</li>
 *   <li>{@code org.jandle.api.ratelimiter} — request rate limiting utilities</li>
 *   <li>{@code org.jandle.api.lifecycle} — application lifecycle hooks</li>
 *   <li>{@code org.jandle.api.exceptions} — framework-specific exceptions</li>
 *   <li>{@code org.jandle.api.logger} — internal request and lifecycle logging</li>
 * </ul>
 *
 * <h2>Encapsulation</h2>
 * All non-exported packages are considered internal implementation details
 * and are not part of the public API contract.
 *
 * <p>Consumers of this module should rely only on the exported packages,
 * as internal packages may change without notice.
 */
module org.jandle {
	requires com.google.gson;
	requires jdk.httpserver;

	exports org.jandle.api;
	exports org.jandle.api.annotations;
	exports org.jandle.api.http;
	exports org.jandle.api.cookies;
	exports org.jandle.api.cors;
	exports org.jandle.api.ratelimiter;
	exports org.jandle.api.lifecycle;
	exports org.jandle.api.exceptions;
	exports org.jandle.api.logger;
}