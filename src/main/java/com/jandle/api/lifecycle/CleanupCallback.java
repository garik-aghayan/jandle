package com.jandle.api.lifecycle;

/**
 * Callback representing a cleanup operation explicitly invoked by the framework.
 *
 * <p>
 * This callback is intended for releasing or resetting resources such as:
 * <ul>
 *     <li>Closing external connections</li>
 *     <li>Clearing caches</li>
 *     <li>Resetting application state</li>
 * </ul>
 */
@FunctionalInterface
public interface CleanupCallback {
	/**
	 * Executes the cleanup logic.
	 */
	void run();
}
