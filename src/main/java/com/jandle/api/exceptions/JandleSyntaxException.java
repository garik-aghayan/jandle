package com.jandle.api.exceptions;

/**
 * Thrown to indicate a syntax-related error within the Jandle framework.
 *
 * <p>
 * This exception is typically used when request data, route definitions,
 * or framework-specific configurations violate expected syntax rules.
 * </p>
 */
public class JandleSyntaxException extends RuntimeException {
	/**
	 * Creates a new {@code JandleSyntaxException} with the specified detail message.
	 *
	 * @param message a description of the syntax error
	 */
	public JandleSyntaxException(String message) {
		super(message);
	}
}
