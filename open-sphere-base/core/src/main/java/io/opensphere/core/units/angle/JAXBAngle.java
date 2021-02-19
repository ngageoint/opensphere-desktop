package io.opensphere.core.units.angle;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.JAXBWrapper;
import io.opensphere.core.util.Utilities;

/**
 * Wrapper for an {@link Coordinates} that can be marshalled and unmarshalled using
 * JAXB.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBAngle implements JAXBWrapper<Coordinates>
{
    /** The magnitude. */
    private double myMagnitude;

    /** The units. */
    private Class<? extends Coordinates> myUnits;

    /**
     * Constructor.
     *
     * @param angle The wrapped angle.
     */
    public JAXBAngle(Coordinates angle)
    {
        myUnits = angle.getClass();
        myMagnitude = angle.getMagnitude();
    }

    /** JAXB constructor. */
    protected JAXBAngle()
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
        JAXBAngle other = (JAXBAngle)obj;
        if (!Utilities.equalsOrBothNaN(myMagnitude, other.myMagnitude))
        {
            return false;
        }
        return Objects.equals(myUnits, other.myUnits);
    }

    @Override
    public Coordinates getWrappedObject()
    {
        return Coordinates.create(myUnits, myMagnitude);
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
