package edu.usfca.cs272.index;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * A class that indexes a given text file or directory containing text files. It
 * lists the stems in each file and updates the inverted index accordingly.
 */
public class TextFileIndexer {
	/**
	 * The inverted index to store word occurrences across multiple files.
	 */
	private final InvertedIndex invertedIndex;
	/**
	 * Stemmer used to index files.
	 */
	private final Stemmer stemmer;

	/**
	 * Constructs a TextFileIndexer with the InvertedIndex.
	 *
	 * @param invertedIndex the inverted index to update
	 */
	public TextFileIndexer(InvertedIndex invertedIndex) {
		this.invertedIndex = invertedIndex;
		this.stemmer = new SnowballStemmer(ENGLISH);
	}

	/**
	 * Indexes the given path. If the path is a directory, all text files within are
	 * indexed; otherwise, it is index directly as a regular file.
	 *
	 * @param path the file or directory to index
	 * @throws IOException if an I/O error occurs when indexing a file or directory
	 */
	public void indexPath(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			indexDirectory(path);
		}
		else {
			indexFile(path);
		}
	}

	/**
	 * Lists the stems in a file and updates the inverted index.
	 *
	 * @param file the file to index
	 * @throws IOException if an error occurs while reading the file
	 */
	public void indexFile(Path file) throws IOException {
		indexFile(file, invertedIndex, stemmer);
	}

	/**
	 * Lists the stems in a file and updates the inverted index.
	 *
	 * @param file the file to index
	 * @param invertedIndex the index to update
	 * @param stemmer the stemmer to use
	 * 
	 * @throws IOException if an error occurs while reading the file
	 */
	public static void indexFile(Path file, InvertedIndex invertedIndex, Stemmer stemmer) throws IOException {
		int position = 1;
		try (BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isBlank()) {
					for (String word : FileStemmer.parse(line)) {
						invertedIndex.add(stemmer.stem(word).toString(), file.toString(), position);
						position++;
					}
				}
			}
		}
	}

	/**
	 * Finds all text files in the directory and indexes each one.
	 *
	 * @param directory the directory to index
	 * @throws IOException if an error occurs while walking the directory or reading
	 *   a file
	 */
	public void indexDirectory(Path directory) throws IOException {
		Set<Path> textFiles = TextFileFinder.uniqueText(directory);
		for (Path file : textFiles) {
			indexFile(file);
		}
	}
}
