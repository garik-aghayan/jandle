package com.jandle.logs;

import com.jandle.internal.logger.TraceLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class TraceLogTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private PrintStream originalOut;

	private TraceLog logger;

	@BeforeEach
	void setUp() {
		originalOut = System.out;
		System.setOut(new PrintStream(outContent));
		logger = new TraceLog();
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}

	@Test
	void infoLogsWithoutThrowables() {
		logger.info("Test info message");
		String output = outContent.toString();
		assertTrue(output.contains("| INFO |"));
		assertTrue(output.contains("Test info message"));
	}

	@Test
	void warningLogsWithoutThrowables() {
		logger.warning("Test warning message");
		String output = outContent.toString();
		assertTrue(output.contains("| WARNING |"));
		assertTrue(output.contains("Test warning message"));
	}

	@Test
	void problemLogsWithThrowable() {
		RuntimeException ex = new RuntimeException("Something went wrong");
		logger.problem(ex, "Test problem message");
		String output = outContent.toString();
		assertTrue(output.contains("| PROBLEM |"));
		assertTrue(output.contains("Test problem message"));
		assertTrue(output.contains("Something went wrong"));
		assertTrue(output.contains("RuntimeException"));
	}

	@Test
	void logWithThrowableOnly() {
		Exception ex = new Exception("Exception occurred");
		logger.log(ex);
		String output = outContent.toString();
		assertTrue(output.contains("[Throwable.message]:"));
		assertTrue(output.contains("Exception occurred"));
	}

	@Test
	void logWithoutThrowable() {
		logger.log("Just a log message");
		String output = outContent.toString();
		assertTrue(output.contains("Just a log message"));
	}

	@Test
	void multipleMessagesLoggedCorrectly() {
		logger.info("Message 1", "Message 2", "Message 3");
		String output = outContent.toString();
		assertTrue(output.contains("Message 1"));
		assertTrue(output.contains("Message 2"));
		assertTrue(output.contains("Message 3"));
	}

	@Test
	void logWithNullThrowableDoesNotThrow() {
		Throwable t = null;
		assertDoesNotThrow(() -> logger.log(t, "Test null throwable"));
	}

	@Test
	void problemWithNullMessagesDoesNotThrow() {
		RuntimeException ex = new RuntimeException("Test exception");
		assertDoesNotThrow(() -> logger.problem(ex));
	}

	@Test
	void infoResetColorAfterLogging() {
		logger.info("Check color reset");
		String output = outContent.toString();
		// ANSI reset code is \u001B[39m
		assertTrue(output.contains("\u001B[39m"));
	}

	@Test
	void warningResetColorAfterLogging() {
		logger.warning("Check color reset");
		String output = outContent.toString();
		assertTrue(output.contains("\u001B[39m"));
	}

	@Test
	void problemResetColorAfterLogging() {
		RuntimeException ex = new RuntimeException("Error");
		logger.problem(ex, "Check color reset");
		String output = outContent.toString();
		assertTrue(output.contains("\u001B[39m"));
	}
}
