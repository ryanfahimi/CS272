package edu.usfca.cs272.templates.jdbc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;

public class DatabaseConnector {
	public final String uri;
	private final Properties login;

	public DatabaseConnector() throws FileNotFoundException, IOException {
		this(Path.of("database.properties"));
	}

	public DatabaseConnector(Path path) throws FileNotFoundException, IOException {
		Properties config = loadConfig(path);

		uri = null; // TODO uri

		login = new Properties();
		// TODO login
	}

	public static Properties loadConfig(Path path) throws FileNotFoundException, IOException {
		Set<String> required = Set.of("username", "password", "database", "hostname");
		Properties config = new Properties();

		try (BufferedReader reader = Files.newBufferedReader(path)) {
			config.load(reader);
		}

		if (!config.keySet().containsAll(required)) {
			String error = "Must provide the following in properties file: ";
			throw new InvalidPropertiesFormatException(error + required);
		}

		return config;
	}

	public Connection getConnection() throws SQLException {
		return null; // TODO getConnection
	}

	public Set<String> getTables(Connection db) throws SQLException {
		Set<String> tables = new HashSet<>();

		// TODO getTables

		return tables;
	}

	public boolean testConnection() {
		boolean okay = false;

		try (Connection db = getConnection();) {
			System.out.println("Executing SHOW TABLES...");
			Set<String> tables = getTables(db);

			if (tables != null) {
				System.out.print("Found " + tables.size() + " tables: ");
				System.out.println(tables);
				okay = true;
			}
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}

		return okay;
	}

	public static void main(String[] args) {
		try {
			Path base = Path.of("src", "main", "resources", "jdbc");
			Path properties = base.resolve("database.properties");

			if (args.length > 0) {
				properties = base.resolve(args[0]);
			}

			System.out.println("Loading " + properties + " ...");

			DatabaseConnector test = new DatabaseConnector(properties);
			System.out.println("Connecting to " + test.uri);

			if (test.testConnection()) {
				System.out.println("Connection to database established.");
			}
			else {
				System.err.println("Unable to connect properly to database.");
			}
		}
		catch (Exception e) {
			System.err.println("Unable to connect properly to database.");
			System.err.println(e.getMessage());
		}
	}
}
