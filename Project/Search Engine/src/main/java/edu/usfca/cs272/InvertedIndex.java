package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents an inverted index that maps words to their source locations and
 * positions. Provides methods for interacting with data and performing both
 * exact and partial searches. The inner SearchResult class encapsulates search
 * results for sources with match count and relevance score.
 */
public class InvertedIndex {
	/**
	 * Logger for logging events in InvertedIndex class.
	 */
	private static final Logger logger = LogManager.getLogger(InvertedIndex.class);

	/**
	 * A tree map storing source identifiers and their associated word counts.
	 */
	private final TreeMap<String, Integer> counts;

	/**
	 * An inverted index tree map storing words, source identifiers, and positions.
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Constructs a new InvertedIndex with an empty index and source word counts
	 * map.
	 */
	public InvertedIndex() {
		this.counts = new TreeMap<>();
		this.index = new TreeMap<>();
		logger.debug("Initialized new InvertedIndex");
	}

	/**
	 * Adds a single word occurrence to the inverted index and updates the word
	 * count while iterating through the words in a source.
	 *
	 * @param word the word to add
	 * @param source the source where the word was found
	 * @param position the position of the word in the source
	 */
	public void add(String word, String source, int position) {
		addCounts(source, position);
		addIndex(word, source, position);
	}

	/**
	 * Adds a list of words from a source to the inverted index and updates the word
	 * count.
	 *
	 * @param words the list of words to add to the index
	 * @param source the source where the words were found
	 */
	public void add(List<String> words, String source) {
		addCounts(words, source);
		addAllIndex(words, source);
	}

	/**
	 * Merges the contents of another InvertedIndex into this index by combining
	 * their source word counts and index mappings.
	 *
	 * @param other the InvertedIndex whose entries are to be merged into this index
	 */
	public void addAll(InvertedIndex other) {
		addAllCounts(other.counts.entrySet());
		addAllIndex(other.index.entrySet());
	}

	/**
	 * Adds the position as the word count for the source in counts if the position
	 * is greater than the current count.
	 *
	 * @param source the source identifier
	 * @param position the position of the word in the source
	 */
	private void addCounts(String source, int position) {
		counts.merge(source, position, Integer::max);
	}

	/**
	 * Adds the word count for a given source to counts.
	 *
	 * @param words the list of words found in the source
	 * @param source the identifier of the source document
	 */
	private void addCounts(List<String> words, String source) {
		int count = words.size();
		if (count > 0) {
			counts.put(source, count);
		}
	}

	/**
	 * Merges all source word count entries from another inverted index into this
	 * index.
	 *
	 * @param otherCounts the set of source count entries from another index
	 */
	private void addAllCounts(Set<Entry<String, Integer>> otherCounts) {
		for (Entry<String, Integer> count : otherCounts) {
			counts.merge(count.getKey(), count.getValue(), Integer::max);
		}
	}

	/**
	 * Adds a word occurrence to the index.
	 *
	 * @param word the word to add
	 * @param source the source in which the word was found
	 * @param position the position of the word in the source
	 */
	private void addIndex(String word, String source, int position) {
		// CITE: Derived from ChatGPT Prompt: "How can I initialize nested structures in
		// Java in one line"
		index.computeIfAbsent(word, k -> new TreeMap<>()).computeIfAbsent(source, k -> new TreeSet<>()).add(position);
	}

	/**
	 * Adds multiple words to the index from a given source.
	 *
	 * @param words the list of words to add
	 * @param source the source in which the words were found
	 */
	private void addAllIndex(List<String> words, String source) {
		int counter = 1;
		for (String word : words) {
			addIndex(word, source, counter);
			counter += 1;
		}
	}

