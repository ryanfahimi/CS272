package edu.usfca.cs272;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Serves static resources from the classpath, supporting deployment in JAR
 * files.
 */
public class StaticServlet extends HttpServlet {
	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202501;

	/** URL path for serving static files. */
	public static final String STATIC_PATH = "/static";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();

		if (path == null || path.equals("/")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String resourcePath = STATIC_PATH + path;

		try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
			if (is == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			if (path.endsWith(".css")) {
				response.setContentType("text/css");
			}
			else if (path.endsWith(".js")) {
				response.setContentType("application/javascript");
			}

			try (OutputStream os = response.getOutputStream()) {
				is.transferTo(os);
			}
		}
	}
}
