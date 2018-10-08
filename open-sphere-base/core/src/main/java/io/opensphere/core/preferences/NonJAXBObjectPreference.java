package io.opensphere.core.preferences;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * A preference key and non-JAXB object value pair.
 *
 * @param <T> The type of value held by this object.
 */
class NonJAXBObjectPreference<T> extends JAXBElementPreference<T>
{
    /**
     * Constructor.
     *
     * @param key The preference key.
     * @param value The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public NonJAXBObjectPreference(String key, T value) throws IllegalArgumentException
    {
        super(key,
                new JAXBElement<>(new QName("value"), value == null ? (Class<T>)Void.class : (Class<T>)value.getClass(), value));
    }
}
