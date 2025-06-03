package edu.usfca.cs272.query;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import edu.usfca.cs272.index.FileStemmer;
import edu.usfca.cs272.index.InvertedIndex;
import edu.usfca.cs272.util.JsonWriter;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Handles query processing for an inverted index. Reads queries from a file,
 * applies stemming to normalize the words, and performs searches using a
 * provided search function. Search results are stored in a sorted map and can
 * be viewed or written to JSON.
 */
public class SerialQueryProcessor implements QueryProcessor {

	/** Stores search results. */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> results;

	/**
	 * Function to perform either partial or exact search.
	 */
	private final Function<Set<String>, List<InvertedIndex.SearchResult>> search;

	/**
	 * Stemmer used to process query words.
	 */
	private final Stemmer stemmer;

	/**
	 * Constructs a QueryProcessor with a search function.
	 * 
	 * @param partial true if using partial search; false if using exact search
	 * @param invertedIndex the inverted index to be used for search operations
	 *
	 */
	public SerialQueryProcessor(boolean partial, InvertedIndex invertedIndex) {
		this.results = new TreeMap<>();
		this.search = partial ? invertedIndex::searchPartial : invertedIndex::searchExact;
		this.stemmer = new SnowballStemmer(ENGLISH);
	}

	/**
	 * Processes a single line of queries, performing a search and storing the
	 * results.
	 *
	 * @param line the line containing query words
	 */
	@Override
	public void processLine(String line) {
		TreeSet<String> query = FileStemmer.uniqueStems(line, stemmer);
		String joinedQuery = String.join(" ", query);
		if (!query.isEmpty() && !results.containsKey(joinedQuery)) {
			results.put(joinedQuery, search.apply(query));
		}
	}

	/**
	 * Returns a sorted set of stored queries.
	 *
	 * @return an unmodifiable sorted set of queries
	 */
	@Override
	public SortedSet<String> viewQueries() {
		return Collections.unmodifiableSortedSet(results.navigableKeySet());
	}

	/**
	 * Returns a list of search results for a given query.
	 *
	 * @param query the query string
	 * @return an unmodifiable list of search results for the query
	 */
	@Override
	public List<InvertedIndex.SearchResult> viewResults(String query) {
		List<InvertedIndex.SearchResult> lineResults = results.get(query);
		return lineResults == null ? Collections.emptyList() : Collections.unmodifiableList(lineResults);
	}

	/**
	 * Returns a JSON-formatted string representation of the search results.
	 *
	 * @return a JSON string representation of the search results
	 */
	@Override
	public String toString() {
		return JsonWriter.writeSearchResults(results);
	}

	/**
	 * Writes the search results to a JSON file.
	 *
	 * @param path the output file path
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	@Override
	public void resultsToJson(Path path) throws IOException {
		JsonWriter.writeSearchResults(results, path);
	}
}
