package io.opensphere.csvcommon.ui.columndefinition.model;

import java.util.Observable;

/**
 * Represents one row in the Column Definition table.
 *
 */
public class ColumnDefinitionRow extends Observable
{
    /**
     * The column name property name.
     */
    public static final String COLUMN_NAME_PROPERTY = "columnName";

    /**
     * The Data Type property name.
     */
    public static final String DATA_TYPE_PROPERTY = "dataType";

    /**
     * The format property name.
     */
    public static final String FORMAT_PROPERTY = "format";

    /**
     * The import property name.
     */
    public static final String IMPORT_PROPERTY = "import";

    /**
     * The id of the column.
     */
    private int myColumnId;

    /**
     * The name of the column.
     */
    private String myColumnName;

    /**
     * The data type of the column.
     */
    private String myDataType;

    /**
     * The format of the column.
     */
    private String myFormat;

    /**
     * Indicates if this column should be imported or not.
     */
    private boolean myIsImport;

    /**
     * Gets the id of the column.
     *
     * @return The id of the column.
     */
    public int getColumnId()
    {
        return myColumnId;
    }

    /**
     * Gets the column name.
     *
     * @return The column name.
     */
    public String getColumnName()
    {
        return myColumnName;
    }

    /**
     * Gets the data type of the column.
     *
     * @return The data type of the column, or null if it is not a special type.
     */
    public String getDataType()
    {
        return myDataType;
    }

    /**
     * Gets the format the data in this column.
     *
     * @return The format of the data in this column.
     */
    public String getFormat()
    {
        return myFormat;
    }

    /**
     * Indicates if this column should be imported.
     *
     * @return True if this column should be imported, false if it should be
     *         skipped on import.
     */
    public boolean isImport()
    {
        return myIsImport;
    }

    /**
     * Sets the column id.
     *
     * @param columnId The id of the column.
     */
    public void setColumnId(int columnId)
    {
        myColumnId = columnId;
    }

    /**
     * Sets the column name.
     *
     * @param columnName The name of the column.
     */
    public void setColumnName(String columnName)
    {
        myColumnName = columnName;
        setChanged();
        notifyObservers(COLUMN_NAME_PROPERTY);
    }

    /**
     * Sets the data type of the column.
     *
     * @param dataType The data type of the column or null if it is not a
     *            special type.
     */
    public void setDataType(String dataType)
    {
        myDataType = dataType;
        setChanged();
        notifyObservers(DATA_TYPE_PROPERTY);
    }

    /**
     * Sets the format of the data in this column.
     *
     * @param format The format fo the dat in this column.
     */
    public void setFormat(String format)
    {
        myFormat = format;
        setChanged();
        notifyObservers(FORMAT_PROPERTY);
    }

    /**
     * Sets if this column should be imported.
     *
     * @param isImport True if this column should be imported, false if it
     *            should be skipped on import.
     */
    public void setIsImport(boolean isImport)
    {
        myIsImport = isImport;
        setChanged();
        notifyObservers(IMPORT_PROPERTY);
    }
}
