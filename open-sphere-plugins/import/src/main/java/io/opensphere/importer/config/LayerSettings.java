package io.opensphere.importer.config;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.mantle.data.LoadsTo;

/**
 * Layer settings.
 */
@XmlRootElement(name = "LayerSettings")
@XmlAccessorType(XmlAccessType.NONE)
public class LayerSettings implements Cloneable
{
    /** The loads-to field. */
    public static String LOADS_TO = "LOADS_TO";

    /** The layer name. */
    @XmlElement(name = "name", required = true)
    private String myName;

    /** Whether the layer is timeline-enabled. */
    @XmlElement(name = "timeline", required = true)
    private boolean myTimelineEnabled;

    /** Whether the layer is metadata-enabled. */
    @XmlElement(name = "metadata", required = true)
    private boolean myMetadataEnabled;

    /** The layer color. */
    @XmlElement(name = "color", required = false)
    private int myColor;

    /** Whether the layer is active. */
    @XmlElement(name = "active", required = false)
    private boolean myIsActive;

    /** Map of fields set by the user. */
    private final Map<String, Boolean> mySetByUserMap = new HashMap<>();

    /**
     * JAXB Constructor.
     */
    public LayerSettings()
    {
    }

    /**
     * Constructor.
     *
     * @param name the layer name
     */
    public LayerSettings(String name)
    {
        myName = name;
        myColor = Color.BLUE.getRGB();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Gets the loadsTo.
     *
     * @return the loadsTo
     */
    public LoadsTo getLoadsTo()
    {
        LoadsTo loadsTo;
        if (myTimelineEnabled)
        {
            loadsTo = myMetadataEnabled ? LoadsTo.TIMELINE : LoadsTo.BASE_WITH_TIMELINE;
        }
        else
        {
            loadsTo = myMetadataEnabled ? LoadsTo.STATIC : LoadsTo.BASE;
        }
        return loadsTo;
    }

    /**
     * Sets the loadsTo.
     *
     * @param loadsTo the loadsTo
     */
    public void setLoadsTo(LoadsTo loadsTo)
    {
        switch (loadsTo)
        {
            case BASE:
                myTimelineEnabled = false;
                myMetadataEnabled = false;
                break;
            case TIMELINE:
                myTimelineEnabled = true;
                myMetadataEnabled = true;
                break;
            case STATIC:
                myTimelineEnabled = false;
                myMetadataEnabled = true;
                break;
            case BASE_WITH_TIMELINE:
                myTimelineEnabled = true;
                myMetadataEnabled = false;
                break;
            default:
                throw new UnexpectedEnumException(loadsTo);
        }
    }

    /**
     * Gets the timelineEnabled.
     *
     * @return the timelineEnabled
     */
    public boolean isTimelineEnabled()
    {
        return myTimelineEnabled;
    }

    /**
     * Sets the timelineEnabled.
     *
     * @param timelineEnabled the timelineEnabled
     */
    public void setTimelineEnabled(boolean timelineEnabled)
    {
        myTimelineEnabled = timelineEnabled;
    }

    /**
     * Gets the metadataEnabled.
     *
     * @return the metadataEnabled
     */
    public boolean isMetadataEnabled()
    {
        return myMetadataEnabled;
    }

    /**
     * Sets the metadataEnabled.
     *
     * @param metadataEnabled the metadataEnabled
     */
    public void setMetadataEnabled(boolean metadataEnabled)
    {
        myMetadataEnabled = metadataEnabled;
    }

    /**
     * Gets the color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return new Color(myColor, true);
    }

    /**
     * Sets the color.
     *
     * @param color the color
     */
    public void setColor(Color color)
    {
        myColor = color.getRGB();
    }

    /**
     * Gets whether the layer is active.
     *
     * @return whether the layer is active
     */
    public boolean isActive()
    {
        return myIsActive;
    }

    /**
     * Sets whether the layer is active.
     *
     * @param isActive whether the layer is active
     */
    public void setActive(boolean isActive)
    {
        myIsActive = isActive;
    }

    /**
     * Gets whether the field was set by the user.
     *
     * @param field the field
     * @return whether the field was set by the user
     */
    public boolean isFieldSetByUser(String field)
    {
        Boolean isSet = mySetByUserMap.get(field);
        return isSet != null && isSet.booleanValue();
    }

    /**
     * Sets the field as set by the user.
     *
     * @param field the field
     */
    public void setFieldSetByUser(String field)
    {
        mySetByUserMap.put(field, Boolean.TRUE);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myTimelineEnabled);
        result = prime * result + HashCodeHelper.getHashCode(myMetadataEnabled);
        result = prime * result + HashCodeHelper.getHashCode(myName);
        result = prime * result + HashCodeHelper.getHashCode(myColor);
        result = prime * result + HashCodeHelper.getHashCode(myIsActive);
        return result;
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
        LayerSettings other = (LayerSettings)obj;
        //@formatter:off
        return myTimelineEnabled == other.myTimelineEnabled
                && myMetadataEnabled == other.myMetadataEnabled
                && Objects.equals(myName, other.myName)
                && myColor == other.myColor
                && myIsActive == other.myIsActive;
        //@formatter:on
    }

    @Override
    public String toString()
    {
        return "LayerSettings [" + stringFields() + "]";
    }

    /**
     * Form the fields into a String for debugging.
     * @return the fields
     */
    protected String stringFields()
    {
        return "myName=" + myName + ", myTimelineEnabled=" + myTimelineEnabled + ", myMetadataEnabled=" + myMetadataEnabled
                + ", myColor=" + myColor + ", myIsActive=" + myIsActive;
    }

    @Override
    public LayerSettings clone()
    {
        try
        {
            return (LayerSettings)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}
