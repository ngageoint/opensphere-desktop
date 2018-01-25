package io.opensphere.core.util;

/**
 * Interface for objects that are not themselves JAXB objects (maybe because
 * they are immutable), but can be stored and retrieved using JAXB objects.
 *
 * @param <E> The type the wrapped object.
 */
@FunctionalInterface
public interface JAXBable<E extends JAXBWrapper<?>>
{
    /**
     * Get the JAXB object that can wrap me.
     *
     * @return The JAXB object.
     */
    E getWrapper();
}
