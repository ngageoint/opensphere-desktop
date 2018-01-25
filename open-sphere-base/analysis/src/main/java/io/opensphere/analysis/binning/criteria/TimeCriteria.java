package io.opensphere.analysis.binning.criteria;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The criteria type for date/time values.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class TimeCriteria extends CriteriaType
{
    /**
     * The criteria type.
     */
    public static final String CRITERIA_TYPE = "Time";

    /**
     * The bin type.
     */
    @XmlAttribute(name = "binType")
    private TimeBinType myBinType;

    @Override
    public String getCriteriaType()
    {
        return CRITERIA_TYPE;
    }

    /**
     * Gets the bin type.
     *
     * @return the bin type
     */
    public TimeBinType getBinType()
    {
        return myBinType;
    }

    /**
     * Sets the bin type.
     *
     * @param binType the bin type
     */
    public void setBinType(TimeBinType binType)
    {
        myBinType = binType;
        setChanged();
        notifyObservers("binType");
    }
}
