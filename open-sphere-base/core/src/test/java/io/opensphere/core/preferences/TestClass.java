package io.opensphere.core.preferences;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A test JAXB object.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TestClass
{
    /** A field. */
    @XmlElementRef
    private AbstractValueClass myValue;

    /**
     * Constructor.
     */
    protected TestClass()
    {
    }

    /**
     * Constructor that takes a value.
     *
     * @param value The value.
     */
    protected TestClass(AbstractValueClass value)
    {
        myValue = value;
    }

    /**
     * Get the value.
     *
     * @return The value.
     */
    public AbstractValueClass getValue()
    {
        return myValue;
    }

    /**
     * Set the value.
     *
     * @param value The value.
     */
    public void setValue(AbstractValueClass value)
    {
        myValue = value;
    }
}
