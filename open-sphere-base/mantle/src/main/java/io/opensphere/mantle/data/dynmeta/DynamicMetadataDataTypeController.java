package io.opensphere.mantle.data.dynmeta;

import java.util.List;
import java.util.Set;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class DynamicColumnDataTypeCoordinator.
 */
public interface DynamicMetadataDataTypeController
{
    /**
     * Adds the dynamic column to the type if not already a defined column name.
     *
     * @param columnName the column name for the dynamic column
     * @param columnClass the column class
     * @param source the source of the add request
     * @return true, if successful, false if column already exists
     */
    boolean addDynamicColumn(String columnName, Class<?> columnClass, Object source);

    /**
     * Removes the dynamic column.
     *
     * @param columnName the column name for the dynamic column
     * @param columnClass the column class
     * @param source the source of the add request
     * @return true, if successful, false if column already exists
     */
    boolean removeDynamicColumn(String columnName, Class<?> columnClass, Object source);

    /**
     * Append values.
     *
     * @param cacheIds the cache ids
     * @param columnIndex the column index
     * @param value the value
     * @param source the source
     */
    void appendValue(List<Long> cacheIds, int columnIndex, Object value, Object source);

    /**
     * Append value to multiple ids.
     *
     * Note: OperationNotSupportedException is thrown if the column does not
     * support appending.
     *
     * @param cacheIds the cache ids
     * @param columnName the column name
     * @param value the value
     * @param source the source
     */
    void appendValue(List<Long> cacheIds, String columnName, Object value, Object source);

    /**
     * Append value.
     *
     * Note: OperationNotSupportedException is thrown if the column does not
     * support appending.
     *
     * @param elementCacheId the element cache id
     * @param columnIndex the column index
     * @param value the value
     * @param source the source
     */
    void appendValue(long elementCacheId, int columnIndex, Object value, Object source);

    /**
     * Append value.
     *
     * Note: OperationNotSupportedException is thrown if the column does not
     * support appending.
     *
     * @param elementCacheId the element cache id
     * @param columnName the column name
     * @param value the value
     * @param source the source
     */
    void appendValue(long elementCacheId, String columnName, Object value, Object source);

    /**
     * Clear dynamic column values.
     *
     * @param columnIndex the column index
     * @param source the source of the change
     * @throws IllegalArgumentException if the column is not a dynamic column.
     */
    void clearValues(int columnIndex, Object source);

    /**
     * Clears the values for all dynamic columns for the specified cache ids.
     *
     * @param cacheIds to clear values.
     * @param source the source of the change.
     */
    void clearValues(List<Long> cacheIds, Object source);

    /**
     * Clear dynamic column values.
     *
     * @param dynamicColumnName the dynamic column name
     * @param source the source of the change
     * @throws IllegalArgumentException if the column is not a dynamic column.
     */
    void clearValues(String dynamicColumnName, Object source);

    /**
     * Column supports appending.
     *
     * @param columnIndex the column index
     * @return true, if successful
     */
    boolean columnSupportsAppending(int columnIndex);

    /**
     * Column supports appending.
     *
     * @param columnName the column name
     * @return true, if successful
     */
    boolean columnSupportsAppending(String columnName);

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    DataTypeInfo getDataTypeInfo();

    /**
     * Gets the dynamic column count.
     *
     * @return the dynamic column count
     */
    int getDynamicColumnCount();

    /**
     * Gets the dynamic column index.
     *
     * @param columnName the column name
     * @return the dynamic column index or -1 if the name is not a dynamic
     *         column.
     */
    int getDynamicColumnIndex(String columnName);

    /**
     * Gets the dynamic column name.
     *
     * @param index the index
     * @return the dynamic column name
     * @throws IllegalArgumentException if index is not a dynamic column
     */
    String getDynamicColumnName(int index);

    /**
     * Gets the dynamic column names.
     *
     * @return the dynamic column names
     */
    Set<String> getDynamicColumnNames();

    /**
     * Gets the dynamic column names of type.
     *
     * @param type the type
     * @param appendableOnly if true returns only columns that can be appended
     *            to.
     * @return the dynamic column names of type
     */
    Set<String> getDynamicColumnNamesOfType(Class<?> type, boolean appendableOnly);

    /**
     * Gets the original column count.
     *
     * @return the original column count
     */
    int getOriginalColumnCount();

    /**
     * Gets the value.
     *
     * @param elementCacheId the element cache id
     * @param columnIndex the column index
     * @return the value
     * @throws IllegalArgumentException if the column is not a dynamic column.
     */
    Object getValue(long elementCacheId, int columnIndex);

    /**
     * Gets the value.
     *
     * @param elementCacheId the element cache id
     * @param columnName the column name
     * @return the value
     * @throws IllegalArgumentException if the column is not a dynamic column.
     */
    Object getValue(long elementCacheId, String columnName);

    /**
     * Checks to see if the columnName is a dynamic column.
     *
     * @param columnName the column name to check
     * @return true if dynamic, false if not or not a valid column.
     */
    boolean isDynamicColumn(String columnName);

    /**
     * Checks if is dynamic column index.
     *
     * @param index the index
     * @return true, if is dynamic column index
     */
    boolean isDynamicColumnIndex(int index);

    /**
     * Checks if is valid new dynamic column name.
     *
     * @param columnName the column name
     * @return true, if is valid new dynamic column name
     */
    boolean isValidNewDynamicColumnName(String columnName);

    /**
     * Sets the value.
     *
     * @param elementCacheId the element cache id
     * @param columnIndex the column index
     * @param value the value
     * @param source the source of the change
     * @throws IllegalArgumentException if the column is not a dynamic column.
     */
    void setValue(long elementCacheId, int columnIndex, Object value, Object source);

    /**
     * Sets the value.
     *
     * @param elementCacheId the element cache id
     * @param columnName the column name
     * @param value the value
     * @param source the source of the change
     */
    void setValue(long elementCacheId, String columnName, Object value, Object source);

    /**
     * Sets the dynamic column values values.
     *
     * @param cacheIds the cache ids
     * @param columnIndex the column index
     * @param value the value@param source the source of the change
     * @param source the source of the change
     * @throws IllegalArgumentException if the column is not a dynamic column.
     */
    void setValues(List<Long> cacheIds, int columnIndex, Object value, Object source);

    /**
     * Sets the values.
     *
     * @param cacheIds the cache ids
     * @param columnName the column name
     * @param value the value
     * @param source the source of the change
     * @throws IllegalArgumentException if the column is not a dynamic column.
     */
    void setValues(List<Long> cacheIds, String columnName, Object value, Object source);

    /**
     * True if this data type supports dynamic columns.
     *
     * @return true if supports, false if not
     */
    boolean supportsDynamicColumns();
}
