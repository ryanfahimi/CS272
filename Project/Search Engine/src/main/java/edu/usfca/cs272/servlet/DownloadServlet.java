package edu.usfca.cs272.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet serving the inverted index as a downloadable JSON file. Responds to
 * HTTP GET requests by writing the contents of the ThreadSafeInvertedIndex to
 * the response output stream.
 */
public class DownloadServlet extends HttpServlet {
	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202501;

	/** Logger for DownloadServlet class. */
	private static final Logger logger = LogManager.getLogger(DownloadServlet.class);

	/**
	 * Handles HTTP GET by setting the response headers for JSON download and
	 * writing the index contents to the output stream.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @throws ServletException if a servlet error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("Processing download request for inverted index");

		response.setContentType("application/json");
		response.setHeader("Content-Disposition", "attachment; filename=\"index.json\"");

		try (PrintWriter out = response.getWriter()) {
			SearchEngine.getInvertedIndex().indexToJson(out);
		}

		logger.info("Successfully served inverted index download");
	}

	/** Default constructor for DownloadServlet. */
	public DownloadServlet() {
	}
}
