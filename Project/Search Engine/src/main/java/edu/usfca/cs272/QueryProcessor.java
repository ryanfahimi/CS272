package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.SortedSet;

/**
 * An interface for processing search queries and storing their corresponding
 * results. Implementations of this interface handle reading query data,
 * performing searches, storing results, and writing them to JSON.
 */
public interface QueryProcessor {

	/**
	 * Reads a query file line by line and processes each query.
	 *
	 * @param path the path to the query file
	 * @throws IOException if an I/O error occurs while reading the file
	 */
	default void processPath(Path path) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				processLine(line);
			}
		}
	}

	/**
	 * Processes a single line of queries, performing a search and storing the
	 * results.
	 *
	 * @param line the line containing query words
	 */
	void processLine(String line);

	/**
	 * Returns the number of unique queries processed.
	 *
	 * @return the number of stored queries
	 */
	default int sizeQueries() {
		return viewQueries().size();
	}

	/**
	 * Returns the number of search results processed for a given query.
	 * 
	 * @param query the query string
	 * @return the number of search results
	 * 
	 */
	default int sizeResults(String query) {
		return viewResults(query).size();
	}

	/**
	 * Returns whether results exist for a given query.
	 *
	 * @param query the query string
	 * @return true if results exist, false otherwise
	 */
	default boolean hasQuery(String query) {
		return viewQueries().contains(query);
	}

	/**
	 * Returns whether a specific result exists for a given query.
	 *
	 * @param query the query string
	 * @param result the search result to check
	 * @return true if the result exists for the given query, false otherwise
	 */
	default boolean hasResult(String query, InvertedIndex.SearchResult result) {
		return viewResults(query).contains(result);
	}

	/**
	 * Returns a sorted set of stored queries.
	 *
	 * @return an unmodifiable sorted set of queries
	 */
	SortedSet<String> viewQueries();

	/**
	 * Returns a list of search results for a given query.
	 *
	 * @param query the query string
	 * @return an unmodifiable list of search results for the query
	 */
	List<InvertedIndex.SearchResult> viewResults(String query);

	/**
	 * Writes the search results to a JSON file.
	 *
	 * @param path the output file path
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	void resultsToJson(Path path) throws IOException;

}
