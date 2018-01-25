package io.opensphere.core.cache;

/**
 * Interface to an object that can provide classes to the cache when the system
 * class loader would fail retrieving that class.
 */
public interface ClassProvider
{
    /**
     * Gets the class or null if it could not find it.
     *
     * @param className The name of the class to retrieve.
     * @return The class or null if there isn't such a class.
     */
    Class<?> getClass(String className);
}
