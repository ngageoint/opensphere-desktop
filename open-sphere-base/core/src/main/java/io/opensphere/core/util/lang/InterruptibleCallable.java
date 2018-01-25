package io.opensphere.core.util.lang;

import java.util.concurrent.Callable;

/**
 * A {@link Callable} that can only throw {@link InterruptedException}.
 *
 * @param <V> The result type.
 */
@FunctionalInterface
public interface InterruptibleCallable<V> extends Callable<V>
{
    @Override
    V call() throws InterruptedException;
}
