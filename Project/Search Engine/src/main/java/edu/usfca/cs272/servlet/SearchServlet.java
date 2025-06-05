package edu.usfca.cs272.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

import edu.usfca.cs272.crawler.LinkFinder;
import edu.usfca.cs272.index.InvertedIndex;
import edu.usfca.cs272.index.InvertedIndex.SearchResult;
import edu.usfca.cs272.util.FileStemmer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet that handles search requests and displays results. Processes search
 * queries and generates HTML responses.
 */
public class SearchServlet extends HttpServlet {
	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202501;

	/** The title to use for this webpage. */
	private static final String TITLE = "Search Engine";

	/** SVG icon for web results. */
	private static final String WEB_ICON = """
			<svg xmlns=\"http://www.w3.org/2000/svg\" class=\"text-blue-500 w-6 h-6\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\" aria-hidden=\"true\">
			  <path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\"
			    d=\"M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9\" />
			</svg>""";

	/** SVG icon for file results. */
	private static final String FILE_ICON = """
			<svg xmlns=\"http://www.w3.org/2000/svg\" class=\"text-green-500 w-6 h-6\" fill=\"none\" viewBox=\"0 0 24 24\" stroke=\"currentColor\" aria-hidden=\"true\">
			  <path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\"
			    d=\"M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z\" />
			</svg>""";

	/** Query parameter name for search string. */
	private static final String QUERY_PARAM = "query";

	/** Query parameter name for exact match flag. */
	private static final String EXACT_PARAM = "exactSearch";

	/** Query parameter name for reverse order flag. */
	private static final String REVERSE_PARAM = "reverseSearch";

	/** Query parameter name for source type filter. */
	private static final String SOURCE_PARAM = "sourceType";

	/** Query parameter name for page number. */
	private static final String PAGE_PARAM = "page";

	/** HTTP protocol scheme. */
	private static final String HTTP = "http";

	/** HTTPS protocol scheme. */
	private static final String HTTPS = "https";

	/** Number of results to display per page. */
	private static final int RESULTS_PER_PAGE = 10;

	/** Size of window of pages to display for pagination. */
	private static final int WINDOW_SIZE = 3;

	/** Path to the HTML template for the search page. */
	private static final String HTML_TEMPLATE_PATH = "templates/index.html";

	/** The HTML template for the search page. */
	private final String htmlTemplate;

