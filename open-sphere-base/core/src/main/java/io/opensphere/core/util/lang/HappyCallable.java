package io.opensphere.core.util.lang;

import java.util.concurrent.Callable;

/**
 * A {@link Callable} that does not throw any checked exceptions.
 *
 * @param <V> The result type.
 */
@FunctionalInterface
public interface HappyCallable<V> extends Callable<V>
{
    @Override
    V call();
}