	/**
	 * Merges all inverted index entries from another index into this index by
	 * adding new words and sources and combining positions for existing words and
	 * sources.
	 *
	 * @param otherWords the set of word entries from another inverted index
	 */
	private void addAllIndex(Set<Entry<String, TreeMap<String, TreeSet<Integer>>>> otherWords) {
		for (Entry<String, TreeMap<String, TreeSet<Integer>>> otherWord : otherWords) {
			String word = otherWord.getKey();
			TreeMap<String, TreeSet<Integer>> otherSources = otherWord.getValue();
			TreeMap<String, TreeSet<Integer>> thisSources = index.get(word);
			if (thisSources == null) {
				index.put(word, otherSources);
			}
			else {
				for (Entry<String, TreeSet<Integer>> otherSource : otherSources.entrySet()) {
					String source = otherSource.getKey();
					TreeSet<Integer> otherPositions = otherSource.getValue();
					TreeSet<Integer> thisPositions = thisSources.get(source);
					if (thisPositions == null) {
						thisSources.put(source, otherPositions);
					}
					else {
						thisPositions.addAll(otherPositions);
					}
				}
			}
		}
	}

	/**
	 * Returns the number of sources stored in the counts map.
	 *
	 * @return the number of sources with word counts
	 */
	public int sizeCounts() {
		return counts.size();
	}

	/**
	 * Returns the number of distinct words stored in the index.
	 *
	 * @return the number of words in the index
	 */
	public int sizeWords() {
		return index.size();
	}

	/**
	 * Returns the total number of sources containing the given word.
	 *
	 * @param word the word to lookup
	 * @return the total number of sources containing the word, or 0 if not present
	 */
	public int sizeSources(String word) {
		TreeMap<String, TreeSet<Integer>> sources = index.get(word);
		return sources == null ? 0 : sources.size();
	}

	/**
	 * Returns the number of occurrences for the given word in the specified source.
	 *
	 * @param word the word to lookup
	 * @param source the source in which to lookup the word
	 * @return the number of positions recorded for the word in the source, or 0 if
	 *   not present
	 */
	public int sizePositions(String word, String source) {
		TreeMap<String, TreeSet<Integer>> sources = index.get(word);
		if (sources != null) {
			TreeSet<Integer> positions = sources.get(source);
			if (positions != null) {
				return positions.size();
			}
		}
		return 0;
	}

	/**
	 * Determines whether the counts map contains the given source.
	 *
	 * @param source the source to lookup
	 * @return true if the source exists in the counts map
	 */
	public boolean hasCounts(String source) {
		return counts.containsKey(source);
	}

	/**
	 * Determines whether the index contains the given word.
	 *
	 * @param word the word to lookup
	 * @return true if the word exists in the index
	 */
	public boolean hasWord(String word) {
		return index.containsKey(word);
	}

	/**
	 * Determines whether the given word appears in the specified source.
	 *
	 * @param word the word to lookup
	 * @param source the source in which to lookup the word
	 * @return true if the word appears in the source
	 */
	public boolean hasSource(String word, String source) {
		TreeMap<String, TreeSet<Integer>> sources = index.get(word);
		return sources != null && sources.containsKey(source);
	}

	/**
	 * Determines whether the given word appears in the specified source at the
	 * given position.
	 *
	 * @param word the word to lookup
	 * @param source the source in which to lookup the word
	 * @param position the position to check
	 * @return true if the position is recorded for the word in the source
	 */
	public boolean hasPosition(String word, String source, int position) {
		TreeMap<String, TreeSet<Integer>> sources = index.get(word);
		if (sources != null) {
			TreeSet<Integer> positions = sources.get(source);
			if (positions != null) {
				return positions.contains(position);
			}
		}
		return false;
	}

	/**
	 * Returns an unmodifiable view of the source word counts.
	 *
	 * @return an unmodifiable sorted map containing the source identifiers and
	 *   their word counts
	 */
	public SortedMap<String, Integer> viewCounts() {
		return Collections.unmodifiableSortedMap(counts);
	}

	/**
	 * Returns an unmodifiable view of the words stored in the index.
	 *
	 * @return an unmodifiable sorted set of words
	 */
	public SortedSet<String> viewWords() {
		return Collections.unmodifiableSortedSet(index.navigableKeySet());
	}

