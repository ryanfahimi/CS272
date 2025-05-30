package edu.usfca.cs272;

import java.util.TreeSet;

import edu.usfca.cs272.utils.WorkQueue;

/**
 * Finds primes, with an inefficient single-threaded implementation made
 * somewhat less inefficient with multithreading using a work queue.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class PrimeFinder {
	/** Prevent instantiating this class of static methods. */
	private PrimeFinder() {
	}

	/**
	 * A terrible and naive approach to determining if a number is prime.
	 *
	 * This is an intentionally TERRIBLE implementation to cause a long-running
	 * calculation. There really is no realistic use of this approach.
	 *
	 * @param number to test if prime
	 * @return true if the number is prime
	 */
	public static boolean isPrime(int number) {
		if (number < 2) {
			return false;
		}

		for (int i = number - 1; i > 1; i--) {
			if (number % i == 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns a collection of all primes less than or equal to the max value.
	 *
	 * @param max the maximum value to evaluate if prime
	 * @return all prime numbers found up to and including max
	 * @throws IllegalArgumentException if parameters not 1 or greater
	 */
	public static TreeSet<Integer> trialDivision(int max) throws IllegalArgumentException {
		if (max < 1) {
			throw new IllegalArgumentException("Parameters must be 1 or greater.");
		}

		TreeSet<Integer> primes = new TreeSet<Integer>();

		for (int i = 1; i <= max; i++) {
			if (isPrime(i)) {
				primes.add(i);
			}
		}

		return primes;
	}

	/**
	 * Uses a work queue to find all primes less than or equal to the maximum value.
	 * The number of threads must be a positive number greater than or equal to 1.
	 *
	 * @param max the maximum value to evaluate if prime
	 * @param threads number of worker threads (must be positive)
	 * @return all prime numbers found up to and including max
	 * @throws IllegalArgumentException if parameters not 1 or greater
	 */
	public static TreeSet<Integer> findPrimes(int max, int threads) throws IllegalArgumentException {
		if (max < 1 || threads < 1) {
			throw new IllegalArgumentException("Parameters must be 1 or greater.");
		}
		WorkQueue tasks = new WorkQueue(threads);
		TreeSet<Integer> primes = new TreeSet<Integer>();
		for (int i = 1; i <= max; i++) {
			tasks.execute(new Task(i, primes));
		}
		tasks.join();

		return primes;
	}

	/**
	 * A task that checks if a given number is prime and adds it to a shared
	 * collection if it is prime. Designed for use with a work queue to enable
	 * multi-threaded prime number calculation.
	 */
	private static class Task implements Runnable {
		/** The number to check. */
		private final Integer number;

		/** A shared set to store prime numbers. */
		private final TreeSet<Integer> primes;

		/**
		 * Initializes a new task for checking if a number is prime.
		 *
		 * @param number the number to check
		 * @param primes the shared collection to store prime numbers
		 */
		public Task(Integer number, TreeSet<Integer> primes) {
			this.number = number;
			this.primes = primes;
		}

		/**
		 * Determines if the assigned number is prime and, if so, adds it to the shared
		 * collection in a thread-safe manner.
		 */
		@Override
		public void run() {
			if (isPrime(number)) {
				synchronized (primes) {
					primes.add(number);
				}
			}
		}
	}
}
