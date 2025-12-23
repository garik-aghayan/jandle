package com.jandle.api.ratelimiter;

import com.jandle.api.lifecycle.Releasable;
import com.jandle.internal.ratelimiter.TokenBucket;

/**
 * Abstraction for storing and managing {@link TokenBucket} instances
 * used by the rate limiter.
 *
 * <p>This interface decouples the {@link RateLimiter} from the
 * underlying storage mechanism, allowing implementations backed by
 * in-memory maps, Redis, databases, or other external systems.</p>
 *
 * <h2>Concurrency Model</h2>
 * <p>
 * Implementations may return either:
 * </p>
 * <ul>
 *   <li>
 *     A <b>live, mutable</b> {@link TokenBucket} instance (typical for
 *     in-memory storage), or
 *   </li>
 *   <li>
 *     A <b>detached snapshot</b> of the bucket state (typical for remote
 *     or persistent storage).
 *   </li>
 * </ul>
 *
 * <p>
 * When a detached bucket is returned, implementations are responsible for
 * persisting updates inside {@link #updateTokenBucket(String, TokenBucket)}
 * in a thread-safe and atomic manner.
 * </p>
 *
 * <h2>Lifecycle</h2>
 * <p>
 * Implementations may allocate background resources (e.g. cleanup schedulers,
 * connections). Such resources should be released in {@link #release()}.
 * </p>
 */
public interface TokenStorage extends Releasable {
	/**
	 * Returns the {@link TokenBucket} associated with the given client key,
	 * creating one if it does not already exist.
	 *
	 * @param ip              client identifier (usually an IP address)
	 * @param bucketCapacity  maximum capacity of the token bucket
	 * @return a token bucket associated with the client
	 */
	TokenBucket getOrCreateTokenBucket(String ip, int bucketCapacity);

	/**
	 * Persists or updates the given token bucket for the specified client.
	 *
	 * <p>
	 * For in-memory implementations, this method is typically a no-op because
	 * the returned bucket instance is already live and mutable.
	 * </p>
	 *
	 * <p>
	 * For remote or persistent implementations, this method should atomically
	 * store the updated bucket state.
	 * </p>
	 *
	 * @param ip     client identifier
	 * @param bucket updated token bucket
	 */
	default void updateTokenBucket(String ip, TokenBucket bucket) {};

	/**
	 * Releases any resources held by this storage implementation.
	 *
	 * <p>
	 * Implementations that do not allocate resources may safely leave this
	 * method empty.
	 * </p>
	 */
	default void release() {};
}
