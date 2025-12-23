# Jandle

**Jandle** is a lightweight, modular Java HTTP framework designed for building HTTP servers and APIs with a strong focus on clarity, extensibility, and minimal boilerplate.  
It provides a clean abstraction over the Java HTTP server, offering routing, filters (middleware), CORS handling, cookies, rate limiting.

The framework is intentionally low-level enough to stay transparent, while still offering powerful building blocks commonly needed when writing backend services.

---

## ✨ Key Features

- 🚀 Simple application bootstrap via `JandleApplication`
- 🧩 Modular architecture with clear package boundaries
- 🛣️ HTTP request routing with handler abstraction
- 🔗 Filter chain (middleware-style request processing)
- 🌍 Built-in **CORS** support with configurable policies
- 🍪 Cookie creation and management utilities
- ⏱️ Token-bucket based **rate limiting**
- 📝 Pluggable logging abstraction
- 🧹 Resource lifecycle and cleanup helpers
- 📦 Java Module System (JPMS) friendly

---

## 📦 Module Overview

Jandle exposes a single Java module:

```
module com.jandle
```

### Package Structure

| Package | Description |
|-------|-------------|
| `com.jandle.api` | Core application bootstrap and public API |
| `com.jandle.api.annotations` | Annotations for handler/filter discovery |
| `com.jandle.api.http` | HTTP abstractions (Request, Response, Chain, Handler, Filter) |
| `com.jandle.api.cookies` | Cookie and cookie attribute APIs |
| `com.jandle.api.cors` | CORS filter and configuration classes |
| `com.jandle.api.exceptions` | Framework-specific exceptions |
| `com.jandle.api.logger` | Logging abstraction used by the framework |
| `com.jandle.api.ratelimiter` | Rate limiting filter implementation |
| `com.jandle.api.lifecycle` | Resource lifecycle & cleanup contracts |

Full API documentation is available here:  
👉 https://garik-aghayan.github.io/jandle/

---

## 🚀 Getting Started

### Creating an Application

```java
HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

JandleApplication app = new JandleApplication(
        server,
        "/api", // global base path. It will be prepended to all paths
        10 * 1024 * 1024 // max request body size in bytes
);

app.start();
```

This initializes:
- An HTTP server
- A base API path
- A request body size limit

---

## 🛣️ Handlers

Handlers process incoming HTTP requests.

```java
@HttpRequestHandler(path = "/hello", method = RequestMethod.GET)
@HttpRequestFilters({ LoggingFilter.class })
public class HelloHandler implements Handler {
    @Override
    public void handle(Request request, Response response) {
        response
            .status(200)
            .sendText("Hello from Jandle!");
    }
}
```

Registering a handler:

```java
app.registerHandlers(new HelloHandler());
```

---

## 🔗 Filters (Middleware)

Filters intercept requests before handlers.

```java
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(Request request, Response response, FilterChain chain) {
        System.out.println("Incoming request: " + request.getPath());
        chain.next(request, response);
    }
}
```

Filters can be applied per-route or globally.

Per route filters can be added as shown below (if you need to pass parameters to the constructor) or with just adding the ```@HttpRequestFilters({ ... })``` annotation to the handler (it will automatically call the no-args constructor).

```java

CustomFilter filter1 = new CustomFilter(arg1, arg2);
OtherFilter filter2 = new OtherFilter(arg);

app.registerHandler(new CustomHandler(), filter1, filter2);
```
Global filters are applied to every route of the app

```java

CustomFilter filter1 = new CustomFilter(arg1, arg2);
OtherFilter filter2 = new OtherFilter(arg);

app.setGlobalFilters(filter1, filter2);
```

---

## 🌍 CORS Support

Jandle includes support for Cross-Origin Resource Sharing.

```java
CorsConfig config = new CorsConfig();
config.setAllowCredentials(true);
config.addAllowedOrigin("https://example.com");

Cors cors = new Cors(config);

app.setGlobalFilters(cors);
```

Attach the CORS filter to routes or the entire application.

---

## 🍪 Set-Cookies

Set-Cookies are handled via a clean API

```java
ResponseCookie cookie = new ResponseCookie("cookie-name", "cookie-value");
cookie
    .setSecure(true)
    .setSameSite(SameSite.STRICT);
response.cookie(cookie);
```

---

## ⏱️ Rate Limiting

A built-in rate limiter filter based on the token bucket algorithm helps protect endpoints.

Typical use cases:
- API abuse prevention
- Basic DoS mitigation
- Per-client throttling

The rate limiter is implemented as a standard filter and can be extended or replaced.

---

## 📝 Logging

Jandle does not force a logging framework on you.

Instead, it provides a small abstraction:

```java
app.setLogger(new JandleLogger() {
    @Override
    public void info(String... messages) {
       ...
    }

    @Override
    public void warning(String... messages) {
        ...
    }

    @Override
    public void problem(Throwable t, String... messages) {
        ...
    }
});
```

This allows integration with:
- `java.util.logging`
- Log4j / Logback
- Custom structured or async loggers

---

## 🔁 Lifecycle

The `lifecycle` package defines interfaces for resources that must be explicitly closed or cleaned up when the application shuts down.

This keeps lifecycle management explicit and predictable.

---

## 🧠 Design Philosophy

- **Explicit over implicit**
- **Minimal magic**
- **Framework as a toolkit, not a black box**
- **Java-first design (JPMS compatible)**

Jandle is especially suitable for:
- Learning how HTTP servers work internally
- Building small to medium backend services
- Creating highly controlled server environments
- Experimental or educational projects

---

## 📚 Documentation

- 📘 JavaDoc: https://garik-aghayan.github.io/jandle/
- 💻 Source code: https://github.com/garik-aghayan/jandle

---

## 👤 Author

**Garik Aghayan**  
Software Developer

- GitHub: https://github.com/garik-aghayan
- LinkedIn: https://www.linkedin.com/in/garik-aghayan-39838135b
- Email: garikaghayan.dev@gmail.com