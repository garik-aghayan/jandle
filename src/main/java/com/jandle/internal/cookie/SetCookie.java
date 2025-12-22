package com.jandle.internal.cookie;

import com.jandle.api.cookies.SameSite;
import com.jandle.internal.http.HttpResponse;

/**
 * Represents an HTTP response cookie and provides a fluent-style
 * API for configuring {@code Set-Cookie} attributes.
 *
 * <p>
 * This class is responsible only for cookie definition and
 * serialization. When converted to a string, it produces a valid
 * {@code Set-Cookie} header value that can be attached to a
 * {@link HttpResponse}.
 * </p>
 *
 * <p>
 * Supported cookie attributes include:
 * </p>
 * <ul>
 *   <li>Name</li>
 *   <li>Value</li>
 *   <li>Domain</li>
 *   <li>Path</li>
 *   <li>Max-Age</li>
 *   <li>Expires</li>
 *   <li>Secure flag</li>
 *   <li>HttpOnly flag</li>
 *   <li>{@link SameSite} policy</li>
 *   <li>Partitioned cookies (CHIPS)</li>
 * </ul>
 *
 * <p>
 * Validation rules are enforced according to modern browser
 * requirements:
 * </p>
 * <ul>
 *   <li>{@code SameSite=None} requires {@code Secure=true}</li>
 *   <li>{@code Partitioned} requires {@code Secure=true}</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre>{@code
 * SetCookie cookie = new SetCookie("session", "abc123");
 * cookie.setHttpOnly(true);
 * cookie.setSecure(true);
 * cookie.setSameSite(SameSite.NONE);
 * cookie.setMaxAgeSeconds(3600);
 *
 * response.cookie(cookie);
 * }</pre>
 */
public class SetCookie {
	private final String name;
	private final String value;
	private String domain;
	private String path = "/";
	private SameSite sameSite = SameSite.LAX;
	private boolean httpOnly = false;
	private boolean secure = false;
	private boolean partitioned = false;
	private long maxAgeSeconds;
	private long expiresMillis;

	/**
	 * Creates a new response cookie with the given name and value.
	 *
	 * @param name  the cookie name
	 * @param value the cookie value
	 */
	public SetCookie(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the cookie name
	 * @return {@code String} name of the cookie
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the cookie value
	 * @return {@code String} value of the cookie
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the cookie domain or {@code null} if not set
	 * @return {@code String} Domain of the cookie if set, otherwise {@code null}
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Sets the cookie domain.
	 *
	 * @param domain the domain attribute
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Returns the cookie path
	 *
	 * @return {@code String} Path of the cookie
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the cookie path.
	 *
	 * @param path the path attribute
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns the {@link SameSite} policy
	 *
	 * @return {@code SameSite} policy of the cookie
	 */
	public SameSite getSameSite() {
		return sameSite;
	}

	/**
	 * Sets the {@link SameSite} policy.
	 *
	 * <p>
	 * {@code SameSite=None} requires {@code Secure=true}.
	 * </p>
	 *
	 * @param sameSite the SameSite policy
	 * @throws IllegalArgumentException if {@code SameSite.NONE} is used without {@code Secure}
	 */
	public void setSameSite(SameSite sameSite) {
		if (sameSite == SameSite.NONE && !secure) throw new IllegalArgumentException("SameSite=None requires Secure");
		this.sameSite = sameSite;
	}

	/**
	 * Returns {@code true} if the cookie is HttpOnly
	 *
	 * @return {@code true} if HttpOnly is set, otherwise {@code false}
	 */
	public boolean isHttpOnly() {
		return httpOnly;
	}

	/**
	 * Enables or disables the HttpOnly flag.
	 *
	 * @param httpOnly whether the cookie should be HttpOnly
	 */
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	/**
	 * Returns {@code true} if the cookie is Secure
	 * @return {@code true} if Secure is set, otherwise {@code false}
	 */
	public boolean isSecure() {
		return secure;
	}

	/**
	 * Enables or disables the Secure flag.
	 *
	 * @param secure whether the cookie should be Secure
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	/**
	 * Returns {@code true} if the cookie is Partitioned
	 * @return {@code true} if Partitioned is set, otherwise {@code false}
	 */
	public boolean isPartitioned() {
		return partitioned;
	}

	/**
	 * Enables or disables the Partitioned (CHIPS) attribute.
	 *
	 * <p>
	 * Partitioned cookies require {@code Secure=true}.
	 * </p>
	 *
	 * @param partitioned whether the cookie should be partitioned
	 * @throws IllegalArgumentException if Secure is not enabled
	 */
	public void setPartitioned(boolean partitioned) {
		if (!secure) throw new IllegalArgumentException("Partitioned requires Secure");
		this.partitioned = partitioned;
	}

	/**
	 * Returns the Max-Age value in seconds
	 * @return Max-Age value of the cookie in seconds as {@code long}
	 */
	public long getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	/**
	 * Sets the Max-Age attribute in seconds.
	 *
	 * @param maxAgeSeconds max age in seconds
	 */
	public void setMaxAgeSeconds(long maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}

	/**
	 * Returns the Expires timestamp in milliseconds since epoch
	 * @return Expires timestamp of the cookie in milliseconds as {@code long}
	 */
	public long getExpiresMillis() {
		return expiresMillis;
	}

	/**
	 * Sets the Expires attribute using a timestamp in milliseconds (UTC).
	 *
	 * @param expiresMillis expiration time in milliseconds
	 */
	public void setExpiresMillis(long expiresMillis) {
		this.expiresMillis = expiresMillis;
	}

	/**
	 * Serializes this cookie into a valid {@code Set-Cookie} header value.
	 *
	 * @return a properly formatted {@code Set-Cookie} string
	 */
	@Override
	public String toString() {
		StringBuilder cookieSb = new StringBuilder();
		cookieSb.append(name).append("=").append(value);

		if (domain != null) cookieSb.append("; Domain=").append(domain);
		if (path != null) cookieSb.append("; Path=").append(path);
		if (maxAgeSeconds > 0) cookieSb.append("; Max-Age=").append(maxAgeSeconds);
		if (expiresMillis > 0) {
			cookieSb.append("; Expires=")
					.append(java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
							.format(java.time.Instant.ofEpochMilli(expiresMillis)
									.atZone(java.time.ZoneOffset.UTC)));
		}
		if (secure) cookieSb.append("; Secure");
		if (httpOnly) cookieSb.append("; HttpOnly");
		if (sameSite != null) cookieSb.append("; SameSite=").append(sameSite);
		if (partitioned) cookieSb.append("; Partitioned");

		return cookieSb.toString();
	}
}
