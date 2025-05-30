package edu.usfca.cs272.tests;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

import edu.usfca.cs272.tests.utils.HomeworkTest;
import edu.usfca.cs272.utils.FileIndex;
import edu.usfca.cs272.utils.ForwardIndex;

/**
 * Tests the {@link FileIndex} class.
 *
 * @see FileIndex
 * @see ForwardIndex
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@Tag("FileIndex")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodName.class)
public class FileIndexTest extends HomeworkTest {
	/** Sample text file. */
	private static final Path ANIMALS = RESOURCES_DIR.resolve("simple").resolve("animals.text");

	/** Sample text file. */
	private static final Path SENTENCES = RESOURCES_DIR.resolve("simple").resolve("sentences.md");

	/** Sample empty file. */
	private static final Path EMPTY = Path.of("empty.txt");

	/** Sample simple file. */
	private static final Path HELLO = Path.of("hello.txt");

	/** Sample simple file. */
	private static final Path WORLD = Path.of("world.txt");

	/** Name of class being tested. */
	private static final String HOMEWORK = FileIndex.class.getSimpleName();

	/**
	 * Creates an empty {@link FileIndex} and returns it casted as a
	 * {@link ForwardIndex}. If this method does not compile, then the
	 * {@link FileIndex} class is not properly implementing the {@link ForwardIndex}
	 * interface.
	 *
	 * @return new empty text file index
	 */
	public static ForwardIndex<Path> createEmpty() {
		/*
		 * IF YOU ARE SEEING COMPILE ERRORS... it is likely you have not yet properly
		 * implemented the interface!
		 */

		return new FileIndex();
	}

	/**
	 * Tests of an index with a single initial path and word.
	 */
	@Nested
	@Order(1)
	@TestMethodOrder(OrderAnnotation.class)
	public class SimpleAddTests {
		/** Placeholder for index object being tested. */
		private ForwardIndex<Path> index;

		/**
		 * Initializes an empty text file index.
		 */
		@BeforeEach
		public void initializeIndex() {
			index = createEmpty();
			index.add(HELLO, "hello");
			index.add(HELLO, "howdy");
			index.add(WORLD, "earth");
		}

		/**
		 * Tests the toString() implementation.
		 */
		@Order(1)
		@Test
		public void testString() {
			boolean actual = index.toString().contains("hello");
			String debug = "Override toString() with a useful implementation!";
			Assertions.assertTrue(actual, debug);
		}

		/**
		 * Tests path exists in index.
		 */
		@Test
		@Order(2)
		public void testHasPathHello() {
			Assertions.assertTrue(index.has(HELLO), index.toString());
		}

		/**
		 * Tests path exists in index.
		 */
		@Test
		@Order(3)
		public void testHasPathWorld() {
			Assertions.assertTrue(index.has(WORLD), index.toString());
		}

		/**
		 * Tests path exists in index.
		 */
		@Test
		@Order(4)
		public void testNotPathWorld() {
			Assertions.assertFalse(index.has(EMPTY), index.toString());
		}

		/**
		 * Test number of paths.
		 */
		@Test
		@Order(5)
		public void testSizePaths() {
			// hello.txt, world.txt
			Assertions.assertEquals(2, index.size(), index.toString());
		}

		/**
		 * Tests word DOES exist for a path.
		 */
		@Test
		@Order(6)
		public void testHasWordHello() {
			Assertions.assertTrue(index.has(HELLO, "hello"), index.toString());
		}

		/**
		 * Tests word DOES exist for a path.
		 */
		@Test
		@Order(7)
		public void testHasWordHowdy() {
			Assertions.assertTrue(index.has(HELLO, "howdy"), index.toString());
		}

		/**
		 * Tests word DOES exist for a path.
		 */
		@Test
		@Order(8)
		public void testNotWordEarth() {
			Assertions.assertFalse(index.has(HELLO, "earth"), index.toString());
		}

		/**
		 * Tests number of words for path.
		 */
		@Test
		@Order(9)
		public void testSizeHello() {
			// hello, howdy
			Assertions.assertEquals(2, index.size(HELLO), index.toString());
		}

		/**
		 * Tests word DOES exist for a path.
		 */
		@Test
		@Order(10)
		public void testNotWordHowdy() {
			Assertions.assertFalse(index.has(WORLD, "howdy"), index.toString());
		}

		/**
		 * Tests word DOES exist for a path.
		 */
		@Test
		@Order(11)
		public void testhasWordEarth() {
			Assertions.assertTrue(index.has(WORLD, "earth"), index.toString());
		}

		/**
		 * Tests number of words for path.
		 */
		@Test
		@Order(12)
		public void testSizeWorld() {
			// earth
			Assertions.assertEquals(1, index.size(WORLD), index.toString());
		}

		/**
		 * Tests paths are fetched properly.
		 */
		@Test
		@Order(13)
		public void testViewPaths() {
			Assertions.assertTrue(index.view().contains(HELLO), index.toString());
		}

		/**
		 * Tests words are fetched properly.
		 */
		@Test
		@Order(14)
		public void testViewWords() {
			Assertions.assertTrue(index.view(HELLO).contains("hello"), index.toString());
		}

		/**
		 * Tests size of paths fetched.
		 */
		@Test
		@Order(15)
		public void testViewPathsSize() {
			Assertions.assertEquals(2, index.view().size(), index.toString());
		}

		/**
		 * Tests size of words fetched.
		 */
		@Test
		@Order(16)
		public void testViewWordsSize() {
			Assertions.assertEquals(2, index.view(HELLO).size(), index.toString());
		}

		/**
		 * Tests adding same location/word pair twice has no impact.
		 */
		@Test
		@Order(17)
		public void testDoubleAdd() {
			index.add(HELLO, "hello");
			Assertions.assertEquals(2, index.size(HELLO), index.toString());
		}

		/**
		 * Tests adding new word for a location.
		 */
		@Test
		@Order(18)
		public void testAddNewWord() {
			index.add(HELLO, "aloha");
			Assertions.assertEquals(3, index.size(HELLO), index.toString());
		}

		/**
		 * Tests adding new location.
		 */
		@Test
		@Order(19)
		public void testAddNewPath() {
			index.add(Path.of("moons.txt"), "europa");
			Assertions.assertEquals(3, index.size(), index.toString());
		}

		/** Creates a new instance of this class. */
			public SimpleAddTests() {}
	}

	/**
	 * Tests empty index.
	 */
	@Nested
	@Order(2)
	@TestMethodOrder(OrderAnnotation.class)
	public class EmptyTests {
		/** Placeholder for index object being tested. */
		private ForwardIndex<Path> index;

		/**
		 * Initializes an empty text file index.
		 */
		@BeforeEach
		public void initializeIndex() {
			index = createEmpty();
		}

		/**
		 * Tests the toString() implementation.
		 */
		@Order(1)
		@Test
		public void testStringEmpty() {
			Assertions.assertFalse(index.toString().contains("TextFileIndex@"),
					"Override toString() with a useful implementation!");
		}

		/**
		 * Tests that there are no paths.
		 */
		@Test
		@Order(2)
		public void testSizePaths() {
			Assertions.assertEquals(0, index.size(), index.toString());
		}

		/**
		 * Tests that there are no words for a path not in our index.
		 */
		@Test
		@Order(3)
		public void testSizeWords() {
			Assertions.assertEquals(0, index.size(EMPTY), index.toString());
		}

		/**
		 * Tests that a path does not exist as expected.
		 */
		@Test
		@Order(4)
		public void testHashPath() {
			Assertions.assertFalse(index.has(EMPTY), index.toString());
		}

		/**
		 * Tests that a word does not exist as expected.
		 */
		@Test
		@Order(5)
		public void testHasWord() {
			Assertions.assertFalse(index.has(EMPTY, "empty"), index.toString());
		}

		/**
		 * Tests that no paths are fetched as expected.
		 */
		@Test
		@Order(6)
		public void testViewPaths() {
			Assertions.assertTrue(index.view().isEmpty(), index.toString());
		}

		/**
		 * Tests that no words are fetched as expected.
		 */
		@Test
		@Order(7)
		public void testViewWords() {
			Assertions.assertTrue(index.view(EMPTY).isEmpty(), index.toString());
		}

		/** Creates a new instance of this class. */
			public EmptyTests() {}
	}

	/**
	 * Tests the addAll implementations.
	 */
	@Nested
	@Order(3)
	@TestMethodOrder(OrderAnnotation.class)
	public class AddAllTests {
		/** The target index. */
		private ForwardIndex<Path> index;

		/** The other index. */
		private ForwardIndex<Path> other;

		/**
		 * Initializes the indexes.
		 */
		@BeforeEach
		public void initializeIndex() {
			index = createEmpty();
			other = createEmpty();
		}

		/**
		 * Tests adding array of one word.
		 */
		@Test
		@Order(1)
		public void testAddAllFalse() {
			index.add(HELLO, new String[] { "hello" });
			Assertions.assertEquals(1, index.size(HELLO), index.toString());
		}

		/**
		 * Tests adding array with two words.
		 */
		@Test
		@Order(2)
		public void testAddAllTrue() {
			index.add(HELLO, new String[] { "hello", "world" });
			Assertions.assertEquals(2, index.size(HELLO), index.toString());
		}

		/**
		 * Tests adding array with two words and one duplicate.
		 */
		@Test
		@Order(3)
		public void testAddAllDuplicate() {
			index.add(HELLO, new String[] { "hello", "world", "hello" });
			Assertions.assertEquals(2, index.size(HELLO), index.toString());
		}

		/**
		 * Tests adding array with mixed adds.
		 */
		@Test
		@Order(4)
		public void testMixedAdds() {
			index.add(HELLO, "hello");
			index.add(HELLO, new String[] { "hello", "world" });
			Assertions.assertEquals(2, index.size(HELLO), index.toString());
		}

		/**
		 * Tests adding an empty index to an empty index.
		 */
		@Test
		@Order(5)
		public void testEmptyEmpty() {
			index.addAll(other);
			Assertions.assertEquals(0, index.size(), index.toString());
			Assertions.assertEquals(0, other.size(), other.toString());
		}

		/**
		 * Tests adding an empty index to a simple index.
		 */
		@Test
		@Order(6)
		public void testSimpleEmpty() {
			index.add(HELLO, "hello");
			index.addAll(other);
			Assertions.assertEquals(1, index.view(HELLO).size(), index.toString());
			Assertions.assertEquals(0, other.view(HELLO).size(), other.toString());
		}

		/**
		 * Tests adding a simple index to an empty index.
		 */
		@Test
		@Order(7)
		public void testEmptySimple() {
			other.add(HELLO, "hello");
			index.addAll(other);
			Assertions.assertEquals(1, index.view(HELLO).size(), index.toString());
			Assertions.assertEquals(1, other.view(HELLO).size(), other.toString());
		}

		/**
		 * Tests adding a simple index to a simple index.
		 */
		@Test
		@Order(8)
		public void testSimpleSimpleDifferent() {
			index.add(HELLO, "hello");
			other.add(HELLO, "world");
			index.addAll(other);
			Assertions.assertEquals(2, index.view(HELLO).size(), index.toString());
			Assertions.assertEquals(1, other.view(HELLO).size(), other.toString());
		}

		/**
		 * Tests adding a simple index to a simple index.
		 */
		@Test
		@Order(9)
		public void testSimpleSimpleSame() {
			index.add(HELLO, "hello");
			other.add(HELLO, "hello");
			index.addAll(other);
			Assertions.assertEquals(1, index.view(HELLO).size(), index.toString());
			Assertions.assertEquals(1, other.view(HELLO).size(), other.toString());
		}

		/**
		 * Tests adding a simple index to a simple index.
		 */
		@Test
		@Order(10)
		public void testComplex() {
			index.add(Path.of("hello.txt"), List.of("hello", "hola"));
			index.add(Path.of("letters.txt"), List.of("a", "b", "c", "c"));

			other.add(Path.of("letters.txt"), List.of("b", "e"));
			other.add(Path.of("planets.txt"), List.of("earth", "mars"));

			index.addAll(other);

			Assertions.assertEquals(3, index.size(), index.toString());
			Assertions.assertEquals(4, index.view(Path.of("letters.txt")).size(), index.toString());
			Assertions.assertEquals(2, other.view(Path.of("letters.txt")).size(), other.toString());
		}

		/** Creates a new instance of this class. */
			public AddAllTests() {}
	}

	/**
	 * Tests of an index with a single initial location and word.
	 */
	@Nested
	@Order(4)
	@TestMethodOrder(OrderAnnotation.class)
	public class ModificationTests {
		/** Placeholder for index object being tested. */
		private ForwardIndex<Path> index;

		/**
		 * Initializes an empty text file index.
		 */
		@BeforeEach
		public void initializeIndex() {
			index = createEmpty();
			index.add(HELLO, "hello");
		}

		/**
		 * Tests that attempts to modify paths in index fails.
		 */
		@Test
		@Order(1)
		public void testPathsClear() {
			Collection<Path> elements = index.view();
			Assertions.assertThrows(Exception.class, () -> elements.clear());
			Assertions.assertTrue(index.has(HELLO), index.toString());
		}

		/**
		 * Tests that attempts to modify paths in index fails.
		 */
		@Test
		@Order(2)
		public void testPathsAdd() {
			Collection<Path> elements = index.view();
			Assertions.assertThrows(Exception.class, () -> elements.add(EMPTY));
			Assertions.assertFalse(index.has(EMPTY), index.toString());
		}

		/**
		 * Tests that attempts to modify words in index fails.
		 */
		@Test
		@Order(3)
		public void testWordsClear() {
			Collection<String> elements = index.view(HELLO);
			Assertions.assertThrows(Exception.class, () -> elements.clear());
			Assertions.assertTrue(index.has(HELLO, "hello"), index.toString());
		}

		/**
		 * Tests that attempts to modify words in index fails.
		 */
		@Test
		@Order(4)
		public void testWordsAdd() {
			Collection<String> elements = index.view(HELLO);
			Assertions.assertThrows(Exception.class, () -> elements.add("world"));
			Assertions.assertFalse(index.has(HELLO, "world"), index.toString());
		}

		/** Creates a new instance of this class. */
			public ModificationTests() {}
	}

	/**
	 * Tests real text files.
	 */
	@Nested
	@Order(5)
	@TestMethodOrder(OrderAnnotation.class)
	public class RealIndexTests {
		/** Placeholder for index object being tested. */
		private ForwardIndex<Path> index;

		/**
		 * Initializes an empty text file index.
		 */
		@BeforeEach
		public void initializeIndex() {
			index = createEmpty();

			index.add(ANIMALS, getWords(ANIMALS));
			index.add(SENTENCES, getWords(SENTENCES));
		}

		/**
		 * Testing whether index was created properly.
		 */
		@Order(1)
		@Test
		public void testAnimalPaths() {
			Assertions.assertTrue(index.has(ANIMALS), index.toString());
		}

		/**
		 * Testing whether index was created properly.
		 */
		@Order(2)
		@Test
		public void testSentencesPaths() {
			Assertions.assertTrue(index.has(SENTENCES), index.toString());
		}

		/**
		 * Testing whether index was created properly.
		 */
		@Order(3)
		@Test
		public void testAnimals() {
			Assertions.assertEquals(8, index.size(ANIMALS), index.toString());
		}

		/**
		 * Testing whether index was created properly.
		 */
		@Order(4)
		@Test
		public void testSentences() {
			Assertions.assertEquals(41, index.size(SENTENCES), index.toString());
		}

		/**
		 * Testing whether index was created properly.
		 */
		@Order(5)
		@Test
		public void testPaths() {
			Set<Path> expected = Set.of(ANIMALS, SENTENCES);
			Assertions.assertTrue(index.view().containsAll(expected), index.toString());
		}

		/**
		 * Testing whether index was created properly.
		 */
		@Order(6)
		@Test
		public void testWords() {
			Set<String> expected = Set.of("okapi", "mongoose", "loris", "axolotl",
					"narwhal", "platypus", "echidna", "tarsier");
			Assertions.assertTrue(index.view(ANIMALS).containsAll(expected), index.toString());
		}

		/** Creates a new instance of this class. */
			public RealIndexTests() {}
	}

	/**
	 * Tests of an index with a single initial location and word.
	 */
	@Nested
	@Order(99)
	@TestMethodOrder(OrderAnnotation.class)
	public class ApproachTests extends HomeworkTest.ApproachTests {
		/**
		 * Testing whether implemented default methods in the interface only.
		 */
		@Order(1)
		@Test
		public void testAddList() {
			String debug = "\nDo not override default methods in " + HOMEWORK
					+ "! For this homework, only implement those in the interface.\n";

			Method[] methods = FileIndex.class.getMethods();

			long found = Arrays.stream(methods)
					.filter(m -> m.getDeclaringClass().equals(FileIndex.class))
					.filter(m -> m.getName().startsWith("add"))
					.filter(m -> m.toString().contains("List"))
					.count();

			Assertions.assertTrue(found == 0, debug);
		}

		/**
		 * Testing whether implemented default methods in the interface only.
		 */
		@Order(2)
		@Test
		public void testAddArray() {
			String debug = "\nDo not override default methods in " + HOMEWORK
					+ "! For this homework, only implement those in the interface.\n";

			Method[] methods = FileIndex.class.getMethods();

			long found = Arrays.stream(methods)
					.filter(m -> m.getDeclaringClass().equals(FileIndex.class))
					.filter(m -> m.getName().startsWith("add"))
					.filter(m -> m.toString().contains("String[]"))
					.count();

			Assertions.assertTrue(found == 0, debug);
		}

		/**
		 * Testing whether implemented default methods in the interface only.
		 */
		@Order(3)
		@Test
		public void testAddForwardIndex() {
			String debug = "\nDo not override default methods in " + HOMEWORK
					+ "! For this homework, only implement those in the interface.\n";

			Method[] methods = FileIndex.class.getMethods();

			long found = Arrays.stream(methods)
					.filter(m -> m.getDeclaringClass().equals(FileIndex.class))
					.filter(m -> m.getName().startsWith("add"))
					.filter(m -> m.toString().contains("ForwardIndex"))
					.count();

			Assertions.assertTrue(found == 0, debug);
		}

		/** Creates a new instance of this class. */
			public ApproachTests() {}
	}

	/**
	 * Helper method to quickly read in a small text file and return words.
	 *
	 * @param path the path
	 * @return the words
	 */
	private static String[] getWords(Path path) {
		try {
			return Files.readString(path, StandardCharsets.UTF_8).toLowerCase().split("\\W+");
		}
		catch (IOException e) {
			Assertions.fail(e);
			return null;
		}
	}

	/** Creates a new instance of this class. */
		public FileIndexTest() {}
}
