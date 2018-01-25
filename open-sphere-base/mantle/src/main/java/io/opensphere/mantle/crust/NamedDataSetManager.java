package io.opensphere.mantle.crust;

/**
 * A contract defining an entity that manages named data sets.
 */
public interface NamedDataSetManager
{
    /**
     * Index a new data set by its name within the resident Map (implementation
     * must be thread-safe).
     *
     * @param data the generic data set to add to the resident map.
     */
    void addDataSet(GenericDataSet data);

    /**
     * Remove a data set from the resident Map (implementation must be
     * thread-safe).
     *
     * @param name name of the removed data
     */
    void removeDataSet(String name);
}
