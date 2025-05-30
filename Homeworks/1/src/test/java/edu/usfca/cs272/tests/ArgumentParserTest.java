package edu.usfca.cs272.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.usfca.cs272.tests.utils.HomeworkTest;
import edu.usfca.cs272.utils.ArgumentParser;

/**
 * Tests for the {@link ArgumentParser} class.
 *
 * @see ArgumentParser
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@Tag("ArgumentParser")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodName.class)
public class ArgumentParserTest extends HomeworkTest {
	/*
	 * Hint: Right-click a nested class to run the tests in that nested class
	 * only. Focus on tests in the order provided in the file. Only focus on one
	 * test at a time. Learn how to read the JUnit output!
	 */

	/**
	 * Tests for the {@link ArgumentParser#isFlag(String)} method.
	 */
	@Nested
	@Order(1)
	@TestMethodOrder(OrderAnnotation.class)
	public class FlagTests {
		/**
		 * Tests values that should be considered valid flags.
		 *
		 * @param flag valid flag value
		 */
		@Order(1)
		@ParameterizedTest(name = "[{index}: \"{0}\"]")
		@ValueSource(
				strings = {
						"-a", "-hello", "-hello world", "-trailing  ", "-résumé", "-über",
						"-abc123", "-with-dash", "-with_underscore", "-@debug", "-#admin",
						"--quiet", })
		public void testValidFlags(String flag) {
			boolean actual = ArgumentParser.isFlag(flag);
			Assertions.assertTrue(actual, flag);
		}

		/**
		 * Tests values that should be considered invalid flags.
		 *
		 * @param flag invalid flag value
		 */
		@Order(2)
		@ParameterizedTest(name = "[{index}: \"{0}\"]")
		@ValueSource(
				strings = {
						"a-b-c", "hello", "hello world", "", " ", "\t", "\n", "-", " - a",
						" -a", "\t-a", "-\ta", "97", "1", "-1", "-42" })
		public void testInvalidFlags(String flag) {
			boolean actual = ArgumentParser.isFlag(flag);
			Assertions.assertFalse(actual, flag);
		}

		/**
		 * Tests that null value should be considered an invalid flag.
		 */
		@Order(3)
		@Test
		public void testNullFlag() {
			boolean actual = ArgumentParser.isFlag(null);
			Assertions.assertFalse(actual, "null");
		}

		/**
		 * Tests a randomly generated string (with letters).
		 */
		@Order(4)
		@Test
		public void testRandomStringFlag() {
			Random random = new Random();

			int a = 'a'; // lowercase a codepoint
			int z = 'z'; // lowercase z codepoint

			// https://www.baeldung.com/java-random-string#java8-alphabetic
			String junk = random.ints(5, a, z + 1)
					.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
					.toString();

			String flag = "-" + junk;

			boolean actual = ArgumentParser.isFlag(flag);
			Assertions.assertTrue(actual, flag);
		}

		/**
		 * Tests a randomly generated string (with digits).
		 */
		@Order(5)
		@Test
		public void testRandomDigitsFlag() {
			Random random = new Random();

			// https://www.baeldung.com/java-random-string#java8-alphabetic
			String junk = random.ints(5, 0, 10)
					.mapToObj(i -> String.valueOf(i))
					.collect(Collectors.joining());

			String flag = "-" + junk;

			boolean actual = ArgumentParser.isFlag(flag);
			Assertions.assertFalse(actual, flag);
		}
	}

	/*
	 * Hint: Right-click a nested class to run the tests in that nested class
	 * only. Focus on tests in the order provided in the file. Only focus on one
	 * test at a time. Learn how to read the JUnit output!
	 */

	/**
	 * Tests how well the {@link ArgumentParser#parse(String[])} method works.
	 */
	@Nested
	@Order(2)
	@TestMethodOrder(OrderAnnotation.class)
	public class ParseTests {
		/**
		 * Checks if number of flags is correct for this test case.
		 */
		@Order(1)
		@Test
		public void testNumFlags() {
			int expected = 5;
			int actual = this.parser.numFlags();

			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Checks if the -d flag was correctly parsed for this test case.
		 */
		@Order(2)
		@Test
		public void testHasFlag() {
			assertTrue(this.parser.hasFlag("-d"), this.debug);
		}

		/**
		 * Checks if the -f flag was correctly parsed for this test case.
		 */
		@Order(3)
		@Test
		public void testHasLastFlag() {
			assertTrue(this.parser.hasFlag("-f"), this.debug);
		}

		/**
		 * Checks if the -g flag does not exist as expected.
		 */
		@Order(4)
		@Test
		public void testHasntFlag() {
			assertFalse(this.parser.hasFlag("-g"), this.debug);
		}

		/**
		 * Checks if the -a value was correctly parsed for this test case.
		 */
		@Order(5)
		@Test
		public void testHasValue() {
			assertTrue(this.parser.hasValue("-a"), this.debug);
		}

		/**
		 * Checks if the -d value was correctly parsed for this test case.
		 */
		@Order(6)
		@Test
		public void testHasFlagNoValue() {
			assertFalse(this.parser.hasValue("-d"), this.debug);
		}

		/**
		 * Checks the value for a non-existent flag.
		 */
		@Order(7)
		@Test
		public void testNoFlagNoValue() {
			assertFalse(this.parser.hasValue("-g"), this.debug);
		}

		/**
		 * Checks if the -b value was correctly parsed for this test case.
		 */
		@Order(8)
		@Test
		public void testGetValueExists() {
			String expected = "bat";
			String actual = this.parser.getString("-b");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Checks if the -d value was correctly parsed for this test case.
		 */
		@Order(9)
		@Test
		public void testGetValueNull() {
			String expected = null;
			String actual = this.parser.getString("-d");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Checks the value for a non-existent flag.
		 */
		@Order(10)
		@Test
		public void testGetValueNoFlag() {
			String expected = null;
			String actual = this.parser.getString("-g");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Checks if the -e flag was correctly parsed for this test case.
		 */
		@Order(11)
		@Test
		public void testGetValueRepeatedFlag() {
			String expected = null;
			String actual = this.parser.getString("-e");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Checks if the -b flag was correctly parsed for this test case.
		 */
		@Order(12)
		@Test
		public void testGetDefaultExists() {
			String expected = "bat";
			String actual = this.parser.getString("-b", "bee");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Checks if the -d flag was correctly parsed for this test case.
		 */
		@Order(13)
		@Test
		public void testGetDefaultNull() {
			String expected = "dog";
			String actual = this.parser.getString("-d", "dog");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Checks if the default value is returned correctly for a non-existent
		 * flag.
		 */
		@Order(14)
		@Test
		public void testGetDefaultMissing() {
			String expected = "goat";
			String actual = this.parser.getString("-g", "goat");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Checks that parsing the same arguments twice does not affect count.
		 */
		@Order(15)
		@Test
		public void testDoubleParse() {
			String[] args = {
					"-a", "42", "-b", "bat", "cat", "-d", "-e", "elk", "-e", "-f" };
			this.parser.parse(args);

			int expected = 5;
			int actual = this.parser.numFlags();

			assertEquals(expected, actual, this.debug);
		}

		/** ArgumentParser object being tested */
		private ArgumentParser parser;

		/** String used to output debug messages when tests fail. */
		private String debug;

		/**
		 * Sets up the parser object with a single test case.
		 */
		@BeforeEach
		public void setup() {
			String[] args = {
					"", "-a", "42", "-b", "bat", "cat", "-d", "-e", "elk", "-1", "-e",
					"-f", null };

			this.parser = new ArgumentParser();
			this.parser.parse(args);
			this.debug = debugOutput(parser, args).get();
		}

		/**
		 * Nullifies the parser object after each test.
		 */
		@AfterEach
		public void teardown() {
			this.parser = null;
		}
	}

	/**
	 * Tests for the {@link ArgumentParser#getPath(String)} and the
	 * {@link ArgumentParser#getInteger(String, int)} methods.
	 */
	@Nested
	@Order(3)
	@TestMethodOrder(OrderAnnotation.class)
	public class ValueTests {
		/**
		 * Checks that {@link ArgumentParser#getString(String)} properly returns a value.
		 */
		@Order(1)
		@Test
		public void testGetValidString() {
			String[] args = { "-p", "hello.txt" };
			ArgumentParser parser = new ArgumentParser(args);

			String expected = "hello.txt";
			String actual = parser.getString("-p");
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getString(String)} returns null as
		 * expected.
		 */
		@Order(2)
		@Test
		public void testGetInvalidString() {
			String[] args = { "-p" };
			ArgumentParser parser = new ArgumentParser(args);

			String expected = null;
			String actual = parser.getString("-p");
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getString(String, String)} returns value
		 * properly when flag/value pair exists.
		 */
		@Order(3)
		@Test
		public void testGetValidDefaultString() {
			String[] args = { "-p", "hello.txt" };
			ArgumentParser parser = new ArgumentParser(args);

			String expected = "hello.txt";
			String actual = parser.getString("-p", "world.txt");
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getString(String, String)} returns value
		 * properly when flag/value pair does not exist.
		 */
		@Order(4)
		@Test
		public void testGetInvalidDefaultString() {
			String[] args = { "-p" };
			ArgumentParser parser = new ArgumentParser(args);

			String expected = "world.txt";
			String actual = parser.getString("-p", "world.txt");
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getPath(String)} properly returns path.
		 */
		@Order(5)
		@Test
		public void testGetValidPath() {
			String[] args = { "-p", "hello.txt" };
			ArgumentParser parser = new ArgumentParser(args);

			Path expected = Path.of("hello.txt");
			Path actual = parser.getPath("-p");
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getPath(String)} returns null as
		 * expected.
		 */
		@Order(6)
		@Test
		public void testGetInvalidPath() {
			String[] args = { "-p" };
			ArgumentParser parser = new ArgumentParser(args);

			Path expected = null;
			Path actual = parser.getPath("-p");
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getPath(String, Path)} returns value
		 * properly when flag/value pair exists.
		 */
		@Order(7)
		@Test
		public void testGetValidDefaultPath() {
			String[] args = { "-p", "hello.txt" };
			ArgumentParser parser = new ArgumentParser(args);

			Path expected = Path.of("hello.txt");
			Path actual = parser.getPath("-p", Path.of("world.txt"));
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getPath(String, Path)} returns value
		 * properly when flag/value pair does not exist.
		 */
		@Order(8)
		@Test
		public void testGetInvalidDefaultPath() {
			String[] args = { "-p" };
			ArgumentParser parser = new ArgumentParser(args);

			Path expected = Path.of("world.txt");
			Path actual = parser.getPath("-p", Path.of("world.txt"));
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getInteger(String, int)} returns the
		 * expected value.
		 */
		@Order(9)
		@Test
		public void testGetIntegerPositive() {
			String[] args = { "-num", "42" };
			ArgumentParser parser = new ArgumentParser(args);

			int expected = 42;
			int actual = parser.getInteger("-num", 0);
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getInteger(String, int)} returns the
		 * expected value.
		 */
		@Order(10)
		@Test
		public void testGetIntegerNoValue() {
			String[] args = { "-num" };
			ArgumentParser parser = new ArgumentParser(args);

			int expected = 0;
			int actual = parser.getInteger("-num", 0);
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getInteger(String, int)} returns the
		 * expected value.
		 */
		@Order(11)
		@Test
		public void testGetIntegerLetterValue() {
			String[] args = { "-num", "hello" };
			ArgumentParser parser = new ArgumentParser(args);

			int expected = 0;
			int actual = parser.getInteger("-num", 0);
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Checks that {@link ArgumentParser#getInteger(String, int)} returns the
		 * expected value.
		 */
		@Order(12)
		@Test
		public void testGetIntegerNegativeValue() {
			String[] args = { "-num", "-13" };
			ArgumentParser parser = new ArgumentParser(args);

			int expected = -13;
			int actual = parser.getInteger("-num", 0);
			assertEquals(expected, actual, debugOutput(parser, args));
		}
	}

	/**
	 * Tests for the {@link ArgumentParser#numFlags()} method.
	 */
	@Nested
	@Order(4)
	@TestMethodOrder(OrderAnnotation.class)
	public class CountTests {
		/**
		 * Tests count for arguments with a single flag.
		 */
		@Order(1)
		@Test
		public void testOneFlag() {
			String[] args = { "-loquat" };
			ArgumentParser parser = new ArgumentParser(args);
			int expected = 1;
			int actual = parser.numFlags();
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Tests count for arguments with a single flag/value pair.
		 */
		@Order(2)
		@Test
		public void testOnePair() {
			String[] args = { "-grape", "raisin" };
			ArgumentParser parser = new ArgumentParser(args);
			int expected = 1;
			int actual = parser.numFlags();
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Tests count for arguments with two flags.
		 */
		@Order(3)
		@Test
		public void testTwoFlags() {
			String[] args = { "-tomato", "-potato" };
			ArgumentParser parser = new ArgumentParser(args);
			int expected = 2;
			int actual = parser.numFlags();
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Tests count for arguments with a single value.
		 */
		@Order(4)
		@Test
		public void testOnlyValue() {
			String[] args = { "rhubarb" };
			ArgumentParser parser = new ArgumentParser(args);
			int expected = 0;
			int actual = parser.numFlags();
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Tests count for arguments with two values.
		 */
		@Order(5)
		@Test
		public void testTwoValues() {
			String[] args = { "constant", "change" };
			ArgumentParser parser = new ArgumentParser(args);
			int expected = 0;
			int actual = parser.numFlags();
			assertEquals(expected, actual, debugOutput(parser, args));

		}

		/**
		 * Tests count for arguments with a non-pair value then flag.
		 */
		@Order(6)
		@Test
		public void testPineapple() {
			String[] args = { "pine", "-apple" };
			ArgumentParser parser = new ArgumentParser(args);
			int expected = 1;
			int actual = parser.numFlags();
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Tests count for arguments with two flag/value pairs.
		 */
		@Order(7)
		@Test
		public void testSquash() {
			String[] args = { "-aubergine", "eggplant", "-courgette", "zucchini" };
			ArgumentParser parser = new ArgumentParser(args);
			int expected = 2;
			int actual = parser.numFlags();
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Tests count for arguments with repeated flags.
		 */
		@Order(8)
		@Test
		public void testFruit() {
			String[] args = {
					"-tangerine", "satsuma", "-tangerine", "clementine", "-tangerine",
					"mandarin" };
			ArgumentParser parser = new ArgumentParser(args);
			int expected = 1;
			int actual = parser.numFlags();
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Tests count for arguments with no elements.
		 */
		@Order(9)
		@Test
		public void testEmpty() {
			String[] args = {};
			ArgumentParser parser = new ArgumentParser(args);
			int expected = 0;
			int actual = parser.numFlags();
			assertEquals(expected, actual, debugOutput(parser, args));
		}

		/**
		 * Tests count for null arguments.
		 */
		@Order(10)
		@Test
		public void testNull() {
			String[] args = null;

			// it is okay to throw a null pointer exception here
			assertThrows(java.lang.NullPointerException.class,
					() -> new ArgumentParser(args).numFlags());
		}
	}

	/**
	 * Tests how well the {@link ArgumentParser#parse(String[])} method works for
	 * the README example.
	 */
	@Nested
	@Order(5)
	@TestMethodOrder(OrderAnnotation.class)
	public class ExampleTests {
		/**
		 * Tests the number of flags.
		 */
		@Order(1)
		@Test
		public void testFlags() {
			int expected = 5;
			int actual = this.parser.numFlags();
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Tests the flag value.
		 */
		@Order(2)
		@Test
		public void testMax() {
			String expected = "false";
			String actual = this.parser.getString("-max");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Tests the flag value.
		 */
		@Order(3)
		@Test
		public void testMin() {
			Integer expected = -10;
			Integer actual = this.parser.getInteger("-min");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Tests the flag value.
		 */
		@Order(4)
		@Test
		public void testDebug() {
			assertTrue(this.parser.hasFlag("-debug"), this.debug);
			assertNull(this.parser.getString("-debug"), this.debug);
		}

		/**
		 * Tests the flag value.
		 */
		@Order(5)
		@Test
		public void testFile() {
			Path expected = Path.of("output.txt");
			Path actual = this.parser.getPath("-f");
			assertEquals(expected, actual, this.debug);
		}

		/**
		 * Tests the flag value.
		 */
		@Order(6)
		@Test
		public void testVerbose() {
			assertTrue(this.parser.hasFlag("-verbose"), this.debug);
			assertNull(this.parser.getString("-verbose"), this.debug);
		}

		/** ArgumentParser object being tested */
		private ArgumentParser parser;

		/** String used to output debug messages when tests fail. */
		private String debug;

		/**
		 * Sets up the parser object with a single test case.
		 */
		@BeforeEach
		public void setup() {
			String[] args = {
					"-max", "false", "-min", "0", "-min", "-10", "hello", "-debug", "-f",
					"output.txt", "-verbose" };

			this.parser = new ArgumentParser();
			this.parser.parse(args);
			this.debug = debugOutput(parser, args).get();
		}

		/**
		 * Nullifies the parser object after each test.
		 */
		@AfterEach
		public void teardown() {
			this.parser = null;
		}
	}

	/**
	 * Imperfect tests to try and determine if the approach may have issues.
	 */
	@Nested
	@Order(99)
	@TestMethodOrder(OrderAnnotation.class)
	public class ApproachTests extends HomeworkTest.ApproachTests {
		/**
		 * Tests that the java.io.File class does not appear in the implementation
		 * code.
		 *
		 * @throws IOException if IO issue occurs
		 */
		@Test
		@Order(1)
		public void testFileClass() throws IOException {
			Assertions.assertFalse(source.contains("import java.io.File"),
					"Do not use the java.io.File class!");
		}

		/**
		 * Tests that the java.io.Paths class does not appear in the implementation.
		 *
		 * @throws IOException if IO issue occurs
		 */
		@Test
		@Order(2)
		public void testPathsClass() throws IOException {
			Assertions.assertFalse(source.contains("import java.nio.file.Paths"),
					"Do not use the java.io.file.Paths class!");
		}

		/**
		 * Attempts to determine if looping is used improperly. There should only be
		 * one loop necessary for the parse method.
		 *
		 * @throws IOException if IO issue occurs
		 */
		@Test
		@Order(3)
		public void testLoopCount() throws IOException {
			String regex = "(?i)\\b(for|while)\\s*\\(";
			long count = Pattern.compile(regex).matcher(source).results().count();

			assertTrue(count <= 1, "Found " + count
					+ " loops in source code. Only 1 should be necessary.");
		}

		/** Source code loaded as a String object. */
		private String source;

		/**
		 * Sets up the parser object with a single test case.
		 *
		 * @throws IOException if IO issue occurs
		 */
		@BeforeEach
		public void setup() throws IOException {
			this.source = getSource(ArgumentParser.class);
		}
	}

	/**
	 * Generates debug output for test failures.
	 *
	 * @param parser the parser being tested
	 * @param args the original arguments
	 * @return the debug output
	 */
	public Supplier<String> debugOutput(ArgumentParser parser, String[] args) {
		String debug = """

				Original: %s
				Parsed: %s
				""";

		return () -> debug.formatted(Arrays.toString(args), parser.toString());
	}

	/** Creates a new instance of this class. */
	public ArgumentParserTest() {
	}
}
