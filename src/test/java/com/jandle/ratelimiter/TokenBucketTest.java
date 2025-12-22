package com.jandle.ratelimiter;

import com.jandle.internal.ratelimiter.TokenBucket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketTest {

	@Test
	void startsWithFullCapacity() {
		TokenBucket bucket = new TokenBucket(5);

		assertEquals(5.0, bucket.getTokens());
	}

	@Test
	void consumesTokenWhenAvailable() {
		TokenBucket bucket = new TokenBucket(1);

		assertTrue(bucket.tryConsume());
		assertEquals(0.0, bucket.getTokens());
	}

	@Test
	void doesNotConsumeWhenEmpty() {
		TokenBucket bucket = new TokenBucket(0);

		assertFalse(bucket.tryConsume());
		assertEquals(0.0, bucket.getTokens());
	}

	@Test
	void refillAddsTokensUpToCapacity() {
		TokenBucket bucket = new TokenBucket(5);

		bucket.tryConsume(); // 4 tokens
		bucket.refill(System.nanoTime(), 10, 5);

		assertEquals(5.0, bucket.getTokens());
	}

	@Test
	void updatesLastAccessTime() {
		TokenBucket bucket = new TokenBucket(1);
		long now = System.nanoTime();

		bucket.updateLastAccessedTime(now);

		assertEquals(now, bucket.getLastAccess());
	}
}
