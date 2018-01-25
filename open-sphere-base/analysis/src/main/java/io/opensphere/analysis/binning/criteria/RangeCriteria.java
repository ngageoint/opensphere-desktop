package io.opensphere.analysis.binning.criteria;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The criteria type that accepts a certain +/- difference between values (bin
 * width) to consider them in the same bin.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RangeCriteria extends CriteriaType
{
    /**
     * The bin width property.
     */
    public static final String BIN_WIDTH_PROP = "binWidth";

    /**
     * The criteria type.
     */
    public static final String CRITERIA_TYPE = "Range";

    /**
     * The +/- difference to allow for each bin.
     */
    @XmlAttribute(name = "binWidth")
    private double myBinWidth = 10;

    /**
     * Gets the bin width.
     *
     * @return The +/- difference to allow for each bin.
     */
    public double getBinWidth()
    {
        return myBinWidth;
    }

    @Override
    public String getCriteriaType()
    {
        return CRITERIA_TYPE;
    }

    /**
     * Sets the bin width.
     *
     * @param binWidth The +/- difference to allow for each bin.
     */
    public void setBinWidth(double binWidth)
    {
        myBinWidth = binWidth;
        setChanged();
        notifyObservers(BIN_WIDTH_PROP);
    }
}
