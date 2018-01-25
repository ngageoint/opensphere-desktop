package io.opensphere.auxiliary.cache.jdbc;

import io.opensphere.core.cache.jdbc.SQLGeneratorImpl;

/**
 * SQL generator with H2 extensions.
 */
public class H2SQLGeneratorImpl extends SQLGeneratorImpl
{
    /**
     * Generate SQL for getting the database page count.
     *
     * @return The SQL.
     */
    public String generateGetPageCount()
    {
        return "SELECT value FROM INFORMATION_SCHEMA.SETTINGS WHERE name = 'info.PAGE_COUNT'";
    }

    /**
     * Generate SQL for getting the database page size.
     *
     * @return The SQL.
     */
    public String generateGetPageSize()
    {
        return "SELECT value FROM INFORMATION_SCHEMA.SETTINGS WHERE name = 'info.PAGE_SIZE'";
    }
}
