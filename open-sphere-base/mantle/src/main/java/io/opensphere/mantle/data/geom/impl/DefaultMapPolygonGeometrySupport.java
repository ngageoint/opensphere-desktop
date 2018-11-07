package io.opensphere.mantle.data.geom.impl;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;

/**
 * Default polygon geometry support. Closed geometry.
 */
public class DefaultMapPolygonGeometrySupport extends AbstractMapPathGeometrySupport implements MapPolygonGeometrySupport
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Polygon fill color of the support. Stored as an integer behind the scenes
     * to save memory as Color is larger
     */
    private int myFillColor;

    /** The interior holes of the polygon. */
    private final Collection<List<? extends LatLonAlt>> myHoles = New.collection();

    /** Filled flag, true if filled, false if not. */
    private boolean myIsFilled;

    /** The flag that sets if the line for the polygon is drawn or not. */
    private boolean myLineIsDrawn = true;

    /** Default constructor. */
    public DefaultMapPolygonGeometrySupport()
    {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param source the source object from which to copy data.
     */
    public DefaultMapPolygonGeometrySupport(DefaultMapPolygonGeometrySupport source)
    {
        super(source);

        myFillColor = source.myFillColor;
        // TODO: Create deep copy here:
        myHoles.addAll(source.myHoles);
        myIsFilled = source.myIsFilled;
        myLineIsDrawn = source.myLineIsDrawn;
    }

    /**
     * CTOR with initial location list.
     *
     * @param locations - the locations
     * @param holes The interior holes of the polygon.
     */
    public DefaultMapPolygonGeometrySupport(List<? extends LatLonAlt> locations,
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
    public synchronized boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultMapPolygonGeometrySupport other = (DefaultMapPolygonGeometrySupport)obj;
        if (myIsFilled != other.myIsFilled || myLineIsDrawn != other.myLineIsDrawn || myFillColor != other.myFillColor)
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
        return myFillColor == 0 ? null : new Color(myFillColor, true);
    }

    @Override
    public Collection<? extends List<? extends LatLonAlt>> getHoles()
    {
        return myHoles;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return hasChildren() ? MapVisualizationType.COMPOUND_FEATURE_ELEMENTS : MapVisualizationType.POLYGON_ELEMENTS;
    }

    @Override
    public synchronized int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myFillColor;
        result = prime * result + (myHoles == null ? 0 : myHoles.hashCode());
        result = prime * result + (myIsFilled ? 1231 : 1237);
        result = prime * result + (myLineIsDrawn ? 1231 : 1237);
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
        return myIsFilled;
    }

    @Override
    public boolean isLineDrawn()
    {
        return myLineIsDrawn;
    }

    @Override
    public void setFillColor(Color c)
    {
        myFillColor = c == null ? 0 : c.getRGB();
    }

    @Override
    public void setFilled(boolean filled)
    {
        myIsFilled = filled;
    }

    @Override
    public void setLineDrawn(boolean drawn)
    {
        myLineIsDrawn = drawn;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.MapGeometrySupport#createCopy()
     */
    @Override
    public MapGeometrySupport createCopy()
    {
        return new DefaultMapPolygonGeometrySupport(this);
    }
}
