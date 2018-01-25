package io.opensphere.core.function;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts four input arguments and returns no
 * result. This is the four-arity specialization of {@link Consumer}. Unlike
 * most other functional interfaces, {@code TriConsumer} is expected to operate
 * via side-effects.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose
 * functional method is {@link #accept(Object, Object, Object, Object)}.
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @param <W> the type of the third argument to the operation
 * @param <X> the type of the fourth argument to the operation
 * @param <E> the exception type thrown by the consumer.
 *
 * @see Consumer
 */
@FunctionalInterface
public interface QuadConsumer<T, U, W, X, E extends Throwable>
{
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param w the third input argument
     * @param x the fourth input argument
     * @throws E if the consumer cannot accept the supplied input.
     */
    void accept(T t, U u, W w, X x) throws E;

    /**
     * Returns a composed {@code QuadConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation. If performing this operation throws an exception, the
     * {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code QuadConsumer} that performs in sequence this
     *         operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default QuadConsumer<T, U, W, X, E> andThen(QuadConsumer<? super T, ? super U, ? super W, ? super X, E> after) throws E
    {
        Objects.requireNonNull(after);

        return (l, r, w, x) ->
        {
            accept(l, r, w, x);
            after.accept(l, r, w, x);
        };
    }
}
