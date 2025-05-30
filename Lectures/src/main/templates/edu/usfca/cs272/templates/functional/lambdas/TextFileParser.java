package edu.usfca.cs272.templates.functional.lambdas;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TextFileParser {
	public static void main(String[] args) throws Exception {
		Path path = Path.of("src", "main", "resources", "text", "sally.txt");

		List<String> list = listCleanWords(path);
		System.out.println(list);
	}

	// TODO Generalize
	public static List<String> listCleanWords(Path path) throws IOException {
		List<String> words = new ArrayList<>();

		try (BufferedReader reader = Files.newBufferedReader(path,
				StandardCharsets.UTF_8)) {
			String line;

			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\\s+");

				for (String token : tokens) {
					String cleaned = token.toLowerCase();
					words.add(cleaned);
				}
			}
		}

		return words;
	}

	public static String removePunctuation(String text) {
		return text.replaceAll("(?U)\\p{Punct}+", "");
	}
}
