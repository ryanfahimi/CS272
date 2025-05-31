package edu.usfca.cs272;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

/**
 * A web server that provides a search interface for the inverted index. Allows
 * users to search through indexed files and URLs.
 */
public class SearchEngine {
	/** Path to the text files directory. */
	private static Path textFiles = null;

	/** Path to the resources directory containing templates and static files. */
	public static final Path RESOURCES = Path.of("src", "main", "resources").toAbsolutePath().normalize();

	/** URL path for serving text files. */
	public static final String TEXT_FILE_PATH = "/files";

	/** URL path for serving static files. */
	public static final String STATIC_PATH = "/static";

	/** Admin password for shutting down Search Engine */
	private static final String PASSWORD = "admin";

	/**
	 * The timestamp when the server started, used to calculate uptime.
	 */
	private static final long START_TIME = System.currentTimeMillis();

	/** Total number of queries processed */
	private static int totalQueries = 0;

	/**
	 * Thread-safe inverted index used to perform search operations.
	 */
	private static ThreadSafeInvertedIndex invertedIndex;

	/** Logger for SearchEngine class. */
	private static final Logger logger = LogManager.getLogger(SearchEngine.class);

	/** Jetty server instance handling incoming HTTP requests. */
	private static Server server;

	/**
	 * Runs the search engine web server with the specified configuration.
	 *
	 * @param port the port to run the server on
	 * @param invertedIndex the thread-safe inverted index to use for searches
	 * @param textFiles the path to the text files directory
	 * @throws Exception if unable to start and run server
	 */
	public static void run(int port, ThreadSafeInvertedIndex invertedIndex, Path textFiles) throws Exception {
		logger.info("Started running SearchEngine");
		SearchEngine.invertedIndex = invertedIndex;

		List<Handler> handlers = new ArrayList<>();

		server = new Server(port);

		ShutdownHandler shutdownHandler = new ShutdownHandler(PASSWORD);
		handlers.add(shutdownHandler);

		if (textFiles != null && Files.exists(textFiles)) {
			SearchEngine.textFiles = textFiles.toAbsolutePath().normalize();
			ResourceHandler textResourceHandler = new ResourceHandler();
			Resource textBaseResource = ResourceFactory.of(textResourceHandler).newResource(SearchEngine.textFiles);
			textResourceHandler.setBaseResource(textBaseResource);
			ContextHandler textResourceContext = new ContextHandler(textResourceHandler, TEXT_FILE_PATH);
			handlers.add(textResourceContext);
		}

		ResourceHandler staticResourceHandler = new ResourceHandler();
		Resource staticBaseResource = ResourceFactory.of(staticResourceHandler).newResource(RESOURCES.resolve("static"));
		staticResourceHandler.setBaseResource(staticBaseResource);
		ContextHandler staticResourceContext = new ContextHandler(staticResourceHandler, STATIC_PATH);
		handlers.add(staticResourceContext);

		ServletContextHandler servletContext = new ServletContextHandler();
		servletContext.addServlet(new ServletHolder(new SearchServlet()), "/");
		servletContext.addServlet(DownloadServlet.class, "/download");
		handlers.add(servletContext);

		server.setHandler(new Handler.Sequence(handlers));

		server.start();

		logger.info("Server: {} with {} threads", server.getState(), server.getThreadPool().getThreads());

		server.join();
	}

	/**
	 * Calculates how long the server has been running since startup.
	 *
	 * @return a human-readable uptime string (days, hours, minutes, seconds)
	 */
	public static String getUptime() {
		long uptime = System.currentTimeMillis() - START_TIME;
		long seconds = (uptime / 1000) % 60;
		long minutes = (uptime / (1000 * 60)) % 60;
		long hours = (uptime / (1000 * 60 * 60)) % 24;
		long days = uptime / (1000 * 60 * 60 * 24);

		if (days > 0) {
			return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
		}
		else if (hours > 0) {
			return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
		}
		else if (minutes > 0) {
			return String.format("%d minutes, %d seconds", minutes, seconds);
		}
		else {
			return String.format("%d seconds", seconds);
		}
	}

	/**
	 * Increments the total query count.
	 */
	public static synchronized void incrementQueryCount() {
		totalQueries++;
	}

	/**
	 * Gets the total number of queries processed.
	 * 
	 * @return the total query count
	 */
	public static int getTotalQueries() {
		return totalQueries;
	}

	/**
	 * Gets the path of the text files directory.
	 * 
	 * @return the text files path
	 */
	public static Path getTextFiles() {
		return textFiles;
	}

	/**
	 * Gets the thread-safe inverted index used to perform search operations.
	 * 
	 * @return the inverted index
	 */
	public static ThreadSafeInvertedIndex getInvertedIndex() {
		return invertedIndex;
	}

	/** Prevent instantiating this class of static methods. */
	private SearchEngine() {
	}
}
