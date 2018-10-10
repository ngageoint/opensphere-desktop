package io.opensphere.core.timeline;

import java.awt.Color;
import java.util.Collection;
import java.util.Objects;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

/** Timeline registry entry. */
class Entry
{
    /** The name. */
    private String myName;

    /** The color. */
    private Color myColor;

    /** The visibility. */
    private boolean myVisible;

    /** The data. */
    private final TLongObjectMap<TimelineDatum> myData;

    /**
     * Constructor.
     *
     * @param name The name
     * @param color The color
     * @param visible The visibility
     */
    public Entry(String name, Color color, boolean visible)
    {
        myName = name;
        myColor = color;
        myVisible = visible;
        myData = new TLongObjectHashMap<>();
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
     * Sets the color.
     *
     * @param color the color
     * @return whether the value changed
     */
    public boolean setColor(Color color)
    {
        boolean changed = false;
        if (!Objects.equals(myColor, color))
        {
            myColor = color;
            changed = true;
        }
        return changed;
    }

    /**
     * Sets the visibility.
     *
     * @param visible the visibility
     * @return whether the value changed
     */
    public boolean setVisible(boolean visible)
    {
        boolean changed = false;
        if (myVisible != visible)
        {
            myVisible = visible;
            changed = true;
        }
        return changed;
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
     * Gets the color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Gets the visibility.
     *
     * @return the visibility
     */
    public boolean isVisible()
    {
        return myVisible;
    }

    /**
     * Adds data.
     *
     * @param data the data to add
     */
    public void addData(Collection<? extends TimelineDatum> data)
    {
        for (TimelineDatum datum : data)
        {
            myData.put(datum.getId(), datum);
        }
    }

    /**
     * Removes data.
     *
     * @param ids the ids to remove
     */
    public void removeData(Collection<? extends Long> ids)
    {
        for (Long id : ids)
        {
            myData.remove(id.longValue());
        }
    }

    /**
     * Clears the data.
     */
    public void clearData()
    {
        myData.clear();
    }

    /**
     * Gets the spans.
     *
     * @return the spans
     */
    public Collection<TimelineDatum> getSpans()
    {
        return myData.valueCollection();
    }

    /**
     * Gets the span as TimelineDatum.
     *
     * @return the spans
     */
    public TLongObjectMap<TimelineDatum> getSpanList()
    {
        return myData;
    }
}
