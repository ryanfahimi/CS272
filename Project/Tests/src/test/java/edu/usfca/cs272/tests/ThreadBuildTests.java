package edu.usfca.cs272.tests;

import static edu.usfca.cs272.tests.utils.ProjectFlag.COUNTS;
import static edu.usfca.cs272.tests.utils.ProjectFlag.INDEX;
import static edu.usfca.cs272.tests.utils.ProjectFlag.TEXT;
import static edu.usfca.cs272.tests.utils.ProjectFlag.THREADS;
import static edu.usfca.cs272.tests.utils.ProjectPath.ACTUAL;
import static edu.usfca.cs272.tests.utils.ProjectPath.EXPECTED;
import static edu.usfca.cs272.tests.utils.ProjectPath.HELLO;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.usfca.cs272.tests.utils.ProjectBenchmarks;
import edu.usfca.cs272.tests.utils.ProjectPath;
import edu.usfca.cs272.tests.utils.ProjectTests;

/**
 * A test suite for project v3.0.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ExtendWith(ProjectTests.TestCounter.class)
public class ThreadBuildTests extends ProjectBenchmarks {
	/** Creates a new instance of this class. */
	public ThreadBuildTests() {}

	/**
	 * Tests that threads are being used for this project. These tests are slow and
	 * should only be run when needed. The tests are also imperfect and may not
	 * reliably pass unless on the GitHub Actions environment.
	 */
	@Nested
	@Order(1)
	@Tag("test-v3.1")
	@Tag("test-v3.2")
	@Tag("test-v3.3")
	@Tag("test-v3.4")
	@TestMethodOrder(OrderAnnotation.class)
	public class ApproachTests {
		/** Creates a new instance of this class. */
		public ApproachTests() {}

		/**
		 * See the JUnit output for test details.
		 */
		@Test
		@Order(1)
		public void testIndex() {
			String[] args = {
					TEXT.flag, ProjectPath.TEXT.text,
					THREADS.flag, ProjectBenchmarks.Threads.TWO.text
			};

			Runnable test = () -> {
				assertNoExceptions(args, LONG_TIMEOUT);
				System.out.println("Random: " + Math.random());
			};

			ProjectBenchmarks.assertMultithreaded(test);
		}
	}

	/**
	 * Tests the output of this project.
	 */
	@Nested
	@Order(2)
	@TestMethodOrder(OrderAnnotation.class)
	@Tag("test-v3.1")
	public class InitialTests {
		/** Creates a new instance of this class. */
		public InitialTests() {}

		/**
		 * See the JUnit output for test details.
		 *
		 * @param threads the threads
		 */
		@ParameterizedTest
		@Order(1)
		@EnumSource()
		public void testHello(ProjectBenchmarks.Threads threads) {
			testIndex("simple", ProjectPath.HELLO, threads);
		}

		/**
		 * See the JUnit output for test details.
		 *
		 * @param threads the threads
		 */
		@ParameterizedTest
		@Order(2)
		@EnumSource()
		public void testSimpleIndex(ProjectBenchmarks.Threads threads) {
			testIndex("simple", ProjectPath.SIMPLE, threads);
		}

		/**
		 * See the JUnit output for test details.
		 *
		 * @param threads the threads
		 */
		@ParameterizedTest
		@Order(3)
		@EnumSource()
		public void testSimpleCounts(ProjectBenchmarks.Threads threads) {
			testCount(ProjectPath.SIMPLE, threads);
		}

		/**
		 * See the JUnit output for test details.
		 *
		 * @param threads the threads
		 */
		@ParameterizedTest
		@Order(4)
		@EnumSource()
		public void testStems(ProjectBenchmarks.Threads threads) {
			testIndex("stems", ProjectPath.STEMS, threads);
		}

		/**
		 * See the JUnit output for test details.
		 *
		 * @param threads the threads
		 */
		@ParameterizedTest
		@Order(5)
		@EnumSource()
		public void testRFCs(ProjectBenchmarks.Threads threads) {
			testIndex("rfcs", ProjectPath.RFCS, threads);
		}

		/**
		 * See the JUnit output for test details.
		 *
		 * @param threads the threads
		 */
		@ParameterizedTest
		@Order(6)
		@EnumSource()
		public void testGutenGreat(ProjectBenchmarks.Threads threads) {
			testIndex("guten", ProjectPath.GUTEN_GREAT, threads);
		}

		/**
		 * Free up memory after running --- useful for following tests.
		 */
		@AfterAll
		public static void freeMemory() {
			ProjectTests.freeMemory();
		}
	}

	/**
	 * Tests the output of this project.
	 */
	@Nested
	@Order(3)
	@TestMethodOrder(OrderAnnotation.class)
	@Tag("test-v3.1")
	@Tag("test-v3.2")
	@Tag("test-v3.3")
	@Tag("test-v3.4")
	public class ComplexTests {
		/** Creates a new instance of this class. */
		public ComplexTests() {}
		/**
		 * See the JUnit output for test details.
		 *
		 * @param threads the threads
		 */
		@ParameterizedTest
		@Order(1)
		@EnumSource()
		public void testTextIndex(ProjectBenchmarks.Threads threads) {
			testIndex(".", ProjectPath.TEXT, threads);
		}

		/**
		 * See the JUnit output for test details.
		 *
		 * @param threads the threads
		 */
		@ParameterizedTest
		@Order(2)
		@EnumSource()
		public void testTextCounts(ProjectBenchmarks.Threads threads) {
			testCount(ProjectPath.TEXT, threads);
		}

		/**
		 * Free up memory after running --- useful for following tests.
		 */
		@AfterAll
		public static void freeMemory() {
			ProjectTests.freeMemory();
		}
	}

	/**
	 * Tests the index exception handling of this project.
	 */
	@Nested
	@Order(4)
	@Tag("test-v3.1")
	@Tag("test-v3.2")
	@Tag("test-v3.3")
	@Tag("test-v3.4")
	@Tag("past-v4")
	@Tag("past-v5")
	@TestMethodOrder(OrderAnnotation.class)
	public class ExceptionTests {
		/** Creates a new instance of this class. */
		public ExceptionTests() {}

		/**
		 * Tests no exceptions are thrown with the provided arguments.
		 *
		 * @throws Exception if exception occurs
		 */
		@Test
		@Order(1)
		public void testNegativeThreads() throws Exception {
			String[] args = { TEXT.flag, HELLO.text, THREADS.flag, "-4", INDEX.flag };
			Files.deleteIfExists(INDEX.path);
			assertNoExceptions(args, SHORT_TIMEOUT);
			Assertions.assertTrue(Files.exists(INDEX.path), INDEX.value);
		}

		/**
		 * Tests no exceptions are thrown with the provided arguments.
		 *
		 * @throws Exception if exception occurs
		 */
		@Test
		@Order(2)
		public void testZeroThreads() throws Exception {
			String[] args = { TEXT.flag, HELLO.text, THREADS.flag, "0", INDEX.flag };
			Files.deleteIfExists(INDEX.path);
			assertNoExceptions(args, SHORT_TIMEOUT);
			Assertions.assertTrue(Files.exists(INDEX.path), INDEX.value);
		}

		/**
		 * Tests no exceptions are thrown with the provided arguments.
		 *
		 * @throws Exception if exception occurs
		 */
		@Test
		@Order(3)
		public void testFractionThreads() throws Exception {
			String[] args = { TEXT.flag, HELLO.text, THREADS.flag, "3.14", INDEX.flag };
			Files.deleteIfExists(INDEX.path);
			assertNoExceptions(args, SHORT_TIMEOUT);
			Assertions.assertTrue(Files.exists(INDEX.path), INDEX.value);
		}

		/**
		 * Tests no exceptions are thrown with the provided arguments.
		 *
		 * @throws Exception if exception occurs
		 */
		@Test
		@Order(4)
		public void testWordThreads() throws Exception {
			String[] args = { TEXT.flag, HELLO.text, THREADS.flag, "fox", INDEX.flag };
			Files.deleteIfExists(INDEX.path);
			assertNoExceptions(args, SHORT_TIMEOUT);
			Assertions.assertTrue(Files.exists(INDEX.path), INDEX.value);
		}

		/**
		 * Tests no exceptions are thrown with the provided arguments.
		 *
		 * @throws Exception if exception occurs
		 */
		@Test
		@Order(5)
		public void testDefaultThreads() throws Exception {
			String[] args = { TEXT.flag, HELLO.text, THREADS.flag, INDEX.flag };
			Files.deleteIfExists(INDEX.path);
			assertNoExceptions(args, SHORT_TIMEOUT);
			Assertions.assertTrue(Files.exists(INDEX.path), INDEX.value);
		}

		/**
		 * Tests no exceptions are thrown with the provided arguments.
		 *
		 * @throws Exception if exception occurs
		 */
		@Test
		@Order(6)
		public void testNoOutput() throws Exception {
			String[] args = { TEXT.flag, HELLO.text, THREADS.flag };
			assertNoExceptions(args, SHORT_TIMEOUT);
		}

		/**
		 * Tests no exceptions are thrown with the provided arguments.
		 *
		 * @throws Exception if exception occurs
		 */
		@Test
		@Order(7)
		public void testOnlyThreads() throws Exception {
			String[] args = { THREADS.flag };
			assertNoExceptions(args, SHORT_TIMEOUT);
		}

		/**
		 * Tests no exceptions are thrown with the provided arguments.
		 *
		 * @throws Exception if exception occurs
		 */
		@Test
		@Order(8)
		public void testOnlyThreadsAndOutput() throws Exception {
			String[] args = { THREADS.flag, INDEX.flag };
			Files.deleteIfExists(INDEX.path);
			assertNoExceptions(args, SHORT_TIMEOUT);
			Assertions.assertTrue(Files.exists(INDEX.path), INDEX.value);
		}
	}

	/**
	 * Tests specific to multithreading.
	 *
	 * THESE ARE SLOW TESTS. AVOID RUNNING UNLESS REALLY NEEDED.
	 */
	@Nested
	@Order(5)
	@TestMethodOrder(OrderAnnotation.class)
	public class ThreadTests {
		/** Creates a new instance of this class. */
		public ThreadTests() {}

		/**
		 * Sets up the tests before running. Only runs the tests if other tests had
		 * no failures.
		 *
		 * @param info test information
		 */
		@BeforeAll
		public static void checkStatus(TestInfo info) {
			// disable if there were earlier failures
			ProjectTests.TestCounter.assertNoFailures(info);
		}

		/**
		 * See the JUnit output for test details.
		 */
		@Test
		@Order(1)
		@Tag("time-v3.1")
		@Tag("time-v3.2")
		public void okayIndexOneMany() {
			timeIndexOneMany(MIN_SPEEDUP);
		}

		/**
		 * See the JUnit output for test details.
		 */
		@Test
		@Order(2)
		@Tag("time-v3.1")
		@Tag("time-v3.2")
		public void okayIndexSingleMulti() {
			timeIndexSingleMulti(MIN_SPEEDUP);
		}
	}

	/**
	 * All-in-one tests of this project functionality.
	 */
	@Nested
	@Order(6)
	@TestMethodOrder(OrderAnnotation.class)
	@Tag("past-v4")
	@Tag("past-v5")
	public class ComboTests {
		/** Creates a new instance of this class. */
		public ComboTests() {}

		/**
		 * See the JUnit output for test details.
		 */
		@Test
		@Order(1)
		public void testCountsIndex() {
			ProjectPath input = ProjectPath.TEXT;
			ProjectBenchmarks.Threads threads = ProjectBenchmarks.Threads.TWO;

			String indexName = String.format("index-%s", input.id);
			String countsName = String.format("counts-%s", input.id);

			Path indexActual = ACTUAL.resolve(indexName + "-" + threads.text + ".json");
			Path countsActual = ACTUAL.resolve(countsName + "-" + threads.text + ".json");

			String[] args = {
					TEXT.flag, input.text,
					INDEX.flag, indexActual.toString(),
					COUNTS.flag, countsActual.toString(),
					THREADS.flag, threads.text
			};

			Map<Path, Path> files = Map.of(
					countsActual, EXPECTED.resolve("counts").resolve(countsName + ".json"),
					indexActual, EXPECTED.resolve("index").resolve(indexName + ".json")
			);

			Executable test = () -> ProjectTests.checkOutput(args, files);
			Assertions.assertTimeoutPreemptively(LONG_TIMEOUT, test);
		}
	}

	/**
	 * Generates the arguments to use for the output test cases. Designed to be used
	 * inside a JUnit test.
	 *
	 * @param subdir the subdir
	 * @param input the input
	 * @param id the id
	 * @param threads the threads
	 */
	public static void testIndex(String subdir, Path input, String id, ProjectBenchmarks.Threads threads) {
		String single = String.format("index-%s.json", id);
		String threaded = String.format("index-%s-%s.json", id, threads.text);
		Path actual = ACTUAL.resolve(threaded).normalize();
		Path expected = EXPECTED.resolve("index").resolve(subdir).resolve(single).normalize();
		String[] args = { TEXT.flag, input.toString(), INDEX.flag, actual.toString(), THREADS.flag, threads.text };
		Executable test = () -> checkOutput(args, actual, expected);
		Assertions.assertTimeoutPreemptively(LONG_TIMEOUT, test);
	}

	/**
	 * Calls {@link ThreadBuildTests#testIndex(String, Path, String, Threads)} using the enum.
	 *
	 * @param subdir the subdir
	 * @param path the path
	 * @param threads the threads
	 * @see ThreadBuildTests#testIndex(String, Path, String, Threads)
	 */
	public static void testIndex(String subdir, ProjectPath path, ProjectBenchmarks.Threads threads) {
		testIndex(subdir, path.path, path.id, threads);
	}

	/**
	 * Generates the arguments to use for the output test cases. Designed to be used
	 * inside a JUnit test.
	 *
	 * @param input the input
	 * @param id the id
	 * @param threads the threads
	 */
	public static void testCount(Path input, String id, ProjectBenchmarks.Threads threads) {
		String single = String.format("counts-%s.json", id);
		String threaded = String.format("counts-%s-%s.json", id, threads.text);
		Path actual = ACTUAL.resolve(threaded).normalize();
		Path expected = EXPECTED.resolve("counts").resolve(single).normalize();
		String[] args = { TEXT.flag, input.toString(), COUNTS.flag, actual.toString(), THREADS.flag, threads.text };
		Executable debug = () -> checkOutput(args, actual, expected);
		Assertions.assertTimeoutPreemptively(LONG_TIMEOUT, debug);
	}

	/**
	 * Calls {@link ThreadBuildTests#testCount(Path, String, Threads)} using the enum.
	 *
	 * @param path the path
	 * @param threads the threads
	 * @see ThreadBuildTests#testCount(Path, String, Threads)
	 */
	public static void testCount(ProjectPath path, ProjectBenchmarks.Threads threads) {
		testCount(path.path, path.id, threads);
	}

	/**
	 * Time building with 1 versus many workers.
	 *
	 * @param target the target speedup to pass these tests
	 */
	public static void timeIndexOneMany(double target) {
		timeIndexOneMany(target, ProjectBenchmarks.Threads.FOUR.num);
	}

	/**
	 * Time building with 1 versus many workers.
	 *
	 * @param target the target speedup to pass these tests
	 * @param threads the maximum number of worker threads to use
	 */
	public static void timeIndexOneMany(double target, int threads) {
		String[] args1 = { TEXT.flag, ProjectPath.TEXT.text, THREADS.flag, String.valueOf(1) };
		String[] args2 = { TEXT.flag, ProjectPath.TEXT.text, THREADS.flag, String.valueOf(threads) };

		// make sure code runs without exceptions before testing
		assertNoExceptions(args1, SHORT_TIMEOUT);
		assertNoExceptions(args2, SHORT_TIMEOUT);

		// then test the timing
		assertTimeoutPreemptively(LONG_TIMEOUT, () -> {
			double result = compare("Build", "1 Worker", args1, threads + " Workers", args2);
			Supplier<String> debug = () -> String.format(format, threads, result, target, "1 worker");
			assertTrue(result >= target, debug);
		});
	}

	/**
	 * Time building with single versus threading.
	 *
	 * @param target the target speedup to pass these tests
	 */
	public static void timeIndexSingleMulti(double target) {
		timeIndexSingleMulti(target, ProjectBenchmarks.Threads.FOUR.num);
	}

	/**
	 * Time building with single versus threading.
	 *
	 * @param target the target speedup to pass these tests
	 * @param threads the maximum number of worker threads to use
	 */
	public static void timeIndexSingleMulti(double target, int threads) {
		String[] args1 = { TEXT.flag, ProjectPath.TEXT.text };
		String[] args2 = { TEXT.flag, ProjectPath.TEXT.text, THREADS.flag, String.valueOf(threads) };

		// make sure code runs without exceptions before testing
		assertNoExceptions(args1, SHORT_TIMEOUT);
		assertNoExceptions(args2, SHORT_TIMEOUT);

		// then test the timing
		assertTimeoutPreemptively(LONG_TIMEOUT, () -> {
			double result = compare("Build", "Single", args1, threads + " Workers", args2);
			Supplier<String> debug = () -> String.format(format, threads, result, target, "single-threading");
			assertTrue(result >= target, debug);
		});
	}
}
