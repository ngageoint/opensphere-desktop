package io.opensphere.core.cache.jdbc;

import io.opensphere.core.util.Utilities;

/**
 * A model for a constraint that requires that the data in some columns in a
 * table match the data in the corresponding columns in another table.
 */
public class ForeignKeyConstraint
{
    /**
     * The names of the columns in the foreign table.
     */
    private final String[] myForeignColumnNames;

    /**
     * The name of the foreign table.
     */
    private final String myForeignTableName;

    /**
     * The names of the columns in the local table that reference the foreign
     * table.
     */
    private final String[] myNativeColumnNames;

    /**
     * Constructor with a single column.
     *
     * @param nativeColumnName The name of the column in the local table that
     *            references the foreign table.
     * @param foreignTableName The name of the foreign table.
     * @param foreignColumnName The name of the column in the foreign table.
     */
    public ForeignKeyConstraint(String nativeColumnName, String foreignTableName, String foreignColumnName)
    {
        Utilities.checkNull(nativeColumnName, "nativeColumnName");
        Utilities.checkNull(foreignTableName, "foreignTableName");
        Utilities.checkNull(foreignColumnName, "foreignColumnName");
        myNativeColumnNames = new String[] { nativeColumnName };
        myForeignTableName = foreignTableName;
        myForeignColumnNames = nativeColumnName.equals(foreignColumnName) ? myNativeColumnNames
                : new String[] { foreignColumnName };
    }

    /**
     * Constructor.
     *
     * @param nativeColumnNames The names of the columns in the local table that
     *            reference the foreign table.
     * @param foreignTableName The name of the foreign table.
     * @param foreignColumnNames The names of the columns in the foreign table.
     */
    public ForeignKeyConstraint(String[] nativeColumnNames, String foreignTableName, String[] foreignColumnNames)
    {
        Utilities.checkNull(nativeColumnNames, "nativeColumnNames");
        Utilities.checkNull(foreignTableName, "foreignTableName");
        Utilities.checkNull(foreignColumnNames, "foreignColumnNames");
        if (nativeColumnNames.length == 0)
        {
            throw new IllegalArgumentException("NativeColumnNames cannot be empty.");
        }
        if (foreignColumnNames.length == 0)
        {
            throw new IllegalArgumentException("ForeignColumnNames cannot be empty.");
        }

        myNativeColumnNames = nativeColumnNames.clone();
        myForeignTableName = foreignTableName;
        myForeignColumnNames = foreignColumnNames.clone();
    }

    /**
     * Get the names of the columns in the foreign table.
     *
     * @return The foreignColumnNames.
     */
    public String[] getForeignColumnNames()
    {
        return myForeignColumnNames.clone();
    }

    /**
     * Get the name of the foreign table.
     *
     * @return The foreignTableName.
     */
    public String getForeignTableName()
    {
        return myForeignTableName;
    }

    /**
     * Get the names of the columns in the local table that reference the
     * foreign table.
     *
     * @return The nativeColumnNames.
     */
    public String[] getNativeColumnNames()
    {
        return myNativeColumnNames.clone();
    }
}
