package io.opensphere.core.geometry;

import java.util.List;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.New;

/**
 * Generates the normals to be used for the ellipsoid mesh.
 */
public class EllipsoidNormalsGenerator
{
    /**
     * Calculates the normals for the given list of triangle strips.
     *
     * @param positions The positions to calculate normals for assumes positions
     *            is a triangle strip.
     * @return The normals for the given positions.
     */
    public List<List<Vector3d>> calculateNormals(List<List<Position>> positions)
    {
        List<List<Vector3d>> normals = New.list();

        for (List<? extends Position> triangleStrip : positions)
        {
            List<Vector3d> stripsNormals = New.list();

            for (Position position : triangleStrip)
            {
                stripsNormals.add(position.asVector3d().getNormalized());
            }

            normals.add(stripsNormals);
        }

        return normals;
    }
}
