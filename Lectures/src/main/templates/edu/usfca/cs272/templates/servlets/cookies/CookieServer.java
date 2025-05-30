package edu.usfca.cs272.templates.servlets.cookies;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;

import edu.usfca.cs272.lectures.servlets.cookies.CookieLandingServlet;
import edu.usfca.cs272.lectures.servlets.cookies.CookieVisitServlet;

public class CookieServer {
	public static void main(String[] args) throws Exception {
		ServletContextHandler handler = new ServletContextHandler();
		handler.addServlet(CookieLandingServlet.class, null); // TODO
		handler.addServlet(CookieVisitServlet.class, null); // TODO

		Server server = new Server(8080);
		server.setHandler(handler);
		server.start();
		server.join();
	}
}
