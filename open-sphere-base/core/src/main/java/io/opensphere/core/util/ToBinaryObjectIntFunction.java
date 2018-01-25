package io.opensphere.core.util;

import java.util.function.BiFunction;

/**
 * Represents a function that accepts two arguments and produces a binary-valued
 * result. This is the {@code binary}-producing primitive specialization for
 * {@link BiFunction}.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose
 * functional method is {@link #apply(Object, int)}.
 *
 * @param <T> the type of the first argument to the function.
 */
@FunctionalInterface
public interface ToBinaryObjectIntFunction<T>
{
    /**
     * Applies this operator to the given operands.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the operator result
     */
    boolean apply(T left, int right);
}
