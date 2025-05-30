package edu.usfca.cs272.tests;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
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

import edu.usfca.cs272.tests.utils.HomeworkTest;
import edu.usfca.cs272.utils.HtmlCleaner;

/**
 * Runs all of the tests associated with this homework.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@Tag("HtmlCleaner")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class HtmlCleanerTest extends HomeworkTest {
	/** Creates a new instance of this class. */
	public HtmlCleanerTest() {
	}

	/**
	 * Tests the {@link HtmlCleaner#stripTags(String)} method.
	 *
	 * @see HtmlCleaner#stripTags(String)
	 */
	@Nested
	@Order(1)
	@TestMethodOrder(OrderAnnotation.class)
	public class TagTests {
		/** Creates a new instance of this class. */
		public TagTests() {
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <b>hello</b> world!
		 * </pre>
		 */
		@Test
		@Order(1)
		public void testSimple() {
			String test = "<b>hello</b> world!";
			String expected = "hello world!";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>
		 * <b>hello
		 * </b> world!
		 * </pre>
		 */
		@Test
		@Order(2)
		public void testSimpleNewLine() {
			String test = "<b>hello\n</b> world!";
			String expected = "hello\n world!";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>{@literal
		 * <a
		 *  name=toc>table of contents</a>
		 * }</pre>
		 */
		@Test
		@Order(3)
		public void testAttributeNewline() {
			String test = "<a \n name=toc>table of contents</a>";
			String expected = "table of contents";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>{@literal
		 * <p>Hello, <strong>world</strong>!</p>
		 * }</pre>
		 */
		@Test
		@Order(4)
		public void testNestedTags() {
			String test = "<p>Hello, <strong>world</strong>!</p>";
			String expected = "Hello, world!";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>{@literal
		 * <p>Hello, <br/>world!</p>
		 * }</pre>
		 */
		@Test
		@Order(5)
		public void testLineBreak() {
			String test = "<p>Hello, <br/>world!</p>";
			String expected = "Hello, world!";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}

		/**
		 * View Javadoc to see HTML rendering of test case:
		 *
		 * <pre>{@literal
		 * <li>=>antelope<=</li>
		 * }</pre>
		 */
		@Test
		@Order(6)
		public void testSymbols() {
			String test = "<li>=>antelope<=</li>";
			String expected = "=>antelope<=";
			String actual = HtmlCleaner.stripTags(test);

			test(test, expected, actual);
		}
	}

	/**
	 * Tests the {@link HtmlCleaner#stripEntities(String)} method.
	 *
	 * @see HtmlCleaner#stripEntities(String)
	 */
	@Nested
	@Order(2)
	@TestMethodOrder(OrderAnnotation.class)
	public class EntityTests {
		/** Creates a new instance of this class. */
		public EntityTests() {
		}

		/**
		 * Tests:
		 * 
		 * <pre>
		 * 2010&ndash;2011
		 * </pre>
		 *
		 * (View Javadoc to see rendering.)
		 */
		@Test
		@Order(1)
		public void testNamed() {
			String test = "2010&ndash;2011";
			String expected = "2010–2011";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests:
		 * 
		 * <pre>
		 * 2010&#8211;2011
		 * </pre>
		 *
		 * (View Javadoc to see rendering.)
		 */
		@Test
		@Order(2)
		public void testNumbered() {
			String test = "2010&#8211;2011";
			String expected = "2010–2011";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests:
		 * 
		 * <pre>
		 * 2010&#x2013;2011
		 * </pre>
		 *
		 * (View Javadoc to see rendering.)
		 */
		@Test
		@Order(3)
		public void testHexadecimal() {
			String test = "2010&#x2013;2011";
			String expected = "2010–2011";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests:
		 * 
		 * <pre>
		 * touche&#769;!
		 * </pre>
		 *
		 * (View Javadoc to see rendering.)
		 */
		@Test
		@Order(4)
		public void testAccentHex() {
			String test = "touche&#769;!";
			String expected = "touché!";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests:
		 * 
		 * <pre>
		 * touch&eacute;!
		 * </pre>
		 *
		 * (View Javadoc to see rendering.)
		 */
		@Test
		@Order(5)
		public void testAccentNamed() {
			String test = "touch&eacute;!";
			String expected = "touché!";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests:
		 * 
		 * <pre>
		 * hello&mdash;good&dash;bye
		 * </pre>
		 *
		 * (View Javadoc to see rendering.)
		 *
		 * <p>Note: "&dash;" is not a valid HTML 4 entity but it is valid in HTML 5.
		 * Since we are using HTML 4 unescaping it will not parse properly.
		 */
		@Test
		@Order(6)
		public void testMultiple() {
			String test = "hello&mdash;good&dash;bye";
			String expected = "hello—goodbye";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests:
		 * 
		 * <pre>{@literal hello & good - bye}</pre>
		 */
		@Test
		@Order(7)
		public void testAmpersand() {
			String test = "hello & good-bye";
			String expected = "hello & good-bye";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests:
		 * 
		 * <pre>{@literal hello & good-bye;}</pre>
		 */
		@Test
		@Order(8)
		public void testAndSemicolon() {
			String test = "hello & good-bye;";
			String expected = "hello & good-bye;";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}

		/**
		 * Tests:
		 * 
		 * <pre>
		 * a&AElig;e
		 * </pre>
		 */
		@Test
		@Order(9)
		public void testAELetter() {
			String test = "a&AElig;e";
			String expected = "aÆe";
			String actual = HtmlCleaner.stripEntities(test);

			test(test, expected, actual);
		}
	}

	/**
	 * Tests the {@link HtmlCleaner#stripComments(String)} method.
	 *
	 * @see HtmlCleaner#stripComments(String)
	 */
	@Nested
	@Order(3)
	@TestMethodOrder(OrderAnnotation.class)
	public class CommentTests {
		/** Creates a new instance of this class. */
		public CommentTests() {
		}

		/**
		 * Tests text with only a simple comment.
		 */
		@Test
		@Order(1)
		public void testSimple() {
			String test = "<!-- hello -->";
			String expected = "";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		/**
		 * Tests text with a single comment within other text.
		 */
		@Test
		@Order(2)
		public void testABC() {
			String test = "A<!-- B -->C";
			String expected = "AC";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		/**
		 * Tests a comment broken up by newlines.
		 */
		@Test
		@Order(3)
		public void testNewLine() {
			String test = "A<!--\n B\r\n -->C";
			String expected = "AC";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		/**
		 * Tests a tag within a comment.
		 */
		@Test
		@Order(4)
		public void testTags() {
			String test = "A<!-- <b>B</b> -->C";
			String expected = "AC";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		/**
		 * Tests a comment with a different closing slash style.
		 */
		@Test
		@Order(5)
		public void testSlashes() {
			String test = "A<!-- B //-->C";
			String expected = "AC";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		/**
		 * Tests text with multiple comments.
		 */
		@Test
		@Order(7)
		public void testMultipleOneLine() {
			String test = "A<!-- B -->C D<!-- E -->F";
			String expected = "AC DF";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		/**
		 * Tests text with multiple comments.
		 */
		@Test
		@Order(8)
		public void testMultipleTwoLines() {
			String test = "A<!-- B -->C\nD<!-- E -->F";
			String expected = "AC\nDF";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		/**
		 * Tests text with multiple comments.
		 */
		@Test
		@Order(9)
		public void testMultipleMixed() {
			String test = "A<!-- B -->C\nD<!-- E\n -->F";
			String expected = "AC\nDF";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		/**
		 * Tests text with multiple comments.
		 */
		@Test
		@Order(10)
		public void testNested() {
			String test = "A<!-- B <hello> C -->D";
			String expected = "AD";
			String actual = HtmlCleaner.stripComments(test);

			test(test, expected, actual);
		}

		/**
		 * Tests text with snippet from gutenberg index.
		 */
		@Test
		@Order(11)
		public void testGuten() {
			String test = """
					<!--
					<li>
					  <a href="1228-h/1228-h.htm">
					    On the Origin of Species, 1st Edition by Charles Darwin (1859)
					  </a>
					</li>
					-->

					<li>
					  <a href="1661-h/1661-h.htm">
					    The Adventures of Sherlock Holmes by Arthur Conan Doyle (1892)
					  </a>
					</li>""";

			String expected = """


					<li>
					  <a href="1661-h/1661-h.htm">
					    The Adventures of Sherlock Holmes by Arthur Conan Doyle (1892)
					  </a>
					</li>""";

			String actual = HtmlCleaner.stripComments(test);
			test(test, expected, actual);
		}
	}

	/**
	 * Tests the {@link HtmlCleaner#stripElement(String, String)} method.
	 *
	 * @see HtmlCleaner#stripElement(String, String)
	 */
	@Nested
	@Order(4)
	@TestMethodOrder(OrderAnnotation.class)
	public class ElementTests {
		/** Creates a new instance of this class. */
		public ElementTests() {
		}

		/**
		 * Tests text with a single style element on a single line.
		 */
		@Test
		@Order(1)
		public void testStyle() {
			String test = "<style type=\"text/css\">body { font-size: 10pt; }</style>";
			String expected = "";
			String actual = HtmlCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		/**
		 * Tests text with a single style element across multiple lines.
		 */
		@Test
		@Order(2)
		public void testStyleNewline1() {
			String test = "<style type=\"text/css\">\r\nbody { font-size: 10pt; }\n</style>";
			String expected = "";
			String actual = HtmlCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		/**
		 * Tests text with a single style element with the style tag containing a
		 * newline.
		 */
		@Test
		@Order(3)
		public void testStyleNewline2() {
			String test = "<style \n type=\"text/css\">\nbody { font-size: 10pt; }\n</style>";
			String expected = "";
			String actual = HtmlCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		/**
		 * Tests text with multiple elements.
		 */
		@Test
		@Order(4)
		public void testMultipleOneLine() {
			String test = "a<test>b</test>c<test>d</test>e";
			String expected = "ace";
			String actual = HtmlCleaner.stripElement(test, "test");

			test(test, expected, actual);
		}

		/**
		 * Tests text with multiple elements.
		 */
		@Test
		@Order(5)
		public void testMultipleTwoLines() {
			String test = "a<test>b</test>c\n<test>d</test>e";
			String expected = "ac\ne";
			String actual = HtmlCleaner.stripElement(test, "test");

			test(test, expected, actual);
		}

		/**
		 * Tests text with multiple elements.
		 */
		@Test
		@Order(6)
		public void testMultipleMixed() {
			String test = "a<test>b</test>c\n<test>d\n</test>e";
			String expected = "ac\ne";
			String actual = HtmlCleaner.stripElement(test, "test");

			test(test, expected, actual);
		}

		/**
		 * Tests text with mixed elements.
		 */
		@Test
		@Order(7)
		public void testMixed() {
			String test = "<title>Hello</title><script>potato</script> world";
			String expected = "<title>Hello</title> world";
			String actual = HtmlCleaner.stripElement(test, "script");

			test(test, expected, actual);
		}

		/**
		 * Tests text with a single style element on a single line with mixed
		 * capitalization.
		 */
		@Test
		@Order(8)
		public void testCapitalized() {
			String test = "<STYLE type=\"text/css\">body { font-size: 10pt; }</stYle>";
			String expected = "";
			String actual = HtmlCleaner.stripElement(test, "style");

			test(test, expected, actual);
		}

		/**
		 * Tests elements that start with the same word.
		 */
		@Test
		@Order(9)
		public void testHead() {
			String test = """
					<head>apple</head>
					<header>banana</header>
					<heading>carrot</heading>
					""";
			String expected = """

					<header>banana</header>
					<heading>carrot</heading>
					""";
			String actual = HtmlCleaner.stripElement(test, "head");

			test(test, expected, actual);
		}

		/**
		 * Tests elements that start with the same word and are nested.
		 */
		@Test
		@Order(10)
		public void testNestedHead() {
			String test = """
					<header><head>daikon</head> radish</header>
					<head><header>spring</header> onion</head>
					""";
			String expected = """
					<header> radish</header>

					""";
			String actual = HtmlCleaner.stripElement(test, "head");

			test(test, expected, actual);
		}
	}

	/**
	 * Tests the {@link HtmlCleaner#stripHtml(String)} method on HTML files.
	 *
	 * @see HtmlCleaner#stripHtml(String)
	 */
	@Nested
	@Order(5)
	@TestMethodOrder(OrderAnnotation.class)
	public class SimpleFileTests {
		/** Creates a new instance of this class. */
		public SimpleFileTests() {
		}

		/**
		 * Tests an actual HTML file.
		 *
		 * @param name name of HTML file
		 * @throws IOException if unable to read test files
		 */
		@ParameterizedTest
		@Order(1)
		@ValueSource(strings = { "hello", "birds", "yellowthroat", "symbols" })
		public void testSimple(String name) throws IOException {
			test(name);
		}
	}

	/**
	 * Tests the {@link HtmlCleaner#stripHtml(String)} method on HTML files.
	 *
	 * @see HtmlCleaner#stripHtml(String)
	 */
	@Nested
	@Order(6)
	@TestMethodOrder(OrderAnnotation.class)
	public class ComplexFileTests {
		/** Creates a new instance of this class. */
		public ComplexFileTests() {
		}

		/**
		 * Tests an actual HTML file.
		 *
		 * @param name name of HTML file
		 * @throws IOException if unable to read test files
		 */
		@ParameterizedTest
		@Order(1)
		@ValueSource(strings = { "pangrams", "rfc475", "1322-h", "allclasses-index", "guten", "50468-h" })
		public void testComplex(String name) throws IOException {
			test(name);
		}
	}

	/**
	 * Tests if on GitHub Actions or not.
	 */
	@Nested
	@Order(99)
	@TestMethodOrder(OrderAnnotation.class)
	public class ApproachTests extends HomeworkTest.ApproachTests {
		/** Creates a new instance of this class. */
		public ApproachTests() {
		}
	}

	/**
	 * Tests that stripping HTML from actual HTML files work.
	 *
	 * @param name the file name to compare
	 * @throws IOException if unable to open or write the test files
	 */
	public static void test(String name) throws IOException {
		Path basePath = Path.of("src", "test", "resources");
		Path htmlPath = basePath.resolve("html").resolve("%s.html".formatted(name));
		Path textPath = basePath.resolve("expected").resolve("%s.txt".formatted(name));

		String html = Files.readString(htmlPath, UTF_8);
		String actual = HtmlCleaner.stripHtml(html).stripTrailing();

		// output actual to file for debugging
		Path outPath = Path.of("actual");
		Files.createDirectories(outPath);
		Files.writeString(outPath.resolve("%s-actual.txt".formatted(name)), actual, UTF_8);

		String expected = Files.readString(textPath, UTF_8).stripTrailing();
		String debug = """
					Differences found!%nCompare expected and actual files:
					Input: src/test/resources/html/%1$s.html
					Expected: src/test/resources/expected/%1$s.txt
					Actual: out/%1$s-actual.txt
				""";
		Assertions.assertEquals(expected, actual, () -> debug.formatted(name));
	}

	/**
	 * Helper method to compare the expect and actual text are equal.
	 *
	 * @param test the text being tested
	 * @param expected the expected output
	 * @param actual the actual output
	 */
	public static void test(String test, String expected, String actual) {
		// just in case any weirdness with special characters
		String cleanExpected = Normalizer.normalize(expected, Normalizer.Form.NFD);
		String cleanActual = Normalizer.normalize(actual, Normalizer.Form.NFD);

		String format = "%nHTML:%n%s%n%nExpected:%n%s%n%nActual:%n%s%n%n";
		Supplier<String> debug = () -> format.formatted(test, cleanExpected, cleanActual);
		Assertions.assertEquals(cleanExpected, cleanActual, debug);
	}
}
