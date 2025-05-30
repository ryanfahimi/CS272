package edu.usfca.cs272.lectures.servlets.demos;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Demonstrates one way to serve both dynamic and static resources. Requires a
 * local web/foto directory with several jpg images to function properly.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class GalleryServer {
	/** The hard-coded port to run this server. */
	public static final int PORT = 8080;

	/**
	 * The logger to use (Jetty is configured via the pom.xml to use Log4j2 as well)
	 */
	public static Logger log = LogManager.getLogger();

	/** Location of file resources. */
	public static final Path base = Path.of("src", "main", "resources")
			.toAbsolutePath().normalize();

	/** Location of photos. */
	public static final Path foto = base.resolve("foto");

	/**
	 * Sets up a Jetty server with a resource, context, and servlet context handler
	 * to respond to requests for both dynamic and static resources.
	 *
	 * @param args unused
	 * @throws Exception if unable to start and run server
	 */
	public static void main(String[] args) throws Exception {
		Server server = new Server(PORT);

		// add static resource holders to web server
		// this indicates where web files are accessible on the file system
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirAllowed(true);

		Resource baseResource = ResourceFactory.of(resourceHandler).newResource(foto);
		resourceHandler.setBaseResource(baseResource);

		// only serve static resources in the "/images" context directory
		// this indicates where web files are accessible via the web server
		ContextHandler resourceContext = new ContextHandler(resourceHandler, "/images");

		// all other requests should be handled by the gallery servlet
		ServletContextHandler servletContext = new ServletContextHandler();
		servletContext.addServlet(GalleryServlet.class, "/");

		// setup handlers (and handler order)
		List<Handler> handlers = List.of(resourceContext, servletContext);
		Handler.Sequence sequence = new Handler.Sequence(handlers);

		// order matters---if you swap the order, we no longer see the individual
		// files listed

		server.setHandler(sequence);
		server.start();

		log.info("Server: {} with {} threads", server.getState(), server.getThreadPool().getThreads());
		server.join();

		// http://localhost:8080/
		// http://localhost:8080/nowhere
		// http://localhost:8080/images
	}

	/**
	 * Automatically adds all of the images found in the web/foto subdirectory to a
	 * gallery page.
	 */
	public static class GalleryServlet extends HttpServlet {
		/** Class version for serialization, in [YEAR][TERM] format (unused). */
		private static final long serialVersionUID = 202501;

		/** The title to use for this webpage. */
		private static final String TITLE = "Gallery";

		/** Creates a new instance of this class. */
		public GalleryServlet() {}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			log.info(request);

			String head = """
					<!DOCTYPE html>
					<html lang="en">

					<head>
					  <meta charset="utf-8">
					  <title>%1$s</title>
					</head>

					<body>
					<h1>%1$s</h1>
					""";

			String loop = """
					<img src="/images/%s" width="150" height="150"/>
					""";

			String foot = """
					<p>This request was handled by thread %s.</p>
					</body>
					</html>
					""";

			PrintWriter out = response.getWriter();
			out.printf(head, TITLE);

			// loop through the local directory and get all the jpg files
			try (DirectoryStream<Path> dir = Files.newDirectoryStream(foto, p -> p.toString().endsWith(".jpg"));) {
				// for each returned file, add to gallery page
				// assumes photos will be served out of the images/ directory
				for (Path file : dir) {
					out.printf(loop, file.getFileName().toString());
				}
			}

			out.printf(foot, Thread.currentThread().getName());

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}
	}

	/** Prevent instantiating this class of static methods. */
	private GalleryServer() {
	}
}
