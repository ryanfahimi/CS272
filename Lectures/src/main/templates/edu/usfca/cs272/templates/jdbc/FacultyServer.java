package edu.usfca.cs272.templates.jdbc;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;

public class FacultyServer {
	public static void main(String[] args) throws Exception {
		Path base = Path.of("src", "main", "resources", "jdbc");
		Path properties = base.resolve("database.properties");

		if (args.length > 0) {
			properties = base.resolve(args[0]);
		}

		DatabaseConnector connector = new DatabaseConnector(properties);
		Set<String> expected = Set.of("faculty_names", "faculty_github", "faculty_courses");

		try (Connection db = connector.getConnection()) {
			Set<String> tables = connector.getTables(db);
			System.out.println("Tables: " + tables);

			if (!tables.containsAll(expected)) {
				throw new SQLException("Missing required faculty tables in database.");
			}
		}

		ContextHandler defaultHandler = new ContextHandler("/favicon.ico");
		defaultHandler.setHandler(new DefaultHandler());

		FacultyServlet servlet = new FacultyServlet(connector);
		ServletContextHandler servletHandler = new ServletContextHandler();
		servletHandler.addServlet(new ServletHolder(servlet), "/");

		Handler.Sequence handlers = new Handler.Sequence(defaultHandler, servletHandler);

		Server server = new Server(8080);
		server.setHandler(handlers);
		server.start();
		server.join();
	}
}
