package edu.usfca.cs272.templates.jdbc;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FacultyServlet extends HttpServlet {
	private static final long serialVersionUID = 202501;
	private static final Logger log = LogManager.getLogger();

	private final DatabaseConnector connector;
	private final String sqlSelect;

	private final String htmlHeader;
	private final String htmlRow;
	private final String htmlFooter;

	private static final Set<String> COLUMNS =
			Set.of("last", "first", "email", "github", "courses");

	public FacultyServlet(DatabaseConnector connector) throws IOException {
		this.connector = connector;

		Path base = Path.of("src", "main", "resources", "jdbc");
		sqlSelect = Files.readString(base.resolve("SELECT.sql"), UTF_8);
		htmlHeader = Files.readString(base.resolve("header.html"), UTF_8);
		htmlRow = Files.readString(base.resolve("row.html"), UTF_8);
		htmlFooter = Files.readString(base.resolve("footer.html"), UTF_8);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info(request.getQueryString());

		String sort = getColumn(request, "sort", "last");
		boolean asc = !isEqual(request, "asc", "false");
		String query = request.getParameter("query");
		String field = getColumn(request, "field", "last");
		boolean onGithub = isEqual(request, "github", "on");
		boolean hasFilter = query != null && !query.isBlank();

		StringBuilder sql = new StringBuilder(sqlSelect);

		// TODO sql

		sql.append(";");
		log.info("SQL: {}", sql);

		StringBuffer action = new StringBuffer();

		if (onGithub) {
			action.append("&github=on");
		}

		if (hasFilter) {
			try {
				String encoded = URLEncoder.encode(query, UTF_8);
				action.append("&field=");
				action.append(field);
				action.append("&query=");
				action.append(encoded);
			}
			catch (Exception e) {
				log.warn("Bad query:", query);
			}
		}

		PrintWriter out = response.getWriter();
		out.println(getHeader(action.toString(), sort, asc));

		try (
				Connection db = connector.getConnection();
				// TODO statement
		) {
			// TODO results
		}
		catch (SQLException e) {
			log.warn(e);
		}

		out.println(getFooter(sort, asc));
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}

	private String getHeader(String filter, String sort, boolean asc) throws IOException {
		Map<String, String> values = new HashMap<>();
		values.put("last", "true");
		values.put("email", "true");
		values.put("github", "true");
		values.put("courses", "true");
		values.put("filter", filter);

		values.put(sort, Boolean.toString(!asc));
		return StringSubstitutor.replace(htmlHeader, values);
	}

	private String getFooter(String sort, boolean asc) throws IOException {
		Map<String, String> values = new HashMap<>();
		values.put("sort", sort);
		values.put("asc", Boolean.toString(asc));
		values.put("thread", Thread.currentThread().getName());
		values.put("date", getLongDate());
		return StringSubstitutor.replace(htmlFooter, values);
	}

	public static String getColumn(HttpServletRequest request, String key, String value) {
		String found = request.getParameter(key);
		return found != null && COLUMNS.contains(found) ? found : value;
	}

	public static boolean isEqual(HttpServletRequest request, String key, String value) {
		String found = request.getParameter(key);
		return found != null && found.equalsIgnoreCase(value);
	}

	public static String escape(ResultSet results, String column) throws SQLException {
		return StringEscapeUtils.escapeHtml4(results.getString(column));
	}

	public static String getLongDate() {
		String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
		LocalDateTime today = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return today.format(formatter);
	}
}
