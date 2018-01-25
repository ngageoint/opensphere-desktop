package io.opensphere.core.geometry;

import java.util.List;

import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.New;

/**
 * Generates the vertices for an ellipsoid given certain parameters. The
 * vertices are in model coordinates and will be centered around 0, 0;
 */
public class EllipsoidVertexGenerator
{
    /**
     * This tolerance is needed are final triangles do not get built.
     */
    private static final double ourTolerance = .0001;

    /**
     * Generates the vertices of an ellipsoid given the specified paramters.
     *
     * @param detailLevel Indication of the number of triangles to represent the
     *            ellipsoid, the higher the number the more triangles and the
     *            smoother the ellipsoid.
     * @param axisAInMeters The main axis length in meters.
     * @param axisBInMeters The minor axis length in meters.
     * @param axisCInMeters The other minor axis length, can be the same as axis
     *            B.
     * @return The ellipse vertices ready to be rendered using triangle strips,
     *         centered around 0, 0.
     */
    public List<List<Position>> generateVertices(int detailLevel, double axisAInMeters, double axisBInMeters,
            double axisCInMeters)
    {
        List<List<Position>> vertices = New.list();
        double pi = Math.PI;
        double tStep = pi / detailLevel;
        double sStep = tStep;

        double axisBRadius = axisBInMeters / 2;
        double axisARadius = axisAInMeters / 2;
        double axisCRadius = axisCInMeters / 2;
        boolean hasDrawnPole = false;
        for (double t = -pi / 2; t <= pi / 2 && !hasDrawnPole; t += tStep)
        {
            List<Position> triangleStrip = New.list();
            for (double s = -pi; s <= pi + ourTolerance; s += sStep)
            {
                double x = axisBRadius * Math.cos(t) * Math.cos(s);
                double y = axisARadius * Math.cos(t) * Math.sin(s);
                double z = axisCRadius * Math.sin(t);

                if (z >= axisCRadius)
                {
                    hasDrawnPole = true;
                }

                triangleStrip.add(new ModelPosition(x, y, z));

                x = axisBRadius * Math.cos(t + tStep) * Math.cos(s);
                y = axisARadius * Math.cos(t + tStep) * Math.sin(s);
                z = axisCRadius * Math.sin(t + tStep);

                triangleStrip.add(new ModelPosition(x, y, z));

                if (z >= axisCRadius)
                {
                    hasDrawnPole = true;
                }
            }

            vertices.add(triangleStrip);
        }

        return vertices;
    }
}
