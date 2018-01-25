package io.opensphere.core.data.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.matcher.GeneralPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * A simple query that has parameters but no values are returned.
 */
public class SimpleIdQuery extends DefaultQuery
{
    /**
     * Construct a simple query with zero parameters.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     */
    public SimpleIdQuery(DataModelCategory dataModelCategory)
    {
        this(dataModelCategory, Collections.<PropertyMatcher<?>>emptyList());
    }

    /**
     * Construct a simple query with multiple parameters.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyMatchers Parameters for the query to satisfy.
     */
    public SimpleIdQuery(DataModelCategory dataModelCategory, List<? extends PropertyMatcher<?>> propertyMatchers)
    {
        super(dataModelCategory, Collections.<PropertyValueReceiver<?>>emptyList(), propertyMatchers,
                Collections.<OrderSpecifier>emptyList());
    }

    /**
     * Construct a simple query with one parameter.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param propertyMatcher A parameter for the query to satisfy.
     */
    public SimpleIdQuery(DataModelCategory dataModelCategory, PropertyMatcher<?> propertyMatcher)
    {
        this(dataModelCategory, Collections.<PropertyMatcher<?>>singletonList(propertyMatcher));
    }

    /**
     * Construct a simple query that will return the ids for records that have a
     * property equal to a given value.
     *
     * @param <T> The type of the property value.
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param value The property value to match.
     * @param propertyDescriptor The property descriptor.
     */
    public <T extends Serializable> SimpleIdQuery(DataModelCategory dataModelCategory, T value,
            PropertyDescriptor<T> propertyDescriptor)
    {
        this(dataModelCategory,
                Collections.<PropertyMatcher<?>>singletonList(new GeneralPropertyMatcher<T>(propertyDescriptor, value)));
    }
}
