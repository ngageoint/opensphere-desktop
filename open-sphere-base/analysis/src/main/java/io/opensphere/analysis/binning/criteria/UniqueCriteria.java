package io.opensphere.analysis.binning.criteria;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The unique binning criteria type.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UniqueCriteria extends CriteriaType
{
    /**
     * The criteria type.
     */
    public static final String CRITERIA_TYPE = "Unique";

    @Override
    public String getCriteriaType()
    {
        return CRITERIA_TYPE;
    }
}
