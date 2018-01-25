package io.opensphere.core.data.util;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * A simple query that requests a single property.
 *
 * @param <T> The property value type.
 */
public class SimpleQuery<T> extends DefaultQuery
{
    /**
     * Construct a simple query that returns all values for a single property.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyDescriptor A description of the desired property.
     */
    public SimpleQuery(DataModelCategory dataModelCategory, PropertyDescriptor<T> propertyDescriptor)
    {
        this(dataModelCategory, propertyDescriptor, Collections.<PropertyMatcher<?>>emptyList());
    }

    /**
     * Construct a simple query that requests a single property with parameters.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyDescriptor A description of the desired property.
     * @param propertyMatchers Parameters for the query to satisfy.
     */
    public SimpleQuery(DataModelCategory dataModelCategory, PropertyDescriptor<T> propertyDescriptor,
            List<? extends PropertyMatcher<?>> propertyMatchers)
    {
        super(dataModelCategory, Collections.singletonList(new DefaultPropertyValueReceiver<T>(propertyDescriptor)),
                propertyMatchers, Collections.<OrderSpecifier>emptyList());
    }

    /**
     * Construct a simple query that requests a single property with one
     * parameter.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyDescriptor A description of the desired property.
     * @param propertyMatcher A parameter for the query to satisfy.
     */
    public SimpleQuery(DataModelCategory dataModelCategory, PropertyDescriptor<T> propertyDescriptor,
            PropertyMatcher<?> propertyMatcher)
    {
        this(dataModelCategory, propertyDescriptor, Collections.<PropertyMatcher<?>>singletonList(propertyMatcher));
    }

    /**
     * Clear any results of the query.
     */
    public void clearResults()
    {
        ((DefaultPropertyValueReceiver<?>)getPropertyValueReceivers().get(0)).clearValues();
    }

    /**
     * Get the results of the query.
     *
     * @return The results.
     */
    public List<T> getResults()
    {
        @SuppressWarnings("unchecked")
        DefaultPropertyValueReceiver<T> receiver = (DefaultPropertyValueReceiver<T>)getPropertyValueReceivers().get(0);
        return receiver.getValues();
    }
}