	/**
	 * Returns an unmodifiable view of the sources in which the given word appears.
	 *
	 * @param word the word to lookup
	 * @return an unmodifiable sorted set of source identifiers, or an empty sorted
	 *   set if the word is not present
	 */
	public SortedSet<String> viewSources(String word) {
		TreeMap<String, TreeSet<Integer>> sources = index.get(word);
		return sources == null ? Collections.emptySortedSet()
				: Collections.unmodifiableSortedSet(sources.navigableKeySet());
	}

	/**
	 * Returns an unmodifiable view of the positions for the given word in the
	 * specified source.
	 *
	 * @param word the word to lookup
	 * @param source the source in which to lookup the word
	 * @return an unmodifiable sorted set of positions, or an empty sorted set if
	 *   not present
	 */
	public SortedSet<Integer> viewPositions(String word, String source) {
		TreeMap<String, TreeSet<Integer>> sources = index.get(word);
		if (sources != null) {
			TreeSet<Integer> positions = sources.get(source);
			if (positions != null) {
				return Collections.unmodifiableSortedSet(positions);
			}
		}
		return Collections.emptySortedSet();
	}

	/**
	 * Returns a string representation of the inverted index and word counts.
	 *
	 * @return a formatted string containing the index and counts
	 */
	@Override
	public String toString() {
		return "Counts:\n" + countstoJson() + "\nIndex:\n" + indexToJson();
	}

	/**
	 * Returns a JSON-formatted string representation of the source word counts.
	 *
	 * @return a JSON string representing the source word counts
	 */
	public String countstoJson() {
		return JsonWriter.writeObject(counts);
	}

	/**
	 * Writes the source word counts to a JSON file.
	 *
	 * @param path the path to the output JSON file
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	public void countsToJson(Path path) throws IOException {
		logger.info("Writing counts JSON to file: {}", path);
		JsonWriter.writeObject(counts, path);
		logger.info("Successfully wrote counts JSON to file: {}", path);
	}

	/**
	 * Writes the source word counts with the provided writer.
	 *
	 * @param writer the provided writer
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	public void countsToJson(Writer writer) throws IOException {
		JsonWriter.writeObject(counts, writer);
	}

	/**
	 * Returns a JSON-formatted string representation of the inverted index.
	 *
	 * @return a JSON string representing the inverted index
	 */
	public String indexToJson() {
		return JsonWriter.writeNestedObjectArrays(index);
	}

	/**
	 * Writes the inverted index to a JSON file.
	 *
	 * @param path the path to the output JSON file
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	public void indexToJson(Path path) throws IOException {
		logger.info("Writing index JSON to file: {}", path);
		JsonWriter.writeNestedObjectArrays(index, path);
		logger.info("Successfully wrote index JSON to file: {}", path);
	}

	/**
	 * Writes the inverted index with the provided writer.
	 *
	 * @param writer the provided writer
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	public void indexToJson(Writer writer) throws IOException {
		JsonWriter.writeNestedObjectArrays(index, writer);
	}

	/**
	 * Performs an partial search on the given query terms.
	 *
	 * @param query the collection of words to search for
	 * @return a sorted list of search results
	 */
	public List<SearchResult> searchPartial(Set<String> query) {
		Map<String, SearchResult> results = new HashMap<>();
		List<SearchResult> sortedResults = new ArrayList<>();

		for (String queryWord : query) {
			// CITE: https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html
			for (var indexWord : index.tailMap(queryWord).entrySet()) {
				if (!indexWord.getKey().startsWith(queryWord)) {
					break;
				}
				updateSearchResults(results, sortedResults, indexWord.getValue().entrySet());
			}
		}
		Collections.sort(sortedResults);
		return sortedResults;
	}

	/**
	 * Performs an exact search on the given query terms.
	 *
	 * @param query the collection of words to search for
	 * @return a sorted list of search results
	 */
	public List<SearchResult> searchExact(Set<String> query) {
		Map<String, SearchResult> results = new HashMap<>();
		List<SearchResult> sortedResults = new ArrayList<>();

		for (String word : query) {
			TreeMap<String, TreeSet<Integer>> sources = index.get(word);
			if (sources != null) {
				updateSearchResults(results, sortedResults, sources.entrySet());
			}
		}
		Collections.sort(sortedResults);
		return sortedResults;
	}

