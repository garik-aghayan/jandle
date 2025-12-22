package com.jandle.internal.ratelimiter;

import com.jandle.api.ratelimiter.TokenStorage;
import com.jandle.internal.logger.TraceLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory {@link TokenStorage} implementation backed by a
 * {@link ConcurrentHashMap}.
 *
 * <p>
 * Each client is mapped to a mutable {@link TokenBucket} instance stored
 * directly in memory. Buckets are cleaned up periodically if they have
 * not been accessed for a configurable idle timeout.
 * </p>
 *
 * <h2>Idle Bucket Cleanup</h2>
 * <p>
 * A background daemon thread periodically removes buckets whose last access
 * time exceeds the configured idle timeout. This prevents unbounded memory
 * growth when clients stop sending requests.
 * </p>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * The storage map is thread-safe. Individual {@link TokenBucket} instances
 * are expected to be synchronized externally when modified.
 * </p>
 *
 * <h2>Lifecycle</h2>
 * <p>
 * This implementation creates a background cleanup scheduler.
 * {@link #release()} should be called during application shutdown to
 * terminate the scheduler gracefully.
 * </p>
 */
public final class DefaultTokenStorage implements TokenStorage {
	private static final long DEFAULT_IDLE_TIMEOUT_NANOS = 10 * 60 * 1_000_000_000L;
	private final ScheduledExecutorService cleaner =
			Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "rate-limit-cleaner");
				t.setDaemon(true);
				t.setUncaughtExceptionHandler((th, ex) ->
						new TraceLog().problem(ex, "Rate-limit cleaner crashed")
				);
				return t;
			});
	private final Map<String, TokenBucket> storage = new ConcurrentHashMap<>();

	/**
	 * Creates an in-memory token storage with a custom idle timeout.
	 *
	 * @param idleTimeoutNanos maximum allowed idle time (in nanoseconds)
	 *                         before a bucket is eligible for removal
	 */
	public DefaultTokenStorage(long idleTimeoutNanos) {
		startCleanupTask(idleTimeoutNanos);
	}

	/**
	 * Creates an in-memory token storage using the default idle timeout (10 minutes).
	 *
	 */
	public DefaultTokenStorage() {
		startCleanupTask(DEFAULT_IDLE_TIMEOUT_NANOS);
	}

	/**
	 * Returns the {@link TokenBucket} associated with the given client IP,
	 * creating a new bucket if none exists.
	 *
	 * <p>
	 * The returned bucket instance is stored directly in memory and is
	 * mutable. Callers are responsible for synchronizing on the bucket
	 * instance when performing compound operations.
	 * </p>
	 *
	 * @param ip       client identifier (typically an IP address)
	 * @param capacity maximum capacity of the token bucket
	 * @return an existing or newly created token bucket
	 */
	@Override
	public TokenBucket getOrCreateTokenBucket(String ip, int capacity) {
		return storage.computeIfAbsent(ip, k -> new TokenBucket(capacity));
	}

	/**
	 * Starts the background cleanup task that periodically removes idle
	 * token buckets.
	 *
	 * <p>
	 * Buckets are considered idle if the time elapsed since their last
	 * access exceeds {@code idleTimeoutNanos}.
	 * </p>
	 *
	 * <p>
	 * The cleanup interval is derived from the idle timeout to balance
	 * responsiveness and overhead.
	 * </p>
	 *
	 * @param idleTimeoutNanos maximum allowed idle time (in nanoseconds)
	 */
	private void startCleanupTask(long idleTimeoutNanos) {
		long cleanupIntervalMinutes = Math.max(1, idleTimeoutNanos / (2 * 60_000_000_000L));
		cleaner.scheduleAtFixedRate(() -> {
			long now = System.nanoTime();

			storage.entrySet().removeIf(entry -> {
				TokenBucket bucket = entry.getValue();
				return now - bucket.getLastAccess() > idleTimeoutNanos;
			});
		}, 5, cleanupIntervalMinutes, TimeUnit.MINUTES);
	}

	/**
	 * Shuts down the background cleanup scheduler.
	 *
	 * <p>
	 * This method should be called during application shutdown to ensure
	 * that the cleanup thread terminates gracefully.
	 * </p>
	 */
	@Override
	public void release() {
		cleaner.shutdown();
	}
}
