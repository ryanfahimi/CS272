package edu.usfca.cs272.lectures.servlets.demos;

import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

/**
 * A simple example of using Jetty and servlets to both serve static resources
 * and dynamic content.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class SimpleFileServer {
	/** The hard-coded port to run this server. */
	public static final int PORT = 8080;

	/**
	 * The logger to use (Jetty is configured via the pom.xml to use Log4j2 as well)
	 */
	public static Logger log = LogManager.getLogger();

	/**
	 * Sets up a Jetty server with both a resource and servlet handler. Able to
	 * respond with static and dynamic content.
	 *
	 * @param args unused
	 * @throws Exception if unable to start server
	 */
	public static void main(String[] args) throws Exception {
		// Enable DEBUG logging (see debug.log file for messages)
		System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG");

		Server server = new Server(PORT);

		// Add static resource holders to web server
		// This indicates where web files are accessible on the file system
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirAllowed(true);

		// Try both "." and "./src"
		Path basePath = Path.of(".").toAbsolutePath().normalize();
		Resource baseResource = ResourceFactory.of(resourceHandler).newResource(basePath);
		resourceHandler.setBaseResource(baseResource);

		// Can still assign servlets to specific requests
		ServletContextHandler servletHandler = new ServletContextHandler();
		servletHandler.addServlet(VisitServer.VisitServlet.class, "/visits");

		// Setup handlers (and handler order)
		List<Handler> handlers = List.of(resourceHandler, servletHandler);
		Handler.Sequence sequence = new Handler.Sequence(handlers);

		server.setHandler(sequence);
		server.start();

		log.info("Server: {} with {} threads", server.getState(), server.getThreadPool().getThreads());
		server.join();

		// http://localhost:8080/
		// http://localhost:8080/visits
		// http://localhost:8080/src/
	}

	/** Prevent instantiating this class of static methods. */
	private SimpleFileServer() {
	}
}
