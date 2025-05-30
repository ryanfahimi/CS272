package edu.usfca.cs272.tests;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import edu.usfca.cs272.tests.utils.HomeworkTest;
import edu.usfca.cs272.utils.FileStemmer;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Tests of the {@link FileStemmer} class.
 *
 * @see FileStemmer
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@Tag("FileStemmer")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class FileStemmerTest extends HomeworkTest {
	// Right-click and run the individual nested test classes!

	/**
	 * Collection of tests.
	 *
	 * @see FileStemmer#addStems(String, opennlp.tools.stemmer.Stemmer, Collection)
	 */
	@Nested
	@Order(1)
	@TestMethodOrder(OrderAnnotation.class)
	public class AddStemsHashTests extends StemmerTests {
		/**
		 * Tests expected output for given test case.
		 *
		 * @param line the line to stem
		 * @param output the expected output
		 */
		@Override
		public void test(String line, String[] output) {
			HashSet<String> expected = new HashSet<>();
			Collections.addAll(expected, output);

			HashSet<String> actual = new HashSet<>();
			FileStemmer.addStems(line, new SnowballStemmer(ENGLISH), actual);

			assertJoined(expected, actual, "Sets do not match; use compare feature in Eclipse for details.");
		}

		/**
		 * Tests repeat calls.
		 */
		@Test
		@Order(7)
		public void testRepeat() {
			HashSet<String> expected = new HashSet<>();
			Collections.addAll(expected, "consign", "consist");

			HashSet<String> actual = new HashSet<>();
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			FileStemmer.addStems("consign consigned consigning consignment", stemmer, actual);
			FileStemmer.addStems("consist consisted consistency consistent consistently consisting consists", stemmer, actual);

			assertJoined(expected, actual, "Sets do not match; use compare feature in Eclipse for details.");
		}
	}

	/**
	 * Collection of tests.
	 *
	 * @see FileStemmer#addStems(String, opennlp.tools.stemmer.Stemmer, Collection)
	 */
	@Nested
	@Order(2)
	@TestMethodOrder(OrderAnnotation.class)
	public class AddStemsLinkedTests extends StemmerTests {
		@Override
		public void test(String line, String[] output) {
			LinkedList<String> expected = new LinkedList<>();
			Collections.addAll(expected, output);

			LinkedList<String> actual = new LinkedList<>();
			FileStemmer.addStems(line, new SnowballStemmer(ENGLISH), actual);

			assertJoined(expected, actual, "Lists do not match; use compare feature in Eclipse for details.");
		}

		/**
		 * Tests repeat calls.
		 */
		@Test
		@Order(7)
		public void testRepeat() {
			LinkedList<String> expected = new LinkedList<>();
			expected.addAll(Collections.nCopies(4, "consign"));
			expected.addAll(Collections.nCopies(7, "consist"));

			LinkedList<String> actual = new LinkedList<>();
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			FileStemmer.addStems("consign consigned consigning consignment", stemmer, actual);
			FileStemmer.addStems("consist consisted consistency consistent consistently consisting consists", stemmer, actual);

			assertJoined(expected, actual, "Sets do not match; use compare feature in Eclipse for details.");
		}
	}

	/**
	 * Collection of tests.
	 *
	 * @see FileStemmer#listStems(String)
	 * @see FileStemmer#listStems(String, opennlp.tools.stemmer.Stemmer)
	 */
	@Nested
	@Order(3)
	@TestMethodOrder(OrderAnnotation.class)
	public class ListStemsTests extends StemmerTests {
		/**
		 * Tests expected output for given test case.
		 *
		 * @param line the line to stem
		 * @param output the expected output
		 */
		@Override
		public void test(String line, String[] output) {
			List<String> expected = Arrays.stream(output).toList();
			List<String> actual = FileStemmer.listStems(line);
			assertJoined(expected, actual, "Lists do not match; use compare feature in Eclipse for details.");
		}
	}

	/**
	 * Collection of tests.
	 *
	 * @see FileStemmer#uniqueStems(String)
	 * @see FileStemmer#uniqueStems(String, opennlp.tools.stemmer.Stemmer)
	 */
	@Nested
	@Order(4)
	@TestMethodOrder(OrderAnnotation.class)
	public class UniqueStemsTests extends StemmerTests {
		@Override
		public void test(String line, String[] output) {
			// converts to list for ordering purposes
			List<String> expected = Arrays.stream(output).sorted().distinct().toList();
			List<String> actual = FileStemmer.uniqueStems(line).stream().toList();
			assertJoined(expected, actual, "Sets do not match; use compare feature in Eclipse for details.");
		}
	}

	/**
	 * Collection of tests.
	 *
	 * @see FileStemmer#listStems(Path)
	 */
	@Nested
	@Order(5)
	@TestMethodOrder(OrderAnnotation.class)
	public class ListStemFileTests {
		/**
		 * Tests expected output for given test case.
		 *
		 * @param path the file path to stem
		 * @param output the expected output
		 * @throws IOException if I/O error occurs
		 */
		public void test(Path path, String[] output) throws IOException {
			List<String> expected = Arrays.stream(output).toList();
			List<String> actual = FileStemmer.listStems(path);
			assertJoined(expected, actual, "Lists do not match; use compare feature in Eclipse for details.");
		}

		/**
		 * Runs a single test case.
		 *
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(1)
		public void testCleaner() throws IOException {
			Path path = BASE_PATH.resolve("cleaner.txt");
			String[] output = { "okapi", "okapi", "mongoos", "lori", "lori", "lori", "axolotl", "narwhal", "platypus",
					"echidna", "tarsier", "antelop", "antelop", "antelop", "antelop", "antelop", "antelop", "antelop", "antelop",
					"antelop", "antelop", "observa", "observ", "observacion", "observ", "observ", "observ", "observ", "observ",
					"observ", "observ", "observ", "observ", "observ" };
			test(path, output);
		}

		/**
		 * Runs a single test case.
		 *
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(2)
		public void testStems() throws IOException {
			Path input = BASE_PATH.resolve("stem-in.txt");
			Path output = BASE_PATH.resolve("stem-out.txt");
			String[] expected = FileStemmer.parse(Files.readString(output, UTF_8));
			test(input, expected);
		}
	}

	/**
	 * Collection of tests.
	 *
	 * @see FileStemmer#uniqueStems(Path)
	 */
	@Nested
	@Order(6)
	@TestMethodOrder(OrderAnnotation.class)
	public class UniqueStemFileTests extends ListStemFileTests {
		@Override
		public void test(Path path, String[] output) throws IOException {
			// converts to list for ordering purposes
			List<String> expected = Arrays.stream(output).sorted().distinct().toList();
			List<String> actual = FileStemmer.uniqueStems(path).stream().toList();
			assertJoined(expected, actual, "Sets do not match; use compare feature in Eclipse for details.");
		}
	}

	/**
	 * Collection of tests.
	 *
	 * @see FileStemmer#listUniqueStems(Path)
	 */
	@Nested
	@Order(7)
	@TestMethodOrder(OrderAnnotation.class)
	public class ListUniqueStemTests {
		/**
		 * Tests expected output for given test case.
		 *
		 * @param expected the expected result
		 * @param actual the actual result
		 */
		public void test(List<? extends Collection<String>> expected, ArrayList<TreeSet<String>> actual) {
			List<String> expectedJoined = expected.stream().map(Collection::toString).toList();
			List<String> actualJoined = actual.stream().map(Set::toString).toList();
			assertJoined(expectedJoined, actualJoined,
					"Lines do not match; use compare feature in Eclipse for details.");
		}

		/**
		 * Runs a single test case.
		 *
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(1)
		public void testCleaner() throws IOException {
			Path input = BASE_PATH.resolve("cleaner.txt");

			List<List<String>> expected = List.of(List.of("lori", "mongoos", "okapi"),
					List.of("axolotl", "echidna", "narwhal", "platypus", "tarsier"), List.of(), List.of("antelop"),
					List.of("antelop"), List.of(), List.of("observ", "observa", "observacion"), List.of("observ"));

			ArrayList<TreeSet<String>> actual = FileStemmer.listUniqueStems(input);
			test(expected, actual);
		}
	}

	/**
	 * Collection of tests. These tests should already pass, since the methods are
	 * given for you already.
	 *
	 * @see FileStemmer#split(String)
	 * @see FileStemmer#clean(String)
	 * @see FileStemmer#parse(String)
	 */
	@Nested
	@Order(8)
	@TestMethodOrder(OrderAnnotation.class)
	public class CleanParseTests {
		/**
		 * Calls {@link FileStemmer#clean(String)} on the supplied text, and makes sure
		 * it matches the expected text.
		 *
		 * @param text the text to clean
		 * @param expected the cleaned output
		 */
		@ParameterizedTest
		@Order(1)
		@CsvSource(textBlock = """
				'hello world','hello world'
				'\t hello  world','\t hello  world'
				'hello, world!','hello world'
				'hello 1 world','hello  world'
				'hello @world','hello world'
				'HELLO WORLD','hello world'
				'¡Hello world!','hello world'
				'héḶlõ ẁörld','hello world'
				'1234567890',''
				'   ','   '
				""")
		public void testClean(String text, String expected) {
			String actual = FileStemmer.clean(text);
			assertEquals(expected, actual);
		}

		/**
		 * Calls {@link FileStemmer#parse(String)} on the supplied text, and makes sure
		 * it matches the expected array.
		 *
		 * @param text the text to parse
		 */
		@ParameterizedTest
		@Order(2)
		@ValueSource(strings = {
				"hello world",
				"\t hello  world ",
				"hello, world!",
				"hello 1 world",
				"hello @world",
				"HELLO WORLD",
				"¡Hello world!",
				"héḶlõ ẁörld"
		})
		public void testParseHello(String text) {
			String[] expected = new String[] { "hello", "world" };
			assertArrayEquals(expected, FileStemmer.parse(text));
		}

		/**
		 * Calls {@link FileStemmer#parse(String)} on the supplied text, and makes sure
		 * it matches an empty array.
		 *
		 * @param text the text to parse
		 */
		@ParameterizedTest
		@Order(3)
		@ValueSource(strings = {" ", "", "1234567890", "\t 11@ "})
		public void testParseEmpty(String text) {
			assertArrayEquals(FileStemmer.EMPTY, FileStemmer.parse(text));
		}
	}

	/**
	 * Attempts to check for issues with the approach.
	 */
	@Nested
	@Order(99)
	@TestMethodOrder(OrderAnnotation.class)
	public class ApproachTests extends HomeworkTest.ApproachTests {
		/**
		 * Checks to see if the File class was imported.
		 */
		@Test
		@Order(1)
		public void testFileImport() {
			String regex = "(?is)\\bimport\\s+java.io.File\\s*;";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(source);
			Assertions.assertFalse(matcher.find(), "Do not use the java.io.File class in your code!");
		}

		/**
		 * Checks to see if try-with-resources was used
		 */
		@Test
		@Order(2)
		public void testTryWithResources() {
			String regex = "(?is)\\btry\\s*\\([^}]+\\)";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(source);
			Assertions.assertTrue(matcher.find(), "Make sure you use try-with-resources in your code!");
		}

		/**
		 * Checks that methods throw exceptions as expected
		 */
		@Test
		@Order(3)
		public void testThrowsNullExceptions() {
			Assertions.assertThrows(NullPointerException.class,
					() -> { Path nullPath = null; FileStemmer.listStems(nullPath); });
		}

		/**
		 * Checks that methods throw exceptions as expected
		 */
		@Test
		@Order(4)
		public void testThrowsDirectoryExceptions() {
			Assertions.assertThrows(IOException.class,
					() -> { Path nullPath = Path.of("src"); FileStemmer.listStems(nullPath); });
		}

		/**
		 * Checks that methods throw exceptions as expected
		 */
		@Test
		@Order(5)
		public void testThrowsNoFileExceptions() {
			Assertions.assertThrows(IOException.class, () -> {
				Path nullPath = Path.of("nowhere");
				FileStemmer.uniqueStems(nullPath);
			});
		}

		/** The source code for TextFileStemmer. */
		private String source;

		/**
		 * Loads the entire source code as a String object.
		 *
		 * @throws IOException if an IO error occurs
		 */
		@BeforeEach
		public void setup() throws IOException {
			this.source = getSource(FileStemmer.class);
		}
	}

	/**
	 * Collection of stemmer tests.
	 *
	 * @see FileStemmer#addStems(String, opennlp.tools.stemmer.Stemmer, Collection)
	 * @see <a href="http://snowballstem.org/algorithms/english/stemmer.html">Stemmer</a>
	 */
	public abstract class StemmerTests {
		/**
		 * Tests expected output for given test case.
		 *
		 * @param line the line to stem
		 * @param output the expected output
		 */
		public abstract void test(String line, String[] output);

		/**
		 * Tests a single word.
		 */
		@Test
		@Order(1)
		public void testOneWord() {
			String line = "conspicuously";
			String[] output = { "conspicu" };
			test(line, output);
		}

		/**
		 * Tests an empty word.
		 */
		@Test
		@Order(2)
		public void testEmpty() {
			test("", new String[] {});
		}

		/**
		 * Tests multiple words without duplicates.
		 */
		@Test
		@Order(3)
		public void testUnique() {
			test("cat bat ant", new String[] { "cat", "bat", "ant" });
		}

		/**
		 * Tests multiple words with duplicates.
		 */
		@Test
		@Order(4)
		public void testDuplicates() {
			test("cat cat bat ant", new String[] { "cat", "cat", "bat", "ant" });
		}

		/**
		 * Tests a subset of stemmer test cases with lowercase words.
		 */
		@Test
		@Order(5)
		public void testLower() {
			String line = String.join(", ", LOWER_STEM_IN);
			test(line, LOWER_STEM_OUT);
		}

		/**
		 * Tests a subset of stemmer test cases with uppercase words.
		 */
		@Test
		@Order(6)
		public void testUpper() {
			String line = String.join(", ", UPPER_STEM_IN);
			test(line, UPPER_STEM_OUT);
		}
	}

	/** Path to the test resources. */
	public static final Path BASE_PATH = RESOURCES_DIR.resolve("stemmer");

	/** Example of lowercase words before stemming */
	public static final String[] LOWER_STEM_IN = { "consign", "consigned", "consigning", "consignment", "consist", "consisted", "consistency",
			"consistent", "consistently", "consisting", "consists", "consolation", "consolations", "consolatory",
			"console", "consoled", "consoles", "consolidate", "consolidated", "consolidating", "consoling", "consolingly",
			"consols", "consonant", "consort", "consorted", "consorting", "conspicuous", "conspicuously", "conspiracy",
			"conspirator", "conspirators", "conspire", "conspired", "conspiring", "constable", "constables", "constance",
			"constancy", "constant" };

	/** Example of lowercase words after stemming */
	public static final String[] LOWER_STEM_OUT = { "consign", "consign", "consign", "consign", "consist", "consist", "consist", "consist",
			"consist", "consist", "consist", "consol", "consol", "consolatori", "consol", "consol", "consol", "consolid",
			"consolid", "consolid", "consol", "consol", "consol", "conson", "consort", "consort", "consort", "conspicu",
			"conspicu", "conspiraci", "conspir", "conspir", "conspir", "conspir", "conspir", "constabl", "constabl",
			"constanc", "constanc", "constant" };

	/** Example of uppercase words before stemming */
	public static final String[] UPPER_STEM_IN = { "KNACK", "KNACKERIES", "KNACKS", "KNAG", "KNAVE", "KNAVES", "KNAVISH", "KNEADED", "KNEADING",
			"KNEE", "KNEEL", "KNEELED", "KNEELING", "KNEELS", "KNEES", "KNELL", "KNELT", "KNEW", "KNICK", "KNIF", "KNIFE",
			"KNIGHT", "KNIGHTLY", "KNIGHTS", "KNIT", "KNITS", "KNITTED", "KNITTING", "KNIVES", "KNOB", "KNOBS", "KNOCK",
			"KNOCKED", "KNOCKER", "KNOCKERS", "KNOCKING", "KNOCKS", "KNOPP", "KNOT", "KNOTS" };

	/** Example of uppercase words after stemming */
	public static final String[] UPPER_STEM_OUT = { "knack", "knackeri", "knack", "knag", "knave", "knave", "knavish", "knead", "knead", "knee",
			"kneel", "kneel", "kneel", "kneel", "knee", "knell", "knelt", "knew", "knick", "knif", "knife", "knight",
			"knight", "knight", "knit", "knit", "knit", "knit", "knive", "knob", "knob", "knock", "knock", "knocker",
			"knocker", "knock", "knock", "knopp", "knot", "knot" };

	/** Creates a new instance of this class. */
	public FileStemmerTest() {
	}
}
