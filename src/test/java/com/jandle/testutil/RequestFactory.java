package com.jandle.testutil;

import com.jandle.internal.http.HttpRequest;
import com.jandle.api.http.RequestMethod;
import com.sun.net.httpserver.Headers;

import java.net.InetSocketAddress;
import java.util.Map;

public final class RequestFactory {

	private RequestFactory() {}

	public static HttpRequest withIp(String ip, RequestMethod method) {
		return new HttpRequest(
				"/",
				method,
				new InetSocketAddress(ip, 12345),
				new InetSocketAddress("127.0.0.1", 8080),
				Map.of(),
				"",
				new Headers(),
				new byte[0]
		);
	}

	public static HttpRequest withoutRemoteAddress(RequestMethod method) {
		return new HttpRequest(
				"/",
				method,
				null,
				new InetSocketAddress("127.0.0.1", 8080),
				Map.of(),
				"",
				new Headers(),
				new byte[0]
		);
	}
}
