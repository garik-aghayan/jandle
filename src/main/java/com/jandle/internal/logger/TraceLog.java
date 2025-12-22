package com.jandle.internal.logger;

import com.jandle.api.logger.JandleLogger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Default console-based implementation of {@link JandleLogger} that outputs
 * structured, timestamped, and color-coded log messages.
 *
 * <p>
 * {@code TraceLog} is intended primarily for development and debugging.
 * It prints log entries to standard output using ANSI color codes:
 * </p>
 *
 * <ul>
 *   <li><b>INFO</b> – blue</li>
 *   <li><b>WARNING</b> – yellow</li>
 *   <li><b>PROBLEM</b> – red</li>
 * </ul>
 *
 * <p>
 * Each log entry includes a UTC timestamp and optional contextual messages.
 * When a {@link Throwable} is provided, its cause, message, and full stack
 * trace are printed in a readable format.
 * </p>
 *
 * <p>
 * This logger is lightweight and has no external dependencies, but it is
 * not intended for high-performance or production-grade logging.
 * </p>
 */
public class TraceLog implements JandleLogger {
	/**
	 * Creates a new {@code TraceLog} logger instance.
	 *
	 * <p>The logger is immediately usable and outputs formatted,
	 * colorized log messages to standard output.</p>
	 */
	public TraceLog() {
	}

	/**
	 * Logs a message with an optional {@link Throwable}.
	 *
	 * <p>
	 * The output includes a UTC timestamp followed by all provided messages.
	 * If a throwable is present, its classname, message, and stack trace are printed.
	 * </p>
	 *
	 * @param t        an optional throwable to log (may be {@code null})
	 * @param messages contextual messages to include in the log entry
	 */
	public void log(Throwable t, String... messages) {
		System.out.print(" " + getFormattedDate());
		for (var message : messages) {
			System.out.print(" | " + message);
		}
		System.out.println();
		if (t != null) {
			System.out.println("[Throwable.class]:");
			System.out.println(" - " + t.getClass().getSimpleName());
			System.out.println("[Throwable.message]:");
			System.out.println(" - " + t.getMessage());
			System.out.println("[Throwable.stackTrace]:");
			for (var elm : t.getStackTrace()) {
				System.out.println(" - " + elm);
			}
		}
	}

	/**
	 * Logs a message without an associated throwable.
	 *
	 * @param messages contextual messages to include in the log entry
	 */
	public void log(String... messages) {
		log(null, messages);
	}

	/**
	 * Logs an informational message.
	 *
	 * <p>
	 * INFO logs are printed in blue and are typically used to indicate
	 * normal application flow and successful operations.
	 * </p>
	 *
	 * @param messages informational messages to log
	 */
	@Override
	public void info(String... messages) {
		System.out.print("\u001B[34m");
		System.out.println("--------");
		System.out.print("| INFO |");
		log(messages);
		System.out.println("--------");
		resetColor();
	}

	/**
	 * Logs a warning message.
	 *
	 * <p>
	 * WARNING logs are printed in yellow and indicate non-fatal issues
	 * or potentially unexpected situations.
	 * </p>
	 *
	 * @param messages warning messages to log
	 */
	@Override
	public void warning(String... messages) {
		System.out.print("\u001B[33m");
		System.out.println("-----------");
		System.out.print("| WARNING |");
		log(messages);
		System.out.println("-----------");
		resetColor();
	}

	/**
	 * Logs a problem message with an associated {@link Throwable}.
	 *
	 * <p>
	 * PROBLEM logs are printed in red and are intended for errors,
	 * exceptions, or critical failures.
	 * </p>
	 *
	 * @param t        the throwable associated with the problem
	 * @param messages contextual messages to include in the log entry
	 */
	@Override
	public void problem(Throwable t, String... messages) {
		System.out.print("\u001B[31m");
		System.out.println("-----------");
		System.out.print("| PROBLEM |");
		log(t, messages);
		System.out.println("-----------");
		resetColor();
	}

	/**
	 * Returns the current timestamp formatted in UTC.
	 *
	 * @return a formatted UTC timestamp string
	 */
	private String getFormattedDate() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss", Locale.ENGLISH)
				.withZone(ZoneId.of("UTC"));
		return formatter.format(Instant.ofEpochMilli(System.currentTimeMillis())) + " UTC";
	}

	/**
	 * Resets the console text color to the default.
	 */
	private void resetColor() {
		System.out.print("\u001B[39m");
	}
}
