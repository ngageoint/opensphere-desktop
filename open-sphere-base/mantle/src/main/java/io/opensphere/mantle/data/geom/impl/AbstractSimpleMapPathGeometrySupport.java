package io.opensphere.mantle.data.geom.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * AbstractSimpleMapPathGeometrySupport.
 */
public abstract class AbstractSimpleMapPathGeometrySupport extends AbstractSimpleGeometrySupport implements MapPathGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The Line type. */
    private LineType myLineTp = LineType.STRAIGHT_LINE;

    /** Line width. */
    private int myLineW = 2;

    /** Location list. */
    private final List<LatLonAlt> myLocs;

    /**
     * Basic CTOR.
     */
    public AbstractSimpleMapPathGeometrySupport()
    {
        myLocs = new LinkedList<>();
    }

    /**
     * Basic CTOR.
     *
     * @param lineWidth - the line width
     */
    public AbstractSimpleMapPathGeometrySupport(int lineWidth)
    {
        this();
        myLineW = lineWidth;
    }

    /**
     * Basic CTOR.
     *
     * @param lineWidth - the line width
     * @param type the {@link LineType}
     */
    public AbstractSimpleMapPathGeometrySupport(int lineWidth, LineType type)
    {
        this();
        myLineW = lineWidth;
        myLineTp = type == null ? LineType.STRAIGHT_LINE : type;
    }

    /**
     * CTOR with initial location list.
     *
     * @param locations - the locations
     */
    public AbstractSimpleMapPathGeometrySupport(List<? extends LatLonAlt> locations)
    {
        myLocs = new LinkedList<>(locations);
    }

    /**
     * CTOR with initial location list and line width.
     *
     * @param locations - initial locations
     * @param lineWidth - initial line width
     */
    public AbstractSimpleMapPathGeometrySupport(List<? extends LatLonAlt> locations, int lineWidth)
    {
        this(locations, lineWidth, LineType.STRAIGHT_LINE);
    }

    /**
     * Instantiates a new abstract map path geometry support.
     *
     * @param locations the locations
     * @param lineWidth the line width
     * @param type the type
     */
    public AbstractSimpleMapPathGeometrySupport(List<? extends LatLonAlt> locations, int lineWidth, LineType type)
    {
        myLocs = new LinkedList<>(locations);
        myLineW = lineWidth;
        myLineTp = type == null ? LineType.STRAIGHT_LINE : type;
    }

    @Override
    public boolean addLocation(LatLonAlt loc)
    {
        boolean added = false;
        synchronized (myLocs)
        {
            added = myLocs.add(loc);
        }
        return added;
    }

    @Override
    public void clearLocations()
    {
        synchronized (myLocs)
        {
            myLocs.clear();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        AbstractSimpleMapPathGeometrySupport other = (AbstractSimpleMapPathGeometrySupport)obj;
        if (myLineW != other.myLineW || !EqualsHelper.equals(myLineTp, other.myLineTp))
        {
            return false;
        }
        return Objects.equals(myLocs, other.myLocs);
    }

    @Override
    public GeographicBoundingBox getBoundingBox(Projection projection)
    {
        return MapGeometrySupportUtils.getBoundingBox(this);
    }

    @Override
    public LineType getLineType()
    {
        return myLineTp;
    }

    @Override
    public int getLineWidth()
    {
        return myLineW;
    }

    /**
     * Gets the list of locations. Note: returns an unmodifiable list.
     *
     * @return the list of locations.
     */
    @Override
    public List<LatLonAlt> getLocations()
    {
        List<LatLonAlt> returnList = null;
        synchronized (myLocs)
        {
            returnList = Collections.unmodifiableList(myLocs);
        }
        return returnList;
    }

    @Override
    public abstract MapVisualizationType getVisualizationType();

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myLineW;
        result = prime * result + (myLineTp == null ? 0 : myLineTp.hashCode());
        result = prime * result + (myLocs == null ? 0 : myLocs.hashCode());
        return result;
    }

    @Override
    public boolean isClosed()
    {
        return false;
    }

    @Override
    public boolean removeLocation(LatLonAlt loc)
    {
        boolean removed = false;
        synchronized (myLocs)
        {
            removed = myLocs.remove(loc);
        }
        return removed;
    }

    @Override
    public void setLineType(LineType type)
    {
        myLineTp = type == null ? LineType.STRAIGHT_LINE : type;
    }

    @Override
    public void setLineWidth(int lineWidth)
    {
        myLineW = lineWidth;
    }

    @Override
    public void setLocations(List<LatLonAlt> locations)
    {
        synchronized (myLocs)
        {
            myLocs.clear();
            if (locations != null)
            {
                myLocs.addAll(locations);
            }
        }
        // fireChanged();
    }
}
