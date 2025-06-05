package edu.usfca.cs272.index;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usfca.cs272.threads.MultiReaderLock;

/**
 * Represents an inverted index that maps words to their source locations and
 * positions. Provides methods for interacting with data and performing both
 * exact and partial searches in a thread-safe manner.
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	/**
	 * Logger for logging events in ThreadSafeInvertedIndex class.
	 */
	private static final Logger logger = LogManager.getLogger(ThreadSafeInvertedIndex.class);

	/** The lock used to protect concurrent access to the underlying set. */
	private final MultiReaderLock lock;

	/**
	 * Constructs a new ThreadSafeInvertedIndex with a MultiReaderLock.
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new MultiReaderLock();
		logger.debug("ThreadSafeInvertedIndex created with a new MultiReaderLock.");
	}

	/**
	 * Acquires the read lock, performs the specified read operation, and then
	 * releases the lock in a thread-safe manner.
	 *
	 * @param <T> the return type of the read operation
	 * @param operation the read operation to perform
	 * @return the result of the read operation
	 */
	private <T> T readOperation(Supplier<T> operation) {
		lock.readLock().lock();
		try {
			return operation.get();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Acquires the write lock, performs the specified write operation, and then
	 * releases the lock in a thread-safe manner.
	 *
	 * @param operation the write operation to perform
	 */
	private void writeOperation(Runnable operation) {
		lock.writeLock().lock();
		try {
			operation.run();
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Adds a list of words from a source to the inverted index and updates the word
	 * count in a thread safe manner.
	 *
	 * @param words the list of words to add to the index
	 * @param source the source where the words were found
	 */
	@Override
	public void add(List<String> words, String source) {
		writeOperation(() -> super.add(words, source));
	}

	/**
	 * Adds a single word occurrence to the inverted index and updates the word
	 * count while iterating through the words in a source in a thread-safe manner.
	 *
	 * @param word the word to add
	 * @param source the source where the word was found
	 * @param position the position of the word in the source
	 */
	@Override
	public void add(String word, String source, int position) {
		writeOperation(() -> super.add(word, source, position));
	}

	/**
	 * Merges the contents of another InvertedIndex into this index by combining
	 * their source word counts and index mappings in a thread-safe manner.
	 *
	 * @param other the InvertedIndex whose entries are to be merged into this index
	 */
	@Override
	public void addAll(InvertedIndex other) {
		writeOperation(() -> super.addAll(other));
	}

	/**
	 * Returns the number of sources stored in the counts map in a thread-safe
	 * manner.
	 *
	 * @return the number of sources with word counts
	 */
	@Override
	public int sizeCounts() {
		return readOperation(super::sizeCounts);
	}

	/**
	 * Returns the number of distinct words stored in the index in a thread-safe
	 * manner.
	 *
	 * @return the number of words in the index
	 */
	@Override
	public int sizeWords() {
		return readOperation(super::sizeWords);
	}

	/**
	 * Returns the total number of sources containing the given word in a
	 * thread-safe manner.
	 *
	 * @param word the word to lookup
	 * @return the total number of sources containing the word, or 0 if not present
	 */
	@Override
	public int sizeSources(String word) {
		return readOperation(() -> super.sizeSources(word));
	}

	/**
	 * Returns the number of occurrences for the given word in the specified source
	 * in a thread-safe manner.
	 *
	 * @param word the word to lookup
	 * @param source the source in which to lookup the word
	 * @return the number of positions recorded for the word in the source, or 0 if
	 *   not present
	 */
	@Override
	public int sizePositions(String word, String source) {
		return readOperation(() -> super.sizePositions(word, source));
	}

	/**
	 * Determines whether the counts map contains the given source in a thread-safe
	 * manner.
	 *
	 * @param source the source to lookup
	 * @return true if the source exists in the counts map
	 */
	@Override
	public boolean hasCounts(String source) {
		return readOperation(() -> super.hasCounts(source));
	}

	/**
	 * Determines whether the index contains the given word in a thread-safe manner.
	 *
	 * @param word the word to lookup
	 * @return true if the word exists in the index
	 */
	@Override
	public boolean hasWord(String word) {
		return readOperation(() -> super.hasWord(word));
	}

	/**
	 * Determines whether the given word appears in the specified source in a
	 * thread-safe manner.
	 *
	 * @param word the word to lookup
	 * @param source the source in which to lookup the word
	 * @return true if the word appears in the source
	 */
	@Override
	public boolean hasSource(String word, String source) {
		return readOperation(() -> super.hasSource(word, source));
	}

	/**
	 * Determines whether the given word appears in the specified source at the
	 * given position in a thread-safe manner.
	 *
	 * @param word the word to lookup
	 * @param source the source in which to lookup the word
	 * @param position the position to check
	 * @return true if the position is recorded for the word in the source
	 */
	@Override
	public boolean hasPosition(String word, String source, int position) {
		return readOperation(() -> super.hasPosition(word, source, position));
	}

	/**
	 * Returns an unmodifiable view of the sources stored in the counts map in a
	 * thread-safe manner.
	 *
	 * @return an unmodifiable collection of source identifiers
	 */
	@Override
	public SortedMap<String, Integer> viewCounts() {
		return readOperation(super::viewCounts);
	}

	/**
	 * Returns an unmodifiable view of the words stored in the index in a
	 * thread-safe manner.
	 *
	 * @return an unmodifiable sorted set of words
	 */
	@Override
	public SortedSet<String> viewWords() {
		return readOperation(super::viewWords);
	}

	/**
	 * Returns an unmodifiable view of the sources in which the given word appears
	 * in a thread-safe manner.
	 *
	 * @param word the word to lookup
	 * @return an unmodifiable sorted set of source identifiers, or an empty sorted
	 *   set if the word is not present
	 */
	@Override
	public SortedSet<String> viewSources(String word) {
		return readOperation(() -> super.viewSources(word));
	}

	/**
	 * Returns an unmodifiable view of the positions for the given word in the
	 * specified source in a thread-safe manner.
	 *
	 * @param word the word to lookup
	 * @param source the source in which to lookup the word
	 * @return an unmodifiable sorted set of positions, or an empty sorted set if
	 *   not present
	 */
	@Override
	public SortedSet<Integer> viewPositions(String word, String source) {
		return readOperation(() -> super.viewPositions(word, source));
	}

	/**
	 * Returns a string representation of the inverted index and word counts in a
	 * thread-safe manner.
	 *
	 * @return a formatted string containing the index and counts
	 */
	@Override
	public String toString() {
		return readOperation(super::toString);
	}

	/**
	 * Returns a JSON-formatted string representation of the source word counts in a
	 * thread-safe manner.
	 *
	 * @return a JSON string representing the source word counts
	 */
	@Override
	public String countstoJson() {
		return readOperation(super::countstoJson);
	}

	/**
	 * Writes the source word counts to a JSON file in a thread-safe manner.
	 *
	 * @param path the path to the output JSON file
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	@Override
	public void countsToJson(Path path) throws IOException {
		lock.readLock().lock();

		try {
			super.countsToJson(path);
			logger.debug("Successfully wrote source word counts to JSON file: {}", path);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Writes the source word counts with the provided writer in a thread-safe
	 * manner.
	 *
	 * @param writer the provided writer
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	@Override
	public void countsToJson(Writer writer) throws IOException {
		lock.readLock().lock();

		try {
			super.countsToJson(writer);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns a JSON-formatted string representation of the inverted index in a
	 * thread-safe manner.
	 *
	 * @return a JSON string representing the inverted index
	 */
	@Override
	public String indexToJson() {
		return readOperation(super::indexToJson);
	}

	/**
	 * Writes the inverted index to a JSON file in a thread-safe manner.
	 *
	 * @param path the path to the output JSON file
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	@Override
	public void indexToJson(Path path) throws IOException {
		lock.readLock().lock();

		try {
			super.indexToJson(path);
			logger.debug("Successfully wrote inverted index to JSON file: {}", path);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Writes the inverted index with the provided writer in a thread-safe manner.
	 *
	 * @param writer the provided writer
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	@Override
	public void indexToJson(Writer writer) throws IOException {
		lock.readLock().lock();

		try {
			super.indexToJson(writer);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Performs an partial search on the given query terms in a thread-safe manner.
	 *
	 * @param query the collection of words to search for
	 * @return a sorted list of search results
	 */
	@Override
	public List<SearchResult> searchPartial(Set<String> query) {
		return readOperation(() -> super.searchPartial(query));
	}

	/**
	 * Performs an exact search on the given query terms in a thread-safe manner.
	 *
	 * @param query the collection of words to search for
	 * @return a sorted list of search results
	 */
	@Override
	public List<SearchResult> searchExact(Set<String> query) {
		return readOperation(() -> super.searchExact(query));
	}
}
