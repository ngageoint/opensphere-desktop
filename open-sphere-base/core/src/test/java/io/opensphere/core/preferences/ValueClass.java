package io.opensphere.core.preferences;

import javax.xml.bind.annotation.XmlRootElement;

/** A test class. */
@XmlRootElement
public class ValueClass extends AbstractValueClass
{
    /** The value. */
    private int myValue;

    /**
     * Constructor.
     *
     * @param value The value.
     */
    public ValueClass(int value)
    {
        myValue = value;
    }

    /**
     * JAXB constructor.
     */
    protected ValueClass()
    {
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        return myValue == ((ValueClass)obj).myValue;
    }

    /**
     * Get the value.
     *
     * @return The value.
     */
    public int getValue()
    {
        return myValue;
    }

    @Override
    public int hashCode()
    {
        return 31 + myValue;
    }

    /**
     * Set the value.
     *
     * @param value The value.
     */
    public void setValue(int value)
    {
        myValue = value;
    }
}
