package io.opensphere.core.preferences;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.w3c.dom.Element;

import io.opensphere.core.util.SupplierX;
import io.opensphere.core.util.XMLUtilities;

/**
 * A preference key and JAXB object value pair.
 *
 * @param <T> The type of value held by this object.
 */
class JAXBObjectPreference<T> extends Preference<T>
{
    /** A supplier for the JAXB context. */
    private final SupplierX<JAXBContext, JAXBException> myJAXBContextSupplier;

    /**
     * Constructor.
     *
     * @param key The preference key.
     * @param contextSupplier Optional supplier of the JAXBContext used to
     *            marshal the value to XML.
     * @param value The value.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    public JAXBObjectPreference(String key, SupplierX<JAXBContext, JAXBException> contextSupplier, T value)
        throws IllegalArgumentException
    {
        super(key, value);
        myJAXBContextSupplier = contextSupplier;
    }

    /**
     * Constructor.
     *
     * @param key The preference key.
     * @param value The value.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    public JAXBObjectPreference(String key, T value) throws IllegalArgumentException
    {
        this(key, (SupplierX<JAXBContext, JAXBException>)null, value);
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod")
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod")
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    protected Element getElement() throws JAXBException
    {
        if (myJAXBContextSupplier == null)
        {
            return XMLUtilities.marshalJAXBObjectToElement(getValue((T)null, (SupplierX<JAXBContext, JAXBException>)null));
        }
        else
        {
            return XMLUtilities.marshalJAXBObjectToElement(getValue((T)null, (SupplierX<JAXBContext, JAXBException>)null),
                    myJAXBContextSupplier.get());
        }
    }
}
