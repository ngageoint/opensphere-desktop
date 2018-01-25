package io.opensphere.core.cache.util;

import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.util.collections.New;

/**
 * A description of a model property array. A property array property allows for
 * arrays of heterogeneous objects to be stored as a single property in a data
 * model. The arrays can be stored and retrieved in bulk, which is faster than
 * storing the array elements as individual properties.
 * <p>
 * A descriptor may specify which columns are active for a particular
 * transaction, allowing the same column type array to be specified, even when
 * different columns are actually being affected. For example:
 * <ul>
 * <li>One component might insert a number of property arrays with three
 * elements each, specifying a column type array like [String, Float, Integer].
 * </li>
 * <li>Then that component could pass off those column types to another
 * component, which might only be interested in the middle element of the
 * arrays.</li>
 * <li>The second component could then request the property arrays, specifying
 * the same column type array passed by the first component, but specifying an
 * active column array of [1].</li>
 * <li>The values returned to the second component will be arrays of length 1,
 * containing Floats.</li>
 * </ul>
 * In this way, the second component doesn't have to be concerned with
 * constructing a new type array that has the correct types; it only has to
 * specify which columns it's interested in.
 */
public class PropertyArrayDescriptor extends PropertyDescriptor<Object[]>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The active column indices. */
    private final int[] myActiveColumns;

    /** The types of the property values in the arrays. */
    private final Class<?>[] myColumnTypes;

    /** The order-by column. */
    private final int myOrderByColumn;

    /**
     * Construct the property descriptor with all columns active.
     *
     * @param propertyName The name of the property.
     * @param columnTypes The array of column types.
     */
    public PropertyArrayDescriptor(String propertyName, Class<?>[] columnTypes)
    {
        this(propertyName, columnTypes, New.sequentialIntArray(0, columnTypes.length));
    }

    /**
     * Construct the property descriptor.
     *
     * @param propertyName The name of the property.
     * @param columnTypes The array of column types. This comprises all columns,
     *            not just the active ones.
     * @param activeColumns An array of column indices indicating which columns
     *            are active.
     */
    public PropertyArrayDescriptor(String propertyName, Class<?>[] columnTypes, int[] activeColumns)
    {
        this(propertyName, columnTypes, activeColumns, 0);
    }

    /**
     * Construct the property descriptor.
     *
     * @param propertyName The name of the property.
     * @param columnTypes The array of column types. This comprises all columns,
     *            not just the active ones.
     * @param activeColumns An array of column indices indicating which columns
     *            are active.
     * @param orderByColumn Index of the column used for ordering. This is only
     *            used in the context of an {@link OrderSpecifier}.
     */
    public PropertyArrayDescriptor(String propertyName, Class<?>[] columnTypes, int[] activeColumns, int orderByColumn)
    {
        super(propertyName, Object[].class);
        myColumnTypes = columnTypes.clone();
        myActiveColumns = activeColumns.clone();
        myOrderByColumn = orderByColumn;

        for (int index = 0; index < activeColumns.length; ++index)
        {
            if (myColumnTypes[activeColumns[index]] == null)
            {
                throw new IllegalArgumentException("Column type is null for active column index " + activeColumns[index]);
            }
        }
    }

    /**
     * Get the active column indices.
     *
     * @return The active column indices.
     */
    public int[] getActiveColumns()
    {
        return myActiveColumns.clone();
    }

    /**
     * Get the array of property types. This comprises all columns, not just the
     * active ones.
     *
     * @return An array of the types.
     */
    public Class<?>[] getColumnTypes()
    {
        return myColumnTypes.clone();
    }

    /**
     * Get the column used for ordering. This is only used in the context of an
     * {@link OrderSpecifier}.
     *
     * @return The order-by column index.
     */
    public int getOrderByColumn()
    {
        return myOrderByColumn;
    }
}
