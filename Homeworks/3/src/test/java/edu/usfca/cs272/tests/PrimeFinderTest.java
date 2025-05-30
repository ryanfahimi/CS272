package edu.usfca.cs272.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.Executable;

import edu.usfca.cs272.PrimeFinder;
import edu.usfca.cs272.tests.utils.HomeworkTest;
import edu.usfca.cs272.utils.WorkQueue;

/**
 * Attempts to test if {@link PrimeFinder#findPrimes(int, int)} and
 * {@link WorkQueue#finish()} implementations are correct. Tests are not perfect
 * and may not catch all implementation issues.
 *
 * @see PrimeFinder#findPrimes(int, int)
 * @see WorkQueue#finish()
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class PrimeFinderTest extends HomeworkTest {
	/**
	 * Tests the work queue code.
	 */
	@Nested
	@Order(1)
	@Tag("PrimeFinder")
	@TestMethodOrder(OrderAnnotation.class)
	public class WorkQueueTests {
		/**
		 * Verifies an empty work queue starts and terminates without exceptions.
		 * Requires the finish() method to be implemented (but it might not be correct).
		 */
		@Test
		@Order(1)
		public void testEmptyQueue() {
			Executable test = () -> {
				System.out.println("Before: " + activeThreads());

				WorkQueue queue = new WorkQueue();
				System.out.println("Active: " + activeThreads());

				queue.join();
				System.out.println(" After: " + activeThreads());
				System.out.println();
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Verifies finishing and joining work when there were tasks but those are
		 * already completed by the time those methods are called. If this fails there
		 * could be an issue with how pending work is tracked.
		 */
		@Test
		@Order(2)
		public void testFinishedWork() {
			int tasks = 10;

			CountDownLatch created = new CountDownLatch(1);
			CountDownLatch finished = new CountDownLatch(tasks);

			Executable test = () -> {
				WorkQueue queue = new WorkQueue(tasks / 2);

				for (int i = 0; i < tasks; i++) {
					queue.execute(() -> {
						try {
							created.await();
							finished.countDown();
						}
						catch (InterruptedException ex) {
							Assertions.fail("Task interrupted; queue did not complete in time.", ex);
						}
					});
				}

				created.countDown(); // indicate all tasks have been created
				finished.await(); // wait for all of the work to finish
				queue.join(); // shutdown work queue (should be empty again)
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Verifies finishing and joining work when there are unfinished tasks by the
		 * time those methods are called. If this fails there could be an issue with how
		 * pending work is tracked.
		 */
		@Test
		@Order(3)
		public void testPendingWork() {
			int tasks = 10;

			CountDownLatch created = new CountDownLatch(1);
			CountDownLatch finished = new CountDownLatch(tasks);

			Executable test = () -> {
				WorkQueue queue = new WorkQueue(tasks / 2);

				for (int i = 0; i < tasks; i++) {
					queue.execute(() -> {
						try {
							created.await();
							Thread.sleep(100);
							finished.countDown();
						}
						catch (InterruptedException ex) {
							Assertions.fail("Task interrupted; queue did not complete in time.", ex);
						}
					});
				}

				created.countDown(); // indicate all tasks have been created
				queue.join(); // shutdown queue while there are active tasks

				long remaining = finished.getCount(); // get number of unfinished tasks
				finished.await(); // make sure all work did finish by this point

				Assertions.assertEquals(remaining, 0, "There should be no unfinished work after exiting the join() method.");
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Tests that work queue can be reused by calling finish multiple times before
		 * the queue is shutdown. If this fails there could be an issue with how pending
		 * work is tracked.
		 */
		@Test
		@Order(4)
		public void testMultiFinish() {
			int workers = 2;
			int tasks = 3;
			int repeats = 2;

			Executable test = () -> {
				WorkQueue queue = new WorkQueue(workers);

				for (int i = 0; i < repeats; i++) {
					CountDownLatch created = new CountDownLatch(1);
					CountDownLatch finished = new CountDownLatch(tasks);

					for (int j = 0; j < tasks; j++) {
						queue.execute(() -> {
							try {
								created.await();
								finished.countDown();
							}
							catch (InterruptedException ex) {
								Assertions.fail("Task interrupted; queue did not complete in time.", ex);
							}
						});
					}

					created.countDown();
					queue.finish();
					finished.await();
				}

				queue.join();
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Tests that work queue is not finishing too soon. If this fails there could be
		 * an issue with how pending work is decremented.
		 */
		@Test
		@Order(5)
		public void testLastWorker() {
			int tasks = 5;

			AtomicInteger counter = new AtomicInteger(0);
			CountDownLatch started = new CountDownLatch(1);

			Executable test = () -> {
				WorkQueue queue = new WorkQueue(3);

				for (int i = 0; i < tasks; i++) {
					long pause = (i + 1) * 100;

					queue.execute(() -> {
						try {
							started.await();
							Thread.sleep(pause);
							counter.incrementAndGet();
						}
						catch (InterruptedException ex) {
							Assertions.fail("Task interrupted; queue did not complete in time.", ex);
						}
					});
				}

				started.countDown();
				queue.finish();

				int value = counter.get();
				Assertions.assertEquals(tasks, value, "The work queue finished before all of the pending work was completed.");

				queue.join();
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Verifies there are no active worker threads after join is called. If this
		 * test fails, then worker threads are not terminating properly after shutdown
		 * has been called.
		 */
		@Test
		@Order(6)
		public void testThreads() {
			Executable test = () -> {
				Set<String> start = activeThreads();

				WorkQueue queue = new WorkQueue(5);

				Thread.sleep(100); // pause to make sure threads start up
				queue.finish();
				queue.shutdown();
				queue.join();
				Thread.sleep(100); // pause to make sure threads shut down

				Set<String> end = activeThreads();
				Assertions.assertEquals(start, end, PRIMES_DEBUG);
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Verifies the work queue can still finish and shutdown if a worker thread
		 * throws a runtime exception. If this test fails, then check where your code
		 * decrements pending work.
		 */
		@Test
		@Order(7)
		public void testWorkerExceptions() {
			Executable test = () -> {
				WorkQueue queue = new WorkQueue(5);

				queue.execute(() -> {
					try {
						Thread.sleep(200);
					}
					catch (InterruptedException e) {
						Assertions.fail("Task interrupted; queue did not complete in time.", e);
					}
				});

				queue.execute(() -> {
					try {
						Thread.sleep(100);
					}
					catch (InterruptedException e) {
						Assertions.fail("Task interrupted; queue did not complete in time.", e);
					}

					// throw runtime exception to see what happens to work queue
					// will cause an EXPECTED message to be output to the console!
					throw new RuntimeException("This is an expected exception; output SHOULD be produced.\n");
				});

				queue.finish();
				queue.shutdown();
				queue.join();
			};

			Assertions.assertTimeoutPreemptively(Duration.ofMillis(500), test, TIMEOUT_DEBUG);
		}
	}

	/**
	 * Tests the results are consistently correct for different numbers of threads.
	 */
	@Nested
	@Order(2)
	@TestMethodOrder(OrderAnnotation.class)
	public class ThreadTests {
		/**
		 * Verify the multithreaded implementation finds the correct primes with one
		 * worker thread.
		 *
		 * @see PrimeFinder#findPrimes(int, int)
		 */
		@RepeatedTest(3)
		@Order(1)
		public void testFindPrimes1Thread() {
			Executable test = () -> {
				TreeSet<Integer> actual = PrimeFinder.findPrimes(1000, 1);
				Assertions.assertEquals(KNOWN_PRIMES.size(), actual.size(), PRIMES_DEBUG);
				Assertions.assertEquals(KNOWN_PRIMES, actual, PRIMES_DEBUG);
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Verify the multithreaded implementation finds the correct primes with two
		 * worker threads.
		 *
		 * @see PrimeFinder#findPrimes(int, int)
		 */
		@RepeatedTest(3)
		@Order(2)
		public void testFindPrimes2Thread() {
			Executable test = () -> {
				TreeSet<Integer> actual = PrimeFinder.findPrimes(1000, 2);
				Assertions.assertEquals(KNOWN_PRIMES.size(), actual.size(), PRIMES_DEBUG);
				Assertions.assertEquals(KNOWN_PRIMES, actual, PRIMES_DEBUG);
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Verify the multithreaded implementation finds the correct primes with five
		 * worker threads.
		 *
		 * @see PrimeFinder#findPrimes(int, int)
		 */
		@RepeatedTest(3)
		@Order(3)
		public void testFindPrimes5Thread() {
			Executable test = () -> {
				TreeSet<Integer> actual = PrimeFinder.findPrimes(1000, 5);
				Assertions.assertEquals(KNOWN_PRIMES.size(), actual.size(), PRIMES_DEBUG);
				Assertions.assertEquals(KNOWN_PRIMES, actual, PRIMES_DEBUG);
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}
	}

	/**
	 * Tests the results are correct for single versus multithreaded approaches.
	 */
	@Nested
	@Order(3)
	@TestMethodOrder(OrderAnnotation.class)
	public class ResultsTests {
		/**
		 * Verify the single-threaded implementation also passes the tests.
		 *
		 * @see PrimeFinder#trialDivision(int)
		 */
		@Test
		@Order(1)
		public void testTrialDivision() {
			Executable test = () -> {
				TreeSet<Integer> actual = PrimeFinder.trialDivision(1000);
				Assertions.assertEquals(KNOWN_PRIMES.size(), actual.size(), PRIMES_DEBUG);
				Assertions.assertEquals(KNOWN_PRIMES, actual, PRIMES_DEBUG);
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Test single and multithreaded results return the same results.
		 *
		 * @see PrimeFinder#findPrimes(int, int)
		 */
		@Order(2)
		@RepeatedTest(3)
		@Tag("PrimeFinder")
		public void testSingleVersusMulti() {
			int max = 5000;
			int threads = 3;

			Executable test = () -> {
				TreeSet<Integer> expected = PrimeFinder.trialDivision(max);
				TreeSet<Integer> actual = PrimeFinder.findPrimes(max, threads);
				Assertions.assertEquals(expected.size(), actual.size(), PRIMES_DEBUG);
				Assertions.assertEquals(expected, actual, PRIMES_DEBUG);
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}
	}

	/**
	 * Benchmarks the multithreading code.
	 */
	@Nested
	@Order(4)
	@TestMethodOrder(OrderAnnotation.class)
	public class BenchmarkTests {
		/** Original log4j2 configuration. */
		public static Configuration ORIGINAL;

		/**
		 * Disables logging and cleans up memory before launching next round of tests.
		 */
		@BeforeAll
		public static void setup() {
			System.out.println("Running " + TIMED_ROUNDS + " benchmark rounds on " +
					Runtime.getRuntime().availableProcessors() + " processors.");

			LogManager.getRootLogger().info("Disabling logging...");
			LoggerContext context = LoggerContext.getContext(false);
			ORIGINAL = context.getConfiguration();

			// create a null configuration
			// (using NullConfiguration causes a StatusLogger warning)
			var builder = ConfigurationBuilderFactory.newConfigurationBuilder();

			builder.add(builder.newAppender("null", "Null"));
			builder.add(builder.newRootLogger(Level.OFF)
					.add(builder.newAppenderRef("null")));

			Configurator.reconfigure(builder.build(false));
			LogManager.getRootLogger().info("Logging disabled.");

			System.gc();
			Thread.yield();
		}

		/**
		 * Enables logging again after benchmarking is complete.
		 */
		@AfterAll
		public static void cleanup() {
			LogManager.getRootLogger().info("Enabling logging...");
			Configurator.reconfigure(ORIGINAL.getConfigurationSource().getURI());
			LogManager.getRootLogger().info("Logging renabled.");
		}

		/**
		 * Verifies multithreading is faster than single threading for a large maximum.
		 */
		@Test
		@Order(1)
		@Tag("PrimeFinderBenchmark")
		public void benchmarkSingleVersusMulti() {
			int max = 5000;
			int threads = 3;

			Executable test = () -> {
				double single = new SingleBenchmarker().benchmark(max);
				double multi = new MultiBenchmarker(threads).benchmark(max);
				double speedup = single / multi;

				String debug = String.format(SINGLE_FORMAT, single, multi, speedup);
				System.out.println();
				System.out.println(debug);

				Assertions.assertAll(debug,
						() -> Assertions.assertTrue(multi < single, "Multithreading must be faster than single threading."),
						() -> Assertions.assertTrue(speedup > SPEEDUP_MINIMUM, "Speedup must be " + SPEEDUP_MINIMUM + "x or faster."));
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Verifies having three worker threads is faster than one worker thread.
		 */
		@Test
		@Order(2)
		public void benchmarkOneVersusThree() {
			int max = 5000;

			Executable test = () -> {
				double multi1 = new MultiBenchmarker(1).benchmark(max);
				double multi3 = new MultiBenchmarker(3).benchmark(max);
				double speedup = multi1 / multi3;

				String debug = String.format(MULTI_FORMAT, multi1, multi3, speedup);
				System.out.println(debug);

				Assertions.assertAll(debug,
						() -> Assertions.assertTrue(multi3 < multi1, "Using 3 workers must be faster than 1 worker thread."),
						() -> Assertions.assertTrue(speedup > SPEEDUP_MINIMUM, "Speedup must be " + SPEEDUP_MINIMUM + "x or faster."));
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}
	}

	/**
	 * Poor attempts to verify the approach is correct.
	 */
	@Nested
	@Order(99)
	@Tag("PrimeFinder")
	@TestMethodOrder(OrderAnnotation.class)
	public class ApproachTests extends HomeworkTest.ApproachTests {
		/** Holds the source code */
		public static String source = null;

		/**
		 * Cleans up memory before launching next round of tests.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@BeforeAll
		public static void setup() throws IOException {
			String name = PrimeFinder.class.getSimpleName() + ".java";
			Path src = Path.of("src", "main", "java");
			Path found = Files.walk(src).filter(p -> p.endsWith(name)).findFirst().get();
			source = Files.readString(found);

			Assertions.assertNotNull(source, "Source code could not be loaded.");
		}

		/**
		 * Verifies the worker threads are terminated. If not, more worker threads will
		 * be active after the {@link PrimeFinder#findPrimes(int, int)} call.
		 *
		 * @throws InterruptedException if unable to sleep
		 */
		@Test
		@Order(1)
		public void testShutdown() throws InterruptedException {
			Executable test = () -> {
				Set<String> start = activeThreads();
				PrimeFinder.findPrimes(1000, 3);
				Set<String> end = activeThreads();

				Supplier<String> debug = () -> {
					Set<String> extra = new TreeSet<>(end);
					extra.removeAll(start);

					return String.format("Found %d extra threads: %s%n", extra.size(), extra);
				};

				Assertions.assertEquals(start.size(), end.size(), debug);
				Assertions.assertEquals(start, end, debug);
			};

			Assertions.assertTimeoutPreemptively(GLOBAL_TIMEOUT, test, TIMEOUT_DEBUG);
		}

		/**
		 * Tests that the java.lang.Thread class does not appear in implementation.
		 *
		 * @throws IOException if unable to read source code
		 */
		@Test
		@Order(2)
		public void testThreadClass() throws IOException {
			String debug = "Do not extend Thread in PrimeFinder! Only the work queue should create worker threads.";
			Assertions.assertFalse(source.matches("(?is).*\\bextends\\s+Thread\\b.*"), debug);
		}

		/**
		 * Tests that the pending variable is not used in the implementation code.
		 *
		 * @throws IOException if unable to read source code
		 */
		@Test
		@Order(3)
		public void testPending() throws IOException {
			String debug = "Only the work queue should track pending or unfinished work.";
			Assertions.assertAll(debug,
					() -> Assertions.assertFalse(source.contains("incrementPending"), "Found incrementPending() in PrimeFinder."),
					() -> Assertions.assertFalse(source.contains("decrementPending"), "Found decrementPending() in PrimeFinder."),
					() -> Assertions.assertFalse(source.contains("int pending"), "Found int pending in PrimeFinder."));
		}

		/**
		 * Tests that the TaskManager class is not used in the implementation code.
		 *
		 * @throws IOException if unable to read source code
		 */
		@Test
		@Order(4)
		public void testTaskManager() throws IOException {
			String debug = "Do not create a TaskManager class. This functionality should be embedded into the WorkQueue instead.";
			Assertions.assertFalse(source.contains("TaskManager"), debug);
		}
	}

	/**
	 * Used to benchmark code. Benchmarking results may be inconsistent, and are
	 * written to favor multithreading.
	 */
	private static abstract class Benchmarker {
		/**
		 * Method that returns a set of primes.
		 *
		 * @param max the maximum size to use
		 * @return set of primes
		 */
		public abstract Set<Integer> run(int max);

		/**
		 * Benchmarks the run method up to the max provided. Fails if any output to
		 * {@link System#err} is detected (usually from the work queue).
		 *
		 * @param max the maximum size to use
		 * @return minimum runtime
		 */
		public double benchmark(int max) {
			PrintStream systemErr = System.err;
			ByteArrayOutputStream console = new ByteArrayOutputStream();
			String buffer = "";
			System.setErr(new PrintStream(console));

			double runtime = 0;

			try {
				runtime = time(max);
				buffer = console.toString();

				if (!buffer.isBlank()) {
					Assertions.fail("Detected System.err output. Is the work queue throwing exceptions?");
				}
			}
			finally {
				System.setErr(systemErr);
				System.err.print(buffer);
			}

			return runtime;
		}

		/**
		 * Times the run method up to the max provided.
		 *
		 * @param max the maximum size to use
		 * @return minimum runtime
		 */
		public double time(int max) {
			Duration minimum = Duration.ofDays(1);
			Set<Integer> previous = run(max); // warmup (untimed) round

			for (int i = 0; i < TIMED_ROUNDS; i++) {
				Instant start = Instant.now();
				Set<Integer> current = run(max);
				Instant end = Instant.now();

				// make sure values did not change this time around
				Assertions.assertEquals(previous, current, PRIMES_DEBUG);
				previous = current;

				// calculate duration elapsed and keep minimum value
				Duration elapsed = Duration.between(start, end);
				minimum = elapsed.compareTo(minimum) < 0 ? elapsed : minimum;
			}

			return (double) minimum.toNanos() / Duration.ofMillis(1).toNanos();
		}
	}

	/**
	 * Used to benchmark single threaded code.
	 */
	private static class SingleBenchmarker extends Benchmarker {
		@Override
		public Set<Integer> run(int max) {
			return PrimeFinder.trialDivision(max);
		}
	}

	/**
	 * Used to benchmark multithreaded code.
	 */
	private static class MultiBenchmarker extends Benchmarker {
		/** Number of threads to use. */
		private final int threads;

		/**
		 * Initializes the number of threads.
		 *
		 * @param threads the number of threads to use
		 */
		public MultiBenchmarker(int threads) {
			this.threads = threads;
		}

		@Override
		public Set<Integer> run(int max) {
			try {
				return PrimeFinder.findPrimes(max, threads);
			}
			catch (IllegalArgumentException e) {
				Assertions.fail("Unexpected exception while benchmarking.", e);
				return null;
			}
		}
	}

	/**
	 * Configures uncaught exception handler for threads before running tests.
	 */
	@BeforeAll
	public static void setup() {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			System.err.println(t.getName() + " encountered exception: " + e.getMessage());
			Assertions.fail(e);
		});
	}

	/**
	 * Returns a list of the active thread names (approximate).
	 *
	 * @return list of active thread names
	 */
	public static Set<String> activeThreads() {
		int active = Thread.activeCount(); // only an estimate
		Thread[] threads = new Thread[active * 2]; // make sure large enough
		Thread.enumerate(threads);
		return Arrays.stream(threads)
				.filter(Objects::nonNull) // remove null values
				.map(Thread::getName) // only keep the thread name
				.filter(name -> !name.startsWith("junit")) // remove junit threads
				.filter(name -> !name.startsWith("surefire")) // remove maven threads
				.collect(Collectors.toSet());
	}

	/** The root logger. */
	public static final Logger root = LogManager.getRootLogger();

	/** The system environment. */
	public static final Map<String, String> ENV = System.getenv();

	/** Indicates whether running on GitHub Actions. */
	public static final boolean GITHUB = Boolean.parseBoolean(ENV.get("CI"))
			&& Boolean.parseBoolean(ENV.get("GITHUB_ACTIONS"));

	/** Format string used for single vs multi benchmarking. */
	private static final String SINGLE_FORMAT = """

			 Single: %8.4fms
			  Multi: %8.4fms
			Speedup: %8.4fx
			""";

	/** Format string used for 1 vs multiple threads benchmarking. */
	private static final String MULTI_FORMAT = """

			1 Worker : %8.4fms
			3 Workers: %8.4fms
			  Speedup: %8.4fx
			""";

	/** Maximum amount of time to wait per test. */
	public static final Duration GLOBAL_TIMEOUT = Duration.ofSeconds(60);

	/** Debug message when a test times out. */
	public static final String TIMEOUT_DEBUG = "This test timed out. Deadlock, over-blocking, or over-notifying can cause timeouts.";

	/** Debug message when primes do not match. */
	public static final String PRIMES_DEBUG = "The expected and actual primes do not match. Under-synchronizing or not waiting until work is finished can cause mismatches.";

	/** Number of timed rounds to run when benchmarking on GitHub Actions. */
	public static final int GITHUB_ROUNDS = 5;

	/** Number of timed rounds to run when benchmarking locally. */
	public static final int LOCAL_ROUNDS = 20;

	/** The number of rounds to use when benchmarking. */
	public static final int TIMED_ROUNDS = GITHUB ? GITHUB_ROUNDS : LOCAL_ROUNDS;

	/** Minimum cutoff speedup required. */
	public static final double SPEEDUP_MINIMUM = 1.1;

	/**
	 * Hard-coded set of known primes to compare against.
	 */
	public static final Set<Integer> KNOWN_PRIMES = Set.of(2, 3, 5, 7, 11, 13, 17,
			19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
			101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173,
			179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257,
			263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349,
			353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439,
			443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541,
			547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631,
			641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733,
			739, 743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829,
			839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941,
			947, 953, 967, 971, 977, 983, 991, 997);
}
