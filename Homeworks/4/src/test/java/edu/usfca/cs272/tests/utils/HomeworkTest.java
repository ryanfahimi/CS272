package edu.usfca.cs272.tests.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import edu.usfca.cs272.tests.utils.HomeworkTest.TestCounter;

/**
 * Base test class.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@ExtendWith(TestCounter.class)
public class HomeworkTest {
	/** Location of the main source directory. */
	public static final Path SOURCE_DIR = Path.of("src", "main", "java");

	/** Location of actual output produced by running tests. */
	public static final Path ACTUAL_DIR = Path.of("actual");

	/** Location of file resources. */
	public static final Path RESOURCES_DIR = Path.of("src", "test", "resources");

	/**
	 * Makes sure the actual output directory exists.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@BeforeAll
	public static void setupEnvironment() throws IOException {
		Files.createDirectories(ACTUAL_DIR);
	}

	/**
	 * Tests if two collections are equal by converting them to text with one item
	 * per line. Results in nicer side-by-side output in Eclipse JUnit view.
	 *
	 * @param expected the expected result
	 * @param actual the actual result
	 * @param debug the debug string to display
	 */
	public static void assertJoined(Collection<String> expected, Collection<String> actual, String debug) {
		String expectedJoined = String.join("\n", expected);
		String actualJoined = String.join("\n", actual);
		assertEquals(expectedJoined, actualJoined, debug + "\n");
	}

	/**
	 * Compares two files as String objects. Strips any trailing whitespace
	 * (including newlines) before comparing.
	 *
	 * @param actualPath the path to the first file (the actual output)
	 * @param expectPath the path to the second file (the expected output)
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public static void assertJoined(Path actualPath, Path expectPath) throws IOException {
		// strips line terminators from file output
		List<String> actualLines = Files.readAllLines(actualPath, UTF_8);
		List<String> expectLines = Files.readAllLines(expectPath, UTF_8);

		// rejoin lines using consistent line terminator
		String actual = String.join("\n", actualLines);
		String expect = String.join("\n", expectLines);

		String format = """

				Compare %s and %s for differences.
				""";

		Supplier<String> debug = () -> String.format(format, actualPath, expectPath);
		Assertions.assertEquals(expect, actual, debug);
	}

	/**
	 * Finds the path of the source code for the provided Java class.
	 *
	 * @param target the target class
	 * @return the source code path
	 */
	public static Path findSource(Class<?> target) {
		String name = target.getSimpleName() + ".java";

		try {
			return Files.walk(SOURCE_DIR).filter(p -> p.endsWith(name)).filter(Files::isReadable).findFirst().get();
		}
		catch (Exception e) {
			Assertions.fail("Unable to find " + name + " source code.", e);
		}

		return null; // never reached
	}

	/**
	 * Returns a source code file as text.
	 *
	 * @param target the class to get
	 * @return the source code
	 * @throws IOException if an IO error occurs
	 */
	public static String getSource(Class<?> target) throws IOException {
		Path found = findSource(target);
		return Files.readString(found, StandardCharsets.UTF_8);
	}

	/**
	 * Tests whether running on GitHub Actions.
	 */
	@Nested
	public static class ApproachTests {
		/** Creates a new instance of this class. */
		public ApproachTests() {
		}

		/**
		 * Outputs a warning/test failure if not running on GitHub Actions.
		 */
		@Test
		@Order(99)
		public void testGitHub() {
			if ("true".equals(System.getenv("GITHUB_ACTIONS"))) {
				// do not run on GitHub Actions environment
				return;
			}

			if (TestCounter.passed > 0 && TestCounter.failed == 0) {
				System.err.printf(
						"Found %d/%d passing tests so far, but you must run these on GitHub Actions to be certain they are passing.",
						TestCounter.passed, TestCounter.passed + TestCounter.failed);
			}

			Assertions.fail("You must run these tests on GitHub Actions.");
		}
	}

	/**
	 * Counts the number of failed tests so far. Used to prevent tests from running
	 * if there are any previous failures.
	 */
	public static class TestCounter implements TestWatcher {
		/** Tracks number of successes. */
		public static int passed = 0;

		/** Tracks number of failures. */
		public static int failed = 0;

		/** Initializes the test counter. */
		public TestCounter() {
			passed = 0;
			failed = 0;
		}

		@Override
		public void testSuccessful(ExtensionContext context) {
			passed++;
		}

		@Override
		public void testFailed(ExtensionContext context, Throwable cause) {
			failed++;
		}
	}

	/** Creates a new instance of this class. */
	public HomeworkTest() {
	}
}
