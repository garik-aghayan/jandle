package org.jandle.api.cors;

import org.jandle.api.http.Filter;
import org.jandle.api.http.RequestMethod;
import org.jandle.api.http.Chain;
import org.jandle.api.http.Request;
import org.jandle.api.http.Response;

import java.io.IOException;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) filter implementation.
 * <p>
 * This filter applies CORS validation and headers according to the provided
 * {@link CorsConfig}. It supports:
 * <ul>
 *   <li>Simple CORS requests</li>
 *   <li>Preflight (OPTIONS) requests</li>
 *   <li>Credentialed requests</li>
 *   <li>Wildcard and explicit origin configurations</li>
 *   <li>Cache-safe behavior using {@code Vary} headers</li>
 * </ul>
 *
 * <p>
 * Same-origin requests are detected and bypass CORS processing entirely
 * for performance and correctness.
 */
public class Cors implements Filter {
	/** Immutable CORS configuration */
	private final CorsConfig config;

	/**
	 * Creates a new CORS filter with the given configuration.
	 *
	 * @param config CORS configuration defining allowed origins, methods,
	 *               headers, credentials, and cache duration
	 */
	public Cors(CorsConfig config) {
		this.config = config;
	}

	/**
	 * Applies CORS validation and headers to the request.
	 * <p>
	 * Execution flow:
	 * <ol>
	 *   <li>Extract {@code Origin} and {@code Host} headers</li>
	 *   <li>Skip CORS handling for same-origin or non-CORS requests</li>
	 *   <li>Validate origin against configuration</li>
	 *   <li>Apply CORS response headers</li>
	 *   <li>Handle preflight requests (OPTIONS)</li>
	 *   <li>Delegate to the remaining filter filterChain</li>
	 * </ol>
	 *
	 * @param req   incoming HTTP request
	 * @param res   outgoing HTTP response
	 * @param filterChain filter filterChain for further processing
	 * @throws IOException if an I/O error occurs while writing the response
	 */
	@Override
	public void doFilter(Request req, Response res, Chain filterChain) throws IOException {
		String origin = req.getHeaderFirst("Origin");
		String host = req.hasHeader("Host")
				? req.getHeaderFirst("Host")
				: null;

		if (origin == null || (host != null && origin.contains(host))) {
			filterChain.next(req, res);
			return;
		}

		if (!isOriginAllowed(origin)) {
			res.sendStatus(403);
			return;
		}

		applyCorsHeaders(res, origin);

		if (req.getMethod() == RequestMethod.OPTIONS) {
			handlePreflight(req, res);
			return;
		}

		filterChain.next(req, res);
	}

	/**
	 * Checks whether the given origin is allowed by the configuration.
	 *
	 * @param origin request origin
	 * @return {@code true} if the origin is allowed, otherwise {@code false}
	 */
	private boolean isOriginAllowed(String origin) {
		return config.getAllowedOrigins().contains("*")
				|| config.getAllowedOrigins().contains(origin);
	}

	/**
	 * Applies standard CORS response headers for both simple and preflight requests.
	 * <p>
	 * This method:
	 * <ul>
	 *   <li>Sets {@code Access-Control-Allow-Origin}</li>
	 *   <li>Enforces credential + wildcard restrictions</li>
	 *   <li>Adds {@code Access-Control-Allow-Credentials} if enabled</li>
	 *   <li>Adds {@code Access-Control-Expose-Headers} if configured</li>
	 *   <li>Adds {@code Vary: Origin} for cache correctness</li>
	 * </ul>
	 *
	 * @param res    HTTP response
	 * @param origin validated request origin
	 * @throws IllegalStateException if credentials are enabled with wildcard origins
	 */
	private void applyCorsHeaders(Response res, String origin) {
		boolean allowAll = config.getAllowedOrigins().contains("*");

		if (allowAll && config.isAllowCredentials()) {
			throw new IllegalStateException(
					"Cannot use allowCredentials=true with allowedOrigins=*"
			);
		}

		res.header("Access-Control-Allow-Origin", allowAll ? "*" : origin)
				.vary("Origin");

		if (config.isAllowCredentials()) {
			res.header("Access-Control-Allow-Credentials", "true");
		}

		if (!config.getExposedHeaders().isEmpty()) {
			res.header("Access-Control-Expose-Headers",
					String.join(", ", config.getExposedHeaders()));
		}
	}

	/**
	 * Handles CORS preflight (OPTIONS) requests.
	 * <p>
	 * This method:
	 * <ul>
	 *   <li>Validates requested HTTP method</li>
	 *   <li>Validates and echoes requested headers when allowed</li>
	 *   <li>Sets {@code Access-Control-Allow-Methods}</li>
	 *   <li>Sets {@code Access-Control-Allow-Headers}</li>
	 *   <li>Sets {@code Access-Control-Max-Age}</li>
	 *   <li>Adds all required {@code Vary} headers</li>
	 * </ul>
	 *
	 * @param req HTTP request
	 * @param res HTTP response
	 * @throws IOException if an I/O error occurs while sending the response
	 */

	private void handlePreflight(Request req, Response res) throws IOException {
		res.vary("Origin");
		res.vary("Access-Control-HttpRequest-Method");
		res.vary("Access-Control-HttpRequest-Headers");

		String requestedMethod =
				req.hasHeader("Access-Control-HttpRequest-Method")
						? req.getHeaderFirst("Access-Control-HttpRequest-Method")
						: null;

		if (requestedMethod == null ||
				!config.getAllowedMethods().contains(requestedMethod)) {
			res.sendStatus(403);
			return;
		}

		res.header("Access-Control-Allow-Methods",
				String.join(", ", config.getAllowedMethods()));

		List<String> requestedHeaders =
				req.getHeader("Access-Control-HttpRequest-Headers");

		if (requestedHeaders != null && !requestedHeaders.isEmpty()) {
			if (config.getAllowedHeaders().contains("*")) {
				res.header(
						"Access-Control-Allow-Headers",
						String.join(", ", requestedHeaders)
				);
			} else {
				res.header(
						"Access-Control-Allow-Headers",
						String.join(", ", config.getAllowedHeaders())
				);
			}
		}

		res.header("Access-Control-Max-Age",
				String.valueOf(config.getMaxAgeSeconds()));

		res.sendStatus(204);
	}
}
