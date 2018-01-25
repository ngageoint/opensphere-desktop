package io.opensphere.core.cache.jdbc;

/**
 * A model for a primary key constraint. A primary key serves as a way to
 * identify each row in a table. The data in the primary key columns must be
 * unique.
 */
public class PrimaryKeyConstraint
{
    /**
     * The names of the columns that compose the constraint.
     */
    private final String[] myColumnNames;

    /**
     * Constructor.
     *
     * @param columnNames The names of the columns that compose the constraint.
     */
    public PrimaryKeyConstraint(String... columnNames)
    {
        myColumnNames = columnNames.clone();
    }

    /**
     * Get the names of the columns that compose the constraint.
     *
     * @return The column names.
     */
    public String[] getColumnNames()
    {
        return myColumnNames.clone();
    }
}
