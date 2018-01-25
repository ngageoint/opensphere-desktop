package io.opensphere.kml.common.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Resettable;

/** KMLSettings domain model. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class KMLSettings implements Serializable, Resettable
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The scaling method. */
    @XmlElement(name = "scalingMethod")
    private volatile ScalingMethod myScalingMethod = ScalingMethod.GOOGLE_EARTH;

    /**
     * Sets the scaling method.
     *
     * @param scalingMethod the scaling method
     */
    public void setScalingMethod(ScalingMethod scalingMethod)
    {
        myScalingMethod = scalingMethod;
    }

    /**
     * Gets the scaling method.
     *
     * @return the scaling method
     */
    public ScalingMethod getScalingMethod()
    {
        return myScalingMethod;
    }

    @Override
    public void reset()
    {
        myScalingMethod = ScalingMethod.GOOGLE_EARTH;
    }
}
