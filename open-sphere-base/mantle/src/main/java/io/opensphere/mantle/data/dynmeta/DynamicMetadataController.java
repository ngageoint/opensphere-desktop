package io.opensphere.mantle.data.dynmeta;

import java.util.List;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Interface DynamicColumnController.
 *
 * @param <T> the generic type
 */
public interface DynamicMetadataController<T>
{
    /**
     * Append value to an existing value or set the value if there is no value.
     *
     * Implements are not required to implement this function and should throw a
     * {@link UnsupportedOperationException} if not implemented. Should follow
     * the supportAppend() result.
     *
     * @param cacheIds the cache ids
     * @param valueToAppend the value to append
     * @param source the source of the change
     * @throws UnsupportedOperationException if not supported
     */
    void appendValue(List<Long> cacheIds, Object valueToAppend, Object source);

    /**
     * Append value.
     *
     * Implements are not required to implement this function and should throw a
     * {@link UnsupportedOperationException} if not implemented. Should follow
     * the supportAppend() result.
     *
     * @param elementId the element id
     * @param valueToAppend the value to append
     * @param source the source of the change
     * @throws UnsupportedOperationException if not supported
     */
    void appendValue(long elementId, Object valueToAppend, Object source);

    /**
     * Clear all values.
     *
     * @param source the source of the change
     */
    void clear(Object source);

    /**
     * Clear values.
     *
     * @param elementIds the element ids
     * @param source the source of the change
     */
    void clearValues(List<Long> elementIds, Object source);

    /**
     * Clear value.
     *
     * @param source the source of the change
     * @param elementIds the element ids
     */
    void clearValues(Object source, long... elementIds);

    /**
     * Gets the class of values for this column.
     *
     * Must return a class of type T.
     *
     * @return the column class
     */
    Class<?> getColumnClass();

    /**
     * Gets the column index.
     *
     * @return the column index
     */
    int getColumnIndex();

    /**
     * Sets the column index.
     *
     * @param index the column index
     */
    void setColumnIndex(int index);

    /**
     * Gets the column name.
     *
     * @return the column name
     */
    String getColumnName();

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    DataTypeInfo getDataTypeInfo();

    /**
     * Gets the value.
     *
     * @param elementId the element id
     * @return the value
     */
    T getValue(long elementId);

    /**
     * Checks if the value is acceptable to the column controller, basically
     * that value is an instance of T or is assignable to T.
     *
     * @param value the value to test
     * @return true, if is acceptable value
     */
    boolean isAcceptableValueType(Object value);

    /**
     * Sets the value.
     *
     * @param elementId the element id
     * @param value the value
     * @param source the source of the change
     */
    void setValue(long elementId, Object value, Object source);

    /**
     * Sets the values.
     *
     * @param cacheIds the cache ids
     * @param value the value
     * @param source the source of the change
     */
    void setValues(List<Long> cacheIds, Object value, Object source);

    /**
     * Returns true if this dynamic column controller supports appending.
     *
     * @return true, if supports appending, false if not.
     */
    boolean supportsAppend();
}
