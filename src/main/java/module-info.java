module org.jandle {
	requires com.google.gson;
	requires jdk.httpserver;

	exports org.jandle.api;
	exports org.jandle.api.annotations;
	exports org.jandle.api.http;
	exports org.jandle.api.cookies;
	exports org.jandle.api.cors;
	exports org.jandle.api.ratelimiter;
	exports org.jandle.api.lifecycle;
	exports org.jandle.api.exceptions;
	exports org.jandle.api.logger;
}