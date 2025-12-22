package com.jandle.api.ratelimiter;

import com.jandle.api.http.Chain;
import com.jandle.api.http.Filter;
import com.jandle.api.http.Request;
import com.jandle.api.http.Response;
import com.jandle.api.resourcemanagment.Releasable;
import com.jandle.internal.ratelimiter.DefaultTokenStorage;
import com.jandle.internal.ratelimiter.TokenBucket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * {@link Filter} implementation that applies per-client rate limiting
 * using the <b>token bucket</b> algorithm.
 *
 * <p>
 * Each client (identified by IP address) is assigned an independent
 * {@link TokenBucket}. Tokens are replenished continuously over time.
 * If a request arrives when insufficient tokens are available, the
 * request is rejected with HTTP {@code 429 Too Many Requests}.
 * </p>
 *
 * <h2>Algorithm</h2>
 * <ul>
 *   <li>Each bucket has a fixed maximum capacity.</li>
 *   <li>Tokens are added at a constant rate ({@code tokensPerSecond}).</li>
 *   <li>Each incoming request consumes one token.</li>
 * </ul>
 *
 * <h2>Rate Limit Headers</h2>
 * <p>
 * This filter sets standard and de-facto rate limit headers on every response:
 * </p>
 * <ul>
 *   <li>{@code RateLimit-Limit} / {@code X-RateLimit-Limit}</li>
 *   <li>{@code RateLimit-Remaining} / {@code X-RateLimit-Remaining}</li>
 *   <li>{@code RateLimit-Reset} / {@code X-RateLimit-Reset}</li>
 *   <li>{@code RateLimit-Policy} / {@code X-RateLimit-Policy}</li>
 * </ul>
 *
 * <p>
 * When a request is rejected, {@code Retry-After} and {@code X-Retry-After}
 * headers are also included.
 * </p>
 *
 * <h2>Storage Abstraction</h2>
 * <p>
 * The filter delegates bucket storage to a {@link TokenStorage} implementation,
 * allowing the rate limiter to work with in-memory, distributed, or persistent
 * storage backends.
 * </p>
 *
 * <h2>Concurrency</h2>
 * <p>
 * Synchronization is performed at the individual bucket level, minimizing
 * contention between unrelated clients.
 * </p>
 */
public class RateLimiter implements Filter, Releasable {
	private final int bucketCapacity;
	private final double tokensPerSecond;
	private final TokenStorage tokenStorage;

	/**
	 * Creates a rate limiting filter using the given token storage.
	 *
	 * @param storage          storage backend for token buckets
	 * @param bucketCapacity   maximum number of tokens per bucket
	 * @param tokensPerSecond  token refill rate
	 */
	public RateLimiter(TokenStorage storage, int bucketCapacity, double tokensPerSecond) {
		this.tokenStorage = storage;
		this.bucketCapacity = bucketCapacity;
		this.tokensPerSecond = tokensPerSecond;
	}

	/**
	 * Creates a rate limiting filter with in-memory storage and default
	 * cleanup behavior.
	 *
	 * @param bucketCapacity   maximum number of tokens per bucket
	 * @param tokensPerSecond  token refill rate
	 */
	public RateLimiter(int bucketCapacity, double tokensPerSecond) {
		this(new DefaultTokenStorage(), bucketCapacity, tokensPerSecond);
	}

	/**
	 * Creates a rate limiting filter with in-memory storage and a custom
	 * idle timeout for unused buckets.
	 *
	 * @param bucketCapacity    maximum number of tokens per bucket
	 * @param tokensPerSecond   token refill rate
	 * @param idleTimeoutNanos  maximum idle time before bucket cleanup
	 */
	public RateLimiter(int bucketCapacity, double tokensPerSecond, long idleTimeoutNanos) {
		this(new DefaultTokenStorage(idleTimeoutNanos), bucketCapacity, tokensPerSecond);
	}

	/**
	 * Applies rate limiting to the incoming httpRequest.
	 *
	 * <p>
	 * Each httpRequest consumes one token from the client's bucket. If no
	 * token is available, the httpRequest is rejected with HTTP {@code 429}
	 * and appropriate rate limit headers are set.
	 * </p>
	 *
	 * <p>
	 * When rate limiting is applied, standard and legacy rate limit headers
	 * are added to the httpResponse regardless of whether the httpRequest is
	 * accepted or rejected.
	 * </p>
	 *
	 * @param httpRequest  incoming httpRequest
	 * @param httpResponse outgoing httpResponse
	 * @param filterChain    filter filterChain
	 *
	 * @throws IOException if an I/O error occurs while handling the httpResponse
	 */
	@Override
	public void doFilter(Request httpRequest, Response httpResponse, Chain filterChain) throws IOException {
		InetSocketAddress remoteAddress = httpRequest.getRemoteAddress();

		if (remoteAddress == null) {
			filterChain.next(httpRequest, httpResponse);
			return;
		}

		InetAddress address = remoteAddress.getAddress();
		String ip = address == null ? remoteAddress.getHostName() : address.getHostAddress();

		long now = System.nanoTime();

		TokenBucket bucket = tokenStorage.getOrCreateTokenBucket(ip, bucketCapacity);

		synchronized (bucket) {
			refill(bucket, now);
			bucket.updateLastAccessedTime(now);

			boolean consumed = bucket.tryConsume();

			tokenStorage.updateTokenBucket(ip, bucket);

			int remaining = (int) Math.floor(bucket.getTokens());
			double missing = Math.max(0, 1.0 - bucket.getTokens());
			long resetSeconds = (long) Math.ceil(missing / tokensPerSecond);

			String limit = String.valueOf(bucketCapacity);
			String reset = String.valueOf(resetSeconds);
			String policy = "token-bucket; capacity=" + bucketCapacity + "; refill=" + tokensPerSecond + "/s";

			httpResponse.header("RateLimit-Limit", limit)
					.header("X-RateLimit-Limit", limit)
					.header("RateLimit-Remaining", String.valueOf(remaining))
					.header("X-RateLimit-Remaining", String.valueOf(remaining))
					.header("RateLimit-Reset", reset)
					.header("X-RateLimit-Reset", reset)
					.header("RateLimit-Policy", policy)
					.header("X-RateLimit-Policy", policy);

			if (!consumed) {
				httpResponse.header("Retry-After", reset)
						.header("X-Retry-After", reset);

				httpResponse.sendStatus(429);
				return;
			}

			filterChain.next(httpRequest, httpResponse);
		}
	}

	/**
	 * Refills the given token bucket based on elapsed time.
	 *
	 * <p>The number of tokens added is calculated using the time difference
	 * between the current timestamp and the bucket's last refill timestamp,
	 * multiplied by {@code tokensPerSecond}. The bucket is then capped at
	 * {@code tokenCapacity}.</p>
	 *
	 * <p>This method does not perform any synchronization and must be called
	 * while holding the bucket's intrinsic lock.</p>
	 *
	 * @param bucket the token bucket to refill
	 * @param now    the current time in nanoseconds (from {@code System.nanoTime()})
	 */
	private void refill(TokenBucket bucket, long now) {
		double secondsElapsed = (now - bucket.getLastRefill()) / 1_000_000_000.0;
		if (secondsElapsed <= 0) return;

		double newTokens = secondsElapsed * tokensPerSecond;
		bucket.refill(now, newTokens, bucketCapacity);
	}


	/**
	 * Shuts down resources associated with this rate limiter.
	 *
	 * <p>
	 * This method delegates to the underlying {@link TokenStorage} and
	 * should be invoked during application shutdown.
	 * </p>
	 */
	@Override
	public void release() {
		tokenStorage.release();
	}
}
