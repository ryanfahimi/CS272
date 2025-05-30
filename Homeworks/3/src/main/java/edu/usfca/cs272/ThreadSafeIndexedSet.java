package edu.usfca.cs272;

import java.util.Collection;
import java.util.NoSuchElementException;

import edu.usfca.cs272.utils.IndexedSet;
import edu.usfca.cs272.utils.MultiReaderLock;

/**
 * A thread-safe version of {@link IndexedSet} using a read/write lock.
 *
 * @param <E> element type
 * @see IndexedSet
 * @see MultiReaderLock
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class ThreadSafeIndexedSet<E> extends IndexedSet<E> {
	/** The lock used to protect concurrent access to the underlying set. */
	private final MultiReaderLock lock;

	/**
	 * Initializes an unsorted thread-safe indexed set.
	 */
	public ThreadSafeIndexedSet() {
		this(false);
	}

	/**
	 * Initializes a thread-safe indexed set.
	 *
	 * @param sorted whether the set should be sorted
	 */
	public ThreadSafeIndexedSet(boolean sorted) {
		super(sorted);
		lock = new MultiReaderLock();
	}

	/**
	 * Returns the identity hashcode of the lock object. Not particularly useful.
	 *
	 * @return the identity hashcode of the lock object
	 */
	public int lockCode() {
		return System.identityHashCode(lock);
	}

	/**
	 * Adds an element to our set in a thread-safe manner.
	 *
	 * @param element element to add
	 * @return true if the element was added (false if it was a duplicate)
	 *
	 */
	@Override
	public boolean add(E element) {
		lock.writeLock().lock();

		try {
			return super.add(element);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Adds the collection of elements to our set in a thread-safe manner.
	 *
	 * @param elements elements to add
	 * @return true if any elements were added (false if were all duplicates)
	 *
	 */
	@Override
	public boolean addAll(Collection<E> elements) {
		lock.writeLock().lock();

		try {
			return super.addAll(elements);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Adds values from an {@link IndexedSet} to a {@link ThreadSafeIndexedSet} in a
	 * thread-safe manner.
	 *
	 * @param elements elements to add
	 * @return true if any elements were added (false if were all duplicates)
	 *
	 */
	@Override
	public boolean addAll(IndexedSet<E> elements) {
		lock.writeLock().lock();

		try {
			return super.addAll(elements);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Returns the number of elements in our set in a thread-safe manner.
	 *
	 * @return number of elements
	 */
	@Override
	public int size() {
		lock.readLock().lock();

		try {
			return super.size();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns whether the element is contained in our set in a thread-safe manner.
	 *
	 * @param element element to search for
	 * @return true if the element is contained in our set
	 */
	@Override
	public boolean contains(E element) {
		lock.readLock().lock();

		try {
			return super.contains(element);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Gets the element at the specified index based on iteration order in a
	 * thread-safe manner. The element at this index may change over time as new
	 * elements are added.
	 *
	 * @param index index of element to get
	 * @return element at the specified index or null of the index was invalid
	 * @throws IndexOutOfBoundsException if index is out of bounds
	 */
	@Override
	public E get(int index) {
		lock.readLock().lock();

		try {
			return super.get(index);
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Gets the first element if it exists in a thread-safe manner.
	 *
	 * @return first element
	 * @throws NoSuchElementException if no first element
	 */
	@Override
	public E first() throws NoSuchElementException {
		lock.readLock().lock();

		try {
			return super.first();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Gets the last element if it exists in a thread-safe manner.
	 *
	 * @return last element
	 * @throws NoSuchElementException if no last element
	 */
	@Override
	public E last() throws NoSuchElementException {
		lock.readLock().lock();

		try {
			return super.last();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unsorted copy (shallow) of this set in a thread-safe manner.
	 *
	 * @return unsorted copy
	 */
	@Override
	public IndexedSet<E> unsortedCopy() {
		lock.readLock().lock();

		try {
			return super.unsortedCopy();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns a sorted copy (shallow) of this set in a thread-safe manner.
	 *
	 * @return sorted copy
	 */
	@Override
	public IndexedSet<E> sortedCopy() {
		lock.readLock().lock();

		try {
			return super.sortedCopy();
		}
		finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns a string representation of the set in a thread-safe manner.
	 * 
	 * @return a string representation of the set
	 */
	@Override
	public String toString() {
		lock.readLock().lock();

		try {
			return super.toString();
		}
		finally {
			lock.readLock().unlock();
		}
	}

}
