package io.opensphere.core.datafilter;

import java.util.List;
import java.util.function.Function;

/**
 * The Interface DataFilter.
 */
public interface DataFilter extends DataFilterItem
{
    /**
     * Get a new filter that is the combination of this filter and another
     * filter.
     *
     * @param filter The other filter.
     * @return The new filter.
     */
    DataFilter and(DataFilter filter);

    /**
     * Clone me, applying a function to the field names in my criteria.
     *
     * @param transform The field name transform.
     * @return The new data filter.
     */
    DataFilter applyFieldNameTransform(Function<String, String> transform);

    /**
     * Gets the columns.
     *
     * @return the columns
     */
    List<? extends String> getColumns();

    /**
     * Gets the number of filters that comprise this filter.
     *
     * @return The number of filters.
     */
    int getFilterCount();

    /**
     * Get the filter description.
     *
     * @return The filter description.
     */
    String getFilterDescription();

    /**
     * Gets the filter group.
     *
     * @return the filter group
     */
    DataFilterGroup getFilterGroup();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Get the server name.
     *
     * @return The server name.
     */
    String getServerName();

    /**
     * Gets the type key.
     *
     * @return the type key
     */
    String getTypeKey();

    /**
     * Checks if is active.
     *
     * @return true, if is active
     */
    boolean isActive();
}
