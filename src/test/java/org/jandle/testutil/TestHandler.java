package org.jandle.testutil;

import org.jandle.api.http.Handler;
import org.jandle.api.http.Request;
import org.jandle.api.http.Response;

public class TestHandler implements Handler {

	private boolean called = false;

	@Override
	public void handle(Request httpRequest, Response httpResponse) {
		called = true;
	}

	public boolean wasCalled() {
		return called;
	}
}
