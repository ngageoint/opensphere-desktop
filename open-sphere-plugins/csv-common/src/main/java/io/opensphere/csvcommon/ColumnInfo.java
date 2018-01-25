package io.opensphere.csvcommon;

import io.opensphere.mantle.data.SpecialKey;

/**
 * Class to hold column info.
 */
public class ColumnInfo
{
    /** The column name. */
    private String myColumnName;

    /** The column class. */
    private Class<?> myColumnClass;

    /** The special key. */
    private SpecialKey mySpecialType;

    /**
     * Constructor.
     *
     * @param columnName the column name
     * @param columnClass the column class
     */
    public ColumnInfo(String columnName, Class<?> columnClass)
    {
        this(columnName, columnClass, null);
    }

    /**
     * Constructor.
     *
     * @param columnName the column name
     * @param columnClass the column class
     * @param specialType the special key
     */
    public ColumnInfo(String columnName, Class<?> columnClass, SpecialKey specialType)
    {
        myColumnName = columnName;
        myColumnClass = columnClass == null ? String.class : columnClass;
        mySpecialType = specialType;
    }

    /**
     * Gets the columnName.
     *
     * @return the columnName
     */
    public String getColumnName()
    {
        return myColumnName;
    }

    /**
     * Sets the columnName.
     *
     * @param columnName the columnName
     */
    public void setColumnName(String columnName)
    {
        myColumnName = columnName;
    }

    /**
     * Gets the columnClass.
     *
     * @return the columnClass
     */
    public Class<?> getColumnClass()
    {
        return myColumnClass;
    }

    /**
     * Sets the columnClass.
     *
     * @param columnClass the columnClass
     */
    public void setColumnClass(Class<?> columnClass)
    {
        myColumnClass = columnClass;
    }

    /**
     * Gets the specialType.
     *
     * @return the specialType
     */
    public SpecialKey getSpecialType()
    {
        return mySpecialType;
    }

    /**
     * Sets the specialType.
     *
     * @param specialType the specialType
     */
    public void setSpecialType(SpecialKey specialType)
    {
        mySpecialType = specialType;
    }
}
