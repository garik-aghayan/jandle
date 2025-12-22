package com.jandle.testutil;

import com.jandle.api.http.Chain;
import com.jandle.api.http.Filter;
import com.jandle.api.http.Request;
import com.jandle.api.http.Response;

import java.io.IOException;

public class TestFilter implements Filter {

	private boolean called = false;
	private final boolean continueChain;

	public TestFilter(boolean continueChain) {
		this.continueChain = continueChain;
	}

	@Override
	public void doFilter(Request request, Response response, Chain chain) throws IOException {
		called = true;
		if (continueChain) {
			chain.next(request, response);
		}
	}

	public boolean wasCalled() {
		return called;
	}
}
