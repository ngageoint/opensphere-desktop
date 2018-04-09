package io.opensphere.core.data;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.Satisfaction;

/**
 * Interface for a provider of data to the {@link DataRegistry}.
 */
public interface DataRegistryDataProvider
{
    /**
     * Determine what combination of intervals can be satisfied by this data
     * provider. If this data provider is unconstrained (it can satisfy any
     * intervals), this returns a satisfaction containing each of the input
     * interval sets.
     *
     * @param dataModelCategory The data model category.
     * @param intervalSets The interval sets being queried.
     * @return {@link Satisfaction}s indicating what portion of the input
     *         interval sets can be satisfied by this data provider.
     */
    Collection<? extends Satisfaction> getSatisfaction(DataModelCategory dataModelCategory,
            Collection<? extends IntervalPropertyValueSet> intervalSets);

    /**
     * Get the name for the thread pool to be used when this data provider is
     * queried.
     *
     * @return The thread pool name.
     */
    String getThreadPoolName();

    /**
     * Determine if this provider can provide data for a certain category.
     *
     * @param category The category, which may contain {@code null}s for
     *            wildcards.
     * @return {@code true} if the category can be handled.
     */
    boolean providesDataFor(DataModelCategory category);

    /**
     * Query this data provider. Results of the query are packaged into
     * {@link Query} objects for deposit into the data registry. The
     * {@link Query} objects are sent to the provided {@code queryReceiver}.
     * <p>
     * <b>Note:</b> The thread this method is called on may be interrupted as an
     * indication to the data provider that the query should be cancelled.
     * Implementors should check the interrupted status of the thread
     * periodically and throw {@link InterruptedException} if necessary.
     *
     * @param category The data model category.
     * @param satisfactions What portion of the interval bounds of the query
     *            this provider claims to satisfy. This will be {@code null} if
     *            the query has no interval bounds.
     * @param parameters The non-interval bounds on the query.
     * @param orderSpecifiers The order specifiers for the query.
     * @param limit The limit on the number of results returned by this query.
     * @param propertyDescriptors Descriptors for the properties to be returned.
     * @param queryReceiver An object that will receive {@link Query} objects
     *            produced by this data provider.
     * @throws InterruptedException If the thread is interrupted.
     * @throws QueryException If the query fails.
     */
    void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws InterruptedException, QueryException;
}
