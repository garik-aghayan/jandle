package org.jandle.api.annotations;

import org.jandle.api.http.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an HTTP request handler.
 * <p>
 * Classes annotated with {@code @HttpRequestHandler} are discovered at runtime
 * and registered as route handlers for the specified HTTP method and path.
 * <p>
 * The annotated class is expected to implement the framework's
 * {@code Handler} interface.
 *
 * <p><strong>Example</strong></p>
 * <pre>{@code
 * @HttpRequestHandler(path = "/users/{id}", method = RequestMethod.GET)
 * public class GetUserHandler implements Handler {
 *     @Override
 *     public void handle(HttpRequest request, HttpResponse response) {
 *         // handle request
 *     }
 * }
 * }</pre>
 *
 * @see RequestMethod
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpRequestHandler {
	/**
	 * The URL path this handler is responsible for.
	 * <p>
	 * May include path parameters and wildcards {@code "*"},{@code "**"} (e.g. {@code /users/{id}}).
	 *
	 * @return the request path pattern
	 */
	String path();
	/**
	 * The HTTP method this handler responds to.
	 *
	 * @return the supported HTTP request method
	 */
	RequestMethod method();
}
