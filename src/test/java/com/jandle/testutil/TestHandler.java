package com.jandle.testutil;

import com.jandle.api.http.Handler;
import com.jandle.api.http.Request;
import com.jandle.api.http.Response;

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
