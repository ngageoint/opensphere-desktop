package io.opensphere.core.util;

/**
 * Interface for an object that wraps another object to make it compatible with
 * JAXB.
 *
 * @param <E> The type of the wrapped object.
 */
@FunctionalInterface
public interface JAXBWrapper<E extends JAXBable<? extends JAXBWrapper<E>>>
{
    /**
     * Get the wrapped object.
     *
     * @return The object.
     */
    E getWrappedObject();
}
