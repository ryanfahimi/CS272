package edu.usfca.cs272.index;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import threads.WorkQueue;

/**
 * ConcurrentTextFileIndexer indexes text files concurrently using a thread-safe
 * inverted index and a work queue.
 */
public class ConcurrentTextFileIndexer extends TextFileIndexer {
	/**
	 * Logger for logging events in ConcurrentTextFileIndexer class.
	 */
	private static final Logger logger = LogManager.getLogger(ConcurrentTextFileIndexer.class);

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
	 * Constructs a ConcurrentTextFileIndexer with the provided inverted index and
	 * work queue.
	 *
	 * @param invertedIndex the thread-safe inverted index to update
	 * @param tasks the work queue for executing indexing tasks
	 */
	public ConcurrentTextFileIndexer(ThreadSafeInvertedIndex invertedIndex, WorkQueue tasks) {
		super(invertedIndex);
		this.invertedIndex = invertedIndex;
		this.tasks = tasks;
		logger.debug("Initialized ConcurrentTextFileIndexer with provided inverted index and work queue.");
	}

	/**
	 * Indexes the given path. If the path is a directory, all text files within are
	 * indexed; otherwise, it is index directly as a regular file.
	 *
	 * @param path the file or directory to index
	 * @throws IOException if an I/O error occurs when indexing a file or directory
	 */
	@Override
	public void indexPath(Path path) throws IOException {
		super.indexPath(path);
		tasks.finish();
	}

	/**
	 * Submits a task to index the specified file.
	 *
	 * @param file the file to index
	 * @throws IOException if an I/O error occurs when submitting the indexing task
	 */
	@Override
	public void indexFile(Path file) throws IOException {
		tasks.execute(new Task(file));
	}

	/**
	 * Private inner class representing a task for indexing a file.
	 */
	private class Task implements Runnable {
		/**
		 * The file to be indexed by this task.
		 */
		private final Path file;

		/**
		 * Constructs a new Task for indexing the given file.
		 *
		 * @param file the file to index
		 */
		public Task(Path file) {
			this.file = file;
		}

		/**
		 * Executes the file indexing task. Reads the file, indexes its content using a
		 * local inverted index, and merges the result into the shared thread-safe
		 * inverted index.
		 */
		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				TextFileIndexer.indexFile(file, local, new SnowballStemmer(ENGLISH));
				invertedIndex.addAll(local);
			}
			catch (IOException e) {
				logger.error("Error indexing file: " + file, e);
				throw new UncheckedIOException(e);
			}
		}
	}

}
