package edu.usfca.cs272.tests;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.usfca.cs272.tests.utils.HomeworkTest;
import edu.usfca.cs272.utils.LinkFinder;

/**
 * Tests the {@link LinkFinder} class.
 *
 * @see LinkFinder
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
@Tag("LinkFinder")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class LinkFinderTest extends HomeworkTest {
	// ███████╗████████╗ ██████╗ ██████╗
	// ██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗
	// ███████╗   ██║   ██║   ██║██████╔╝
	// ╚════██║   ██║   ██║   ██║██╔═══╝
	// ███████║   ██║   ╚██████╔╝██║
	// ╚══════╝   ╚═╝    ╚═════╝ ╚═╝
	/*
	 * ...and read this. The remote tests will NOT run unless you are passing the
	 * local tests first. You should not try to run the remote tests until the local
	 * tests are passing, and should avoid rapidly re-running the remote tests over
	 * and over again.
	 *
	 * You risk being blocked by our web server for making too many requests!
	 */

	/** Creates a new instance of this class. */
	public LinkFinderTest() {}

	/**
	 * Tests if links use the HTTP(s) protocols.
	 */
	@Nested
	@Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	public class LocalHTTPTests {
		/** Creates a new instance of this class. */
		public LocalHTTPTests() {}

		/**
		 * Tests a link that uses the HTTP or HTTPS protocols (case-insensitive).
		 *
		 * @param link the link that is uses http(s)
		 */
		@ParameterizedTest
		@Order(1)
		@ValueSource(strings = {
				"https://www.example.com/",
				"http://www.example.com/",
				"HTTPS://www.example.com/",
				"HTTP://www.example.com/",
				"hTTps://localhost/",
				GITHUB
		})
		public void testHttp(String link) {
			URI uri = URI.create(link);
			Assertions.assertTrue(LinkFinder.isHttp(uri), uri.toString());
		}

		/**
		 * Tests a link that does not use the HTTP or HTTPS protocols (case-insensitive).
		 *
		 * @param link the link that does not use http(s)
		 */
		@ParameterizedTest
		@Order(2)
		@ValueSource(strings = {
				"mailto:sjengle@usfca.edu",
				"index.html",
				"ftp://www.example.com/",
				"www.example.com"
		})
		public void testNotHttp(String link) {
			URI uri = URI.create(link);
			Assertions.assertFalse(LinkFinder.isHttp(uri), uri.toString());
		}

		/**
		 * Tests a null link.
		 */
		@Test
		@Order(3)
		public void testNull() {
			Assertions.assertFalse(LinkFinder.isHttp(null));
		}
	}

	/**
	 * Group of local tests.
	 */
	@Nested
	@Order(2)
	@TestClassOrder(ClassOrderer.OrderAnnotation.class)
	public class LocalTests {
		/** Creates a new instance of this class. */
		public LocalTests() {}

		/**
		 * Tests links on locally-created HTML text (not actual webpages).
		 */
		@Nested
		@Order(1)
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		public class LocalValidTests {
			/** Creates a new instance of this class. */
			public LocalValidTests() {}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(1)
			public void testSimple() {
				String link = "http://www.usfca.edu/";
				String html = """
						<a href="http://www.usfca.edu/">
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(2)
			public void testFragment() {
				String link = "http://docs.python.org/library/string.html?highlight=string";
				String html = """
						<a href="http://docs.python.org/library/string.html?highlight=string#module-string">
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(3)
			public void testUppercase() {
				String link = "HTTP://WWW.USFCA.EDU/";
				String html = """
						<A HREF="HTTP://WWW.USFCA.EDU/">
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(4)
			public void testMixedCase() {
				String link = "http://www.usfca.edu/";
				String html = """
						<A hREf="http://www.usfca.edu/">
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(5)
			public void testSpaces() {
				String link = "http://www.usfca.edu/";
				String html = """
						<a href = "http://www.usfca.edu/" >
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(6)
			public void testOneNewline() {
				String link = "http://www.usfca.edu/";
				String html = """
						<a href =
							"http://www.usfca.edu/">
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(7)
			public void testManyNewlines() {
				String link = "http://www.usfca.edu/";
				String html = """
						<a

						href
						=
						"http://www.usfca.edu/"
						>
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(8)
			public void testSnippet() {
				String link = "http://www.usfca.edu/";
				String html = """
						<p>
							<a href="http://www.usfca.edu">USFCA</a>
							is in San Francisco.
						</p>
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(9)
			public void testDefaultPath() {
				String link = "http://www.example.com/";
				String html = """
						<a href="http://www.example.com">
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(10)
			public void testRelative() {
				String link = "http://www.example.com/index.html";
				String html = """
						<a href="index.html">
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(11)
			public void testHREFLast() {
				String link = "http://www.example.com/index.html";
				String html = """
						<a name="home" href="index.html">
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(12)
			public void testHREFFirst() {
				String link = "http://www.example.com/index.html";
				String html = """
						<a href="index.html" class="primary">
						""";

				testValid(link, html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(13)
			public void testMultipleAttributes() {
				String link = "http://www.example.com/index.html";
				String html = """
						<a name="home" target="_top" href="index.html" id="home" accesskey="A">
						""";
				testValid(link, html);
			}
		}

		/**
		 * Tests links on locally-created HTML text (not actual webpages).
		 */
		@Nested
		@Order(2)
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		public class LocalInvalidTests {
			/** Creates a new instance of this class. */
			public LocalInvalidTests() {}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(1)
			public void testNoHREF() {
				String html = """
						<a name = "home">
						""";

				testInvalid(html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(2)
			public void testNoAnchor() {
				String html = """
						<h1>Home</h1>
						""";

				testInvalid(html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(3)
			public void testMixedNoHREF() {
				String html = """
						<a name=href>The href = "link" attribute is useful.</a>
						""";

				testInvalid(html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(4)
			public void testLinkTag() {
				String html = """
						<link rel="stylesheet" type="text/css" href="style.css">
						""";

				testInvalid(html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(5)
			public void testNoTag() {
				String html = """
						<p>The a href="http://www.google.com" attribute is often used in HTML.</p>"
						""";

				testInvalid(html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(6)
			public void testJavascript() {
				String html = """
						<a href="javascript:alert('Hello!');">Say hello!</a>
						""";

				testInvalid(html);
			}

			/**
			 * Tests a single link.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(7)
			public void testEmail() {
				String html = """
						<a href="mailto:sjengle@cs.usfca.edu">sjengle@cs.usfca.edu</a>
						""";

				testInvalid(html);
			}
		}

		/**
		 * Tests links on locally-created HTML text (not actual webpages).
		 */
		@Nested
		@Order(3)
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		public class LocalMultipleTests {
			/** Creates a new instance of this class. */
			public LocalMultipleTests() {}

			/**
			 * Tests multiple links within the HTML text.
			 *
			 * @see LinkFinder#listUris(URI, String)
			 */
			@Test
			@Order(1)
			public void testMultiple() {
				String html = """
						<h1><a name="about">About</a></h1>

						<p>The <a class="primary" href="index.html#top">Department of
						Computer Science</a> offers an undergraduate and graduate degree at
						<a href="http://www.usfca.edu">University of San Francisco</a>.</p>

						<p>Interested? To find out more about those degrees, visit the URI
						<a href="https://www.usfca.edu/catalog/undergraduate/arts-sciences/computer-science">
						https://www.usfca.edu/catalog/undergraduate/arts-sciences/computer-science</a>.</p>
						""";

				List<URI> expected = List.of(
						URI.create("https://www.cs.usfca.edu/index.html"),
						URI.create("http://www.usfca.edu/"),
						URI.create("https://www.usfca.edu/catalog/undergraduate/arts-sciences/computer-science"));

				URI base = URI.create("https://www.cs.usfca.edu/");
				ArrayList<URI> actual = LinkFinder.listUris(base, html);
				compareLists(expected, actual, html);
			}
		}
	}

	/**
	 * Group of remote tests.
	 */
	@Nested
	@Order(3)
	@TestClassOrder(ClassOrderer.OrderAnnotation.class)
	@TestInstance(Lifecycle.PER_CLASS)
	public class RemoteTests {
		/** Creates a new instance of this class. */
		public RemoteTests() {}

		/**
		 * Only run remote tests if local tests passed with 0 failures.
		 */
		@BeforeAll
		public void checkStatus() {
			String format = """
					%nFound %d passing and %d failing local tests.
					Remote tests disabled unless local tests pass!""";

			Supplier<String> debug = () -> format.formatted(TestCounter.passed, TestCounter.failed);
			assumeTrue(TestCounter.passed > 0 && TestCounter.failed == 0, debug);
		}

		/**
		 * Tests links on actual webpages.
		 */
		@Nested
		@Order(1)
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		public class RemoteSimpleTests {
			/** Creates a new instance of this class. */
			public RemoteSimpleTests() {}

			/**
			 * Tests listing links for a remote URI.
			 */
			@Test
			@Order(1)
			public void testHello() {
				ArrayList<URI> expected = new ArrayList<>();
				testRemote("input/simple/hello.html", expected);
			}

			/**
			 * Tests listing links for a remote URI.
			 */
			@Test
			@Order(2)
			public void testSimple() {
				URI base = URI.create(GITHUB);

				List<URI> expected = List.of(
						base.resolve("input/simple/a/b/c/subdir.html"),
						base.resolve("input/simple/capital_extension.HTML"),
						base.resolve("input/simple/double_extension.html.txt"),
						base.resolve("input/simple/empty.html"),
						base.resolve("input/simple/hello.html"),
						base.resolve("input/simple/mixed_case.htm"),
						base.resolve("input/simple/no_extension"),
						base.resolve("input/simple/no_extension"),
						base.resolve("input/simple/position.html"),
						base.resolve("input/simple/stems.html"),
						base.resolve("input/simple/symbols.html"),
						base.resolve("input/simple/dir.txt"),
						base.resolve("input/simple/wrong_extension.html"));

				testRemote("input/simple/index.html", expected);
			}

			/**
			 * Tests listing links for a remote URI.
			 *
			 */
			@Test
			@Order(3)
			public void testBirds() {
				List<URI> expected = getBirdURIs();
				testRemote("input/birds/index.html", expected);
			}
		}

		/**
		 * Tests links on actual webpages.
		 */
		@Nested
		@Order(2)
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		public class RemoteGutenTests {
			/** Creates a new instance of this class. */
			public RemoteGutenTests() {}

			/**
			 * Tests listing links for a remote URI.
			 */
			@Test
			@Order(1)
			public void testGuten1400() {
				URI base = URI.create(GITHUB);
				URI guten = base.resolve("input/guten/1400-h/");
				List<URI> copies = Collections.nCopies(59, guten.resolve("1400-h.htm"));
				List<URI> images = List.of(
						guten.resolve("images/0012.jpg"), guten.resolve("images/0037.jpg"),
						guten.resolve("images/0072.jpg"), guten.resolve("images/0082.jpg"),
						guten.resolve("images/0132.jpg"), guten.resolve("images/0189.jpg"),
						guten.resolve("images/0223.jpg"), guten.resolve("images/0242.jpg"),
						guten.resolve("images/0245.jpg"), guten.resolve("images/0279.jpg"),
						guten.resolve("images/0295.jpg"), guten.resolve("images/0335.jpg"),
						guten.resolve("images/0348.jpg"), guten.resolve("images/0393.jpg"),
						guten.resolve("images/0399.jpg"));

				List<URI> expected = new ArrayList<>();
				expected.addAll(copies); // appears 59 times in table of contents
				expected.addAll(images); // followed by many images

				testRemote("input/guten/1400-h/1400-h.htm", expected);
			}

			/**
			 * Tests listing links for a remote URI.
			 */
			@Test
			@Order(2)
			public void testGutenberg() {
				List<URI> expected = getGutenURIs();
				testRemote("input/guten/index.html", expected);
			}
		}

		/**
		 * Tests links on actual webpages.
		 */
		@Nested
		@Order(3)
		@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
		public class RemoteRecurseTests {
			/** Creates a new instance of this class. */
			public RemoteRecurseTests() {}

			/**
			 * Tests listing links for a remote URI.
			 */
			@Test
			@Order(1)
			public void testRecurse() {
				URI link02 = URI.create("https://www.cs.usfca.edu/~cs272/recurse/link02.html");
				List<URI> expected = List.of(link02);
				testRemote("https://www.cs.usfca.edu/~cs272/recurse/link01.html", expected);
			}
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
		public ApproachTests() {}
	}

	/**
	 * Tests a valid link.
	 *
	 * @param link the link that should be found
	 * @param html the HTML to parse for that link
	 */
	public static void testValid(String link, String html) {
		URI base = URI.create("http://www.example.com");

		List<URI> expected = List.of(URI.create(link));
		List<URI> actual = LinkFinder.listUris(base, html);
		compareLists(expected, actual, html);
	}

	/**
	 * Tests an invalid link.
	 *
	 * @param html the HTML to parse without a valid link
	 */
	public static void testInvalid(String html) {
		URI base = URI.create("http://www.example.com");
		ArrayList<URI> actual = LinkFinder.listUris(base, html);
		compareLists(Collections.emptyList(), actual, html);
	}

	/**
	 * Tests link parsing from a remote URI.
	 *
	 * @param link the link to fetch
	 * @param expected the expected links
	 */
	public static void testRemote(String link, List<URI> expected) {
		URI base = URI.create(GITHUB);
		URI uri = base.resolve(link);

		Assertions.assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
			String html = getHTML(uri);
			List<URI> actual = LinkFinder.listUris(uri, html);
			compareLists(expected, actual, html);
		});
	}

	/**
	 * Compares lists as joined Strings for easier side-by-side comparison.
	 *
	 * @param expected list of expected values
	 * @param actual list of actual values
	 * @param debug the debug output
	 */
	public static void compareLists(List<?> expected, List<?> actual, String debug) {
		var joiner = Collectors.joining("\n");
		String expectedText = expected.stream().map(Object::toString).collect(joiner);
		String actualText = actual.stream().map(Object::toString).collect(joiner);
		Supplier<String> supplier = () -> "\n" + debug + "\n";
		Assertions.assertEquals(expectedText, actualText, supplier);
	}

	/**
	 * Helper method to get the expected URIs from a remote webpage.
	 *
	 * @return list of expected URIs
	 */
	public static final List<URI> getBirdURIs() {
		URI base = URI.create(GITHUB);

		return List.of(
				base.resolve("input/birds/albatross.html"),
				base.resolve("input/birds/birds.html"),
				base.resolve("input/birds/blackbird.html"),
				base.resolve("input/birds/bluebird.html"),
				base.resolve("input/birds/cardinal.html"),
				base.resolve("input/birds/chickadee.html"),
				base.resolve("input/birds/crane.html"),
				base.resolve("input/birds/crow.html"),
				base.resolve("input/birds/cuckoo.html"),
				base.resolve("input/birds/dove.html"),
				base.resolve("input/birds/duck.html"),
				base.resolve("input/birds/eagle.html"),
				base.resolve("input/birds/egret.html"),
				base.resolve("input/birds/falcon.html"),
				base.resolve("input/birds/finch.html"),
				base.resolve("input/birds/goose.html"),
				base.resolve("input/birds/gull.html"),
				base.resolve("input/birds/hawk.html"),
				base.resolve("input/birds/heron.html"),
				base.resolve("input/birds/hummingbird.html"),
				base.resolve("input/birds/ibis.html"),
				base.resolve("input/birds/kingfisher.html"),
				base.resolve("input/birds/loon.html"),
				base.resolve("input/birds/magpie.html"),
				base.resolve("input/birds/mallard.html"),
				base.resolve("input/birds/meadowlark.html"),
				base.resolve("input/birds/mockingbird.html"),
				base.resolve("input/birds/nighthawk.html"),
				base.resolve("input/birds/osprey.html"),
				base.resolve("input/birds/owl.html"),
				base.resolve("input/birds/pelican.html"),
				base.resolve("input/birds/pheasant.html"),
				base.resolve("input/birds/pigeon.html"),
				base.resolve("input/birds/puffin.html"),
				base.resolve("input/birds/quail.html"),
				base.resolve("input/birds/raven.html"),
				base.resolve("input/birds/roadrunner.html"),
				base.resolve("input/birds/robin.html"),
				base.resolve("input/birds/sandpiper.html"),
				base.resolve("input/birds/sparrow.html"),
				base.resolve("input/birds/starling.html"),
				base.resolve("input/birds/stork.html"),
				base.resolve("input/birds/swallow.html"),
				base.resolve("input/birds/swan.html"),
				base.resolve("input/birds/tern.html"),
				base.resolve("input/birds/turkey.html"),
				base.resolve("input/birds/vulture.html"),
				base.resolve("input/birds/warbler.html"),
				base.resolve("input/birds/woodpecker.html"),
				base.resolve("input/birds/wren.html"),
				base.resolve("input/birds/yellowthroat.html"),
				base.resolve("input/birds/nowhere.html"),
				base.resolve("input/birds/"));
	}

	/**
	 * Helper method to get the expected URIs from a remote webpage.
	 *
	 * @return list of expected URIs
	 */
	public static List<URI> getGutenURIs() {
		URI base = URI.create(GITHUB);
		return List.of(
				base.resolve("input/guten/1400-h/1400-h.htm"),
				base.resolve("input/guten/2701-h/2701-h.htm"),
				base.resolve("input/guten/50468-h/50468-h.htm"),
				base.resolve("input/guten/1322-h/1322-h.htm"),
				base.resolve("input/guten/1228-h/1228-h.htm"), // present unless removing comments
				base.resolve("input/guten/1661-h/1661-h.htm"),
				base.resolve("input/guten/22577-h/22577-h.htm"),
				base.resolve("input/guten/37134-h/37134-h.htm"));
	}

	/**
	 * Helper method to get the HTML from a URI. (Cannot be used for projects.)
	 *
	 * @param uri the URI to fetch
	 * @return the HTML as a String object
	 * @throws IOException if unable to get HTML
	 */
	public static String getHTML(URI uri) throws IOException {
		try (InputStream input = uri.toURL().openStream()) {
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/** Base URI for the GitHub test website. */
	public static final String GITHUB = "https://usf-cs272n-spring2025.github.io/project-web/";
}
