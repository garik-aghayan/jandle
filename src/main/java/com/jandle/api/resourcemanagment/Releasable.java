package com.jandle.api.resourcemanagment;

/**
 * Represents a component that holds resources requiring explicit cleanup.
 *
 * <p>
 * {@code Releasable} is typically implemented by filters or other
 * framework components that manage resources such as:
 * </p>
 * <ul>
 *   <li>Threads or executors</li>
 *   <li>Open connections</li>
 *   <li>Timers or schedulers</li>
 * </ul>
 *
 * <p>
 * The {@link #release()} method is invoked during application shutdown
 * to allow graceful resource cleanup.
 * </p>
 */
public interface Releasable {
	/**
	 * Releases any held resources.
	 *
	 * <p>
	 * Implementations should ensure this method is idempotent
	 * and safe to call during shutdown.
	 * </p>
	 */
	void release();
}
