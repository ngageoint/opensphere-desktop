package io.opensphere.auxiliary.cache.jdbc;

/**
 * Interface for a facility that compacts the database.
 */
@FunctionalInterface
public interface DatabaseCompactor
{
    /**
     * Delete data for some groups and compact the database.
     *
     * @param groupIds The group ids.
     */
    void compact(int[] groupIds);
}
