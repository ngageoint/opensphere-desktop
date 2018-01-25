package io.opensphere.core.data.util;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.DataRegistry;

/**
 * A query for property values that match a number of query parameters.
 *
 * @see DataRegistry#submitQuery(Query)
 */
public interface Query
{
    /**
     * Get the number of values to be returned in each batch.
     *
     * @return The batch size.
     */
    int getBatchSize();

    /**
     * Get the data model category for this query. Any portions of the category
     * that are {@code null} are wildcards.
     *
     * @return The data model category.
     */
    DataModelCategory getDataModelCategory();

    /**
     * Get the total number of values expected.
     *
     * @return The total number of values.
     */
    int getLimit();

    /**
     * Get the order specifiers for this query. Results shall be ordered first
     * by the first specifier in this list, and then ties broken by the second
     * specifier, etc. If this method returns an empty list or <code>null</code>
     * , the order is unspecified. Queries with unspecified order are faster
     * than ordered queries.
     *
     * @return The order specifiers, or <code>null</code> if there are none.
     */
    List<? extends OrderSpecifier> getOrderSpecifiers();

    /**
     * The parameters that specify the bounds of the query.
     *
     * @return A list of parameters, or <code>null</code> if there are none.
     */
    List<? extends PropertyMatcher<?>> getParameters();

    /**
     * Get the receivers to be called with the requested property values.
     *
     * @return The property receivers.
     */
    Collection<? extends PropertyValueReceiver<?>> getPropertyValueReceivers();

    /**
     * Get the start index to be handled. The first index is 0.
     *
     * @return The start index.
     */
    int getStartIndex();
}
