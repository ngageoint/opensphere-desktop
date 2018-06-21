package io.opensphere.core.data.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.util.Utilities;

/**
 * Default query implementation.
 */
public class DefaultQuery implements Query
{
    /** The batch size for the query. */
    private final int myBatchSize;

    /** The data model category for the query, which may contain wildcards. */
    private final DataModelCategory myDataModelCategory;

    /** The limit on the number of records returned. */
    private final int myLimit;

    /** The order specifiers. */
    private final List<? extends OrderSpecifier> myOrderSpecifiers;

    /** The parameters on the query. */
    private final List<? extends PropertyMatcher<?>> myParameters;

    /** The receivers for the query results. */
    private final List<? extends PropertyValueReceiver<?>> myPropertyValueReceivers;

    /** What index to start with. */
    private final int myStartIndex;

    /**
     * Construct a query that gets all results in one batch, that has no
     * parameters and no order.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyValueReceivers The objects that define what properties are
     *            desired and also accept the results.
     */
    public DefaultQuery(DataModelCategory dataModelCategory,
            Collection<? extends PropertyValueReceiver<?>> propertyValueReceivers)
    {
        this(dataModelCategory, propertyValueReceivers, Collections.<PropertyMatcher<?>>emptyList(),
                Collections.<OrderSpecifier>emptyList());
    }

    /**
     * Construct a query that gets all results in one batch.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyValueReceivers The objects that define what properties are
     *            desired and also accept the results.
     * @param parameters The parameters on the query.
     * @param orderSpecifiers If the results need to be ordered, this list
     *            specifies the order-by properties. Otherwise this list may be
     *            empty.
     */
    public DefaultQuery(DataModelCategory dataModelCategory,
            Collection<? extends PropertyValueReceiver<?>> propertyValueReceivers,
            @Nullable List<? extends PropertyMatcher<?>> parameters, @Nullable List<? extends OrderSpecifier> orderSpecifiers)
    {
        this(dataModelCategory, propertyValueReceivers, parameters, orderSpecifiers, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Construct the query.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyValueReceivers The objects that define what properties are
     *            desired and also accept the results.
     * @param parameters The parameters on the query.
     * @param orderSpecifiers If the results need to be ordered, this list
     *            specifies the order-by properties. Otherwise this list may be
     *            empty.
     * @param startIndex The index of the first result to return (zero-based).
     * @param batchSize The size of the batches to be returned.
     * @param limit The limit on the total number of records returned (all
     *            batches).
     */
    public DefaultQuery(DataModelCategory dataModelCategory,
            Collection<? extends PropertyValueReceiver<?>> propertyValueReceivers,
            @Nullable List<? extends PropertyMatcher<?>> parameters, @Nullable List<? extends OrderSpecifier> orderSpecifiers,
            int startIndex, int batchSize, int limit)
    {
        Utilities.checkNull(propertyValueReceivers, "propertyValueReceivers");
        myDataModelCategory = Utilities.checkNull(dataModelCategory, "dataModelCategory");
        myPropertyValueReceivers = unmodCopy(propertyValueReceivers);
        myParameters = unmodCopy(parameters);
        myOrderSpecifiers = unmodCopy(orderSpecifiers);
        myStartIndex = startIndex;
        myBatchSize = batchSize;
        myLimit = limit;
    }

    /**
     * Null-tolerant method for creating an unmodifiable List from a given,
     * possibly modifiable, Collection. The new List is made from a copy of the
     * original so that new List is not affected if the original is modified. If
     * the argument is null, then the result is also null.
     *
     * @param c bla
     * @param <E> bla
     * @return bla
     */
    private static <E> List<E> unmodCopy(Collection<E> c)
    {
        if (c == null)
        {
            return null;
        }
        return Collections.unmodifiableList(new ArrayList<>(c));
    }

    /**
     * Construct a query that takes parameters but doesn't return any values.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param parameters The parameters on the query.
     */
    public DefaultQuery(DataModelCategory dataModelCategory, List<? extends PropertyMatcher<?>> parameters)
    {
        this(dataModelCategory, Collections.<PropertyValueReceiver<?>>emptySet(), parameters,
                Collections.<OrderSpecifier>emptyList());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (Utilities.sameInstance(this, obj))
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultQuery other = (DefaultQuery)obj;
        return myDataModelCategory.equals(other.myDataModelCategory)
                && myPropertyValueReceivers.equals(other.myPropertyValueReceivers) && myParameters.equals(other.myParameters)
                && myStartIndex == other.myStartIndex && myBatchSize == other.myBatchSize && myLimit == other.myLimit;
    }

    @Override
    public int getBatchSize()
    {
        return myBatchSize;
    }

    @Override
    public DataModelCategory getDataModelCategory()
    {
        return myDataModelCategory;
    }

    @Override
    public int getLimit()
    {
        return myLimit;
    }

    @Override
    public List<? extends OrderSpecifier> getOrderSpecifiers()
    {
        return myOrderSpecifiers;
    }

    @Override
    public List<? extends PropertyMatcher<?>> getParameters()
    {
        return myParameters;
    }

    @Override
    public List<? extends PropertyValueReceiver<?>> getPropertyValueReceivers()
    {
        return myPropertyValueReceivers;
    }

    @Override
    public int getStartIndex()
    {
        return myStartIndex;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myDataModelCategory.hashCode();
        result = prime * result + myPropertyValueReceivers.hashCode();
        result = prime * result + myParameters.hashCode();
        result = prime * result + myStartIndex;
        result = prime * result + myBatchSize;
        result = prime * result + myLimit;
        return result;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(getClass().getSimpleName()).append("[category[").append(getDataModelCategory())
                .append("] params[").append(getParameters()).append("]]").toString();
    }
}
