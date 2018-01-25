package io.opensphere.analysis.binning.criteria;

import java.util.Observable;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Interface to a binning criteria type such as unique, or range.
 */
@XmlTransient //Prevents the mapping of a JavaBean property/type to XML representation
@XmlSeeAlso({UniqueCriteria.class, RangeCriteria.class})
public abstract class CriteriaType extends Observable
{
    /**
     * Gets the criteria type.
     *
     * @return The criteria type.
     */
    public abstract String getCriteriaType();
}
