package io.opensphere.core.data;

import io.opensphere.core.data.util.DataModelCategory;

/**
 * Interface for listeners interested in changes to data registry contents.
 *
 * @param <T> The type of the property values of interest to the listener.
 */
public interface DataRegistryListener<T>
{
    /**
     * Method called when the registry is completely cleared.
     *
     * @param source the source of the operation
     * @see #valuesRemoved(DataModelCategory, long[], Iterable, Object)
     */
    void allValuesRemoved(Object source);

    /**
     * Get if this listener needs the array of ids to be supplied with each
     * notification. If this returns <code>false</code>, the caller has the
     * option of passing an empty array as the {@code ids} parameter to the
     * notification calls.
     *
     * @return A boolean.
     */
    boolean isIdArrayNeeded();

    /**
     * Get if this listener would like the
     * {@link #valuesRemoved(DataModelCategory, long[], Iterable, Object)}
     * method called when objects are removed. Returning true from this method
     * will cause removing values to be slower.
     *
     * @return boolean
     */
    boolean isWantingRemovedObjects();

    /**
     * Method called after values are added to the registry.
     *
     * @param dataModelCategory The data model category for the added models.
     * @param ids The data registry ids for the added models.
     * @param newValues The new values, in the same order as the ids.
     * @param source the source of the operation
     */
    void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends T> newValues, Object source);

    /**
     * Method called after values are removed from the registry. This method is
     * only called if there are removed values cached in memory.
     * <p>
     * This is <b>not</b> called if all values are removed! Be sure to implement
     * {@link #allValuesRemoved(Object)} as well.
     *
     * @param dataModelCategory The data model category for the removed models.
     * @param ids The data registry ids for the removed models.
     * @param removedValues The removed values. This will <b>only</b> provide
     *            the values that are currently cached in memory.
     * @param source the source of the operation
     * @see #valuesRemoved(DataModelCategory, long[], Object)
     * @see #allValuesRemoved(Object)
     */
    void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends T> removedValues, Object source);

    /**
     * Method called after values are removed from the registry.
     * <p>
     * This is <b>not</b> called if all values are removed! Be sure to implement
     * {@link #allValuesRemoved(Object)} as well.
     *
     * @param dataModelCategory The data model category for the removed models.
     * @param ids The data registry ids for the removed models.
     * @param source the source of the operation
     *
     * @see #allValuesRemoved(Object)
     */
    void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Object source);

    /**
     * Method called after values are updated in the registry.
     *
     * @param dataModelCategory The data model category for the updated models.
     * @param ids The data registry ids for the updated models.
     * @param newValues The new values, in the same order as the ids.
     * @param source the source of the operation
     */
    void valuesUpdated(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends T> newValues, Object source);
}
