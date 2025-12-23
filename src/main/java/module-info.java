module com.jandle {
	requires com.google.gson;
	requires jdk.httpserver;

	exports com.jandle.api;
	exports com.jandle.api.annotations;
	exports com.jandle.api.http;
	exports com.jandle.api.cookies;
	exports com.jandle.api.cors;
	exports com.jandle.api.ratelimiter;
	exports com.jandle.api.lifecycle;
	exports com.jandle.api.exceptions;
	exports com.jandle.api.logger;
}