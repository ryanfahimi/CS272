package edu.usfca.cs272.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.usfca.cs272.LoggerSetup;
import edu.usfca.cs272.tests.utils.HomeworkTest;

/**
 * Runs a couple of tests to make sure Log4j2 is setup.
 *
 * NOTE: There are better ways to test log configuration---we will keep it
 * simple to make sure you can run and configure Log4j2.
 *
 * This is also not the most informative configuration---it is just one of the
 * most testable ones that handle stack trace output.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@Tag("LoggerSetup")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(OrderAnnotation.class)
public class LoggerSetupTest extends HomeworkTest {
	/** Path to actual debug.log file. */
	private static final Path DEBUG_LOG = Path.of("debug.log");

	/** Path to expected debug.txt file. */
	private static final Path DEBUG_TXT = RESOURCES_DIR.resolve("debug.txt");

	/** Original System.out print stream. */
	public static final PrintStream SYSTEM_OUT = System.out;

	/** Custom System.out print stream. */
	public static final ByteArrayOutputStream CUSTOM_OUT = new ByteArrayOutputStream();

	/**
	 * Setup the output stream to capture console output. This must happen
	 * in a step before the LoggerSetup.main is called for the output capture
	 * to work properly.
	 *
	 * @throws Exception if unable to setup console and logger output
	 */
	@BeforeAll
	public static void setupOutput() throws Exception {
		System.setOut(new PrintStream(CUSTOM_OUT));

		Configurator.reconfigure(new DefaultConfiguration());
		Configurator.reconfigure(URI.create("classpath:log4j2.xml"));
	}

	/**
	 * Tests approach, including configuration file location and name.
	 */
	@Nested
	@Order(1)
	@TestMethodOrder(OrderAnnotation.class)
	public class SetupTests {
		/**
		 * Run LoggerSetup.main and capture console and file output.
		 *
		 * @throws IOException if an IO error occurs
		 */
		@BeforeAll
		public static void captureOutput() throws IOException {
			Files.deleteIfExists(DEBUG_LOG);
			LoggerSetup.main(null);

			System.out.flush();
			System.setOut(SYSTEM_OUT);

			LogManager.shutdown(); // forces log files to be written from buffer
		}

		/**
		 * Make sure you are not using the log4j2-test.* name. That is the config file
		 * name used for test code not main code.
		 *
		 * @throws IOException if IO error occurs
		 */
		@Test
		@Order(1)
		public void testNotTest() throws IOException {
			var found = Files.walk(Path.of("."))
					.filter(Files::isRegularFile)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(name -> name.matches("log4j2-test\\.\\w+"))
					.toList();

			assertEquals(Collections.emptyList(), found, "\nDo not use the \"log4j2-test.*\" filename to configure logging of main code.\n");
		}

		/**
		 * Make sure you are using the correct file name.
		 *
		 * @throws IOException if IO error occurs
		 */
		@Test
		@Order(2)
		public void testNameCorrect() throws IOException {
			var found = Files.walk(Path.of("."))
					.filter(Files::isRegularFile)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(name -> name.matches("log4j2\\.\\w+"))
					.toList();

			assertTrue(!found.isEmpty(), "\nCould not find configuration file with correct name.\n");
		}

		/**
		 * Make sure you are using the correct location for the configuration file.
		 *
		 * @throws IOException if IO error occurs
		 */
		@Test
		@Order(3)
		public void testLocationCorrect() throws IOException {
			var found = Files.walk(Path.of("src", "main", "resources"))
					.filter(Files::isRegularFile)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(name -> name.matches("log4j2.*\\.\\w+"))
					.toList();

			assertTrue(!found.isEmpty(), "\nCould not find configuration file in the correct location.\n");
		}

		/**
		 * Make sure you are using the correct extension for the configuration file.
		 *
		 * @throws IOException if IO error occurs
		 */
		@Test
		@Order(4)
		public void testExtensionCorrect() throws IOException {
			var found = Files.walk(Path.of("."))
					.filter(Files::isRegularFile)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(name -> name.matches("(?i)log4j2.*\\.(properties|ya?ml|jso?n|xml)"))
					.toList();

			assertTrue(!found.isEmpty(), "\nCould not find configuration file with the correct extension.\n");
		}
	}

	/**
	 * Tests console output.
	 */
	@Nested
	@Order(2)
	@TestMethodOrder(OrderAnnotation.class)
	public class ConsoleTests {
		/** Lines of captured console output. */
		public static List<String> CONSOLE;

		/** Starting line for class-specific log output. */
		public static int CLASS_START;

		/**
		 * Run LoggerSetup.main and capture console output at the same time.
		 * @throws IOException if an IO error occurs
		 */
		@BeforeAll
		public static void captureOutput() throws IOException {
			CONSOLE = CUSTOM_OUT.toString().lines().map(String::strip).toList();
			CONSOLE.forEach(System.out::println);

			CLASS_START = Collections.indexOfSubList(CONSOLE, List.of("Class Logger:"));
		}

		/**
		 * Tests that the capture of the console output was successful. If this fails,
		 * the other tests are also likely to fail.
		 */
		@Test
		@Order(1)
		public void testConsoleCapture() {
			boolean found = CONSOLE.stream().anyMatch(line -> line.contains("Falcon"));
			Assertions.assertTrue(found, "Console output may not have been captured correctly. Make sure any console appenders use the attribute: follow=\"false\"");
		}

		/**
		 * Tests the root logger console output and compares to expected.
		 */
		@Test
		@Order(2)
		public void testRootConsole() {
			if (CLASS_START < 0) {
				Assertions.fail("Could not find root logger vs class logger console output.");
			}

			List<String> expected = List.of(
					"INFO: Ibis",
					"WARN: Wren",
					"ERROR: Eastern Eagle",
					"FATAL: Catching Falcon"
					);

			List<String> actual = CONSOLE.subList(1, CLASS_START - 1);
			HomeworkTest.assertJoined(expected, actual, "Console output for the root logger was incorrect.");
		}

		/**
		 * Tests the class-specific logger console output and compares to expected.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(3)
		public void testClassConsole() throws IOException {
			if (CLASS_START < 0) {
				Assertions.fail("Could not find root logger vs class logger console output.");
			}

			List<String> expected = List.of("FATAL: Catching Falcon");
			List<String> actual = CONSOLE.subList(CLASS_START + 1, CONSOLE.size());
			HomeworkTest.assertJoined(expected, actual, "Console output for the class-specific logger was incorrect.");
		}
	}

	/**
	 * Tests part of file output.
	 */
	@Nested
	@Order(3)
	@TestMethodOrder(OrderAnnotation.class)
	public class FileTests {
		/** Lines of the actual debug.log file. */
		public static List<String> ACTUAL;

		/** Lines of the expected debug.txt file. */
		public static List<String> EXPECTED;

		/**
		 * Runs the LoggerSetup.main method to generate the debug.log file and
		 * loads the actual and expected output.
		 *
		 * @throws IOException if an IO error occurs
		 */
		@BeforeAll
		public static void loadOutput() throws IOException {
			Assertions.assertAll("\nUnable to load actual or expected log files.\n",
					() -> Assertions.assertTrue(Files.exists(DEBUG_LOG)),
					() -> Assertions.assertTrue(Files.exists(DEBUG_TXT)));

			ACTUAL = Files.readAllLines(DEBUG_LOG);
			EXPECTED = Files.readAllLines(DEBUG_TXT);
		}

		/**
		 * Tests that the expected levels are in the output file.
		 *
		 * @param expected the expected level output to find in the debug file
		 * @throws IOException if unable to read debug file
		 */
		@ParameterizedTest
		@Order(1)
		@ValueSource(strings = { "Turkey", "Duck", "Ibis", "Wren", "Eagle", "Falcon" })
		public void testLevels(String expected) throws IOException {
			boolean found = ACTUAL.stream().anyMatch(line -> line.contains(expected));
			Assertions.assertTrue(found, "\nCheck the level configuration for the log file. Unable to find: " + expected + "\n");
		}

		/**
		 * Tests if the number of lines in the LoggerSetup source code changed.
		 *
		 * @throws IOException if unable to open source code
		 */
		@Test
		@Order(2)
		public void testLoggerSetup() throws IOException {
			Path source = HomeworkTest.findSource(LoggerSetup.class);
			long actual = Files.lines(source).count();
			long expected = 51; // number of original lines

			Assertions.assertEquals(expected, actual, "\nIt looks like you modified the LoggerSetup source code. Restore it to the original version!\n");
		}

		/**
		 * Tests the debug.log has the expected output for levels below error.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(3)
		public void testBelowError() throws IOException {
			Predicate<String> filter = line -> line.matches("(?i)\\[.*?\\b(TR|DE|IN|WA).*?\\].*?");

			// only test the non-exception output from files
			String expected = EXPECTED.stream()
					.filter(filter)
					.map(String::stripTrailing)
					.collect(Collectors.joining("\n"));

			String actual = ACTUAL.stream()
					.filter(filter)
					.map(String::stripTrailing)
					.collect(Collectors.joining("\n"));

			assertEquals(expected, actual, "\nCompare debug.log and test/resources/debug.txt in Eclipse.\n");
		}

		/**
		 * Tests the debug.log has the expected output for levels above warn.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(4)
		public void testAboveWarn() throws IOException {
			Predicate<String> filter = line -> line.matches("(?i).*?\\b(ER|FA|at)\\w*?\\b.*?");

			// only test the non-exception output from files
			String expected = EXPECTED.stream()
					.filter(filter)
					.map(String::stripTrailing)
					.collect(Collectors.joining("\n"));

			String actual = ACTUAL.stream()
					.filter(filter)
					.map(String::stripTrailing)
					.collect(Collectors.joining("\n"));

			assertEquals(expected, actual, "\nCompare debug.log and test/resources/debug.txt in Eclipse.\\n");
		}
	}

	/**
	 * Tests if on GitHub Actions or not.
	 */
	@Nested
	@Order(99)
	@TestMethodOrder(OrderAnnotation.class)
	public class ApproachTests extends HomeworkTest.ApproachTests {
	}
}
