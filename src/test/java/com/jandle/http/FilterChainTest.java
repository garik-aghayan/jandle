package com.jandle.http;

import com.jandle.api.http.Filter;
import com.jandle.api.http.Handler;
import com.jandle.internal.http.FilterChain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilterChainTest {

	@Test
	void filtersExecutedInOrder_thenHandler() throws Exception {
		StringBuilder trace = new StringBuilder();

		Filter f1 = (req, res, chain) -> {
			trace.append("A");
			chain.next(req, res);
		};

		Filter f2 = (req, res, chain) -> {
			trace.append("B");
			chain.next(req, res);
		};

		Handler handler = (req, res) -> trace.append("H");

		FilterChain filterChain = new FilterChain(new Filter[]{f1, f2}, handler);
		filterChain.next(null, null);

		assertEquals("ABH", trace.toString());
	}

	@Test
	void filterStopsChain_handlerNotExecuted() throws Exception {
		StringBuilder trace = new StringBuilder();

		Filter blocker = (req, res, chain) -> trace.append("X");
		Handler handler = (req, res) -> trace.append("H");

		FilterChain filterChain = new FilterChain(new Filter[]{blocker}, handler);
		filterChain.next(null, null);

		assertEquals("X", trace.toString());
	}
}
