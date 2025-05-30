package edu.usfca.cs272;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Ryan Fahimi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class Driver {
	/**
	 * Logger for logging events in Driver class.
	 */
	private static final Logger logger = LogManager.getLogger(Driver.class);

	/**
	 * Command-line flag to specify the path to the text file or directory to index.
	 */
	private static final String TEXT_FLAG = "-text";

	/**
	 * Command-line flag to specify the output JSON file path for word counts.
	 */
	private static final String COUNTS_FLAG = "-counts";

	/**
	 * Command-line flag to specify the output JSON file path for inverted index.
	 */
	private static final String INDEX_FLAG = "-index";

	/** Command-line flag to specify the query file path. */
	private static final String QUERY_FLAG = "-query";

	/**
	 * Command-line flag to specify the output JSON file path for search results.
	 */
	private static final String RESULTS_FLAG = "-results";

	/**
	 * Command-line flag to specify whether to use partial search.
	 */
	private static final String PARTIAL_FLAG = "-partial";

	/**
	 * Command-line flag to specify the amount of threads to use for building and
	 * searching the inverted index.
	 */
	private static final String THREADS_FLAG = "-threads";

	/**
	 * Command-line flag to specify the HTML seed URL for web page indexing.
	 */
	private static final String HTML_FLAG = "-html";

	/**
	 * Command-line flag to specify the maximum number of URIs to crawl from the
	 * seed.
	 */
	private static final String CRAWL_FLAG = "-crawl";

	/**
	 * Command-line flag to specify to launch a multithreaded search engine web
	 * server.
	 */
	private static final String SERVER_FLAG = "-server";

	/**
	 * Default filename used when no custom output file is provided for word counts.
	 */
	private static final String DEFAULT_COUNTS_FILENAME = "counts.json";

	/**
	 * Default filename used when no custom output file is provided for inverted
	 * index.
	 */
	private static final String DEFAULT_INDEX_FILENAME = "index.json";

	/**
	 * Default filename used when no custom output file is provided for search
	 * results.
	 */
	private static final String DEFAULT_RESULTS_FILENAME = "results.json";

	/**
	 * Default number of threads used for multi-threading.
	 */
	private static final Integer DEFAULT_THREADS = 5;

	/**
	 * Default number of URIs to crawl when the crawl flag is used without a value.
	 */
	private static final Integer DEFAULT_TOTAL_URIS = 1;

	/**
	 * Default port for the web server.
	 */
	private static final Integer DEFAULT_PORT = 8080;

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		try {
			Instant start = Instant.now();

			logWorkingDirectory();
			logArguments(args);

			processThreadedFlags(new ArgumentParser(args));

			logElapsedTime(start);
		}
		catch (Exception e) {
			logger.error("An unexpected error occurred", e);
		}
	}

	/**
	 * Logs the current working directory.
	 */
	private static void logWorkingDirectory() {
		logger.info("Working Directory: {}", Path.of(".").toAbsolutePath().normalize());
	}

	/**
	 * Logs the command-line arguments.
	 *
	 * @param args the program arguments
	 */
	private static void logArguments(String[] args) {
		logger.info("Arguments: {}", Arrays.toString(args));
	}

	/**
	 * Processes the thread flag and HTML flag from the command-line arguments to
	 * determine whether to run in multi-threaded or single-threaded mode.
	 *
	 * @param argParser the argument parser containing command-line options
	 */
	private static void processThreadedFlags(ArgumentParser argParser) {
		if (argParser.hasFlag(THREADS_FLAG) || argParser.hasFlag(HTML_FLAG) || argParser.hasFlag(SERVER_FLAG)) {
			int threads = argParser.getPositiveInteger(THREADS_FLAG, DEFAULT_THREADS);
			logger.info("Multithreading with {} thread{}", threads, threads > 1 ? "s" : "");
			runMultiThreaded(argParser, threads);
		}
		else {
			logger.info("Singlethreading detected");
			runSingleThreaded(argParser);
		}
	}

	/**
	 * Initializes a work queue, thread-safe inverted index, text file indexer, and
	 * concurrent query processor. Executes text indexing, query processing, and
	 * JSON output generation concurrently.
	 *
	 * @param argParser the argument parser containing command-line options
	 * @param threads the number of threads used for multi-threading
	 */
	private static void runMultiThreaded(ArgumentParser argParser, int threads) {
		logger.info("Starting multi-threaded processing...");
		WorkQueue tasks = new WorkQueue(threads);
		ThreadSafeInvertedIndex invertedIndex = new ThreadSafeInvertedIndex();
		ConcurrentTextFileIndexer textFileIndexer = new ConcurrentTextFileIndexer(invertedIndex, tasks);
		WebCrawler webCrawler = new WebCrawler(invertedIndex, tasks,
				argParser.getPositiveInteger(CRAWL_FLAG, DEFAULT_TOTAL_URIS));
		ConcurrentQueryProcessor queryProcessor = new ConcurrentQueryProcessor(argParser.hasFlag(PARTIAL_FLAG),
				invertedIndex, tasks);

		processHtmlFlag(argParser, webCrawler);
		processPathInputFlags(argParser, textFileIndexer, queryProcessor);
		processServerFlag(argParser, invertedIndex);

		tasks.shutdown();

		processOutputFlags(argParser, invertedIndex, queryProcessor);

		tasks.join();

		logger.info("Finished multi-threaded processing.");
	}

	/**
	 * Initializes a standard inverted index, text file indexer, and query
	 * processor. Executes text indexing, query processing, and JSON output
	 * generation in a single thread.
	 *
	 * @param argParser the argument parser containing command-line options
	 */
	private static void runSingleThreaded(ArgumentParser argParser) {
		logger.info("Starting single-threaded processing...");
		InvertedIndex invertedIndex = new InvertedIndex();
		TextFileIndexer textFileIndexer = new TextFileIndexer(invertedIndex);
		SerialQueryProcessor queryProcessor = new SerialQueryProcessor(argParser.hasFlag(PARTIAL_FLAG), invertedIndex);

		processPathInputFlags(argParser, textFileIndexer, queryProcessor);
		processOutputFlags(argParser, invertedIndex, queryProcessor);

		logger.info("Finished single-threaded processing.");
	}

	/**
	 * Processes the HTML flag and crawls the content at the given URI if valid.
	 *
	 * @param argParser the argument parser containing command-line options
	 * @param webSiteIndexer the web page indexer to process the URI
	 */
	private static void processHtmlFlag(ArgumentParser argParser, WebCrawler webSiteIndexer) {
		if (!argParser.hasFlag(HTML_FLAG)) {
			logger.warn("No {} flag provided. Skipping.", HTML_FLAG);
			return;
		}

		String seed = argParser.getString(HTML_FLAG);

		if (seed == null) {
			logger.error("Invalid or null seed uri provided for {} flag.", HTML_FLAG);
			return;
		}

		logger.info("Processing {} flag at \"{}\"", HTML_FLAG, seed);

		try {
			webSiteIndexer.crawl(seed);
		}
		catch (UncheckedIOException e) {
			logger.error("Error processing {} at \"{}\"", HTML_FLAG, seed, e);
		}
	}

	/**
	 * Processes the server flag and starts the search engine web server if the flag
	 * is present.
	 *
	 * @param argParser the argument parser containing command-line options
	 * @param invertedIndex the thread-safe inverted index to use for searches
	 */
	private static void processServerFlag(ArgumentParser argParser, ThreadSafeInvertedIndex invertedIndex) {
		if (argParser.hasFlag(SERVER_FLAG)) {
			try {
				SearchEngine.run(argParser.getPositiveInteger(SERVER_FLAG, DEFAULT_PORT), invertedIndex,
						argParser.getPath(TEXT_FLAG, null));
			}
			catch (IOException e) {
				logger.error("Error reading or writing index.html", e);
			}
			catch (Exception e) {
				logger.error("Error with SearchEngine server: {}", e);
			}
		}
	}

	/**
	 * Processes the path-related input flags for text indexing and query
	 * processing.
	 *
	 * @param argParser the argument parser containing command-line options
	 * @param textFileIndexer the indexer to process text file input
	 * @param queryProcessor the processor to handle query input
	 */
	private static void processPathInputFlags(ArgumentParser argParser, TextFileIndexer textFileIndexer,
			QueryProcessor queryProcessor) {
		processPathFlag(argParser, TEXT_FLAG, null, textFileIndexer::indexPath);
		processPathFlag(argParser, QUERY_FLAG, null, queryProcessor::processPath);
	}

	/**
	 * Processes the output flags and writes the corresponding JSON files for word
	 * counts, inverted index, and search results.
	 *
	 * @param argParser the argument parser
	 * @param invertedIndex the inverted index used for output
	 * @param queryProcessor the query processor used for output
	 */
	private static void processOutputFlags(ArgumentParser argParser, InvertedIndex invertedIndex,
			QueryProcessor queryProcessor) {
		processPathFlag(argParser, COUNTS_FLAG, Path.of(DEFAULT_COUNTS_FILENAME), invertedIndex::countsToJson);

		processPathFlag(argParser, INDEX_FLAG, Path.of(DEFAULT_INDEX_FILENAME), invertedIndex::indexToJson);

		processPathFlag(argParser, RESULTS_FLAG, Path.of(DEFAULT_RESULTS_FILENAME), queryProcessor::resultsToJson);
	}

	/**
	 * Processes a specific path-related command-line flag. If the flag is present
	 * and the associated path is valid, applies the specified processor to that
	 * path, and handles any I/O exceptions that may occur.
	 *
	 * @param argParser the argument parser containing command-line options
	 * @param flag the command-line flag to process
	 * @param defaultPath the default path to use if the flag does not specify a
	 *   path
	 * @param processor a function that consumes the path and performs I/O
	 *   operations, such as reading a file or writing JSON
	 */
	private static void processPathFlag(ArgumentParser argParser, String flag, Path defaultPath,
			IOThrowingConsumer<Path> processor) {
		if (!argParser.hasFlag(flag)) {
			logger.warn("No {} flag provided. Skipping.", flag);
			return;
		}

		Path path = argParser.getPath(flag, defaultPath);

		if (path == null) {
			logger.error("Invalid or null path provided for {} flag.", flag);
			return;
		}

		logger.info("Processing {} flag at \"{}\"", flag, path);

		try {
			processor.accept(path);
		}
		catch (IOException | UncheckedIOException e) {
			logger.error("Error processing {} at \"{}\"", flag, path, e);
		}
	}

	/**
	 * Logs the elapsed time since the given start time.
	 *
	 * @param start the start time
	 */
	private static void logElapsedTime(Instant start) {
		final long elapsedMillis = Duration.between(start, Instant.now()).toMillis();
		final double elapsedSeconds = elapsedMillis / 1000.0;
		logger.info("Elapsed: {} seconds", String.format("%.3f", elapsedSeconds));
	}

	/** Prevent instantiating this class of static methods. */
	private Driver() {
	}
}
