package io.opensphere.core.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.New;

/**
 * A {@link Geometry} that models a series of connected line segments. There is
 * a line segment drawn between the last vertex and the first vertex to complete
 * the polygon.
 */
public class PolygonGeometry extends PolylineGeometry
{
    /** Interior holes to the polygon. */
    private final Collection<? extends List<? extends Position>> myHoles;

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public PolygonGeometry(PolygonGeometry.Builder<?> builder, PolygonRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        Collection<List<? extends Position>> holes = New.collection();
        for (List<? extends Position> hole : builder.getHoles())
        {
            if (!hole.isEmpty()) // ignore empty holes
            {
                holes.add(New.unmodifiableList(hole));
            }
        }
        myHoles = New.unmodifiableCollection(holes);
    }

    /**
     * Special constructor for subclasses.
     *
     * @param builder The abstract geometry builder.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     * @param smooth When true, line smoothing is used.
     * @param type Type type of line.
     */
    protected PolygonGeometry(PolygonGeometry.Builder<?> builder, PolygonRenderProperties renderProperties,
            Constraints constraints, boolean smooth, LineType type)
    {
        super(builder, renderProperties, constraints, smooth, type);
        Collection<List<? extends Position>> holes = New.collection();
        for (List<? extends Position> hole : builder.getHoles())
        {
            List<? extends Position> holeCopy = Collections.unmodifiableList(new ArrayList<Position>(hole));
            holes.add(holeCopy);
        }
        myHoles = Collections.unmodifiableCollection(new ArrayList<List<? extends Position>>(holes));
    }

    @Override
    public PolygonGeometry clone()
    {
        return (PolygonGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.createBuilder();
        builder.addHoles(myHoles);
        return builder;
    }

    @Override
    public PolygonGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new PolygonGeometry(createBuilder(), (PolygonRenderProperties)renderProperties, constraints);
    }

    /**
     * Get the holes.
     *
     * @return the holes
     */
    public Collection<? extends List<? extends Position>> getHoles()
    {
        return myHoles;
    }

    @Override
    public PolygonRenderProperties getRenderProperties()
    {
        return (PolygonRenderProperties)super.getRenderProperties();
    }

    @Override
    public boolean jtsIntersectionTests(JTSIntersectionTests test, List<Polygon> polygons, GeometryFactory geomFactory)
    {
        boolean intersects = false;
        if (!getRenderProperties().isHidden() && polygons != null && !polygons.isEmpty() && !getVertices().isEmpty())
        {
            boolean addBeginAsEndToClose = false;
            Position first = getVertices().get(0);
            Position last = getVertices().get(getVertices().size() - 1);
            if (!Objects.equals(first, last))
            {
                addBeginAsEndToClose = true;
            }
            Coordinate[] coords = new Coordinate[getVertices().size() + (addBeginAsEndToClose ? 1 : 0)];
            Position p = null;
            Vector3d vec = null;
            for (int i = 0; i < getVertices().size(); i++)
            {
                p = getVertices().get(i);
                vec = p.asVector3d();
                coords[i] = new Coordinate(vec.getX(), vec.getY());
            }
            if (addBeginAsEndToClose)
            {
                coords[coords.length - 1] = coords[0];
            }
            com.vividsolutions.jts.geom.Geometry g = null;
            try
            {
                if (coords.length == 0 || coords.length >= 4)
                {
                    g = geomFactory.createPolygon(geomFactory.createLinearRing(coords), null);
                }
                else
                {
                    // Handle the case where we have a bad polygon in the mix by
                    // trying to make a line
                    // string out of the coordinates.
                    g = geomFactory.createLineString(coords);
                }
            }
            catch (RuntimeException e2)
            {
                // If all else failed bail we don't want to try to do the
                // intersection test here if
                // our jts geometry construct fails.
                g = null;
            }

            if (g != null)
            {
                for (Polygon aPolygon : polygons)
                {
                    intersects = doJTSIntersectionTests(test, g, aPolygon);
                    if (intersects)
                    {
                        break;
                    }
                }
            }
            else
            {
                intersects = areAnyCoordinatesOverlappingOrIntersecting(polygons, geomFactory, coords);
            }
        }
        return intersects;
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<Position>();
    }

    /**
     * Do jts intersection tests.
     *
     * @param test the tests to run.
     * @param g the jts Geometry to try the tests on
     * @param aPolygon the a JTS polygon to use for the test.
     * @return true, if any of the tests are true
     */
    private boolean doJTSIntersectionTests(JTSIntersectionTests test, com.vividsolutions.jts.geom.Geometry g, Polygon aPolygon)
    {
        JTSIntersectionTests tests = test == null ? ALL_INTERSECTION_TESTS : test;
        return tests.isContains() && aPolygon.contains(g) || tests.isIntersects() && aPolygon.intersects(g)
                || tests.isOverlaps() && aPolygon.overlaps(g);
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends PolylineGeometry.Builder<T>
    {
        /** Interior holes to the polygon. */
        private final Collection<List<? extends T>> myHoles = New.collection();

        /**
         * Add a hole in the polygon.
         *
         * @param hole Head like a hole, black as your soul.
         */
        public void addHole(List<? extends T> hole)
        {
            myHoles.add(New.unmodifiableList(hole));
        }

        /**
         * Add holes in the polygon.
         *
         * @param holes I'd rather die than give you control.
         */
        public void addHoles(Collection<? extends List<? extends T>> holes)
        {
            if (holes != null)
            {
                for (List<? extends T> hole : holes)
                {
                    myHoles.add(New.unmodifiableList(hole));
                }
            }
        }

        /**
         * Get the holes.
         *
         * @return the holes
         */
        public Collection<List<? extends T>> getHoles()
        {
            return myHoles;
        }
    }
}
