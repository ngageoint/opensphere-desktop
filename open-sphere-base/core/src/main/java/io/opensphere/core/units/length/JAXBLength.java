package io.opensphere.core.units.length;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.JAXBWrapper;
import io.opensphere.core.util.Utilities;

/**
 * Wrapper for a {@link Length} that can be marshalled and unmarshalled using
 * JAXB.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@javax.annotation.concurrent.ThreadSafe
public class JAXBLength implements JAXBWrapper<Length>
{
    /** The magnitude. */
    private double myMagnitude;

    /** The units. */
    private Class<? extends Length> myUnits;

    /**
     * Constructor.
     *
     * @param length The wrapped length.
     */
    public JAXBLength(Length length)
    {
        myUnits = length.getClass();
        myMagnitude = length.getMagnitude();
    }

    /** JAXB constructor. */
    protected JAXBLength()
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
        JAXBLength other = (JAXBLength)obj;
        if (!Utilities.equalsOrBothNaN(myMagnitude, other.myMagnitude))
        {
            return false;
        }
        return Objects.equals(myUnits, other.myUnits);
    }

    @Override
    public Length getWrappedObject()
    {
        return Length.create(myUnits, myMagnitude);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(myMagnitude);
        result = prime * result + (int)(temp ^ temp >>> 32);
        result = prime * result + (myUnits == null ? 0 : myUnits.hashCode());
        return result;
    }
}