	/**
	 * Constructs a new SearchServlet with the given inverted index and loads the
	 * HTML template.
	 *
	 * @throws IOException if the HTML template cannot be read
	 */
	public SearchServlet() throws IOException {
		if (SearchEngine.RESOURCES_EXIST) {
			this.htmlTemplate = Files.readString(SearchEngine.RESOURCES.resolve(HTML_TEMPLATE_PATH), StandardCharsets.UTF_8);
		}
		else {
			try (InputStream is = SearchServlet.class.getResourceAsStream("/" + HTML_TEMPLATE_PATH)) {
				if (is == null) {
					throw new IOException("Could not find index.html in classpath");
				}
				this.htmlTemplate = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
	}

	/**
	 * Handles HTTP GET requests by processing search queries and generating HTML
	 * responses with search results.
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @throws ServletException if a servlet error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String query = request.getParameter(QUERY_PARAM);
		boolean exact = "true".equals(request.getParameter(EXACT_PARAM));
		boolean reverse = "true".equals(request.getParameter(REVERSE_PARAM));
		String sourceType = request.getParameter(SOURCE_PARAM);
		int page = parsePageNumber(request.getParameter(PAGE_PARAM));

		boolean hasQuery = query != null && !query.isBlank();

		long start = System.nanoTime();
		List<SearchResult> results = processQuery(query, exact, reverse, sourceType);
		long end = System.nanoTime();
		double elapsed = (end - start) / 1_000_000_000.0;
		List<SearchResult> pageResults = paginateResults(results, page);

		Map<String, String> values = new HashMap<>();
		values.put("title", TITLE);
		values.put("query", hasQuery ? StringEscapeUtils.escapeHtml4(query) : "");
		values.put("results", hasQuery ? buildResultsHtml(pageResults, query, request, results.size(), page, elapsed) : "");
		values.put("pagination", buildPaginationHtml(request, results.size(), page));
		values.put("uptime", SearchEngine.getUptime());
		values.put("totalQueries", String.valueOf(SearchEngine.getTotalQueries()));
		values.put("totalWords", String.valueOf(SearchEngine.getInvertedIndex().sizeWords()));
		values.put("totalSources", String.valueOf(SearchEngine.getInvertedIndex().sizeCounts()));

		renderTemplate(values, response);
	}

	/**
	 * Parses the page number parameter safely, defaulting to 1 on error or missing
	 * value.
	 *
	 * @param pageNumber the raw page parameter as a String
	 * @return a valid page number (>= 1)
	 */
	private static int parsePageNumber(String pageNumber) {
		try {
			return pageNumber == null ? 1 : Math.max(1, Integer.parseInt(pageNumber));
		}
		catch (NumberFormatException e) {
			return 1;
		}
	}

	/**
	 * Executes the search query against the inverted index, applies source
	 * filtering, optionally reverses the order, and returns the full result list.
	 *
	 * @param query the search query string
	 * @param exact true for exact matches only, false for partial matches
	 * @param reverse true to reverse the result order
	 * @param sourceType the filter for source type ("web", "local", or null/all)
	 * @return the list of SearchResult objects matching the criteria
	 */
	private static List<SearchResult> processQuery(String query, boolean exact, boolean reverse, String sourceType) {
		if (query == null || query.isBlank()) {
			return List.of();
		}

		var stemmedQuery = FileStemmer.uniqueStems(query);

		List<SearchResult> results = exact ? SearchEngine.getInvertedIndex().searchExact(stemmedQuery)
				: SearchEngine.getInvertedIndex().searchPartial(stemmedQuery);

		SearchEngine.incrementQueryCount();

		results = results.stream().filter(r -> filterBySource(r, sourceType)).collect(Collectors.toList());

		if (reverse) {
			Collections.reverse(results);
		}

		return results;
	}

	/**
	 * Determines if a SearchResult's source should be included based on the
	 * sourceType.
	 *
	 * @param result the SearchResult to test
	 * @param sourceType the source filter string ("web", "local", or null/all)
	 * @return true if the result matches the filter, false otherwise
	 */
	private static boolean filterBySource(SearchResult result, String sourceType) {
		if (sourceType == null || "all".equals(sourceType)) {
			return true;
		}
		boolean isWeb = LinkFinder.isHttp(result.getSource());
		return "web".equals(sourceType) ? isWeb : !isWeb;
	}

	/**
	 * Merges the template with provided values and writes the resulting HTML to the
	 * response.
	 *
	 * @param values a map of placeholder names to replacement strings
	 * @param response the HttpServletResponse to write to
	 * @throws IOException if an I/O error occurs
	 */
	private void renderTemplate(Map<String, String> values, HttpServletResponse response) throws IOException {
		String html = StringSubstitutor.replace(htmlTemplate, values);
		response.setContentType("text/html");
		try (PrintWriter out = response.getWriter()) {
			out.print(html);
		}
	}

	/**
	 * Returns a sublist of results corresponding to the given page.
	 *
	 * @param results the full list of SearchResult objects
	 * @param page the 1-based page number
	 * @return a sublist representing the page, or an empty list if out of range
	 */
	private static List<InvertedIndex.SearchResult> paginateResults(List<InvertedIndex.SearchResult> results, int page) {
		int from = (page - 1) * RESULTS_PER_PAGE;
		int to = Math.min(from + RESULTS_PER_PAGE, results.size());
		return from >= results.size() ? List.of() : results.subList(from, to);
	}

	/**
	 * Builds the HTML for displaying search results, including count and cards.
	 *
	 * @param results the results to display on this page
	 * @param query the original query string
	 * @param request the HttpServletRequest for context
	 * @param totalResults the total number of hits across all pages
	 * @param page the current page number
	 * @param elapsed the search time in seconds
	 * @return an HTML snippet for insertion into the template
	 */
	private static String buildResultsHtml(List<InvertedIndex.SearchResult> results, String query,
			HttpServletRequest request, int totalResults, int page, double elapsed) {
		if (results.isEmpty()) {
			return """
					<div class="p-6 text-gray-600 dark:text-gray-400 text-center">
					  There are no results for %s.
					</div>
					""".formatted(StringEscapeUtils.escapeHtml4(query));
		}

		String count = (page == 1)
				? "Found %d result%s in %.3f seconds".formatted(totalResults, totalResults == 1 ? "" : "s", elapsed)
				: "%d–%d of %d results".formatted((page - 1) * RESULTS_PER_PAGE + 1,
						(page - 1) * RESULTS_PER_PAGE + results.size(), totalResults);

		String cards = results.stream().map(r -> renderCard(r, request)).collect(Collectors.joining());

		return """
				<div class="p-4 pb-2">
				  <div class="text-sm text-gray-600 dark:text-gray-400 pb-2 mb-1 border-b border-gray-200 dark:border-gray-700">%s</div>
				  %s
				</div>
				"""
				.formatted(count, cards);
	}

	/**
	 * Renders a single result card as an HTML snippet.
	 *
	 * @param result the SearchResult to render
	 * @param request the HttpServletRequest for URL context
	 * @return an HTML snippet for the card
	 */
	private static String renderCard(InvertedIndex.SearchResult result, HttpServletRequest request) {
		boolean isWeb = LinkFinder.isHttp(result.getSource());
		String url = StringEscapeUtils.escapeHtml4(isWeb ? result.getSource() : fileToUri(request, result.getSource()));
		double score = result.getScore();
		String scoreClass = score >= 0.7 ? "bg-green-500" : score >= 0.4 ? "bg-yellow-400" : "bg-red-500";
		int pct = (int) (score * 100);

		return """
				<div class="flex items-center p-4 border-b border-gray-200 dark:border-gray-700 last:border-b-0">
				  %s
				  <div class="flex-1 min-w-0 ml-4 pr-4">
				    <a href="%s"
				       class="text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 block truncate
				              visited:text-purple-600 dark:visited:text-purple-400 visited:hover:text-purple-800 dark:visited:hover:text-purple-300">
				      %s
				    </a>
				    <div class="flex gap-6 text-gray-500 dark:text-gray-400 text-sm mt-1">
				      <span>Matches: %d</span>
				      <span class="flex items-center gap-2">Relevance:
				        <div class="w-16 h-1 bg-gray-200 dark:bg-gray-600 rounded overflow-hidden">
				          <div class="h-full rounded %s" style="width:%d%%"></div>
				        </div>
				        <span>%.2f</span>
				      </span>
				    </div>
				  </div>
				</div>
				"""
				.formatted(isWeb ? WEB_ICON : FILE_ICON, url, url, result.getMatchCount(), scoreClass, pct, score);
	}

	/**
	 * Formats a file path into a URL for display and linking.
	 *
	 * @param request the HttpServletRequest for base URI
	 * @param file the source file path
	 * @return a formatted URL string for the source
	 */
	private static String fileToUri(HttpServletRequest request, String file) {
		Path path = Path.of(file).toAbsolutePath().normalize();
		Path relativePath = SearchEngine.getTextFiles().relativize(path);

		return getFilesUri(request).resolve(relativePath.toString().replace(File.separatorChar, '/')).toString();
	}

	/**
	 * Gets the base URI for file resources based on the server configuration.
	 *
	 * @param request the HTTP request
	 * @return the base URI for file resources
	 */
	public static URI getFilesUri(HttpServletRequest request) {
		String scheme = request.getScheme();
		String schemeLower = scheme.toLowerCase();

		int port = request.getServerPort();
		boolean defaultPort = (schemeLower.equals(HTTP) && port == 80) || (schemeLower.equals(HTTPS) && port == 443);

		StringBuilder uri = new StringBuilder();
		uri.append(scheme).append("://").append(request.getServerName());

		if (!defaultPort) {
			uri.append(':').append(port);
		}
		uri.append(SearchEngine.TEXT_FILE_PATH).append('/');

		return URI.create(uri.toString());
	}

	/**
	 * Builds the HTML for pagination controls. Preserves all existing query
	 * parameters and injects the correct page number.
	 *
	 * @param request the HTTP request used to extract and preserve query parameters
	 * @param totalResults the total number of search results
	 * @param currentPage the current page number (1-based)
	 * @return a nav element containing page links or an empty string if only one
	 *   page
	 */
	private static String buildPaginationHtml(HttpServletRequest request, int totalResults, int currentPage) {
		int totalPages = (int) Math.ceil(totalResults / (double) RESULTS_PER_PAGE);
		if (totalPages <= 1) {
			return "";
		}

		Map<String, String> params = Collections.list(request.getParameterNames())
				.stream()
				.collect(Collectors.toMap(Function.identity(),
						name -> URLEncoder.encode(request.getParameter(name), StandardCharsets.UTF_8)));

		StringBuilder html = new StringBuilder();

		addLink(html, request, currentPage - 1, "&laquo; Prev", false, currentPage == 1, params);

		int[] range = computeRange(totalPages, currentPage, WINDOW_SIZE);

		if (range[0] > 1) {
			addLink(html, request, 1, "1", false, false, params);
			addEllipsis(html, range[0] > 2);
		}

		for (int p = range[0]; p <= range[1]; p++) {
			addLink(html, request, p, Integer.toString(p), p == currentPage, false, params);
		}

		if (range[1] < totalPages) {
			addEllipsis(html, range[1] < totalPages - 1);
			addLink(html, request, totalPages, Integer.toString(totalPages), false, false, params);
		}

		addLink(html, request, currentPage + 1, "Next &raquo;", false, currentPage == totalPages, params);

		return String.format("<nav aria-label=\"Pagination\" class=\"mt-12 flex justify-center gap-2 text-base\">%s</nav>",
				html);
	}

	/**
	 * Appends a pagination link, current-page indicator, or disabled span to the
	 * HTML builder.
	 *
	 * @param html the StringBuilder collecting HTML fragments
	 * @param request the HTTP request used for URL construction
	 * @param pageNum the page number this link should point to
	 * @param label the text label for the link or span
	 * @param isCurrent true if this item represents the active page
	 * @param isDisabled true if this link should be rendered as a non-clickable
	 *   span
	 * @param params the map of encoded query parameters to preserve in the URL
	 */
	private static void addLink(StringBuilder html, HttpServletRequest request, int pageNum, String label,
			boolean isCurrent, boolean isDisabled, Map<String, String> params) {
		if (isDisabled) {
			html.append(String.format("<span class=\"px-3 py-1 rounded-full text-gray-400\">%s</span>", label));
		}
		else if (isCurrent) {
			html.append(String.format(
					"<span aria-current=\"page\" class=\"px-3 py-1 rounded-full bg-blue-600 text-white font-semibold\">%s</span>",
					label));
		}
		else {
			html.append(String.format(
					"<a href=\"%s\" class=\"px-3 py-1 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 text-blue-600 dark:text-blue-400\">%s</a>",
					buildPaginationUrl(request, pageNum, params), label));
		}
	}

	/**
	 * Constructs a query string with all preserved parameters and an updated page
	 * number.
	 *
	 * @param request the HTTP request containing original parameters
	 * @param targetPage the page number to set in the generated query string
	 * @param params the map of encoded parameters to modify
	 * @return a string beginning with '?' and containing encoded parameters joined
	 *   by an ampersand
	 */
	private static String buildPaginationUrl(HttpServletRequest request, int targetPage, Map<String, String> params) {
		params.put(PAGE_PARAM, Integer.toString(targetPage));

		return "?" + params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
	}

	/**
	 * Computes the sliding window [start, end] of page numbers around the current
	 * page. Ensures the window never goes out of bounds.
	 *
	 * @param totalPages the total count of pages
	 * @param current the current page index
	 * @param window the maximum window size
	 * @return a two-element array {start, end} representing the inclusive page
	 *   range
	 */
	private static int[] computeRange(int totalPages, int current, int window) {
		int half = window / 2;
		int start = Math.max(1, current - half);
		int end = Math.min(totalPages, current + half);

		if (current <= half) {
			end = Math.min(totalPages, window);
		}
		if (current + half >= totalPages) {
			start = Math.max(1, totalPages - window + 1);
		}

		return new int[] { start, end };
	}

	/**
	 * Appends an ellipsis span when the condition is true.
	 *
	 * @param html the StringBuilder collecting HTML fragments
	 * @param condition true to append the ellipsis separator
	 */
	private static void addEllipsis(StringBuilder html, boolean condition) {
		if (condition) {
			html.append("<span class=\"rounded-full text-gray-400\">…</span>");
		}
	}
}
