package io.opensphere.controlpanels.styles.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Observable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The bulls eye's style options model.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class StyleOptions extends Observable implements Serializable
{
    /**
     * The color property.
     */
    public static final String COLOR_PROP = "color";

    /**
     * The icon id property.
     */
    public static final String ICON_PROP = "icon";

    /**
     * The size property.
     */
    public static final String SIZE_PROP = "size";

    /**
     * The style property.
     */
    public static final String STYLE_PROP = "style";

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The color of the bulls eye.
     */
    @XmlJavaTypeAdapter(ColorAdapter.class)
    @XmlAttribute(name = "color")
    private Color myColor = Color.RED;

    /**
     * Indicates if the size has been set by a user.
     */
    @XmlAttribute(name = "hasSizeBeenSet")
    private boolean myHasSizeBeenSet;

    /**
     * The id of the icon to display in this style.
     */
    @XmlAttribute(name = "iconId")
    private int myIconId;

    /**
     * The size of the bulls eye.
     */
    @XmlAttribute(name = "size")
    private int mySize = 5;

    /**
     * The style of the bulls eye.
     */
    @XmlAttribute(name = "style")
    private Styles myStyle = Styles.POINT;

    /**
     * The available styles for the bulls eye.
     */
    private transient ObservableList<Styles> myStyles;

    /**
     * Copy the contents from another (which was probably edited).
     *
     * @param other the other style options
     */
    public void copyFrom(StyleOptions other)
    {
        setColor(other.getColor());
        setIconId(other.getIconId());
        setSize(other.getSize());
        setStyle(other.getStyle());
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
        StyleOptions other = (StyleOptions)obj;
        return Objects.equals(myColor, other.myColor) && myIconId == other.myIconId && mySize == other.mySize
                && myStyle == other.myStyle;
    }

    /**
     * Gets the color of the bulls eye.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Gets the id of the icon for this style.
     *
     * @return the iconId.
     */
    public int getIconId()
    {
        return myIconId;
    }

    /**
     * Gets the size of the bulls eye.
     *
     * @return the size
     */
    public int getSize()
    {
        return mySize;
    }

    /**
     * Gets the style of the bulls eye.
     *
     * @return the style
     */
    public Styles getStyle()
    {
        return myStyle;
    }

    /**
     * Gets the available styles for the bulls eye and initializes the list if
     * it has not yet been initialized. In the latter case, all styles options
     * are included by default.
     *
     * @return the styles
     */
    public ObservableList<Styles> getStyles()
    {
        if (myStyles == null)
        {
            myStyles = FXCollections.observableArrayList(Styles.values());
        }
        return myStyles;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(myColor, Integer.valueOf(myIconId), Integer.valueOf(mySize), myStyle);
    }

    /**
     * Indicates if the size has been set by a user.
     *
     * @return True if the user has set the size, false otherwise.
     */
    public boolean hasSizeBeenSet()
    {
        return myHasSizeBeenSet;
    }

    /**
     * Sets the color of the bulls eye.
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
     * Sets the id of the icon for this style.
     *
     * @param iconId the iconId to set.
     */
    public void setIconId(int iconId)
    {
        myIconId = iconId;
        setChanged();
        notifyObservers(ICON_PROP);
    }

    /**
     * Sets the size of the bulls eye.
     *
     * @param size the size to set
     */
    public void setSize(int size)
    {
        mySize = size;
        myHasSizeBeenSet = true;
        setChanged();
        notifyObservers(SIZE_PROP);
    }

    /**
     * Sets the style of the bulls eye.
     *
     * @param style the style to set
     */
    public void setStyle(Styles style)
    {
        myStyle = style;
        setChanged();
        notifyObservers(STYLE_PROP);
    }

    /**
     * Specify a list of Styles options.
     *
     * @param styles the options to be made available
     */
    public void setStyles(List<Styles> styles)
    {
        myStyles = FXCollections.observableList(styles);
    }
}
