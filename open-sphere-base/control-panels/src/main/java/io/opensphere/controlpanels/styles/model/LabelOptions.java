package io.opensphere.controlpanels.styles.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.Observable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Objects;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;

/**
 * Contains the configured values for the bull's eye's labels.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class LabelOptions extends Observable implements Serializable
{
    /**
     * The color property.
     */
    public static final String COLOR_PROP = "color";

    /**
     * The label size property.
     */
    public static final String SIZE_PROP = "size";

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The labels color.
     */
    @XmlJavaTypeAdapter(ColorAdapter.class)
    @XmlAttribute(name = "color")
    private Color myColor = Color.WHITE;

    /**
     * The columns that contribute to the label.
     */
    @XmlElement(name = "columnLabels")
    private final ColumnLabels myColumnLabels = new ColumnLabels();

    /**
     * The labels size.
     */
    @XmlAttribute(name = "size")
    private int mySize = 14;

    /**
     * Gets the labels color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * The columns that contribute to the label.
     *
     * @return the columnLabels
     */
    public ColumnLabels getColumnLabels()
    {
        return myColumnLabels;
    }

    /**
     * Gets the labels size.
     *
     * @return the size
     */
    public int getSize()
    {
        return mySize;
    }

    /**
     * Sets the labels color.
     *
     * @param color the color to set
     */
    public void setColor(Color color)
    {
        myColor = color;
        setChanged();
        notifyObservers(COLOR_PROP);
    }

    /**
     * Sets the labels size.
     *
     * @param size the size to set
     */
    public void setSize(int size)
    {
        mySize = size;
        setChanged();
        notifyObservers(SIZE_PROP);
    }

    /**
     * Copy the contents of another (which was probably edited).
     *
     * @param other the other label options
     */
    public void copyFrom(LabelOptions other)
    {
        setColor(other.getColor());
        setSize(other.getSize());
        myColumnLabels.copyFrom(other.getColumnLabels());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(myColor, myColumnLabels, Integer.valueOf(mySize));
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
        LabelOptions other = (LabelOptions)obj;
        return Objects.equal(myColor, other.myColor) && Objects.equal(myColumnLabels, other.myColumnLabels)
                && mySize == other.mySize;
    }
}
