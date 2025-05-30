package edu.usfca.cs272.tests.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
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

	/** Location of .git folder. */
	public static final Path GIT_DIR = Path.of(".git").toAbsolutePath().normalize();

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
	 * Checks if a commit should be made before the tests are run.
	 *
	 * @throws Exception if unable to parse git information
	 *
	 * @see <a href="https://github.com/eclipse-jgit/jgit/wiki/User-Guide">jgit</a>
	 * @see <a href="https://github.com/centic9/jgit-cookbook">jgit-cookbook</a>
	 */
	@BeforeAll
	public static void checkCommits() throws Exception {
		if ("true".equals(System.getenv("GITHUB_ACTIONS"))) {
			// do not run on GitHub Actions environment
			return;
		}

		if (!Files.isDirectory(GIT_DIR)) {
			Assertions.fail("Unable to locate .git directory...");
		}

		// setup repository builder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		builder = builder.setGitDir(GIT_DIR.toFile())
				.readEnvironment()
				.findGitDir();

		// try to load repository
		try (
			Repository repository = builder.build();
			Git git = new Git(repository);
		) {
			Status status = git.status().call();

			// get uncommitted changes to java files only
			List<String> changes = status.getUncommittedChanges()
					.stream()
					.filter(s -> s.endsWith(".java"))
					.toList();

			// if there are uncommitted java changes,
			// check how long its been since the last commit
			if (!changes.isEmpty()) {
				System.err.printf("Found %d uncommitted changes: %s%n", changes.size(), changes);
				Iterator<RevCommit> logs = git.log().call().iterator();

				if (logs.hasNext()) {
					RevCommit commit = logs.next();

					// get commit date/time and current date/time in same time zone
					ZoneId zone = commit.getAuthorIdent().getTimeZone().toZoneId();
					Instant timestamp = Instant.ofEpochSecond(commit.getCommitTime());
					ZonedDateTime committed = ZonedDateTime.ofInstant(timestamp, zone);
					ZonedDateTime today = ZonedDateTime.now(zone);

					// get elapsed minutes since last commit
					Duration elapsed = Duration.between(committed, today);
					long minutes = elapsed.toMinutes();

					// output a warning and stop running tests if over 30 minutes
					if (minutes > 30) {
						String date = DateTimeFormatter.ofPattern("h:mm a 'on' MMM d, yyyy").format(committed);
						String output = "Your last commit was at " + date + ". " +
								"Please make a new commit before running the tests.";

						System.err.println(output);
						Assertions.fail(output);
					}
				}
				else {
					String output = "Please make a first commit before running the tests.";
					System.err.println(output);
					Assertions.fail(output);
				}
			}
		}
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
			return Files.walk(SOURCE_DIR)
					.filter(p -> p.endsWith(name))
					.filter(Files::isReadable)
					.findFirst()
					.get();
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
		public ApproachTests() {}

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
				System.err.printf("Found %d/%d passing tests so far, but you must run these on GitHub Actions to be certain they are passing.", TestCounter.passed, TestCounter.passed + TestCounter.failed);
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
