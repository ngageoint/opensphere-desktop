package io.opensphere.core.terrain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicConvexPolygon;
import io.opensphere.core.model.GeographicPolygon;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.SimpleTesseraBlockBuilder;

/**
 * Special case for the north terrain triangle that is actually two triangles,
 * split at the 180 degree meridian.
 */
public class NorthTerrainTriangle extends TerrainTriangle
{
    /**
     * Constructor.
     *
     * @param locA the location of the a vertex
     * @param locB the location of the b vertex
     * @param locC the location of the c vertex
     */
    public NorthTerrainTriangle(GeographicPosition locA, GeographicPosition locB, GeographicPosition locC)
    {
        super((TerrainTriangle)null, locA, locB, locC);
    }

    @Override
    public boolean contains(GeographicPosition loc)
    {
        if (getLeftChild() != null)
        {
            return getLeftChild().contains(loc) || getRightChild().contains(loc);
        }
        return false;
    }

    @Override
    public TerrainTriangle getContainingTriangle(GeographicPosition loc, boolean allowDegenerate)
    {
        TerrainTriangle tri;
        if (getLeftChild().contains(loc))
        {
            tri = getLeftChild().getContainingTriangle(loc, allowDegenerate);
        }
        else
        {
            tri = getRightChild().getContainingTriangle(loc, allowDegenerate);
        }
        return tri;
    }

    @Override
    public synchronized List<TerrainTriangle> getOverlappingTriangles(GeographicConvexPolygon polygon, boolean petrified)
    {
        List<TerrainTriangle> overlap = new ArrayList<>();
        if (getLeftChild() != null)
        {
            overlap.addAll(getLeftChild().getOverlappingTriangles(polygon, petrified));
            overlap.addAll(getRightChild().getOverlappingTriangles(polygon, petrified));
        }
        else
        {
            overlap.add(this);
        }
        return overlap;
    }

    @Override
    public void getOverlappingTriangles(Polygon polygon, Collection<TerrainTriangle> fullyContained,
            Collection<TerrainTriangle> partiallyContained)
    {
        getLeftChild().getOverlappingTriangles(polygon, fullyContained, partiallyContained);
        getRightChild().getOverlappingTriangles(polygon, fullyContained, partiallyContained);
    }

    @Override
    public void getTesserae(SimpleTesseraBlockBuilder<TerrainVertex> triBuilder,
            SimpleTesseraBlockBuilder<TerrainVertex> quadBuilder, GeographicConvexPolygon polygon, boolean petrified)
    {
        getLeftChild().getTesserae(triBuilder, quadBuilder, polygon, petrified);
        getRightChild().getTesserae(triBuilder, quadBuilder, polygon, petrified);
    }

    @Override
    public boolean isInBox(GeographicBoundingBox box)
    {
        return true;
    }

    @Override
    public void modifyElevation(GeographicPolygon region, boolean providerChanged)
    {
        getLeftChild().modifyElevation(region, providerChanged);
        getRightChild().modifyElevation(region, providerChanged);
    }
}
