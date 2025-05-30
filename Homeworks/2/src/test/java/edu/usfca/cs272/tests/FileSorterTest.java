package edu.usfca.cs272.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.function.Supplier;

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
import edu.usfca.cs272.utils.FileSorter;
import edu.usfca.cs272.utils.FileSorter.FileMetadata;

/**
 * Tests for the {@link FileSorter} class.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@Tag("FileSorter")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodName.class)
public class FileSorterTest extends HomeworkTest {
	/** Location of test files. */
	public static final Path SORTER = RESOURCES_DIR.resolve("sorter");

	/**
	 * Tests a specific comparator.
	 *
	 * @see FileMetadata
	 */
	@Nested
	@Order(1)
	@TestMethodOrder(OrderAnnotation.class)
	public class PathTests extends ComparatorTests {
		@Override
		@BeforeEach
		@SuppressWarnings("unchecked")
		public void setup() {
			// can't use Comparator.naturalOrder() here because not guaranteed
			// interface implemented correctly
			Comparator<FileSorter.FileMetadata> temp = new Comparator<FileSorter.FileMetadata>() {
				@Override
				public int compare(FileSorter.FileMetadata first, FileSorter.FileMetadata second) {
					try {
						Comparable<FileSorter.FileMetadata> casted1 = (Comparable<FileMetadata>) first;
						return casted1.compareTo(second);
					}
					catch (ClassCastException e) {
						// fail test because unable to cast
						fail("\nFileMetadata does not implement the necessary interface.");
					}

					return 0;
				}
			};

			test = temp;
			small = Path.of("a", "z.txt");
			large = Path.of("z", "a.txt");
			equal = Path.of(".", "z", "a.txt");
		}

		/** Creates a new instance of this class. */
		public PathTests() {}
	}

	/**
	 * Tests a specific comparator.
	 *
	 * @see FileSorter#DATE_COMPARATOR
	 */
	@Nested
	@Order(2)
	@TestMethodOrder(OrderAnnotation.class)
	public class DateTests extends ComparatorTests {
		@Override
		@BeforeEach
		public void setup() throws IOException {
			test = FileSorter.DATE_COMPARATOR;
			small = SORTER.resolve("a_large_size.txt");
			large = SORTER.resolve("z_small_size.txt");
			equal = SORTER.resolve("a_equal_size.txt");

			Instant now = Instant.now();
			Instant past = now.minus(Duration.ofHours(1));

			Files.setLastModifiedTime(small, FileTime.from(past));
			Files.setLastModifiedTime(large, FileTime.from(now));
			Files.setLastModifiedTime(equal, FileTime.from(now));
		}

		/** Creates a new instance of this class. */
		public DateTests() {}
	}

	/**
	 * Tests a specific comparator.
	 *
	 * @see FileSorter#NAME_COMPARATOR
	 */
	@Nested
	@Order(3)
	@TestMethodOrder(OrderAnnotation.class)
	public class NameTests extends ComparatorTests {
		@Override
		@BeforeEach
		public void setup() {
			test = FileSorter.NAME_COMPARATOR;
			small = Path.of("z", "a.txt");
			large = Path.of("a", "z.txt");
			equal = Path.of("Z.TXT");
		}

		/** Creates a new instance of this class. */
		public NameTests() {}
	}

	/**
	 * Tests a specific comparator.
	 *
	 * @see FileSorter#SIZE_COMPARATOR
	 */
	@Nested
	@Order(4)
	@TestMethodOrder(OrderAnnotation.class)
	public class SizeTests extends ComparatorTests {
		@Override
		@BeforeEach
		public void setup() {
			test = FileSorter.SIZE_COMPARATOR; // descending sort order
			small = SORTER.resolve("a_large_size.txt");
			large = SORTER.resolve("z_small_size.txt");
			equal = SORTER.resolve("a_equal_size.txt");
		}

		/** Creates a new instance of this class. */
		public SizeTests() {}
	}

	/**
	 * Tests a specific comparator.
	 *
	 * @see FileSorter#DATE_COMPARATOR
	 */
	@Nested
	@Order(5)
	@TestMethodOrder(OrderAnnotation.class)
	public class NestedTests extends ComparatorTests {
		@Override
		@BeforeEach
		public void setup() {
			test = FileSorter.getNested();

			// handles case where sizes are different
			small = SORTER.resolve("a_large_size.txt");
			large = SORTER.resolve("z_small_size.txt");

			// handles case where size, name, and path are same
			equal = SORTER.resolve(".").resolve("z_small_size.txt");
		}

		/**
		 * Tests that this comparator was implemented properly.
		 *
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(4)
		public void testSameSizeUniqueName() throws IOException {
			// paths don't exist, so sizes are all -1
			small = Path.of("z", "a.txt");
			large = Path.of("a", "z.txt");

			FileSorter.FileMetadata one = new FileSorter.FileMetadata(large);
			FileSorter.FileMetadata two = new FileSorter.FileMetadata(small);
			String debug = String.format("%s > %s", one, two);
			assertTrue(test.compare(one, two) > 0, debug);
		}

		/**
		 * Tests that this comparator was implemented properly.
		 *
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(5)
		public void testSameSizeSameName() throws IOException {
			// paths don't exist, so sizes are all -1
			small = Path.of("a", "a.txt");
			large = Path.of("z", "a.txt");

			FileSorter.FileMetadata one = new FileSorter.FileMetadata(large);
			FileSorter.FileMetadata two = new FileSorter.FileMetadata(small);
			String debug = String.format("%s > %s", one, two);
			assertTrue(test.compare(one, two) > 0, debug);
		}

		/** Creates a new instance of this class. */
		public NestedTests() {}
	}

	/**
	 * Tests approaches used.
	 *
	 * @see FileSorter
	 */
	@Nested
	@Order(6)
	@TestMethodOrder(OrderAnnotation.class)
	public class ApproachTests extends HomeworkTest.ApproachTests {
		/**
		 * Tests approach.
		 */
		@Test
		@Order(1)
		public void testInterface() {
			Type[] types = FileSorter.FileMetadata.class.getGenericInterfaces();

			if (types.length == 1) {
				String name = types[0].getTypeName();

				String format1 = "%nThe Comparable interface not properly implemented. %nFound: %s%n";
				String format2 = "%nThe interface generic type not properly specified. %nFound: %s%n";
				Supplier<String> error1 = () -> String.format(format1, name);
				Supplier<String> error2 = () -> String.format(format2, name);

				assertAll(
						() -> assertTrue(name.contains("Comparable"), error1),
						() -> assertTrue(name.contains("FileMetadata"), error2));
			}
			else {
				fail("\nFileMetadata does not implement the necessary interface.");
			}
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(2)
		public void testInterfaceType() {
			Class<?> test = FileSorter.FileMetadata.class;
			boolean expectAnonymous = false;
			boolean expectMember = true;
			boolean expectStatic = true;
			boolean expectLambda = false;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(3)
		public void testDateComparatorType() {
			Class<?> test = FileSorter.DATE_COMPARATOR.getClass();
			boolean expectAnonymous = false;
			boolean expectMember = true;
			boolean expectStatic = true;
			boolean expectLambda = false;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(4)
		public void testNameComparatorType() {
			Class<?> test = FileSorter.NAME_COMPARATOR.getClass();
			boolean expectAnonymous = false;
			boolean expectMember = true;
			boolean expectStatic = false;
			boolean expectLambda = false;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(5)
		public void testSizeComparatorType() {
			Class<?> test = FileSorter.SIZE_COMPARATOR.getClass();
			boolean expectAnonymous = false;
			boolean expectMember = false;
			boolean expectStatic = false;
			boolean expectLambda = true;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}

		/**
		 * Tests approach.
		 */
		@Test
		@Order(6)
		public void testNestedComparatorType() {
			Class<?> test = FileSorter.getNested().getClass();
			boolean expectAnonymous = true;
			boolean expectMember = false;
			boolean expectStatic = false;
			boolean expectLambda = false;
			testClassDefinition(test, expectAnonymous, expectMember, expectStatic, expectLambda);
		}

		/** Creates a new instance of this class. */
		public ApproachTests() {}
	}

	/**
	 * Tests if the class is defined as expected.
	 *
	 * @param test the class to test
	 * @param expectAnonymous whether the class should be anonymous
	 * @param expectMember whether the class should be an inner/member class
	 * @param expectStatic whether the class should be a static class
	 * @param expectLambda whether the class should be a lambda expression
	 */
	public static void testClassDefinition(Class<?> test, boolean expectAnonymous, boolean expectMember,
			boolean expectStatic, boolean expectLambda) {
		boolean actualAnonymous = test.isAnonymousClass();
		boolean actualMember = test.isMemberClass();
		boolean actualStatic = Modifier.isStatic(test.getModifiers());
		boolean actualLambda = test.isSynthetic() && test.getTypeName().contains("$$Lambda");

		assertAll(
				() -> assertEquals(expectAnonymous, actualAnonymous, "is anonymous?"),
				() -> assertEquals(expectMember, actualMember, "is inner class?"),
				() -> assertEquals(expectStatic, actualStatic, "is static class?"),
				() -> assertEquals(expectLambda, actualLambda, "is lambda expression?"));
	}

	/**
	 * Tests a specific comparator.
	 */
	@TestMethodOrder(OrderAnnotation.class)
	public abstract class ComparatorTests {
		/** The comparator being tested. */
		public Comparator<FileSorter.FileMetadata> test;

		/** A Path expected to be smaller than the small Path object. */
		public Path small;

		/** A Path expected to be larger than the small Path object. */
		public Path large;

		/** A Path expected to be equal to the large Path object. */
		public Path equal;

		/**
		 * Sets the comparator being tested.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@BeforeEach
		public abstract void setup() throws IOException;

		/**
		 * Tests that this comparator was implemented properly.
		 *
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(1)
		public void testPositive() throws IOException {
			FileSorter.FileMetadata one = new FileSorter.FileMetadata(large);
			FileSorter.FileMetadata two = new FileSorter.FileMetadata(small);
			String debug = String.format("%s > %s", one, two);
			assertTrue(test.compare(one, two) > 0, debug);
		}

		/**
		 * Tests that this comparator was implemented properly.
		 *
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(2)
		public void testNegative() throws IOException {
			FileSorter.FileMetadata one = new FileSorter.FileMetadata(small);
			FileSorter.FileMetadata two = new FileSorter.FileMetadata(large);
			String debug = String.format("%s < %s", one, two);
			assertTrue(test.compare(one, two) < 0, debug);
		}

		/**
		 * Tests that this comparator was implemented properly.
		 *
		 * @throws IOException if I/O error occurs
		 */
		@Test
		@Order(3)
		public void testEqual() throws IOException {
			FileSorter.FileMetadata one = new FileSorter.FileMetadata(large);
			FileSorter.FileMetadata two = new FileSorter.FileMetadata(equal);
			String debug = String.format("%s == %s", one, two);
			assertTrue(test.compare(one, two) == 0, debug);
		}

		/** Creates a new instance of this class. */
		public ComparatorTests() {}
	}

	/** Creates a new instance of this class. */
	public FileSorterTest() {}
}
