package io.opensphere.core.util;

import java.util.function.Predicate;

/**
 * A {@linkplain Predicate} that can supply a message that describes the result
 * of the test.
 *
 * @param <T> The input for the predicate.
 */
public interface PredicateWithMessage<T> extends Predicate<T>
{
    /**
     * Get a message describing the result of the last test.
     *
     * @return The message.
     */
    String getMessage();
}
