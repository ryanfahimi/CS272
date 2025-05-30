package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for crawling URIs and storing their content into an
 * inverted index.
 */
public class WebCrawler {
	/**
	 * Logger for logging events in WebCrawler class.
	 */
	private static final Logger logger = LogManager.getLogger(WebCrawler.class);

	/**
	 * The inverted index to store word occurrences across multiple files in a
	 * thread-safe manner.
	 */
	private final ThreadSafeInvertedIndex invertedIndex;

	/**
	 * WorkQueue for managing concurrent tasks.
	 */
	private final WorkQueue tasks;

	/**
	 * Maximum number of redirects to follow while fetching HTML.
	 */
	public final static int MAX_REDIRECTS = 3;

	/**
	 * Total number of URIs to crawl before stopping.
	 */
	private int totalUris;

	/**
	 * Set of URIs that have already been crawled to prevent duplication.
	 */
	private final Set<URI> crawledUris;

	/**
	 * Initializes a {@link WebCrawler} with the given thread-safe inverted index
	 * and work queue.
	 *
	 * @param invertedIndex the thread-safe inverted index
	 * @param tasks the work queue for managing tasks
	 * @param totalUris the total number of URIs to crawl
	 */
	public WebCrawler(ThreadSafeInvertedIndex invertedIndex, WorkQueue tasks, int totalUris) {
		this.invertedIndex = invertedIndex;
		this.tasks = tasks;
		this.totalUris = totalUris;
		this.crawledUris = new HashSet<>();
		logger.debug("Initialized WebCrawler with provided inverted index and work queue.");
	}

	/**
	 * Starts a task to crawl the specified URI.
	 *
	 * @param seed the URI to index
	 */
	public void crawl(String seed) {
		URI uri = LinkFinder.clean(seed);
		if (uri != null) {
			crawledUris.add(uri);
			tasks.execute(new Task(uri));
		}
		tasks.finish();
	}

	/**
	 * Indexes the given text and add its contents to the inverted index.
	 *
	 * @param source the source identifier
	 * @param text the text content to index
	 * @param invertedIndex the inverted index to update
	 * @param stemmer the stemmer to use for word normalization
	 * @throws IOException if an I/O error occurs while reading the string
	 */
	public static void indexText(String source, String text, InvertedIndex invertedIndex, Stemmer stemmer)
			throws IOException {
		int position = 1;
		try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isBlank()) {
					for (String word : FileStemmer.parse(line)) {
						if (!word.isEmpty()) {
							invertedIndex.add(stemmer.stem(word).toString(), source, position);
							position++;
						}
					}
				}
			}
		}
	}

	/**
	 * Runnable task responsible for fetching, cleaning, parsing, and indexing the
	 * HTML content of a specific URI, as well as discovering and queuing additional
	 * links to index.
	 */
	private class Task implements Runnable {

		/**
		 * The URI to index.
		 */
		private final URI uri;

		/**
		 * Initializes a task with the specified URI.
		 *
		 * @param uri the URI to process
		 */
		public Task(URI uri) {
			this.uri = uri;
		}

		/**
		 * Processes the HTML content of the URI, extracts and cleans text, indexes it
		 * locally, and merges it into the shared inverted index. Also finds and queues
		 * new links to crawl.
		 */
		@Override
		public void run() {
			try {
				String html = HtmlFetcher.fetch(uri, MAX_REDIRECTS);
				if (html != null) {
					html = HtmlCleaner.stripBlockElements(html);
					ArrayList<URI> links = LinkFinder.listUris(uri, html);
					synchronized (crawledUris) {
						for (URI link : links) {
							if (crawledUris.size() >= totalUris) {
								break;
							}
							if (crawledUris.add(link)) {
								tasks.execute(new Task(link));
							}
						}
					}
					html = HtmlCleaner.stripTags(html);
					html = HtmlCleaner.stripEntities(html);
					InvertedIndex local = new InvertedIndex();
					indexText(uri.toString(), html, local, new SnowballStemmer(ENGLISH));
					invertedIndex.addAll(local);
				}
			}
			catch (IOException e) {
				logger.error("Error indexing html: " + uri, e);
				throw new UncheckedIOException(e);
			}
		}
	}
}
