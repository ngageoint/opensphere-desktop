package io.opensphere.mantle.data.geom.impl;

import java.awt.Color;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * Abstract implementation for a {@link MapGeometrySupport} that provides a
 * color and time span.
 */
public abstract class AbstractDefaultMapGeometrySupport extends AbstractMapGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Color of the support. Stored as an integer behind the scenes to save
     * memory as Color is larger
     */
    private int myColor = java.awt.Color.WHITE.getRGB();

    /**
     * CTOR.
     */
    public AbstractDefaultMapGeometrySupport()
    {
    }

    @Override
    public synchronized boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        AbstractDefaultMapGeometrySupport other = (AbstractDefaultMapGeometrySupport)obj;
        return myColor == other.myColor;
    }

    @Override
    public Color getColor()
    {
        return new Color(myColor, true);
    }

    @Override
    public TimeSpan getTimeSpan()
    {
        TimeSpan ts = (TimeSpan)getItemFromDynamicStorage(TimeSpan.class);
        return ts == null ? TimeSpan.TIMELESS : ts;
    }

    @Override
    public abstract MapVisualizationType getVisualizationType();

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myColor;
        return result;
    }

    @Override
    public void setColor(Color c, Object source)
    {
        myColor = c == null ? 0 : c.getRGB();
    }

    @Override
    public void setTimeSpan(TimeSpan ts)
    {
        if (ts == null)
        {
            removeItemFromDynamicStorage(TimeSpan.class);
        }
        else
        {
            putItemInDynamicStorage(ts);
        }
    }
}
