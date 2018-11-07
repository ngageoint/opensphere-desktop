package io.opensphere.mantle.data.geom.impl;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;

/**
 * Simple polygon geometry support. Closed geometry. Does not support children
 * geometries.
 */
public class SimpleMapPolygonGeometrySupport extends AbstractSimpleMapPathGeometrySupport implements MapPolygonGeometrySupport
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Polygon fill color of the support. Stored as an integer behind the scenes
     * to save memory as Color is larger
     */
    private int myColorOfFill;

    /** Filled flag, true if filled, false if not. */
    private boolean myFilled;

    /** The interior holes of the polygon. */
    private final Collection<List<? extends LatLonAlt>> myHoles = New.collection();

    /** The flag that sets if the line for the polygon is drawn or not. */
    private boolean myLineDrawn = true;

    /** Default constructor. */
    public SimpleMapPolygonGeometrySupport()
    {
        super();
    }

    /**
     * Default constructor.
     *
     * @param source the source object from which to copy data.
     */
    public SimpleMapPolygonGeometrySupport(SimpleMapPolygonGeometrySupport source)
    {
        super(source);

        myColorOfFill = source.myColorOfFill;
        myFilled = source.myFilled;
        // TODO: make this a deep copy:
        myHoles.addAll(source.myHoles);
        myLineDrawn = source.myLineDrawn;
    }

    /**
     * Constructor with initial location list.
     *
     * @param locations the locations
     * @param holes The interior holes of the polygon.
     */
    public SimpleMapPolygonGeometrySupport(List<? extends LatLonAlt> locations,
            Collection<? extends List<? extends LatLonAlt>> holes)
    {
        super(locations);
        if (holes != null)
        {
            for (List<? extends LatLonAlt> hole : holes)
            {
                myHoles.add(New.unmodifiableList(hole));
            }
        }
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
        SimpleMapPolygonGeometrySupport other = (SimpleMapPolygonGeometrySupport)obj;
        if (myFilled != other.myFilled || myLineDrawn != other.myLineDrawn || myColorOfFill != other.myColorOfFill)
        {
            return false;
        }

        // Because checking the collections requires checking all of the
        // elements, do this last.
        if (!myHoles.equals(other.myHoles))
        {
            return false;
        }
        return super.equals(obj);
    }

    @SuppressWarnings("PMD.SimplifiedTernary")
    @Override
    public Color getFillColor()
    {
        return myColorOfFill == 0 ? null : new Color(myColorOfFill, true);
    }

    @Override
    public Collection<List<? extends LatLonAlt>> getHoles()
    {
        return myHoles;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return MapVisualizationType.POLYGON_ELEMENTS;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myColorOfFill;
        result = prime * result + (myFilled ? 1231 : 1237);
        result = prime * result + (myHoles == null ? 0 : myHoles.hashCode());
        result = prime * result + (myLineDrawn ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean isClosed()
    {
        return true;
    }

    @Override
    public boolean isFilled()
    {
        return myFilled;
    }

    @Override
    public boolean isLineDrawn()
    {
        return myLineDrawn;
    }

    @Override
    public void setFillColor(Color c)
    {
        myColorOfFill = c == null ? 0 : c.getRGB();
    }

    @Override
    public void setFilled(boolean filled)
    {
        myFilled = filled;
    }

    @Override
    public void setLineDrawn(boolean drawn)
    {
        myLineDrawn = drawn;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.MapGeometrySupport#createCopy()
     */
    @Override
    public SimpleMapPolygonGeometrySupport createCopy()
    {
        return new SimpleMapPolygonGeometrySupport(this);
    }
}
