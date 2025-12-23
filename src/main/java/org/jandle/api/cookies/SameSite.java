package org.jandle.api.cookies;

/**
 * Represents the {@code SameSite} attribute of an HTTP cookie.
 *
 * <p>
 * The {@code SameSite} attribute controls whether a cookie is sent
 * with cross-site requests, providing protection against
 * Cross-Site HttpRequest Forgery (CSRF) attacks.
 *
 * <p>
 * Supported values:
 *
 * <ul>
 *   <li>{@link #NONE} – The cookie is sent with both same-site and
 *       cross-site requests. This value requires the cookie to be
 *       marked as {@code Secure} in modern browsers.</li>
 *   <li>{@link #STRICT} – The cookie is sent only with same-site
 *       requests and is never included in cross-site contexts.</li>
 *   <li>{@link #LAX} – The cookie is sent with same-site requests and
 *       with top-level navigations (e.g., clicking a link), but not
 *       with most cross-site subrequests.</li>
 * </ul>
 *
 * <p>
 * This enum is typically used when building a {@code Set-Cookie}
 * header via {@link ResponseCookie}.
 */
public enum SameSite {
	/**
	 * The cookie is sent with both same-site and cross-site requests.
	 *
	 * <p>This value provides the least protection and is intended for
	 *  use cases such as third-party authentication.
	 *
	 * <p><strong>Note:</strong> {@code SameSite=None} requires the
	 * {@code Secure} attribute to be set.
	 */
	NONE,
	/**
	 * The cookie is sent only with same-site requests.
	 *
	 * <p>Cross-site requests will never include this cookie, providing
	 * the strongest protection against CSRF attacks.
	 */
	STRICT,
	/**
	 * The cookie is sent with same-site requests and with top-level
	 * cross-site navigation using safe HTTP methods (such as {@code GET}).
	 *
	 * <p>This is a balanced default that provides reasonable security
	 * while preserving common navigation flows.
	 */
	LAX;

	/**
	 * Returns the properly formatted {@code SameSite} attribute value
	 * as required by the {@code Set-Cookie} HTTP header.
	 *
	 * @return a capitalized {@code SameSite} value
	 */
	@Override
	public String toString() {
		return switch (this) {
			case NONE -> "None";
			case STRICT -> "Strict";
			default -> "Lax";
		};
	}
}
