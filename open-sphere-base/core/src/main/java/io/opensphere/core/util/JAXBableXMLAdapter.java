package io.opensphere.core.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Implementation of {@link XmlAdapter} for use with {@link JAXBable} objects.
 *
 * @param <S> The type of the wrapper object.
 * @param <T> The type of the wrapped object.
 */
public class JAXBableXMLAdapter<S extends JAXBWrapper<T>, T extends JAXBable<S>> extends XmlAdapter<S, T>
{
    @Override
    public S marshal(T v)
    {
        return v.getWrapper();
    }

    @Override
    public T unmarshal(S v)
    {
        return v.getWrappedObject();
    }
}
