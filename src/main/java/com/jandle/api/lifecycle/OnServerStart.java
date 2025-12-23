package com.jandle.api.lifecycle;

/**
 * Callback invoked after the HTTP server has successfully started.
 *
 * <p>
 * This callback is executed once the server is fully initialized and
 * ready to accept incoming requests.
 *
 * <p>
 * Typical use cases include:
 * <ul>
 *     <li>Logging startup information</li>
 *     <li>Initializing background tasks</li>
 *     <li>Registering external resources</li>
 * </ul>
 */
@FunctionalInterface
public interface OnServerStart {
	/**
	 * Executes logic after the server has started.
	 */
	void run();
}
