package edu.usfca.cs272.util;

import java.util.LinkedList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple work queue implementation based on the IBM developerWorks article by
 * Brian Goetz. It is up to the user of this class to keep track of whether
 * there is any pending work remaining.
 *
 * @see <a href=
 *   "https://web.archive.org/web/20210126172022/https://www.ibm.com/developerworks/library/j-jtp0730/index.html">Java
 *   Theory and Practice: Thread Pools and Work Queues</a>
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class WorkQueue {
	/** Workers that wait until work (or tasks) are available. */
	private final Worker[] workers;

	/** Queue of pending work (or tasks). */
	private final LinkedList<Runnable> tasks;

	/** Used to signal the workers should terminate. */
	private volatile boolean shutdown;

	/** Logger used for this class. */
	private static final Logger logger = LogManager.getLogger(WorkQueue.class);

	/** The amount of pending (or unfinished) work. */
	private int pending;

	/**
	 * Starts a work queue with the default number of threads.
	 *
	 * @see #WorkQueue(int)
	 */
	public WorkQueue() {
		this(Runtime.getRuntime().availableProcessors());
	}

	/**
	 * Starts a work queue with the specified number of threads.
	 *
	 * @param threads number of worker threads; should be greater than 1
	 */
	public WorkQueue(int threads) {
		this.tasks = new LinkedList<Runnable>();
		this.workers = new Worker[threads];
		this.shutdown = false;
		this.pending = 0;

		for (int i = 0; i < threads; i++) {
			workers[i] = new Worker();
			workers[i].start();
		}

		logger.debug("Work queue started with {} thread(s).", workers.length);
	}

	/**
	 * Adds a work (or task) request to the queue. A worker thread will process this
	 * request when available.
	 *
	 * @param task work request (in the form of a {@link Runnable} object)
	 * @throws IllegalStateException if the work queue is already shutdown
	 */
	public void execute(Runnable task) throws IllegalStateException {
		if (shutdown) {
			logger.error("Attempted to submit task while work queue is shutdown.");
			throw new IllegalStateException("Work queue is shutdown.");
		}

		incrementPending();
		synchronized (tasks) {
			tasks.addLast(task);
			tasks.notifyAll();
		}
	}

	/**
	 * Safely increments the shared pending variable.
	 */
	private synchronized void incrementPending() {
		pending++;
	}

	/**
	 * Safely decrements the shared pending variable, and wakes up any threads
	 * waiting for work to be completed.
	 */
	private synchronized void decrementPending() {
		pending--;

		if (pending == 0) {
			this.notifyAll();
		}
	}

	/**
	 * Waits for all pending work (or tasks) to be finished. Does not terminate the
	 * worker threads so that the work queue can continue to be used.
	 */
	public synchronized void finish() {
		try {
			while (pending > 0) {
				this.wait();
			}
		}
		catch (InterruptedException e) {
			logger.warn("Work queue interrupted while finishing.");
			logger.catching(Level.WARN, e);
			Thread.currentThread().interrupt();
		}
		logger.debug("Pending work finished");
	}

	/**
	 * Similar to {@link Thread#join()}, waits for all the work to be finished and
	 * the worker threads to terminate. The work queue cannot be reused after this
	 * call completes.
	 */
	public void join() {
		try {
			logger.debug("Join called: waiting for all tasks to finish and workers to terminate.");
			finish();
			shutdown();

			for (Worker worker : workers) {
				worker.join();
			}

			logger.debug("All {} worker thread(s) terminated.", workers.length);
		}
		catch (InterruptedException e) {
			logger.warn("Work queue interrupted while joining.");
			logger.catching(Level.WARN, e);
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work (or tasks) will not be
	 * finished, but threads in-progress will not be interrupted.
	 */
	public void shutdown() {
		shutdown = true;

		synchronized (tasks) {
			tasks.notifyAll();
		}

		logger.debug("Work queue shutdown triggered.");
	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return number of worker threads
	 */
	public int size() {
		return workers.length;
	}

	/**
	 * Waits until work (or a task) is available in the work queue. When work is
	 * found, will remove the work from the queue and run it.
	 *
	 * <p>If a shutdown is detected, will exit instead of grabbing new work from the
	 * queue. These threads will continue running in the background until a shutdown
	 * is requested.
	 */
	private class Worker extends Thread {
		/**
		 * Initializes a worker thread with a custom name.
		 */
		public Worker() {
			setName("Worker" + getName());
		}

		/**
		 * Continuously retrieves and executes tasks from the queue.
		 */
		@Override
		public void run() {
			Runnable task = null;

			try {
				while (true) {
					synchronized (tasks) {
						while (tasks.isEmpty() && !shutdown) {
							tasks.wait();
						}

						if (shutdown) {
							break;
						}

						task = tasks.removeFirst();
					}

					try {
						task.run();
					}
					catch (RuntimeException e) {
						logger.error("{} encountered an exception while running.", this.getName());
						logger.catching(Level.ERROR, e);
					}
					decrementPending();
				}
			}
			catch (InterruptedException e) {
				logger.warn("{} interrupted while waiting.", this.getName());
				logger.catching(Level.WARN, e);
				Thread.currentThread().interrupt();
			}
		}
	}
}
