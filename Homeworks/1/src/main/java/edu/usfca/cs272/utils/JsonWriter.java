package edu.usfca.cs272.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private static final String NEW_LINE = "\n";

	/**
	 * Represents a comma followed by a newline character.
	 */
	private static final String COMMA_NEW_LINE = ",\n";

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
		writer.write(LEFT_BRACKET + NEW_LINE);
		var it = elements.iterator();
		if (it.hasNext()) {
			writeIndent(it.next().toString(), writer, indent + 1);
			while (it.hasNext()) {
				writer.write(COMMA_NEW_LINE);
				writeIndent(it.next().toString(), writer, indent + 1);
			}
			writer.write(NEW_LINE);
		}
		writeIndent(RIGHT_BRACKET, writer, indent);
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
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
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
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
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
		writer.write(LEFT_CURLY + NEW_LINE);
		var it = elements.entrySet().iterator();
		if (it.hasNext()) {
			writeObjectEntry(it.next(), writer, indent);
			while (it.hasNext()) {
				writer.write(COMMA_NEW_LINE);
				writeObjectEntry(it.next(), writer, indent);
			}
			writer.write(NEW_LINE);
		}
		writeIndent(RIGHT_CURLY, writer, indent);
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
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
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
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
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
		writer.write(LEFT_CURLY + NEW_LINE);
		var it = elements.entrySet().iterator();
		if (it.hasNext()) {
			writeObjectArrayEntry(it.next(), writer, indent);
			while (it.hasNext()) {
				writer.write(COMMA_NEW_LINE);
				writeObjectArrayEntry(it.next(), writer, indent);
			}
			writer.write(NEW_LINE);
		}
		writeIndent(RIGHT_CURLY, writer, indent);
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
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
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
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
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
		var it = elements.iterator();
		if (it.hasNext()) {
			writeArrayObjectEntry(it.next(), writer, indent);
			while (it.hasNext()) {
				writer.write(COMMA_NEW_LINE);
				writeArrayObjectEntry(it.next(), writer, indent);
			}
			writer.write(NEW_LINE);
		}
		writeIndent(RIGHT_BRACKET, writer, indent);
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
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
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
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper method to write a key-value pair in a JSON object where the value is a
	 * Number.
	 *
	 * @param entry the map entry containing the key and number value
	 * @param writer the writer to output the JSON content
	 * @param indent the current indentation level
	 * @throws IOException if an IO error occurs while writing
	 */
	private static void writeObjectEntry(Map.Entry<String, ? extends Number> entry, Writer writer, int indent)
			throws IOException {
		writeQuote(entry.getKey(), writer, indent + 1);
		writer.write(COLON_SPACE);
		writer.write(entry.getValue().toString());
	}

	/**
	 * Helper method to write a key-value pair in a JSON object where the value is a
	 * collection of Numbers.
	 *
	 * @param entry the map entry containing the key and collection of numbers
	 * @param writer the writer to output the JSON content
	 * @param indent the current indentation level
	 * @throws IOException if an IO error occurs while writing
	 */
	private static void writeObjectArrayEntry(Map.Entry<String, ? extends Collection<? extends Number>> entry,
			Writer writer, int indent) throws IOException {
		writeQuote(entry.getKey(), writer, indent + 1);
		writer.write(COLON_SPACE);
		writeArray(entry.getValue(), writer, indent + 1);
	}

	/**
	 * Helper method to write a JSON object as an element within a JSON array.
	 *
	 * @param element the JSON object represented as a map with String keys and
	 *   Number values
	 * @param writer the writer to output the JSON content
	 * @param indent the current indentation level
	 * @throws IOException if an IO error occurs while writing
	 */
	private static void writeArrayObjectEntry(Map<String, ? extends Number> element, Writer writer, int indent)
			throws IOException {
		writeIndent(writer, indent + 1);
		writeObject(element, writer, indent + 1);
	}

	/** Prevent instantiating this class of static methods. */
	private JsonWriter() {
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		Set<Integer> empty = Collections.emptySet();
		Set<Integer> single = Set.of(42);
		List<Integer> simple = List.of(65, 66, 67);

		System.out.println("\nArrays:");
		System.out.println(writeArray(empty));
		System.out.println(writeArray(single));
		System.out.println(writeArray(simple));

		System.out.println("\nObjects:");
		System.out.println(writeObject(Collections.emptyMap()));
		System.out.println(writeObject(Map.of("hello", 42)));
		System.out.println(writeObject(Map.of("hello", 42, "world", 67)));

		System.out.println("\nNested Arrays:");
		System.out.println(writeObjectArrays(Collections.emptyMap()));
		System.out.println(writeObjectArrays(Map.of("hello", single)));
		System.out.println(writeObjectArrays(Map.of("hello", single, "world", simple)));

		System.out.println("\nNested Objects:");
		System.out.println(writeArrayObjects(Collections.emptyList()));
		System.out.println(writeArrayObjects(Set.of(Map.of("hello", 3.12))));
		System.out.println(writeArrayObjects(Set.of(Map.of("hello", 3.12, "world", 2.04), Map.of("apple", 0.04))));
	}
}
