package io.opensphere.mantle.data.geom.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.projection.Projection;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.geom.util.MapGeometrySupportUtils;

/**
 * AbstractSimpleMapPathGeometrySupport.
 */
public abstract class AbstractSimpleMapPathGeometrySupport extends AbstractSimpleGeometrySupport implements MapPathGeometrySupport
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The Line type. */
    private LineType myLineType = LineType.STRAIGHT_LINE;

    /** Line width. */
    private int myLineWidth = 2;

    /** Location list. */
    private final List<LatLonAlt> myLocations;

    /** Default constructor. */
    public AbstractSimpleMapPathGeometrySupport()
    {
        myLocations = new LinkedList<>();
    }

    /**
     * Copy constructor.
     *
     * @param source the source object from which to copy data.
     */
    public AbstractSimpleMapPathGeometrySupport(AbstractSimpleMapPathGeometrySupport source)
    {
        super(source);
        myLineType = source.myLineType;
        myLineWidth = source.myLineWidth;

        // TODO make this a deep copy:
        myLocations = new LinkedList<>(source.myLocations);
    }

    /**
     * Secondary initialization constructor.
     *
     * @param lineWidth the line width
     */
    public AbstractSimpleMapPathGeometrySupport(int lineWidth)
    {
        this();
        myLineWidth = lineWidth;
    }

    /**
     * Secondary initialization constructor.
     *
     * @param lineWidth the line width
     * @param type the {@link LineType}
     */
    public AbstractSimpleMapPathGeometrySupport(int lineWidth, LineType type)
    {
        this();
        myLineWidth = lineWidth;
        myLineType = type == null ? LineType.STRAIGHT_LINE : type;
    }

    /**
     * Secondary initialization constructor.
     *
     * @param locations the locations
     */
    public AbstractSimpleMapPathGeometrySupport(List<? extends LatLonAlt> locations)
    {
        myLocations = new LinkedList<>(locations);
    }

    /**
     * Secondary initialization constructor with initial location list and line
     * width.
     *
     * @param locations initial locations
     * @param lineWidth initial line width
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
        myLocations = new LinkedList<>(locations);
        myLineWidth = lineWidth;
        myLineType = type == null ? LineType.STRAIGHT_LINE : type;
    }

    @Override
    public boolean addLocation(LatLonAlt loc)
    {
        boolean added = false;
        synchronized (myLocations)
        {
            added = myLocations.add(loc);
        }
        return added;
    }

    @Override
    public void clearLocations()
    {
        synchronized (myLocations)
        {
            myLocations.clear();
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
        if (myLineWidth != other.myLineWidth || !Objects.equals(myLineType, other.myLineType))
        {
            return false;
        }
        return Objects.equals(myLocations, other.myLocations);
    }

    @Override
    public GeographicBoundingBox getBoundingBox(Projection projection)
    {
        return MapGeometrySupportUtils.getBoundingBox(this);
    }

    @Override
    public LineType getLineType()
    {
        return myLineType;
    }

    @Override
    public int getLineWidth()
    {
        return myLineWidth;
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
        synchronized (myLocations)
        {
            returnList = Collections.unmodifiableList(myLocations);
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
        result = prime * result + myLineWidth;
        result = prime * result + (myLineType == null ? 0 : myLineType.hashCode());
        result = prime * result + (myLocations == null ? 0 : myLocations.hashCode());
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
        synchronized (myLocations)
        {
            removed = myLocations.remove(loc);
        }
        return removed;
    }

    @Override
    public void setLineType(LineType type)
    {
        myLineType = type == null ? LineType.STRAIGHT_LINE : type;
    }

    @Override
    public void setLineWidth(int lineWidth)
    {
        myLineWidth = lineWidth;
    }

    @Override
    public void setLocations(List<LatLonAlt> locations)
    {
        synchronized (myLocations)
        {
            myLocations.clear();
            if (locations != null)
            {
                myLocations.addAll(locations);
            }
        }
    }
}
