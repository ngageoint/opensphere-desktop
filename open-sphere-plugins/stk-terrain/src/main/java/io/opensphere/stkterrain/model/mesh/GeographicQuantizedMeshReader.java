package io.opensphere.stkterrain.model.mesh;

import java.io.Serializable;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.math.Plane;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.PolygonUtilities;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;

/** Reads elevations out of a QuantizedMesh. */
@Immutable
public class GeographicQuantizedMeshReader implements Serializable
{
    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    /** The maximum coordinate value. */
    private static final int MAX_COORD = 32767;

    /** The min lon. */
    private final double myMinLonD;

    /** The min lat. */
    private final double myMinLatD;

    /** The delta lon. */
    private final double myDeltaLonD;

    /** The delta lat. */
    private final double myDeltaLatD;

    /**
     * Constructor.
     *
     * @param bbox The geographic bounding box
     */
    public GeographicQuantizedMeshReader(GeographicBoundingBox bbox)
    {
        myMinLonD = bbox.getMinLonD();
        myMinLatD = bbox.getMinLatD();
        myDeltaLonD = bbox.getDeltaLonD();
        myDeltaLatD = bbox.getDeltaLatD();
    }

    /**
     * Gets the elevation in meters at the given point.
     *
     * @param geoPoint the geographic point
     * @param mesh the quantized mesh
     * @return the elevation in meters
     */
    public double getElevationM(GeographicPosition geoPoint, QuantizedMesh mesh)
    {
        ModelPosition modelPoint = new ModelPosition(geoToModel(geoPoint.getLatLonAlt().getLonD(), myMinLonD, myDeltaLonD),
                geoToModel(geoPoint.getLatLonAlt().getLatD(), myMinLatD, myDeltaLatD), 0);

        if (!MathUtil.between(modelPoint.getX(), 0, MAX_COORD) || !MathUtil.between(modelPoint.getY(), 0, MAX_COORD))
        {
            throw new IllegalArgumentException("Point " + geoPoint + " is not contained in this tile");
        }

        double elevation = 0.;
        boolean foundTriangle = false;

        // Reused for efficiency
        List<ModelPosition> modelTriangle = New.list(3);
        modelTriangle.add(null);
        modelTriangle.add(null);
        modelTriangle.add(null);

        for (int i = 0, n = mesh.getIndexData().getIndexCount(); i < n; i += 3)
        {
            if (PolygonUtilities.containsConvex(getTriangle(mesh, i, modelTriangle), modelPoint, 0.))
            {
                elevation = getAltitude(mesh, modelPoint, modelTriangle);
                foundTriangle = true;
                break;
            }
        }

        assert foundTriangle;

        return elevation;
    }

    /**
     * Gets the model triangle starting at the given index index.
     *
     * @param mesh the quantized mesh
     * @param indexIndexStart the first index index
     * @param triangle the model triangle object to reuse
     * @return the triangle
     */
    private static List<ModelPosition> getTriangle(QuantizedMesh mesh, int indexIndexStart, List<ModelPosition> triangle)
    {
        Indices indexData = mesh.getIndexData();
        int iA = indexData.getIndex(indexIndexStart);
        int iB = indexData.getIndex(indexIndexStart + 1);
        int iC = indexData.getIndex(indexIndexStart + 2);

        VertexData vertexData = mesh.getVertexData();
        triangle.set(0, new ModelPosition(vertexData.getU(iA), vertexData.getV(iA), vertexData.getHeight(iA)));
        triangle.set(1, new ModelPosition(vertexData.getU(iB), vertexData.getV(iB), vertexData.getHeight(iB)));
        triangle.set(2, new ModelPosition(vertexData.getU(iC), vertexData.getV(iC), vertexData.getHeight(iC)));

        return triangle;
    }

    /**
     * Calculates the altitude of the point within the triangle.
     *
     * @param mesh the quantized mesh
     * @param modelPoint the model point
     * @param modelTriangle the model triangle
     * @return the altitude
     */
    private static double getAltitude(QuantizedMesh mesh, ModelPosition modelPoint, List<? extends ModelPosition> modelTriangle)
    {
        // There may be a faster way to do this
        Plane modelPlane = new Plane(modelTriangle.get(0).asVector3d(), modelTriangle.get(1).asVector3d(),
                modelTriangle.get(2).asVector3d());
        Vector3d intersection = modelPlane.getIntersection(new Vector3d(modelPoint.getX(), modelPoint.getY(), 0),
                new Vector3d(modelPoint.getX(), modelPoint.getY(), MAX_COORD));
        double deltaHeight = mesh.getHeader().getMaxHeight() - mesh.getHeader().getMinHeight();
        double altitude = modelToGeo(intersection.getZ(), mesh.getHeader().getMinHeight(), deltaHeight);
        return altitude;
    }

    /**
     * Converts a geographic value to a model value.
     *
     * @param geoValue the geographic value
     * @param min the minimum geographic value
     * @param delta the geographic delta
     * @return the model value
     */
    private static int geoToModel(double geoValue, double min, double delta)
    {
        return (int)Math.round(MAX_COORD * (geoValue - min) / delta);
    }

    /**
     * Converts a model value to a geographic value.
     *
     * @param modelValue the model value
     * @param min the minimum geographic value
     * @param delta the geographic delta
     * @return the geographic value
     */
    private static double modelToGeo(double modelValue, double min, double delta)
    {
        return min + delta * modelValue / MAX_COORD;
    }
}
