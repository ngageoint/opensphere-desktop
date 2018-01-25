package io.opensphere.core.terrain;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/** Utility methods which operate on terrain triangles. */
public final class TerrainTriangleUtilities
{
    /**
     * Determine the locations of the differences between the two triangle
     * trees.
     *
     * @param tri1 The first triangle tree.
     * @param tri2 The second triangle tree.
     * @return a collection of bounding boxes which bound all differences.
     */
    // TODO if the collection is provided, it will save a lot of collection
    // creations
    public static Collection<? extends GeographicBoundingBox> locateDifferences(TerrainTriangle tri1, TerrainTriangle tri2)
    {
        List<GeographicBoundingBox> changes = New.list();

        // If the triangles are locked, then they will be the same instance, so
        // do not check the children.
        if (Utilities.sameInstance(tri1, tri2))
        {
            return changes;
        }
        if (tri1.getLeftChild() != null && tri2.getLeftChild() == null)
        {
            changes.add(tri1.getGeographicPolygon().getBoundingBox());
        }
        else if (tri1.getLeftChild() == null && tri2.getLeftChild() != null)
        {
            changes.add(tri2.getGeographicPolygon().getBoundingBox());
        }
        else if (tri1.getLeftChild() != null && tri2.getLeftChild() != null)
        {
            changes.addAll(locateDifferences(tri1.getLeftChild(), tri2.getLeftChild()));
            changes.addAll(locateDifferences(tri1.getRightChild(), tri2.getRightChild()));
        }

        return changes;
    }

    /** Disallow instantiation. */
    private TerrainTriangleUtilities()
    {
    }
}
