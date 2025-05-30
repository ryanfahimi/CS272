package edu.usfca.cs272.lectures.servlets.cookies;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Demonstrates how to create, use, and clear cookies.
 *
 * @see CookieVisitServlet
 * @see CookieLandingServlet
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class CookieLandingServlet extends HttpServlet {
	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202501;

	/** Logger for this servlet. */
	private static final Logger log = LogManager.getLogger();

	/** The title to use for this webpage. */
	private static final String TITLE = "Cookies!";

	/** Location of the HTML template for this servlet. */
	private static final Path TEMPLATE_PATH = Path.of("src", "main", "resources", "cookies",  "cookie_landing.html");

	/** HTML template. */
	private final String template;

	/**
	 * Initializes the HTML template.
	 *
	 * @throws IOException if unable to load the template
	 */
	public CookieLandingServlet() throws IOException {
		template = Files.readString(TEMPLATE_PATH, UTF_8);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.info(request);

		Map<String, String> values = new HashMap<>();

		// configure the page info
		values.put("title", TITLE);
		values.put("url", request.getRequestURL().toString());
		values.put("path", request.getRequestURI());
		values.put("timestamp", LocalDateTime.now().format(ISO_DATE_TIME));
		values.put("thread", Thread.currentThread().getName());

		// configure the form info
		values.put("method", "POST");
		values.put("action", "/visits");

		// generate html (approach inefficient for large templates)
		String html = StringSubstitutor.replace(template, values);

		// setup response
		response.setContentType("text/html");
		response.setStatus(SC_OK);

		// output html
		PrintWriter out = response.getWriter();
		out.write(html);

		// finish up response
		response.flushBuffer();
	}
}
