package com.jandle.ratelimiter;

import com.jandle.internal.ratelimiter.DefaultTokenStorage;
import com.jandle.internal.ratelimiter.TokenBucket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultTokenStorageTest {

	@Test
	void returnsSameBucketForSameIp() {
		DefaultTokenStorage storage = new DefaultTokenStorage();

		TokenBucket a = storage.getOrCreateTokenBucket("1.2.3.4", 5);
		TokenBucket b = storage.getOrCreateTokenBucket("1.2.3.4", 5);

		assertSame(a, b);
	}

	@Test
	void createsDifferentBucketsForDifferentIps() {
		DefaultTokenStorage storage = new DefaultTokenStorage();

		TokenBucket a = storage.getOrCreateTokenBucket("1.1.1.1", 5);
		TokenBucket b = storage.getOrCreateTokenBucket("2.2.2.2", 5);

		assertNotSame(a, b);
	}

	@Test
	void releaseShutsDownCleanerGracefully() {
		DefaultTokenStorage storage = new DefaultTokenStorage();

		assertDoesNotThrow(storage::release);
	}
}
