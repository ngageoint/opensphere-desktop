package io.opensphere.analysis.binning.criteria;

import java.util.Observable;
import java.util.Observer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents one bin criteria that can be placed on a specified column.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BinCriteriaElement extends Observable implements Observer
{
    /**
     * The criteria type property.
     */
    public static final String CRITERIA_TYPE_PROP = "criteriaType";

    /**
     * The field property.
     */
    public static final String FIELD_PROP = "field";

    /**
     * The criteria details.
     */
    private CriteriaType myCriteriaType;

    /**
     * The field this criteria is applied to.
     */
    @XmlAttribute(name = "field")
    private String myField;

    /**
     * Gets the criteria details.
     *
     * @return The criteria details.
     */
    public CriteriaType getCriteriaType()
    {
        return myCriteriaType;
    }

    /**
     * Gets the field this criteria is applied to.
     *
     * @return The field this criteria is applied to.
     */
    public String getField()
    {
        return myField;
    }

    /**
     * Sets the criteria details.
     *
     * @param criteriaType The criteria details.
     */
    @XmlElement(name = "criteriaType")
    public void setCriteriaType(CriteriaType criteriaType)
    {
        if (myCriteriaType != null)
        {
            myCriteriaType.deleteObserver(this);
        }
        myCriteriaType = criteriaType;
        if (myCriteriaType != null)
        {
            myCriteriaType.addObserver(this);
        }
        setChanged();
        notifyObservers(CRITERIA_TYPE_PROP);
    }

    /**
     * Sets the field this criteria is applied to.
     *
     * @param field The field this criteria is applied to.
     */
    public void setField(String field)
    {
        myField = field;
        setChanged();
        notifyObservers(FIELD_PROP);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        setChanged();
        notifyObservers(CRITERIA_TYPE_PROP);
    }
}
