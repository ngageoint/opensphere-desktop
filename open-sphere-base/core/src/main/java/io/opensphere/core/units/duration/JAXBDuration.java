package io.opensphere.core.units.duration;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.JAXBWrapper;

/**
 * Wrapper for a {@link Duration} that can be marshalled and unmarshalled using
 * JAXB.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class JAXBDuration implements JAXBWrapper<Duration>
{
    /** The scale for the magnitude. */
    private int myScale;

    /** The units for the duration. */
    private Class<? extends Duration> myUnits;

    /** The unscaled magnitude. Units are defined by subclasses. */
    private long myUnscaledMagnitude;

    /**
     * Constructor that takes a Duration.
     *
     * @param dur The duration.
     */
    public JAXBDuration(Duration dur)
    {
        setDuration(dur);
    }

    /** JAXB constructor. */
    protected JAXBDuration()
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
        JAXBDuration other = (JAXBDuration)obj;
        if (myScale != other.myScale)
        {
            return false;
        }
        if (!Objects.equals(myUnits, other.myUnits))
        {
            return false;
        }
        return myUnscaledMagnitude == other.myUnscaledMagnitude;
    }

    @Override
    public Duration getWrappedObject()
    {
        return Duration.create((Class<? extends Duration>)myUnits, myUnscaledMagnitude, myScale);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myScale;
        result = prime * result + (myUnits == null ? 0 : myUnits.hashCode());
        result = prime * result + (int)(myUnscaledMagnitude ^ myUnscaledMagnitude >>> 32);
        return result;
    }

    /**
     * Mutator for the duration.
     *
     * @param duration The duration to set.
     */
    public void setDuration(Duration duration)
    {
        myUnits = duration.getClass();
        myUnscaledMagnitude = duration.getUnscaledMagnitude();
        myScale = duration.getScale();
    }
}
