package io.opensphere.core.preferences;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.w3c.dom.Element;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;

/**
 * A preference key and JAXB object value pair.
 *
 * @param <T> The type of value held by this object.
 */
class JAXBElementPreference<T> extends Preference<T>
{
    /**
     * Constructor.
     *
     * @param key The preference key.
     * @param value The value.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    public JAXBElementPreference(String key, JAXBElement<T> value) throws IllegalArgumentException
    {
        super(key, Utilities.checkNull(value, "value").getDeclaredType(), value);
    }

    @Override
    protected Element getElement() throws JAXBException
    {
        if (getData() instanceof JAXBElement)
        {
            return XMLUtilities.marshalJAXBObjectToElement(getData());
        }
        return (Element)getData();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T getValue() throws JAXBException
    {
        Object data = getData();
        if (data instanceof JAXBElement)
        {
            return ((JAXBElement<T>)data).getValue();
        }
        synchronized (data)
        {
            return (T)XMLUtilities.readXMLObject((Element)data, JAXBElement.class).getValue();
        }
    }
}
