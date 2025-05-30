package edu.usfca.cs272.lectures.servlets.messages;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;

/**
 * Demonstrates how to create a simple message board using Jetty and servlets,
 * as well as how to initialize servlets when you need to call its constructor.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class MessageServer {
	/** The hard-coded port to run this server. */
	public static final int PORT = 8080;

	/**
	 * Sets up a Jetty server with different servlet instances.
	 *
	 * @param args unused
	 * @throws Exception if unable to start and run server
	 */
	public static void main(String[] args) throws Exception {
		Server server = new Server(PORT);

		ServletContextHandler handler = new ServletContextHandler();

		// must use servlet holds when need to call a constructor
		handler.addServlet(new ServletHolder(new MessageServlet()), "/pie");
		handler.addServlet(new ServletHolder(new MessageServlet()), "/cake");
		handler.addServlet(new ServletHolder(new BulmaMessageServlet()), "/bulma");

		server.setHandler(handler);
		server.start();
		server.join();
	}

	/** Prevent instantiating this class of static methods. */
	private MessageServer() {
	}
}
