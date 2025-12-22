package com.jandle.api.annotations;

import com.jandle.api.JandleApplication;
import com.jandle.api.http.Filter;
import com.jandle.internal.http.FilterChain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attaches one or more HTTP filters to a request handler.
 * <p>
 * Filters declared via this annotation are executed in the order they
 * are listed, before the handler logic is invoked.
 * <p>
 * Each filter class <strong>must</strong> provide a public no-argument
 * constructor, as instances are created reflectively by the framework.
 *
 * <p>
 * If a filter requires constructor arguments (e.g. configuration, services),
 * it must be registered programmatically using
 * {@code JandleApplication.registerHandler(Handler handler, Filter... filters)} instead of this annotation.
 *
 * <p><strong>Example</strong></p>
 * <pre>{@code
 * @HttpRequestFilters({
 *     AuthFilter.class,
 *     RateLimiter.class
 * })
 * public class ProtectedHandler implements Handler {
 *     // ...
 * }
 * }</pre>
 *
 * <p><strong>Filters with Constructors</strong></p>
 * <pre>{@code
 * JandleApplication app = new JandleApplication();
 *
 * RateLimiter rateLimiter =
 *     new RateLimiter(tokenStorage, config);
 *
 * app.registerHandler(
 *     new MyHandler(),
 *     rateLimiter
 * );
 * }</pre>
 *
 * @see JandleApplication
 * @see Filter
 * @see FilterChain
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpRequestFilters {
	/**
	 * The filter classes to be applied to the annotated handler.
	 *
	 * @return an array of filter classes
	 */
	Class<? extends Filter>[] value();
}
