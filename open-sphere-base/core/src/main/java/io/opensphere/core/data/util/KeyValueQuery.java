package io.opensphere.core.data.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.matcher.GeneralPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * A key-value query. Return results for a <i>value</i> property that correspond
 * to a desired <i>key</i> property.
 *
 * @param <K> The type for the key property value.
 * @param <V> The type for the value property results.
 */
public class KeyValueQuery<K extends Serializable, V> extends DefaultQuery
{
    /**
     * Do a key-value query. Return results for a <i>value</i> property that
     * correspond to a desired <i>key</i> property.
     *
     * @param dataModelCategory The category of the models to be returned from
     *            the query. Any {@code null}s in the category are wildcards.
     * @param key The desired key.
     * @param keyPropertyDescriptor A description of the desired key property.
     * @param valuePropertyDescriptor A description of the desired value
     *            property.
     */
    public KeyValueQuery(DataModelCategory dataModelCategory, K key, PropertyDescriptor<K> keyPropertyDescriptor,
            PropertyDescriptor<V> valuePropertyDescriptor)
    {
        super(dataModelCategory, Collections.singletonList(new DefaultPropertyValueReceiver<>(valuePropertyDescriptor)),
                Collections.<PropertyMatcher<K>>singletonList(new GeneralPropertyMatcher<>(keyPropertyDescriptor, key)),
                Collections.<OrderSpecifier>emptyList());
    }

    /**
     * Get the results of the query.
     *
     * @return The results.
     */
    public List<V> getResults()
    {
        @SuppressWarnings("unchecked")
        DefaultPropertyValueReceiver<V> receiver = (DefaultPropertyValueReceiver<V>)getPropertyValueReceivers().get(0);
        return receiver.getValues();
    }
}
