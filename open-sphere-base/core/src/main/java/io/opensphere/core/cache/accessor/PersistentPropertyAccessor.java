package io.opensphere.core.cache.accessor;

/**
 * This is a marker interface for accessors that provide properties that may be
 * persisted.
 *
 * @param <S> The type of object that provides the property values.
 * @param <T> The type of the property values.
 */
public interface PersistentPropertyAccessor<S, T> extends PropertyAccessor<S, T>
{
}