	/**
	 * Updates the search results by adding new entries and updating match counts.
	 *
	 * @param results the map storing search results
	 * @param sortedResults the list of sorted search results
	 * @param sources the set of source entries containing word counts
	 */
	private void updateSearchResults(Map<String, SearchResult> results, List<SearchResult> sortedResults,
			Set<Entry<String, TreeSet<Integer>>> sources) {
		for (var source : sources) {
			results.computeIfAbsent(source.getKey(), k -> {
				var result = new SearchResult(k);
				sortedResults.add(result);
				return result;
			}).incrementMatchCount(source.getValue().size());
		}
	}

	/**
	 * Represents a single search result with match count and relevance score.
	 */
	public class SearchResult implements Comparable<SearchResult> {
		/** The source associated with the search result. */
		private final String source;

		/** The number of matches found in the source. */
		private int matchCount;

		/** The total number of word occurrences found in the source */
		private final int totalWords;

		/** The relevance score of the result. */
		private double score;

		/**
		 * Constructs a search result with a source, match count, and total words for
		 * scoring.
		 *
		 * @param source the source document
		 */
		public SearchResult(String source) {
			this.source = source;
			this.matchCount = 0;
			this.totalWords = counts.get(source);
			this.score = 0.0;
		}

		/**
		 * Compares this search result to another based on score, match count, and
		 * source.
		 *
		 * @param o the other search result to compare to
		 * @return a negative integer, zero, or a positive integer as this result is
		 *   less than, equal to, or greater than the specified result
		 */
		@Override
		public int compareTo(SearchResult o) {
			return (o.score != this.score) ? Double.compare(o.score, this.score)
					: (o.matchCount != this.matchCount) ? Integer.compare(o.matchCount, this.matchCount)
					: this.source.compareToIgnoreCase(o.source);
		}

		/**
		 * Checks if this SearchResult is equal to another object. Two SearchResult
		 * objects are considered equal if they have the same score, matchCount, and
		 * source (case-insensitive).
		 *
		 * @param obj the object to compare
		 * @return true if this object is equal to the specified object, false otherwise
		 */
		@Override
		public boolean equals(Object obj) {
			// CITE: Derived from ChatGPT Prompt: "Can I replace Object with my class type
			// in an overridden equals method in Java"
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			SearchResult result = (SearchResult) obj;
			return this.score == result.getScore() && this.matchCount == result.getMatchCount()
					&& source.equalsIgnoreCase(result.getSource());
		}

		/**
		 * Returns the hash code of this SearchResult. The hash code is computed based
		 * on the score, matchCount, and source values.
		 *
		 * @return the hash code value for this object
		 */
		@Override
		public int hashCode() {
			return Objects.hash(score, matchCount, source.toLowerCase());
		}

		/**
		 * Returns the source associated with the search result.
		 *
		 * @return the source document
		 */
		public String getSource() {
			return source;
		}

		/**
		 * Returns the number of matches found in the source.
		 *
		 * @return the match count
		 */
		public int getMatchCount() {
			return matchCount;
		}

		/**
		 * Returns the relevance score of the result.
		 *
		 * @return the relevance score
		 */
		public double getScore() {
			return score;
		}

		/**
		 * Sets the match count for this search result and updates the relevance score.
		 *
		 * @param matchCount the new match count to set
		 */
		private void setMatchCount(int matchCount) {
			this.matchCount = matchCount;
			this.score = (double) matchCount / totalWords;
		}

		/**
		 * Increments the match count by a specified amount and updates the relevance
		 * score.
		 *
		 * @param matchCount the number of additional matches to add
		 */
		private void incrementMatchCount(int matchCount) {
			setMatchCount(this.matchCount + matchCount);
		}

		/**
		 * Returns a JSON-formatted string representation of the search result.
		 *
		 * @return a JSON string representation of the search result
		 */
		@Override
		public String toString() {
			return JsonWriter.writeSearchResult(this);
		}
	}
}
