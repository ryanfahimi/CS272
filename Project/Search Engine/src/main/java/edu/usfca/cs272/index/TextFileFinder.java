package edu.usfca.cs272.index;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for finding all text files in a directory using lambda
 * expressions and streams.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class TextFileFinder {
	/**
	 * A lambda expression that returns true if the path is a file that ends in a
	 * .txt or .text extension (case-insensitive). Useful for
	 * {@link Files#walk(Path, FileVisitOption...)}.
	 *
	 * @see Files#isRegularFile(Path, java.nio.file.LinkOption...)
	 * @see Path#getFileName()
	 *
	 * @see String#toLowerCase()
	 * @see String#compareToIgnoreCase(String)
	 * @see String#endsWith(String)
	 *
	 * @see Files#walk(Path, FileVisitOption...)
	 */
	public static final Predicate<Path> IS_TEXT = path -> {
		if (!Files.isRegularFile(path)) {
			return false;
		}
		String name = path.getFileName().toString().toLowerCase();
		return name.endsWith(".txt") || name.endsWith(".text");
	};

	/**
	 * Returns a stream of all paths within the starting path that match the
	 * provided filter. Follows any symbolic links encountered.
	 *
	 * @param start the initial path to start with
	 * @param keep function that determines whether to keep a path
	 * @return a stream of paths
	 * @throws IOException if an IO error occurs
	 *
	 * @see FileVisitOption#FOLLOW_LINKS
	 * @see Files#walk(Path, FileVisitOption...)
	 */
	public static Stream<Path> find(Path start, Predicate<Path> keep) throws IOException {
		return Files.walk(start, FileVisitOption.FOLLOW_LINKS).filter(keep);
	};

	/**
	 * Returns a stream of text files, following any symbolic links encountered.
	 *
	 * @param start the initial path to start with
	 * @return a stream of text files
	 * @throws IOException if an IO error occurs
	 *
	 * @see #find(Path, Predicate)
	 * @see #IS_TEXT
	 */
	public static Stream<Path> findText(Path start) throws IOException {
		return find(start, IS_TEXT);
	}

	/**
	 * Returns a list of text files using streams.
	 *
	 * @param start the initial path to search
	 * @return list of text files
	 * @throws IOException if an IO error occurs
	 *
	 * @see #findText(Path)
	 * @see Stream#toList()
	 */
	public static List<Path> listText(Path start) throws IOException {
		try (Stream<Path> stream = findText(start)) {
			return stream.toList();
		}
	}

	/**
	 * Returns a list of text files using streams if the provided path is a valid
	 * directory, otherwise returns a list containing only the default path.
	 *
	 * @param start the starting path
	 * @param defaultPath the default to include if the starting path is not a valid
	 *   directory
	 * @return a list of paths
	 * @throws IOException if an IO error occurs
	 *
	 * @see #listText(Path)
	 * @see List#of()
	 * @see Files#isDirectory(Path, java.nio.file.LinkOption...)
	 */
	public static List<Path> listText(Path start, Path defaultPath) throws IOException {
		if (Files.isDirectory(start)) {
			return listText(start);
		}
		return List.of(defaultPath);
	}

	/**
	 * Returns a set of text files using streams.
	 *
	 * @param start the initial path to search
	 * @return set of text files
	 * @throws IOException if an IO error occurs
	 */
	public static Set<Path> uniqueText(Path start) throws IOException {
		try (Stream<Path> stream = findText(start)) {
			return stream.collect(Collectors.toSet());
		}
	}

	/** Prevent instantiating this class of static methods. */
	private TextFileFinder() {
	}
}
