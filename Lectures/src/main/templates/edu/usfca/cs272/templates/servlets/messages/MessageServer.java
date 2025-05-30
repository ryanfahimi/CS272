package edu.usfca.cs272.templates.servlets.messages;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;

public class MessageServer {
	public static final int PORT = 8080;

	public static void main(String[] args) throws Exception {
		Server server = new Server(PORT);
		ServletContextHandler handler = new ServletContextHandler();

		// TODO

		server.setHandler(handler);
		server.start();
		server.join();
	}
}
