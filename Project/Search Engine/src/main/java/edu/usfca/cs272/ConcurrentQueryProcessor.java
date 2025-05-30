package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles query processing for an inverted index concurrently. Reads queries
 * from a file, applies stemming to normalize the words, and performs searches
 * using a provided search function. Search results are stored in a sorted map
 * and can be viewed or written to JSON. A WorkQueue is used to execute tasks
 * concurrently.
 */
public class ConcurrentQueryProcessor implements QueryProcessor {

	/**
	 * Logger for logging events in QueryProcessor class.
	 */
	private static final Logger logger = LogManager.getLogger(ConcurrentQueryProcessor.class);

	/** Stores search results. */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> results;

	/**
	 * Function to perform either partial or exact search.
	 */
	private final Function<Set<String>, List<InvertedIndex.SearchResult>> search;

	/**
	 * WorkQueue for managing concurrent tasks.
	 */
	private final WorkQueue tasks;

	/**
	 * Constructs a ConcurrentQueryProcessor with a search function and a WorkQueue
	 * for task management.
	 * 
	 * @param partial true if using partial search; false if using exact search
	 * @param invertedIndex the inverted index to be used for search operations
	 * @param tasks the WorkQueue to manage concurrent query processing tasks
	 */
	public ConcurrentQueryProcessor(boolean partial, InvertedIndex invertedIndex, WorkQueue tasks) {
		this.results = new TreeMap<>();
		this.search = partial ? invertedIndex::searchPartial : invertedIndex::searchExact;
		this.tasks = tasks;
		logger.debug("Initialized ConcurrentQueryProcessor with given search function");
	}

	/**
	 * Reads a query file line by line and processes each query concurrently.
	 *
	 * @param path the path to the query file
	 * @throws IOException if an I/O error occurs while reading the file
	 */
	@Override
	public void processPath(Path path) throws IOException {
		logger.info("Starting to process query file: {}", path);
		QueryProcessor.super.processPath(path);
		tasks.finish();
		logger.info("Finished processing query file: {}", path);
	}

	/**
	 * Executes a task processing a single line of queries in a thread-safe manner.
	 *
	 * @param line the line containing query words
	 */
	@Override
	public void processLine(String line) {
		tasks.execute(new Task(line));
	}

	/**
	 * Returns the number of unique queries processed in a thread-safe manner.
	 *
	 * @return the number of stored queries
	 */
	@Override
	public int sizeQueries() {
		synchronized (results) {
			return results.size();
		}
	}

	/**
	 * Returns the number of search results processed for a given query in a
	 * thread-safe manner.
	 * 
	 * @param query the query string
	 * @return the number of search results
	 * 
	 */
	@Override
	public int sizeResults(String query) {
		synchronized (results) {
			List<InvertedIndex.SearchResult> lineResults = results.get(query);
			return lineResults == null ? 0 : lineResults.size();
		}
	}

	/**
	 * Returns whether results exist for a given query in a thread-safe manner.
	 *
	 * @param query the query string
	 * @return true if results exist, false otherwise
	 */
	@Override
	public boolean hasQuery(String query) {
		synchronized (results) {
			return results.containsKey(query);
		}
	}

	/**
	 * Returns whether a specific result exists for a given query in a thread-safe
	 * manner.
	 *
	 * @param query the query string
	 * @param result the search result to check
	 * @return true if the result exists for the given query, false otherwise
	 */
	@Override
	public boolean hasResult(String query, InvertedIndex.SearchResult result) {
		synchronized (results) {
			List<InvertedIndex.SearchResult> lineResults = results.get(query);
			return lineResults != null && lineResults.contains(result);
		}
	}

	/**
	 * Returns a sorted set of stored queries in a thread-safe manner.
	 *
	 * @return an unmodifiable sorted set of queries
	 */
	@Override
	public SortedSet<String> viewQueries() {
		synchronized (results) {
			return Collections.unmodifiableSortedSet(results.navigableKeySet());
		}
	}

	/**
	 * Returns a list of search results for a given query in a thread-safe manner.
	 *
	 * @param query the query string
	 * @return an unmodifiable list of search results for the query
	 */
	@Override
	public List<InvertedIndex.SearchResult> viewResults(String query) {
		synchronized (results) {
			List<InvertedIndex.SearchResult> lineResults = results.get(query);
			return lineResults == null ? Collections.emptyList() : Collections.unmodifiableList(lineResults);
		}
	}

	/**
	 * Returns a JSON-formatted string representation of the search results in a
	 * thread-safe manner.
	 *
	 * @return a JSON string representation of the search results
	 */
	@Override
	public String toString() {
		synchronized (results) {
			return JsonWriter.writeSearchResults(results);
		}
	}

	/**
	 * Writes the search results to a JSON file in a thread-safe manner.
	 *
	 * @param path the output file path
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	@Override
	public void resultsToJson(Path path) throws IOException {
		logger.debug("Writing search results to JSON file: {}", path);
		synchronized (results) {
			JsonWriter.writeSearchResults(results, path);
		}
		logger.debug("Successfully wrote search results to JSON file: {}", path);
	}

	/**
	 * Private inner class representing a task for processing a query line.
	 */
	private class Task implements Runnable {
		/**
		 * The query line to be processed.
		 */
		private final String line;

		/**
		 * Constructs a new Task for processing a query line.
		 *
		 * @param line the query line to be processed
		 */
		public Task(String line) {
			this.line = line;
		}

		/**
		 * Processes a single line of queries, performing a search and storing the
		 * results in a thread-safe manner.
		 *
		 */
		@Override
		public void run() {
			TreeSet<String> query = FileStemmer.uniqueStems(line);
			if (query.isEmpty()) {
				return;
			}
			String joinedQuery = String.join(" ", query);
			synchronized (results) {
				if (results.containsKey(joinedQuery)) {
					return;
				}
				else {
					results.put(joinedQuery, null);
				}
			}
			List<InvertedIndex.SearchResult> lineResults = search.apply(query);
			synchronized (results) {
				results.put(joinedQuery, lineResults);
			}
		}
	}

}
