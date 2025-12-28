package org.jandle.api;

import com.google.gson.JsonParseException;
import org.jandle.api.http.*;
import org.jandle.api.lifecycle.OnServerStart;
import org.jandle.api.lifecycle.CleanupCallback;
import org.jandle.internal.http.FilterChain;
import org.jandle.internal.http.HttpRequest;
import org.jandle.internal.http.HttpResponse;
import com.sun.net.httpserver.Headers;
import org.jandle.api.logger.JandleLogger;
import org.jandle.internal.logger.TraceLog;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jandle.internal.router.RouteNode;
import org.jandle.internal.router.SearchResult;
import org.jandle.internal.util.PathUtil;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * Core entry point of the Jandle HTTP framework.
 *
 * <p>
 * {@code JandleApplication} is responsible for:
 * <ul>
 *     <li>Registering HTTP handlers and filters</li>
 *     <li>Routing incoming requests to matching handlers</li>
 *     <li>Constructing {@link HttpRequest} and {@link HttpResponse} objects</li>
 *     <li>Executing the filter chain and handler</li>
 *     <li>Handling framework-level errors</li>
 * </ul>
 *
 * <p>
 * This class implements {@link HttpHandler} and is intended to be registered
 * directly with {@link HttpServer}.
 *
 * <p><b>HttpRequest lifecycle:</b>
 * <ol>
 *     <li>{@link #handle(HttpExchange)} is invoked by the HTTP server</li>
 *     <li>The request method and path are resolved</li>
 *     <li>A matching route is searched</li>
 *     <li>{@link HttpRequest} and {@link HttpResponse} are created</li>
 *     <li>A {@link FilterChain} of filters is executed</li>
 *     <li>The final {@link Handler} handles the request</li>
 * </ol>
 */
public class JandleApplication implements HttpHandler {
	/**
	 * Base path that is prepended to all registered handler paths.
	 */
	private final String basePath;
	/**
	 * Maximum allowed size of the request body in bytes.
	 * A value of {@code -1} means unlimited.
	 */
	private final int maxBodyBytes;
	/**
	 * Routing tree indexed by HTTP method.
	 */
	private final ConcurrentMap<RequestMethod, RouteNode> requestMethodToHandlersMap = new ConcurrentHashMap<>();
	/**
	 * Underlying HTTP server instance.
	 */
	private final HttpServer server;
	/**
	 * Filters that are applied to all requests before handler-specific filters.
	 */
	private Filter[] globalFilters;
	/**
	 * Logger used for framework-level logging.
	 */
	private JandleLogger logger = new TraceLog();
	/**
	 * Executor used by the HTTP server for handling requests.
	 */
	private Executor executor;

	/**
	 * Creates a new {@code JandleApplication}.
	 *
	 * @param server       the {@link HttpServer} instance
	 * @param basePath     base path for all handlers
	 * @param maxBodyBytes maximum request body size in bytes, or {@code -1} for unlimited
	 */
	public JandleApplication(HttpServer server, String basePath, int maxBodyBytes) {
		this.basePath = basePath;
		this.maxBodyBytes = maxBodyBytes < 0 ? -1 : maxBodyBytes;
		this.server = server;
	}

	/**
	 * Creates a new {@code JandleApplication} with unlimited request body size.
	 *
	 * @param server   the {@link HttpServer} instance
	 * @param basePath base path for all handlers
	 */
	public JandleApplication(HttpServer server, String basePath) {
		this(server, basePath, -1);
	}

	/**
	 * Creates a new {@code JandleApplication} with no base path ("").
	 *
	 * @param server       the {@link HttpServer} instance
	 * @param maxBodyBytes maximum request body size in bytes, or {@code -1} for unlimited
	 */
	public JandleApplication(HttpServer server, int maxBodyBytes) {
		this(server, "", maxBodyBytes);
	}

	/**
	 * Creates a new {@code JandleApplication} with no base path ("") and unlimited body size.
	 *
	 * @param server the {@link HttpServer} instance
	 */
	public JandleApplication(HttpServer server) {
		this(server, "", -1);
	}

	/**
	 * Entry point for all incoming HTTP requests.
	 *
	 * <p>
	 * This method:
	 * <ul>
	 *     <li>Resolves the HTTP method</li>
	 *     <li>Searches for a matching handler</li>
	 *     <li>Executes the filter chain</li>
	 *     <li>Handles framework-level errors</li>
	 * </ul>
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		HttpResponse httpResponse = new HttpResponse(exchange);
		try {
			RequestMethod reqMethodReceived = RequestMethod.fromString(exchange.getRequestMethod());
			if (reqMethodReceived != null) {
				boolean isHandled = searchAndExecuteChain(exchange, reqMethodReceived, httpResponse);
				if (isHandled) return;
			}
			httpResponse.sendStatus(404);
		}
		catch (JsonParseException e) {
			logger.problem(e);
			httpResponse.status(400).sendJson(Map.of("messages", List.of(e.getClass(), e.getMessage())));
		}
	}

	/**
	 * Registers a handler and associates it with its route and filters.
	 *
	 * <p>
	 * Filters declared via annotations on the handler class
	 * (e.g. {@code @HttpRequestFilters}) are used by default.
	 * </p>
	 *
	 * <p>
	 * If no filters are explicitly provided, the filters defined by annotations
	 * are instantiated and applied automatically.
	 * </p>
	 *
	 * <p>
	 * If filter instances are provided to this method, they are used instead of
	 * creating new instances via reflection. This allows filters to:
	 * <ul>
	 *     <li>Use parameterized constructors</li>
	 *     <li>Receive external configuration or dependencies</li>
	 *     <li>Share state between handlers if desired</li>
	 * </ul>
	 *
	 * @param handler the handler instance to register
	 * @param filters optional filter instances; if empty, annotation-declared
	 *                filters are used
	 * @throws InvocationTargetException if reflective instantiation fails
	 * @throws NoSuchMethodException     if a required constructor is missing
	 * @throws InstantiationException    if a filter or handler cannot be instantiated
	 * @throws IllegalAccessException    if a constructor is not accessible
	 */
	public void registerHandler(Handler handler, Filter... filters) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		Map<String, Object> handlerData = PathUtil.getHandlerData(handler, basePath);
		RequestMethod requestMethod = RequestMethod.fromString((String) handlerData.get("method"));
		String[] pathSegments = (String[]) handlerData.get("pathSegments");
		filters = filters.length == 0 ? (Filter[]) handlerData.get("filters") : filters;
		requestMethodToHandlersMap.computeIfAbsent(requestMethod, k -> new RouteNode());
		requestMethodToHandlersMap.get(requestMethod).registerNode(pathSegments, handler, getFiltersWithGlobalsAdded(filters));
		logger.info(requestMethod.name() + " Handler registered", PathUtil.pathFromSegments(pathSegments), "Filters: " + filters.length, "Handler: " + handler.getClass().getName());
	}

	/**
	 * Registers multiple handlers using their annotation-defined configuration.
	 *
	 * <p>
	 * Each handler is processed exactly as if {@link #registerHandler(Handler, Filter...)}
	 * were called individually with no explicitly provided filters.
	 *
	 * <p>
	 * This means:
	 * <ul>
	 *     <li>Routing information (path and HTTP method) is resolved from annotations</li>
	 *     <li>Filters declared via annotations are instantiated and applied</li>
	 *     <li>Global filters are automatically prepended</li>
	 * </ul>
	 *
	 * <p>
	 * This method is best suited for simple use cases where filters:
	 * <ul>
	 *     <li>Have no constructor parameters</li>
	 *     <li>Do not require external configuration</li>
	 *     <li>Can be safely instantiated via reflection</li>
	 * </ul>
	 *
	 * <p>
	 * For handlers that require filters with parameterized constructors
	 * or externally managed dependencies, use
	 * {@link #registerHandler(Handler, Filter...)} and provide filter instances
	 * explicitly.
	 * </p>
	 *
	 * @param handlers one or more handler instances to register
	 * @throws InvocationTargetException if reflective instantiation of a handler
	 *                                   or its filters fails
	 * @throws NoSuchMethodException     if a required constructor is missing
	 * @throws InstantiationException    if a handler or filter cannot be instantiated
	 * @throws IllegalAccessException    if a constructor is not accessible
	 */
	public void registerHandlers(Handler... handlers) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		for (var handler : handlers) {
			registerHandler(handler);
		}
	}

	/**
	 * Searches for a matching route and executes the filter chain.
	 *
	 * @return {@code true} if a matching handler was found
	 */
	private boolean searchAndExecuteChain(HttpExchange exchange, RequestMethod reqMethodReceived, Response response) throws IOException {
		String pathReceived = URLDecoder.decode(exchange.getRequestURI().getPath(), StandardCharsets.UTF_8);
		String[] pathSegments = PathUtil.segmentsOfPath(pathReceived);
		RouteNode root = requestMethodToHandlersMap.get(reqMethodReceived);

		if (root == null) {
			response.sendStatus(404);
			return true;
		}

		SearchResult searchResult = root.getNodeAtPath(pathSegments, 0, new HashMap<>());
		if (searchResult != null) {
			Request request = getRequestData(exchange, response, pathReceived, searchResult.params());
			if (request == null) return true;
			Chain filterChain = new FilterChain(searchResult.routeNode().getFilers(), searchResult.routeNode().getHandler());
			filterChain.next(request, response);
			return true;
		}
		return false;
	}

	/**
	 * Builds a {@link Request} object from the {@link HttpExchange}.
	 *
	 * <p>
	 * Enforces request body size limits and reads the entire request body
	 * into memory.
	 */
	private Request getRequestData(
			HttpExchange exchange,
			Response response,
			String pathReceived,
			Map<String, String> params
	) throws IOException {
		RequestMethod requestMethod = RequestMethod.fromString(exchange.getRequestMethod());
		String query = exchange.getRequestURI().getQuery();
		InetSocketAddress remoteAddress = exchange.getRemoteAddress();
		InetSocketAddress localAddress = exchange.getLocalAddress();
		Headers headers = exchange.getRequestHeaders();

		try (InputStream reqBodyIs = exchange.getRequestBody()) {
			byte[] bodyBytes;
			if (maxBodyBytes != -1) {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				byte[] chunk = new byte[8192];
				int total = 0;
				int b;
				while ((b = reqBodyIs.read(chunk)) != -1) {
					total += b;
					if (total > maxBodyBytes) {
						logger.warning("HttpResponse body size too big", requestMethod.name(), pathReceived);
						response.sendStatus(413);
						return null;
					}
					buffer.write(chunk, 0, b);
				}
				bodyBytes = buffer.toByteArray();
			} else {
				bodyBytes = reqBodyIs.readAllBytes();
			}

			return new HttpRequest(
					pathReceived,
					requestMethod,
					remoteAddress,
					localAddress,
					params,
					query,
					headers,
					bodyBytes
			);
		}
	}

	/**
	 * Sets filters that are applied to all handlers.
	 *
	 * @param globalFilters filters to apply to all handlers
	 */
	public void setGlobalFilters(Filter... globalFilters) {
		this.globalFilters = globalFilters;
	}

	/**
	 * Returns the executor.
	 *
	 * @return the {@code Executor} established for this server or {@code null} if not set
	 */
	public Executor getExecutor() {
		return this.executor;
	}

	/**
	 * Sets the executor used by the underlying HTTP server. Defaults to {@code null}
	 *
	 * @param executor the {@code Executor} to set or {@code null} to use the default implementation
	 */
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	/**
	 * Starts the HTTP server using the configured settings.
	 *
	 * <p>
	 * This method:
	 * <ul>
	 *     <li>Registers this application as the root {@link HttpHandler}</li>
	 *     <li>Applies the configured {@link Executor}, if any</li>
	 *     <li>Starts the underlying {@link HttpServer}</li>
	 * </ul>
	 *
	 * <p>
	 * This overload does not register a startup callback.
	 * To execute custom logic after the server has started,
	 * use {@link #start(OnServerStart)}.
	 */
	public void start() {
		start(null);
	}

	/**
	 * Starts the HTTP server and optionally executes a callback after startup.
	 *
	 * <p>
	 * The provided callback is invoked <b>after</b> the server has successfully
	 * started and is ready to accept requests.
	 *
	 * <p>
	 * Typical use cases include:
	 * <ul>
	 *     <li>Logging startup information</li>
	 *     <li>Initializing background tasks</li>
	 *     <li>Registering external resources</li>
	 * </ul>
	 *
	 * @param cb optional startup callback executed after the server starts;
	 *           may be {@code null}
	 */
	public void start(OnServerStart cb) {
		this.server.createContext("/", this);
		this.server.setExecutor(executor);
		this.server.start();

		if (cb != null) cb.run();
	}

	/**
	 * Stops the HTTP server after a given delay.
	 *
	 * <p>
	 * The delay allows in-flight exchanges to complete before
	 * the server is fully shut down.
	 *
	 * <p>
	 * This overload does not execute a cleanup callback.
	 * To perform cleanup logic before shutdown,
	 * use {@link #stop(int, CleanupCallback)}.
	 *
	 * @param delay delay in seconds before the server is stopped
	 */
	public void stop(int delay) {
		stop(delay, null);
	}

	/**
	 * Stops the HTTP server after a given delay and optionally executes
	 * a cleanup callback.
	 *
	 * <p>
	 * The callback is executed <b>before</b> the underlying
	 * {@link HttpServer} is stopped.
	 *
	 * <p>
	 * Typical use cases include:
	 * <ul>
	 *     <li>Releasing resources</li>
	 *     <li>Flushing logs</li>
	 *     <li>Gracefully shutting down background tasks</li>
	 * </ul>
	 *
	 * @param delay delay in seconds before the server is stopped
	 * @param cb    optional cleanup callback executed before stopping;
	 *              may be {@code null}
	 */
	public void stop(int delay, CleanupCallback cb) {
		if (cb != null) cleanup(cb);

		this.server.stop(delay);
	}

	/**
	 * Combines global filters with handler-specific filters. Globals are added first
	 */
	private Filter[] getFiltersWithGlobalsAdded(Filter... filters) {
		if (globalFilters == null || globalFilters.length == 0) return filters;

		List<Filter> result = new LinkedList<>();

		result.addAll(Arrays.asList(globalFilters));
		result.addAll(Arrays.asList(filters));

		return result.toArray(Filter[]::new);
	}

	/**
	 * Sets a custom logger.
	 *
	 * @param logger custom {@code JandleLogger} logger. If {@code null} is set the default {@code TraceLogger} logger is used.
	 */
	public void setLogger(JandleLogger logger) {
		if (logger != null)
			this.logger = logger;
	}

	/**
	 * Executes the provided cleanup callback.
	 *
	 * @param cb cleanup callback to execute
	 */
	public void cleanup(CleanupCallback cb) {
		cb.run();
	}
}
