package com.jandle.api.cookies;

public interface ResponseCookie {
	/**
	 * Returns the cookie name
	 * @return {@code String} name of the cookie
	 */
	String getName();

	/**
	 * Returns the cookie value
	 * @return {@code String} value of the cookie
	 */
	String getValue();

	/**
	 * Returns the cookie domain or {@code null} if not set
	 * @return {@code String} Domain of the cookie if set, otherwise {@code null}
	 */
	String getDomain();
	/**
	 * Sets the cookie domain.
	 *
	 * @param domain the domain attribute
	 */
	void setDomain(String domain);

	/**
	 * Returns the cookie path
	 *
	 * @return {@code String} Path of the cookie
	 */
	String getPath();

	/**
	 * Sets the cookie path.
	 *
	 * @param path the path attribute
	 */
	void setPath(String path);

	/**
	 * Returns the {@link SameSite} policy
	 *
	 * @return {@code SameSite} policy of the cookie
	 */
	SameSite getSameSite();

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
	void setSameSite(SameSite sameSite);

	/**
	 * Returns {@code true} if the cookie is HttpOnly
	 *
	 * @return {@code true} if HttpOnly is set, otherwise {@code false}
	 */
	boolean isHttpOnly();

	/**
	 * Enables or disables the HttpOnly flag.
	 *
	 * @param httpOnly whether the cookie should be HttpOnly
	 */
	void setHttpOnly(boolean httpOnly);

	/**
	 * Returns {@code true} if the cookie is Secure
	 * @return {@code true} if Secure is set, otherwise {@code false}
	 */
	boolean isSecure();

	/**
	 * Enables or disables the Secure flag.
	 *
	 * @param secure whether the cookie should be Secure
	 */
	void setSecure(boolean secure);

	/**
	 * Returns {@code true} if the cookie is Partitioned
	 * @return {@code true} if Partitioned is set, otherwise {@code false}
	 */
	boolean isPartitioned();

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
	void setPartitioned(boolean partitioned);

	/**
	 * Returns the Max-Age value in seconds
	 * @return Max-Age value of the cookie in seconds as {@code long}
	 */
	long getMaxAgeSeconds();

	/**
	 * Sets the Max-Age attribute in seconds.
	 *
	 * @param maxAgeSeconds max age in seconds
	 */
	void setMaxAgeSeconds(long maxAgeSeconds);

	/**
	 * Returns the Expires timestamp in milliseconds since epoch
	 * @return Expires timestamp of the cookie in milliseconds as {@code long}
	 */
	long getExpiresMillis();

	/**
	 * Sets the Expires attribute using a timestamp in milliseconds (UTC).
	 *
	 * @param expiresMillis expiration time in milliseconds
	 */
	void setExpiresMillis(long expiresMillis);
}
