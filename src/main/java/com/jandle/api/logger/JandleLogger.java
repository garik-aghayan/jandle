package com.jandle.api.logger;

/**
 * Abstraction for logging within the Jandle framework.
 *
 * <p>
 * {@code JandleLogger} allows the framework to remain independent
 * of any specific logging implementation.
 * </p>
 *
 * <p>
 * Implementations may log to the console, files, external systems,
 * or structured logging backends.
 * </p>
 */
public interface JandleLogger {
	/**
	 * Logs an informational message.
	 *
	 * @param messages informational messages to log
	 */
	void info(String... messages);

	/**
	 * Logs a warning message indicating a potential problem
	 * or unexpected situation.
	 *
	 * @param messages warning messages to log
	 */
	void warning(String... messages);

	/**
	 * Logs a problem message without an associated throwable.
	 *
	 * <p>
	 * This method delegates to {@link #problem(Throwable, String...)}
	 * with a {@code null} throwable.
	 * </p>
	 *
	 * @param messages problem-related messages
	 */
	default void problem(String... messages){
		problem(null, messages);
	};

	/**
	 * Logs a problem message with an associated throwable.
	 *
	 * <p>
	 * Intended for errors, exceptions, and critical failures.
	 * </p>
	 *
	 * @param t        the throwable associated with the problem (may be {@code null})
	 * @param messages contextual messages to log
	 */
	void problem(Throwable t, String... messages);
}
