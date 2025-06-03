package edu.usfca.cs272.util;

import java.io.IOException;

//CITE: Derived from ChatGPT Prompt: "How can I implement a functional interface that throws an IOException"
/**
 * A functional interface that represents an operation that accepts a single
 * input argument and may throw an {@link IOException}.
 *
 * @param <T> the type of the input to the operation
 */
@FunctionalInterface
public interface IOThrowingConsumer<T> {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the input argument
	 * @throws IOException if an I/O error occurs
	 */
	void accept(T t) throws IOException;
}
