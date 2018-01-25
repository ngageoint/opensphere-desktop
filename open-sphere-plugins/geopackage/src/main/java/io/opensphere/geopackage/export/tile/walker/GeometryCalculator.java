package io.opensphere.geopackage.export.tile.walker;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.collections.New;

/**
 * A helper class that will calculate if geometries are within a bounding box
 * and return the ones at overlap or are within a given bounding box.
 */
public class GeometryCalculator
{
    /**
     * Checks to see if any of the geometries are full contained within the
     * given bounding box.
     *
     * @param geometries The list of geometries to check.
     * @param boundingBox The bounding box.
     * @return True if any of the geometries are fully contained, false
     *         otherwise.
     */
    public boolean anyAtFullContainment(Collection<? extends AbstractTileGeometry<?>> geometries, GeographicBoundingBox boundingBox)
    {
        boolean hasAFullContainment = false;

        for (AbstractTileGeometry<?> geometry : geometries)
        {
            if (geometry.getBounds() instanceof GeographicBoundingBox)
            {
                hasAFullContainment = boundingBox.contains((GeographicBoundingBox)geometry.getBounds());
                if (hasAFullContainment)
                {
                    break;
                }
            }
        }

        return hasAFullContainment;
    }

    /**
     * Gets the geometries that are viewable within the given bounding box.
     *
     * @param geometries The superset of geometries.
     * @param boundingBox The bounding box.
     * @return The geometries that are viewable within the specified bounding
     *         box.
     */
    public List<AbstractTileGeometry<?>> getContainingGeometries(Collection<? extends AbstractTileGeometry<?>> geometries, GeographicBoundingBox boundingBox)
    {
        List<AbstractTileGeometry<?>> containingGeometries = New.list();

        for (AbstractTileGeometry<?> geometry : geometries)
        {
            if (overlaps(geometry, boundingBox))
            {
                containingGeometries.add(geometry);
            }
        }

        return containingGeometries;
    }

    /**
     * Determines if the geometry overlaps the bounding box.
     *
     * @param geometry the geometry
     * @param boundingBox the bounding box
     * @return whether they overlap
     */
    public static boolean overlaps(AbstractTileGeometry<?> geometry, GeographicBoundingBox boundingBox)
    {
        boolean overlaps = false;
        if (geometry.getBounds() instanceof GeographicBoundingBox)
        {
            GeographicBoundingBox geometryBounds = (GeographicBoundingBox)geometry.getBounds();
            overlaps = boundingBox.contains(geometryBounds) || boundingBox.intersects(geometryBounds)
                    || geometryBounds.contains(boundingBox);
        }
        return overlaps;
    }
}
