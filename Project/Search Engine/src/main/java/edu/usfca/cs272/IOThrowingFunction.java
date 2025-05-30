package edu.usfca.cs272;

import java.io.IOException;

/**
 * A functional interface that allows for checked {@link IOException} to be
 * thrown. This interface is generic so it can be used with any input and output
 * types.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface IOThrowingFunction<T, R> {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 * @throws IOException if an IO error occurs during function execution
	 */
	R apply(T t) throws IOException;
}
