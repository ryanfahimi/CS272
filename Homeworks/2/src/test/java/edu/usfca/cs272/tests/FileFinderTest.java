package edu.usfca.cs272.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.usfca.cs272.tests.utils.HomeworkTest;
import edu.usfca.cs272.utils.FileFinder;

/**
 * Tests the {@link FileFinder} class.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@Tag("FileFinder")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodName.class)
public class FileFinderTest extends HomeworkTest {
	/**
	 * Tests that text extensions are detected properly.
	 *
	 * @see FileFinder#IS_TEXT
	 */
	@Nested
	@Order(1)
	@TestMethodOrder(OrderAnnotation.class)
	public class TextExtensionTests {
		/**
		 * Tests files that SHOULD be considered text files.
		 *
		 * @param file the file name
		 */
		@Order(1)
		@ParameterizedTest
		@ValueSource(strings = { "animals_copy.text", "capital_extension.TXT", "empty.txt",
				"position.teXt", "words.tExT", "digits.tXt" })
		public void testIsTextFile(String file) {
			Path path = SIMPLE.resolve(file);
			Assertions.assertTrue(FileFinder.IS_TEXT.test(path), path::toString);
		}

		/**
		 * Tests files that SHOULD NOT be considered text files.
		 *
		 * @param file the file name
		 */
		@Order(2)
		@ParameterizedTest
		@ValueSource(strings = { "double_extension.txt.html", "no_extension",
				"wrong_extension.html", "dir.txt", "nowhere.txt", ".txt" })
		public void testIsNotTextFile(String file) {
			Path path = SIMPLE.resolve(file);
			Assertions.assertFalse(FileFinder.IS_TEXT.test(path), path::toString);
		}

		/** Creates a new instance of this class. */
		public TextExtensionTests() {}
	}

	/**
	 * Tests the default find functionality.
	 *
	 * @see FileFinder#findText(Path)
	 */
	@Nested
	@Order(2)
	@TestMethodOrder(OrderAnnotation.class)
	public class FindTextTests {
		/**
		 * Tests the {@link FileFinder} method.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(1)
		public void testDirectoryOneFile() throws IOException {
			Path directory = SIMPLE.resolve("dir.txt");
			Path expected = directory.resolve("findme.Txt");
			Path actual = FileFinder.findText(directory).findFirst().get();
			Assertions.assertEquals(expected, actual, directory.toString());
		}

		/**
		 * Tests the {@link FileFinder} method.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(2)
		public void testOneFile() throws IOException {
			Path directory = SIMPLE.resolve("hello.txt");
			Path expected = directory;
			Path actual = FileFinder.findText(directory).findFirst().get();
			Assertions.assertEquals(expected, actual, directory.toString());
		}

		/**
		 * Tests the {@link FileFinder} method.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(3)
		public void testNestedDirectory() throws IOException {
			String actual = FileFinder.findText(SIMPLE).distinct().sorted().map(Path::toString).collect(newLines);

			Path[] paths = new Path[] {
					SIMPLE.resolve(".txt").resolve("hidden.txt"),
					SIMPLE.resolve("a").resolve("b").resolve("c").resolve("d").resolve("subdir.txt"),
					SIMPLE.resolve("dir.txt").resolve("findme.Txt"),
					SIMPLE.resolve("animals_copy.text"),
					SIMPLE.resolve("animals_double.text"),
					SIMPLE.resolve("animals.text"),
					SIMPLE.resolve("capital_extension.TXT"),
					SIMPLE.resolve("capitals.txt"),
					SIMPLE.resolve("digits.tXt"),
					SIMPLE.resolve("empty.txt"),
					SIMPLE.resolve("hello.txt"),
					SIMPLE.resolve("position.teXt"),
					SIMPLE.resolve("symbols.txt"),
					SIMPLE.resolve("words.tExT")
			};

			String expected = Stream.of(paths).distinct().sorted()
					.map(Path::toString).collect(newLines);

			// String comparison for nicer debugging in JUnit (but less efficient)
			Assertions.assertEquals(expected, actual);
		}

		/**
		 * Tests the stream has the expected number of paths.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(4)
		public void testNestedDirectorySize() throws IOException {
			Assertions.assertEquals(14, FileFinder.findText(SIMPLE).count());
		}

		/**
		 * Tests that IO exceptions are NOT caught in the methods.
		 */
		@Test
		@Order(5)
		public void testException() {
			Path nowhere = SIMPLE.resolve("nowhere.txt");
			Executable test = () -> FileFinder.findText(nowhere);
			String debug = "Do not catch IO exceptions in your methods!";
			Assertions.assertThrows(IOException.class, test, debug);
		}

		/** Creates a new instance of this class. */
		public FindTextTests() {}
	}

	/**
	 * Tests the general find functionality.
	 *
	 * @see FileFinder#findText(Path)
	 */
	@Nested
	@Order(3)
	@TestMethodOrder(OrderAnnotation.class)
	public class FindGeneralTests {
		/**
		 * Tests the general {@link FileFinder#find(Path, Predicate)} method works as
		 * expected.
		 *
		 * @throws IOException if an IO error occurs.
		 */
		@Test
		@Order(1)
		public void testMarkdown() throws IOException {
			Predicate<Path> html = p -> p.toString().endsWith(".md");
			Path[] paths = new Path[] {
					SIMPLE.resolve("a").resolve("b").resolve("c").resolve("markdown.md"),
					SIMPLE.resolve("sentences.md")
			};

			String expected = Stream.of(paths).sorted().map(Path::toString).collect(newLines);
			String actual = FileFinder.find(SIMPLE, html).sorted().map(Path::toString).collect(newLines);

			// String comparison for nicer debugging in JUnit (but less efficient)
			Assertions.assertEquals(expected, actual);
		}

		/**
		 * Tests the general {@link FileFinder#find(Path, Predicate)} method works as
		 * expected.
		 *
		 * @throws IOException if an IO error occurs.
		 */
		@Test
		@Order(2)
		public void testHtmlFiles() throws IOException {
			Predicate<Path> html = p -> p.toString().endsWith(".html");
			Path[] paths = new Path[] { SIMPLE.resolve("double_extension.txt.html"), SIMPLE.resolve("wrong_extension.html") };
			String expected = Stream.of(paths).sorted().map(Path::toString).collect(newLines);
			String actual = FileFinder.find(SIMPLE, html).sorted().map(Path::toString).collect(newLines);

			// String comparison for nicer debugging in JUnit (but less efficient)
			Assertions.assertEquals(expected, actual);
		}

		/**
		 * Tests the general {@link FileFinder#find(Path, Predicate)} method works as
		 * expected.
		 *
		 * @throws IOException if an IO error occurs.
		 */
		@Test
		@Order(3)
		public void testSubdirectories() throws IOException {
			Path[] paths = new Path[] { SIMPLE, SIMPLE.resolve(".txt"), SIMPLE.resolve("a"), SIMPLE.resolve("a").resolve("b"),
					SIMPLE.resolve("a").resolve("b").resolve("c"), SIMPLE.resolve("a").resolve("b").resolve("c").resolve("d"),
					SIMPLE.resolve("dir.txt") };

			String expected = Stream.of(paths).sorted().map(Path::toString).collect(newLines);
			String actual = FileFinder.find(SIMPLE, Files::isDirectory).sorted().map(Path::toString).collect(newLines);

			// String comparison for nicer debugging in JUnit (but less efficient)
			Assertions.assertEquals(expected, actual);
		}

		/**
		 * Tests that symbolic links (i.e. shortcuts) are working as expected. The test
		 * will be skipped on systems that do not support symbolic links.
		 *
		 * @throws IOException if an IO error occurs.
		 */
		@Test
		@Order(4)
		public void testSymbolicLinks() throws IOException {
			Path parent = SIMPLE.getParent();
			Path symbolic = parent.resolve("symbolic");

			try {
				Files.deleteIfExists(symbolic);
				Files.createSymbolicLink(symbolic.toAbsolutePath(), SIMPLE.toAbsolutePath());
			}
			catch (Exception e) {
				String warning = "Warning: Unable to test symbolic links on your system. This test will be skipped.";
				System.err.println(warning);

				// skip the test
				Assumptions.assumeTrue(false, warning);
			}

			Stream<Path> expected = FileFinder.findText(SIMPLE);
			Stream<Path> actual = FileFinder.findText(symbolic);

			Assertions.assertEquals(expected.count(), actual.count());
		}

		/**
		 * Tests that IO exceptions are NOT caught in the methods.
		 */
		@Test
		@Order(5)
		public void testException() {
			Path nowhere = SIMPLE.resolve("nowhere.txt");
			Executable test = () -> FileFinder.find(nowhere, p -> true);
			String debug = "Do not catch IO exceptions in your methods!";
			Assertions.assertThrows(IOException.class, test, debug);
		}

		/** Creates a new instance of this class. */
		public FindGeneralTests() {}
	}

	/**
	 * Tests the text list functionality.
	 *
	 * @see FileFinder#listText(Path)
	 */
	@Nested
	@Order(4)
	@TestMethodOrder(OrderAnnotation.class)
	public class TextListTests {
		/**
		 * Tests the {@link FileFinder} method.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(1)
		public void testDirectoryOneFile() throws IOException {
			Path directory = SIMPLE.resolve("dir.txt");
			List<Path> expected = List.of(directory.resolve("findme.Txt"));
			List<Path> actual = FileFinder.listText(directory);
			Assertions.assertEquals(expected, actual, directory.toString());
		}

		/**
		 * Tests the {@link FileFinder} method.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(2)
		public void testOneFile() throws IOException {
			Path directory = SIMPLE.resolve("hello.txt");
			List<Path> expected = List.of(directory);
			List<Path> actual = FileFinder.listText(directory);
			Assertions.assertEquals(expected, actual, directory.toString());
		}

		/**
		 * Tests the {@link FileFinder} method.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(3)
		public void testNestedList() throws IOException {
			List<Path> expectedList = List.of(
					SIMPLE.resolve(".txt").resolve("hidden.txt"),
					SIMPLE.resolve("a").resolve("b").resolve("c").resolve("d").resolve("subdir.txt"),
					SIMPLE.resolve("dir.txt").resolve("findme.Txt"),
					SIMPLE.resolve("animals_copy.text"),
					SIMPLE.resolve("animals_double.text"),
					SIMPLE.resolve("animals.text"),
					SIMPLE.resolve("capital_extension.TXT"),
					SIMPLE.resolve("capitals.txt"),
					SIMPLE.resolve("digits.tXt"),
					SIMPLE.resolve("empty.txt"),
					SIMPLE.resolve("hello.txt"),
					SIMPLE.resolve("position.teXt"),
					SIMPLE.resolve("symbols.txt"),
					SIMPLE.resolve("words.tExT")
			);

			List<Path> actualList = FileFinder.listText(SIMPLE);
			String actual = actualList.stream().sorted().map(Path::toString).collect(newLines);
			String expected = expectedList.stream().sorted().map(Path::toString).collect(newLines);

			// Uses String comparison for nicer debugging in JUnit (but less efficient)
			Assertions.assertEquals(expected, actual);
		}

		/**
		 * Tests the list has the expected number of paths.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(4)
		public void testNestedListSize() throws IOException {
			var actual = FileFinder.listText(SIMPLE);
			Assertions.assertEquals(14, actual.size(), actual::toString);
		}

		/**
		 * Tests that IO exceptions are NOT caught in the methods.
		 */
		@Test
		@Order(5)
		public void testException() {
			Path nowhere = SIMPLE.resolve("nowhere.txt");
			Executable test = () -> FileFinder.listText(nowhere);
			String debug = "Do not catch IO exceptions in your methods!";
			Assertions.assertThrows(IOException.class, test, debug);
		}

		/** Creates a new instance of this class. */
		public TextListTests() {}
	}

	/**
	 * Tests the default list functionality.
	 *
	 * @see FileFinder#listText(Path, Path)
	 */
	@Nested
	@Order(5)
	@TestMethodOrder(OrderAnnotation.class)
	public class DefaultListTests {
		/**
		 * Tests the {@link FileFinder} method.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(1)
		public void testDirectoryOneFile() throws IOException {
			Path directory = SIMPLE.resolve("dir.txt");
			Path hello = SIMPLE.resolve("hello.txt");
			List<Path> expected = List.of(directory.resolve("findme.Txt"));
			List<Path> actual = FileFinder.listText(directory, hello);
			Assertions.assertEquals(expected, actual);
		}

		/**
		 * Tests the {@link FileFinder} method.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(2)
		public void testOneFile() throws IOException {
			Path sentences = SIMPLE.resolve("sentences.md");
			Path hello = SIMPLE.resolve("hello.txt");
			List<Path> expected = List.of(hello);
			List<Path> actual = FileFinder.listText(sentences, hello);
			Assertions.assertEquals(expected, actual);
		}

		/**
		 * Tests the {@link FileFinder} method.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(5)
		public void testNowhere() throws IOException {
			Path nowhere = SIMPLE.resolve("nowhere.txt");
			List<Path> actual = FileFinder.listText(nowhere, nowhere);
			List<Path> expected = List.of(nowhere);
			Assertions.assertEquals(expected, actual, nowhere.toString());
		}

		/** Creates a new instance of this class. */
		public DefaultListTests() {}
	}

	/**
	 * Tests the approach.
	 */
	@Nested
	@Order(99)
	@TestMethodOrder(OrderAnnotation.class)
	public class ApproachTests extends HomeworkTest.ApproachTests {
		/*
		 * These only approximately determine if a lambda function was used and the File
		 * class was NOT used.
		 */

		/** Class to test */
		public static Class<?> IS_TEXT = FileFinder.IS_TEXT.getClass();

		/**
		 * Tests that the {@link FileFinder#IS_TEXT} is not an anonymous class.
		 */
		@Test
		@Order(1)
		public void testAnonymous() {
			Assertions.assertFalse(IS_TEXT.isAnonymousClass());
		}

		/**
		 * Tests that the {@link FileFinder#IS_TEXT} is not an enclosing class.
		 */
		@Test
		@Order(2)
		public void testEnclosingClass() {
			Assertions.assertNull(IS_TEXT.getEnclosingClass());
		}

		/**
		 * Tests that the {@link FileFinder#IS_TEXT} is not a synthetic class.
		 */
		@Test
		@Order(3)
		public void testSyntheticClass() {
			Assertions.assertTrue(IS_TEXT.isSynthetic());
		}

		/**
		 * Tests that the {@link FileFinder#IS_TEXT} is likely a lambda function.
		 */
		@Test
		@Order(4)
		public void testClassName() {
			String actual = IS_TEXT.getTypeName();
			String[] parts = actual.split("[$]+");
			Assertions.assertTrue(parts[1].startsWith("Lambda"));
		}

		/**
		 * Tests that the java.io.File class does not appear in the implementation code.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(5)
		public void testFileClass() throws IOException {
			String source = getSource(FileFinder.class);
			Assertions.assertFalse(source.contains("import java.io.File;"));
			Assertions.assertFalse(source.contains(".toFile()"));
		}

		/** Creates a new instance of this class. */
		public ApproachTests() {}
	}

	/** Creates a new instance of this class. */
	public FileFinderTest() {}

	/** Path to directory of text files */
	public static final Path SIMPLE = RESOURCES_DIR.resolve("simple");

	/** Used to collect information into Strings with newlines */
	public static final Collector<CharSequence, ?, String> newLines = Collectors.joining("\n");

	/**
	 * Runs before any tests to make sure environment is setup.
	 */
	@BeforeAll
	public static void checkEnvironment() {
		Assumptions.assumeTrue(Files.isDirectory(SIMPLE));
		Assumptions.assumeTrue(Files.exists(SIMPLE.resolve("hello.txt")));
	}
}
