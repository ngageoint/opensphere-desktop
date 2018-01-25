package io.opensphere.core.data.util;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * A simple query that requests a single property, but does not store the
 * results until completion. This is intended for cases where either the
 * requester does not use the results or the query returns so much data that it
 * needs to used in pieces by obtaining it from persistent store.
 *
 * @param <T> The property value type.
 */
public class SimpleDisposingQuery<T> extends DefaultQuery
{
    /**
     * A property value receiver which throws out values.
     *
     * @param <T> The type of the received properties.
     */
    private static class DisposingPropertyValueReceiver<T> extends AbstractPropertyValueReceiver<T>
    {
        /**
         * Constructor.
         *
         * @param propertyDescriptor The property descriptor.
         */
        public DisposingPropertyValueReceiver(PropertyDescriptor<T> propertyDescriptor)
        {
            super(propertyDescriptor);
        }

        @Override
        public void receive(List<? extends T> values)
        {
            // The purpose of this receiver is to do nothing with the results.
        }
    }

    /**
     * Construct a simple query that obtains all values for a single property,
     * but does not provide results back to the requester.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyDescriptor A description of the desired property.
     */
    public SimpleDisposingQuery(DataModelCategory dataModelCategory, PropertyDescriptor<T> propertyDescriptor)
    {
        this(dataModelCategory, propertyDescriptor, Collections.<PropertyMatcher<?>>emptyList());
    }

    /**
     * Construct a simple query that requests a single property with parameters,
     * but does not provide results back to the requester.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyDescriptor A description of the desired property.
     * @param propertyMatchers Parameters for the query to satisfy.
     */
    public SimpleDisposingQuery(DataModelCategory dataModelCategory, PropertyDescriptor<T> propertyDescriptor,
            List<? extends PropertyMatcher<?>> propertyMatchers)
    {
        super(dataModelCategory, Collections.singletonList(new DisposingPropertyValueReceiver<T>(propertyDescriptor)),
                propertyMatchers, Collections.<OrderSpecifier>emptyList());
    }

    /**
     * Construct a simple query that requests a single property with one
     * parameter, but does not provide results back to the requester.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyDescriptor A description of the desired property.
     * @param propertyMatcher A parameter for the query to satisfy.
     */
    public SimpleDisposingQuery(DataModelCategory dataModelCategory, PropertyDescriptor<T> propertyDescriptor,
            PropertyMatcher<?> propertyMatcher)
    {
        this(dataModelCategory, propertyDescriptor, Collections.<PropertyMatcher<?>>singletonList(propertyMatcher));
    }

    /**
     * Get the results of the query.
     *
     * @return The results.
     */
    public List<T> getResults()
    {
        throw new UnsupportedOperationException("Getting results is not supported for a disposing query.");
    }
}
