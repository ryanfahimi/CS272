package edu.usfca.cs272;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generates several log messages to test Log4j2 setup.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class LoggerSetup {
	// NOTE: See the writeup for what to do for this homework.
	// NOTE: Do NOT modify this class!

	/**
	 * Outputs all levels of messages to a logger.
	 *
	 * @param log the logger to use
	 */
	public static void outputMessages(Logger log) {
		log.trace("Turkey");
		log.debug("Duck");
		log.info("Ibis");
		log.warn("Wren");

		// NOTE: Two ways of handling exceptions (appear differently in log)
		log.error("Eastern", new Exception("Eagle"));
		log.catching(Level.FATAL, new RuntimeException("Falcon"));
	}

	/**
	 * Generates several log messages to two different loggers.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		System.out.println("Root Logger:");
		outputMessages(LogManager.getRootLogger());
		System.out.println();

		// NOTE: The class logger is named: edu.usfca.cs272.LoggerSetup
		System.out.println("Class Logger:");
		outputMessages(LogManager.getLogger());
	}

	/** Prevent instantiating this class of static methods. */
	private LoggerSetup() {
	}
}
