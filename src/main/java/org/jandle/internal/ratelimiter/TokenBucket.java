package org.jandle.internal.ratelimiter;

/**
 * Represents a token bucket used for rate limiting.
 *
 * <p>The bucket holds a current token count and tracks timestamps for
 * the last refill and last access. This class does not perform any
 * synchronization; callers are responsible for external synchronization
 * when accessing or mutating its state.</p>
 *
 * <p>All time values are expected to be in {@code System.nanoTime()} units.</p>
 */
final public class TokenBucket {
	private double tokens;
	private long lastRefill;
	private long lastAccess;

	/**
	 * Creates a new token bucket initialized to full capacity.
	 *
	 * @param capacity the maximum number of tokens this bucket can hold
	 */
	public TokenBucket(double capacity) {
		this.tokens = capacity;
		this.lastRefill = System.nanoTime();
		this.lastAccess = this.lastRefill;
	}

	/**
	 * Returns the current number of tokens in the bucket
	 * @return {@code double} number of tokens in the bucket
	 */
	public double getTokens() {
		return tokens;
	}

	/**
	 * Attempts to consume 1 token.
	 *
	 * @return {@code true} if enough tokens were available and consumed;
	 *         {@code false} otherwise
	 */
	public boolean tryConsume() {
		if (this.tokens >= 1) {
			this.tokens --;
			return true;
		}
		return false;
	}

	/**
	 * Refills the bucket with newly generated tokens.
	 *
	 * @param lastRefillTime timestamp of this refill operation
	 * @param newTokens      number of tokens to add
	 * @param capacity       maximum allowed token capacity
	 */
	public void refill(long lastRefillTime, double newTokens, double capacity) {
		this.tokens = Math.min(capacity, getTokens() + newTokens);
		lastRefill = lastRefillTime;
	}

	/**
	 * Returns timestamp of the last refill operation
	 * @return timestamp of the last refill operation
	 */
	public long getLastRefill() {
		return lastRefill;
	}

	/**
	 * Returns timestamp of the last access to this bucket
	 * @return timestamp of the last access to this bucket
	 */
	public long getLastAccess() {
		return lastAccess;
	}

	/**
	 * Updates the last-access timestamp.
	 *
	 * @param now current time in nanoseconds
	 */
	public void updateLastAccessedTime(long now) {
		this.lastAccess = now;
	}
}
