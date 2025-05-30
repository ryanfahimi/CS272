package edu.usfca.cs272.templates.servlets.adventure;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

public class AdventureServer {
	public static void main(String[] args) throws Exception {
		// TODO
		Handler.Sequence handlers = null;

		Server server = new Server(8080);
		server.setHandler(handlers);
		server.start();
		server.join();
	}
}
