package edu.usfca.cs272.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.usfca.cs272.index.InvertedIndex;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class JsonWriter {
	/**
	 * Represents the standard indentation.
	 */
	private static final String INDENTATION = "  ";

	/**
	 * Represents a newline character.
	 */
	private static final String NEW_LINE = System.lineSeparator();

	/**
	 * Represents a comma followed by a newline character.
	 */
	private static final String COMMA_NEW_LINE = "," + NEW_LINE;

	/**
	 * Represents the left square bracket "[".
	 */
	private static final String LEFT_BRACKET = "[";

	/**
	 * Represents the right square bracket "]".
	 */
	private static final String RIGHT_BRACKET = "]";

	/**
	 * Represents the left curly brace "{".
	 */
	private static final String LEFT_CURLY = "{";

	/**
	 * Represents the right curly brace "}".
	 */
	private static final String RIGHT_CURLY = "}";

	/**
	 * Represents a double quote character (").
	 */
	private static final String QUOTE = "\"";

	/**
	 * Represents a colon followed by a space ": ".
	 */
	private static final String COLON_SPACE = ": ";

	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write(INDENTATION);
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(QUOTE);
		writer.write(element);
		writer.write(QUOTE);
	}

	/**
	 * Helper that creates a BufferedWriter for the given path and passes it to the
	 * consumer.
	 *
	 * @param path the file path to write to
	 * @param consumer the lambda that uses the writer
	 * @throws IOException if an IO error occurs
	 */
	private static void withBufferedWriter(Path path, IOThrowingConsumer<BufferedWriter> consumer) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			consumer.accept(writer);
		}
	}

	/**
	 * Helper that creates a StringWriter, passes it to the function, and returns
	 * the result. Returns null if an IOException occurs.
	 *
	 * @param function the lambda that uses the writer
	 * @return the result of applying the lambda
	 */
	private static String withStringWriter(IOThrowingFunction<StringWriter, String> function) {
		StringWriter writer = new StringWriter();
		try {
			return function.apply(writer);
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * A generic helper for writing a collection of elements with a common JSON
	 * format.
	 *
	 * @param <T> the type of elements in the collection
	 * @param elements the collection to write
	 * @param writer the writer to use
	 * @param indent the current indentation level for the closing bracket
	 * @param elementWriter a lambda that writes a single element
	 * @throws IOException if an IO error occurs
	 */
	private static <T> void writeArray(Collection<T> elements, Writer writer, int indent,
			IOThrowingConsumer<T> elementWriter) throws IOException {
		writer.write(LEFT_BRACKET + NEW_LINE);
		var it = elements.iterator();
		if (it.hasNext()) {
			elementWriter.accept(it.next());
			while (it.hasNext()) {
				writer.write(COMMA_NEW_LINE);
				elementWriter.accept(it.next());
			}
			writer.write(NEW_LINE);
		}
		writeIndent(writer, indent);
		writer.write(RIGHT_BRACKET);
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		writeArray(elements, writer, indent, element -> writeIndent(element.toString(), writer, indent + 1));
	}

	/**
	 * Writes the collection as a pretty JSON array using the provided writer and no
	 * initial indentation.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer) throws IOException {
		writeArray(elements, writer, 0);
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		withBufferedWriter(path, writer -> writeArray(elements, writer));
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		return withStringWriter(writer -> { writeArray(elements, writer); return writer.toString(); });
	}

	/**
	 * A generic helper for writing a map's entries with a common JSON format.
	 *
	 * @param <V> the type of values in the map
	 * @param map the map to write
	 * @param writer the writer to use
	 * @param indent the current indentation level for the closing bracket
	 * @param valueWriter a lambda that writes a single map value
	 * @throws IOException if an IO error occurs
	 */
	private static <V> void writeObject(Map<String, V> map, Writer writer, int indent, IOThrowingConsumer<V> valueWriter)
			throws IOException {
		writer.write(LEFT_CURLY + NEW_LINE);
		var it = map.entrySet().iterator();
		if (it.hasNext()) {
			writeEntry(it.next(), writer, indent + 1, valueWriter);
			while (it.hasNext()) {
				writer.write(COMMA_NEW_LINE);
				writeEntry(it.next(), writer, indent + 1, valueWriter);
			}
			writer.write(NEW_LINE);
		}
		writeIndent(writer, indent);
		writer.write(RIGHT_CURLY);
	}

	/**
	 * A generic helper for writing a single map entry with a common JSON format.
	 * 
	 * @param <V> the type of values in the map
	 * @param entry the entry to write
	 * @param writer the writer to use
	 * @param indent the current indentation level for the map key
	 * @param valueWriter a lambda that writes a single map value
	 * @throws IOException if an IO error occurs
	 */
	private static <V> void writeEntry(Entry<String, V> entry, Writer writer, int indent,
			IOThrowingConsumer<V> valueWriter) throws IOException {
		writeQuote(entry.getKey(), writer, indent);
		writer.write(COLON_SPACE);
		valueWriter.accept(entry.getValue());
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent) throws IOException {
		writeObject(elements, writer, indent, value -> writer.write(value.toString()));
	}

	/**
	 * Writes the map as a pretty JSON object using the provided writer and no
	 * initial indentation.
	 *
	 * @param elements the map to write
	 * @param writer the writer to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer) throws IOException {
		writeObject(elements, writer, 0);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		withBufferedWriter(path, writer -> writeObject(elements, writer));
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		return withStringWriter(writer -> { writeObject(elements, writer); return writer.toString(); });
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writeObject(elements, writer, indent, value -> writeArray(value, writer, indent + 1));
	}

	/**
	 * Writes the map whose values are collections as a pretty JSON object with
	 * nested arrays using the provided writer and no initial indentation.
	 *
	 * @param elements the map to write
	 * @param writer the writer to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer)
			throws IOException {
		writeObjectArrays(elements, writer, 0);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		withBufferedWriter(path, writer -> writeObjectArrays(elements, writer));
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
		return withStringWriter(writer -> { writeObjectArrays(elements, writer); return writer.toString(); });
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write(LEFT_BRACKET + NEW_LINE);
		writeArray(elements, writer, indent, element -> {
			writeIndent(writer, indent + 1);
			writeObject(element, writer, indent + 1);
		});
	}

	/**
	 * Writes the collection whose elements are maps as a pretty JSON array with
	 * nested objects using the provided writer and no initial indentation.
	 *
	 * @param elements the collection to write
	 * @param writer the writer to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer)
			throws IOException {
		writeArrayObjects(elements, writer, 0);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		withBufferedWriter(path, writer -> writeArrayObjects(elements, writer));
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		return withStringWriter(writer -> { writeArrayObjects(elements, writer); return writer.toString(); });
	}

	/**
	 * Writes the elements as a pretty JSON object with nested object arrays. The
	 * generic notation used allows this method to be used for any type of map where
	 * keys are String and values are another map containing nested collections of
	 * number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeNestedObjectArrays(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements, Writer writer, int indent)
			throws IOException {
		writeObject(elements, writer, indent, value -> writeObjectArrays(value, writer, indent + 1));
	}

	/**
	 * Writes the map whose values are maps of collections as a pretty JSON object
	 * with nested object arrays using the provided writer and no initial
	 * indentation.
	 *
	 * @param elements the map to write
	 * @param writer the writer to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeNestedObjectArrays(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements, Writer writer)
			throws IOException {
		writeNestedObjectArrays(elements, writer, 0);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested object arrays to a
	 * file. The generic notation used allows this method to be used for any type of
	 * map where keys are String and values are another map containing nested
	 * collections of number objects.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeNestedObjectArrays(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements, Path path)
			throws IOException {
		withBufferedWriter(path, writer -> writeNestedObjectArrays(elements, writer));
	}

	/**
	 * Returns the elements as a pretty JSON objects with nested objects with nested
	 * arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeNestedObjectArrays(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements) {
		return withStringWriter(writer -> { writeNestedObjectArrays(elements, writer); return writer.toString(); });
	}

	/**
	 * Writes a single search result in a structured JSON format.
	 *
	 * @param writer the writer to use
	 * @param indent the indentation level for formatting
	 * @param searchResult the search result to write
	 * @throws IOException if an IO error occurs
	 */
	private static void writeSearchResult(InvertedIndex.SearchResult searchResult, Writer writer, int indent)
			throws IOException {
		writeIndent(writer, indent);
		writer.write(LEFT_CURLY + NEW_LINE);
		writeQuote("count", writer, indent + 1);
		writer.write(COLON_SPACE + String.valueOf(searchResult.getMatchCount()) + COMMA_NEW_LINE);
		writeQuote("score", writer, indent + 1);
		writer.write(COLON_SPACE + String.format("%.8f", searchResult.getScore()) + COMMA_NEW_LINE);
		writeQuote("where", writer, indent + 1);
		writer.write(COLON_SPACE);
		writeQuote(searchResult.getSource(), writer, 0);
		writer.write(NEW_LINE);
		writeIndent(writer, indent);
		writer.write(RIGHT_CURLY);
	}

	/**
	 * Writes a single search result using the provided writer.
	 *
	 * @param result the result to write
	 * @param writer the writer to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResult(InvertedIndex.SearchResult result, Writer writer) throws IOException {
		writeSearchResult(result, writer, 0);
	}

	/**
	 * Writes the search result to a file in JSON format.
	 *
	 * @param result the search result to write
	 * @param path the file path to write to
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResult(InvertedIndex.SearchResult result, Path path) throws IOException {
		withBufferedWriter(path, writer -> writeSearchResult(result, writer));
	}

	/**
	 * Returns the search result as a JSON formatted string.
	 *
	 * @param result the search result to format
	 * @return a JSON formatted string of the search result
	 */
	public static String writeSearchResult(InvertedIndex.SearchResult result) {
		return withStringWriter(writer -> { writeSearchResult(result, writer); return writer.toString(); });
	}

	/**
	 * Writes the search results to the given writer in a pretty JSON format.
	 *
	 * @param results the search results to write
	 * @param writer the writer to use
	 * @param indent the current indentation level for formatting
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResults(Map<String, List<InvertedIndex.SearchResult>> results, Writer writer,
			int indent) throws IOException {
		writeObject(results, writer, indent,
				value -> writeArray(value, writer, indent + 1, element -> writeSearchResult(element, writer, indent + 2)));
	}

	/**
	 * Writes the map of queries to search results as a pretty JSON object using the
	 * provided writer.
	 *
	 * @param results the query → results map to write
	 * @param writer the writer to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResults(Map<String, List<InvertedIndex.SearchResult>> results, Writer writer)
			throws IOException {
		writeSearchResults(results, writer, 0);
	}

	/**
	 * Writes the search results to a file in JSON format.
	 *
	 * @param results the search results to write
	 * @param path the file path to write to
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResults(Map<String, List<InvertedIndex.SearchResult>> results, Path path)
			throws IOException {
		withBufferedWriter(path, writer -> writeSearchResults(results, writer));
	}

	/**
	 * Returns the search results as a JSON formatted string.
	 *
	 * @param results the search results to format
	 * @return a JSON formatted string of the search results
	 */
	public static String writeSearchResults(Map<String, List<InvertedIndex.SearchResult>> results) {
		return withStringWriter(writer -> { writeSearchResults(results, writer); return writer.toString(); });
	}

	/** Prevent instantiating this class of static methods. */
	private JsonWriter() {
	}
}
