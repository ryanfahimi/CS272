package edu.usfca.cs272.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;

/**
 * A simple class for sorting files.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class FileSorter {
	/**
	 * A simple comparable class for storing and comparing files. Should implement
	 * the {@link Comparable} interface to allow for comparison between
	 * {@link FileMetadata} objects by their {@link FileMetadata#path} paths.
	 *
	 * @see Comparable
	 * @see Path#compareTo(Path)
	 * @see FileMetadata#path
	 */
	public static class FileMetadata implements Comparable<FileMetadata> {
		/** The normalized file path. */
		private final Path path;

		/** The file name. */
		private final String name;

		/** The file size. */
		private final long size;

		/** The last modified date of the file. */
		private final FileTime date;

		/**
		 * Initializes a file and its metadata.
		 *
		 * @param path the text file source
		 * @throws IOException if I/O error occurs
		 */
		public FileMetadata(Path path) throws IOException {
			this.path = path.normalize();
			this.name = path.getFileName().toString();
			this.size = Files.isRegularFile(path) ? Files.size(path) : -1;
			this.date = Files.isRegularFile(path) ? Files.getLastModifiedTime(path) : FileTime.fromMillis(0);
		}

		/**
		 * Returns a string representation of the file metadata.
		 *
		 * @return a formatted string containing the file path (or name), size, and last
		 *   modified date
		 */
		@Override
		public String toString() {
			String output = this.path.getNameCount() < 1 ? this.name : path.toString();
			return String.format("%s (%s bytes, modified %s)", output, size, date);
		}

		/**
		 * Compares {@link FileMetadata} objects by their {@link Path} source.
		 *
		 * @param other the other object to compare against
		 * @return a negative integer, zero, or a positive integer as the first argument
		 *   is less than, equal to, or greater than the second.
		 *
		 * @see Path#compareTo(Path)
		 * @see Comparable#compareTo(Object)
		 */
		@Override
		public int compareTo(FileMetadata other) {
			return path.compareTo(other.path);
		}
	}

	/**
	 * A comparator that compares files by their last modified date, defined using a
	 * static nested class.
	 *
	 * @see FileMetadata#date
	 * @see FileTime#compareTo(FileTime)
	 */
	public static class DateComparator implements Comparator<FileMetadata> {

		/**
		 * Compares its two {@link FileMetadata} arguments for order based on the last
		 * modified date.
		 *
		 * @param o1 the first file metadata to be compared
		 * @param o2 the second file metadata to be compared
		 * @return a negative integer, zero, or a positive integer as the first
		 *   argument's date is less than, equal to, or greater than the second's
		 */
		@Override
		public int compare(FileMetadata o1, FileMetadata o2) {
			return o1.date.compareTo(o2.date);
		}
	}

	/**
	 * A comparator that compares files by their last modified date, defined using a
	 * static nested class.
	 *
	 * @see FileMetadata#date
	 * @see FileTime#compareTo(FileTime)
	 */
	public static final Comparator<FileMetadata> DATE_COMPARATOR = new DateComparator();

	/**
	 * A comparator that compares text files in case insensitive order by their
	 * name, defined using a non-static inner class.
	 *
	 * @see FileMetadata#name
	 * @see String#CASE_INSENSITIVE_ORDER
	 */
	public class NameComparator implements Comparator<FileMetadata> {
		/**
		 * Compares its two {@link FileMetadata} arguments for order based on the file
		 * name, ignoring case considerations.
		 *
		 * @param o1 the first file metadata to be compared
		 * @param o2 the second file metadata to be compared
		 * @return a negative integer, zero, or a positive integer as the first file
		 *   name is alphabetically less than, equal to, or greater than the second file
		 *   name
		 */
		@Override
		public int compare(FileMetadata o1, FileMetadata o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.name, o2.name);
		}
	}

	/**
	 * A comparator that compares text files in case insensitive order by their
	 * name, defined using a non-static inner class.
	 *
	 * @see FileMetadata#name
	 * @see String#CASE_INSENSITIVE_ORDER
	 */
	public static final Comparator<FileMetadata> NAME_COMPARATOR = new FileSorter().new NameComparator();

	/**
	 * A comparator that compares text files by their size with the largest sizes
	 * first (descending order), defined using a lambda expression.
	 *
	 * @see FileMetadata#size
	 * @see Long#compare(long, long)
	 */
	public static final Comparator<FileMetadata> SIZE_COMPARATOR = (o1, o2) -> Long.compare(o2.size, o1.size);;

	/**
	 * Returns a comparator created using an anonymous inner class that compares
	 * text files by the {@link #SIZE_COMPARATOR} if the sizes are not equal. If the
	 * sizes are equal, then compares using a {@link #NAME_COMPARATOR} instead. If
	 * the names are equal, then compares by the {@link FileMetadata} natural sort
	 * order (by its {@link Path} source).
	 *
	 * @see #SIZE_COMPARATOR
	 * @see #NAME_COMPARATOR
	 * @see FileMetadata#compareTo(FileMetadata)
	 *
	 * @return a comparator created using an anonymous inner class
	 */
	public static Comparator<FileMetadata> getNested() {
		return new Comparator<FileMetadata>() {
			/**
			 * Compares two {@link FileMetadata} objects using size, name, and natural
			 * order.
			 *
			 * @param o1 the first file metadata to be compared
			 * @param o2 the second file metadata to be compared
			 * @return a negative integer, zero, or a positive integer as defined by the
			 *   comparison logic
			 */
			@Override
			public int compare(FileMetadata o1, FileMetadata o2) {
				int cmp = SIZE_COMPARATOR.compare(o1, o2);
				if (cmp != 0) {
					return cmp;
				}
				cmp = NAME_COMPARATOR.compare(o1, o2);
				if (cmp != 0) {
					return cmp;
				}
				return o1.compareTo(o2);
			}
		};
	}

	/** Creates a new instance of this class. */
	public FileSorter() {
	}
}
