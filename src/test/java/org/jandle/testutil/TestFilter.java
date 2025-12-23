package org.jandle.testutil;

import org.jandle.api.http.Chain;
import org.jandle.api.http.Filter;
import org.jandle.api.http.Request;
import org.jandle.api.http.Response;

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
