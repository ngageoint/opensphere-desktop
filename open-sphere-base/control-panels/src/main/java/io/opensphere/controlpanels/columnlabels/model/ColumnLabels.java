package io.opensphere.controlpanels.columnlabels.model;

import java.io.Serializable;
import java.util.Observable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

import io.opensphere.core.util.ObservableList;

/**
 * The model used to configure the columns to use for a label.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class ColumnLabels extends Observable implements Serializable
{
    /**
     * The always show labels property.
     */
    public static final String ALWAYS_SHOW_LABELS_PROP = "alwaysShow";

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates if the labels should always be shown.
     */
    @XmlAttribute(name = "alwaysShowLabels")
    private boolean myAlwaysShowLabels = true;

    /**
     * The list of column values to use to make a label.
     */
    @XmlElement(name = "columns")
    private final ObservableList<ColumnLabel> myColumnsInLabel = new ObservableList<>();

    /**
     * Gets the list of column values to use to make a label.
     *
     * @return the columnsInLabel
     */
    public ObservableList<ColumnLabel> getColumnsInLabel()
    {
        return myColumnsInLabel;
    }

    /**
     * Indicates if the labels should always be shown.
     *
     * @return the alwaysShowLabels
     */
    public boolean isAlwaysShowLabels()
    {
        return myAlwaysShowLabels;
    }

    /**
     * Sets if the labels should always be shown.
     *
     * @param alwaysShowLabels the alwaysShowLabels to set
     */
    public void setAlwaysShowLabels(boolean alwaysShowLabels)
    {
        myAlwaysShowLabels = alwaysShowLabels;
        setChanged();
        notifyObservers(ALWAYS_SHOW_LABELS_PROP);
    }

    /**
     * Copy the contents of another (which was probably edited).
     *
     * @param other the other column label
     */
    public void copyFrom(ColumnLabels other)
    {
        setAlwaysShowLabels(other.isAlwaysShowLabels());
        myColumnsInLabel.clear();
        myColumnsInLabel.addAll(other.getColumnsInLabel());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(Boolean.valueOf(myAlwaysShowLabels), myColumnsInLabel);
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
        ColumnLabels other = (ColumnLabels)obj;
        return myAlwaysShowLabels == other.myAlwaysShowLabels && Objects.equal(myColumnsInLabel, other.myColumnsInLabel);
    }
}
