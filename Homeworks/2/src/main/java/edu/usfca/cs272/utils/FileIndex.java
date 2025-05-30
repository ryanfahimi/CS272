package edu.usfca.cs272.utils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A special type of {@link ForwardIndex} that indexes the UNIQUE words that
 * were found in a text file (represented by {@link Path} objects).
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class FileIndex implements ForwardIndex<Path> {
	/**
	 * The file index map storing text files and unique words.
	 */
	private final Map<Path, Set<String>> index;

	/**
	 * Constructs a new FileIndex with an empty index.
	 */
	public FileIndex() {
		index = new HashMap<>();
	}

	@Override
	/**
	 * Adds a word to the index for the given file location.
	 *
	 * @param location the file path
	 * @param word the word to add
	 */
	public void add(Path location, String word) {
		index.computeIfAbsent(location, k -> new HashSet<>()).add(word);

	}

	@Override
	/**
	 * Returns the total number of indexed file locations.
	 *
	 * @return the size of the index
	 */
	public int size() {
		return index.size();
	}

	@Override
	/**
	 * Returns the number of unique words stored for the given file location.
	 *
	 * @param location the file path
	 * @return the number of unique words for the file
	 */
	public int size(Path location) {
		// CITE: Derived from ChatGPT Prompt: "What's the easiest way to make an empty
		// list in Java"
		return index.getOrDefault(location, Collections.emptySet()).size();
	}

	@Override
	/**
	 * Checks if a given file location is indexed.
	 *
	 * @param location the file path
	 * @return True if the location is indexed, False otherwise
	 */
	public boolean has(Path location) {
		return index.containsKey(location);
	}

	@Override
	public boolean has(Path location, String word) {
		/**
		 * Checks if a specific word exists in the given file location.
		 *
		 * @param location the file path
		 * @param word the word to check
		 * @return True if the word exists in the indexed file, False otherwise
		 */
		return index.getOrDefault(location, Collections.emptySet()).contains(word);
	}

	@Override
	public Collection<Path> view() {
		/**
		 * Returns an unmodifiable view of all indexed file locations.
		 *
		 * @return a collection of file paths
		 */
		return Collections.unmodifiableCollection(index.keySet());
	}

	@Override
	/**
	 * Returns an unmodifiable view of the unique words stored for a given file
	 * location.
	 *
	 * @param location the file path
	 * @return a collection of unique words for the file
	 */
	public Collection<String> view(Path location) {
		return Collections.unmodifiableCollection(index.getOrDefault(location, Collections.emptySet()));
	}

	@Override
	/**
	 * Returns a string representation of the file index.
	 *
	 * @return a string representation of the index
	 */
	public String toString() {
		return index.entrySet()
				.stream()
				.map(entry -> entry.getKey() + " -> " + entry.getValue())
				.collect(Collectors.joining(System.lineSeparator()));
	}
}
