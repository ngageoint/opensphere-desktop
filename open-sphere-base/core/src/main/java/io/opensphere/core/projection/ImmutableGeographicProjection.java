package io.opensphere.core.projection;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Tessera;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.Viewer;

/** A geographic projection whose model is immutable. */
public abstract class ImmutableGeographicProjection extends AbstractGeographicProjection
{
    /** The globe model that performs tessellation. */
    // TODO We should ensure that this is an immutable model.
    private final GeographicProjectionModel myModel;

    /**
     * Create a terrain projection which uses the given globe.
     *
     * @param model the globe which backs this projection.
     * @param modelCenter The center for eye coordinates which should be used
     *            with this projection.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ImmutableGeographicProjection(GeographicProjectionModel model, Vector3d modelCenter)
    {
        myModel = model;
        super.setModelCenter(modelCenter);
    }

    @Override
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(GeographicPosition start,
            GeographicPosition end, LineType type, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        return myModel.convertLineToModel(start, end, type, modelCenter);
    }

    @Override
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(ProjectionCursor start, GeographicPosition end,
            LineType type, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        return myModel.convertLineToModel(start, end, type, modelCenter);
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertPolygonToModelMesh(Polygon polygon,
            Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        return myModel.convertPolygonToModelMesh(polygon, modelCenter);
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertQuadToModel(GeographicPosition vert1,
            GeographicPosition vert2, GeographicPosition vert3, GeographicPosition vert4, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        return myModel.convertQuadToModel(vert1, vert2, vert3, vert4, modelCenter);
    }

    @Override
    public Vector3d convertToModel(GeographicPosition inPos, Vector3d modelCenter)
    {
        return myModel.convertToModel(inPos, modelCenter);
    }

    @Override
    public GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference)
    {
        return myModel.convertToPosition(inPos, altReference);
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertTriangleToModel(GeographicPosition vert1,
            GeographicPosition vert2, GeographicPosition vert3, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        return myModel.convertTriangleToModel(vert1, vert2, vert3, modelCenter);
    }

    @Override
    public double getDistanceFromModelCenterM(GeographicPosition position)
    {
        Vector3d terrain = myModel.getTerrainModelPosition(position, null);
        return terrain.getLength();
    }

    @Override
    public ElevationManager getElevationManager()
    {
        return myModel.getElevationManager();
    }

    @Override
    public double getElevationOnTerrainM(GeographicPosition position)
    {
        return myModel.getElevationOnTerrainM(position);
    }

    @Override
    public double getMinimumTerrainDistance(Viewer view)
    {
        return myModel.getMinimumInviewDistance(view);
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        return myModel.getNormalAtPosition(inPos);
    }

    @Override
    public Vector3d getSurfaceIntersection(Vector3d pointA, Vector3d pointB)
    {
        return myModel.getSurfaceIntersection(pointA, pointB);
    }

    @Override
    public Vector3d getTerrainIntersection(Ray3d ray, Viewer view)
    {
        return myModel.getTerrainIntersection(ray, view);
    }

    @Override
    public boolean isOutsideModel(Vector3d modelCoordinates)
    {
        // TODO The correct way to do this should be convert to position
        // with TERRAIN reference, then check to see if the alt is positive.
//        GeographicPosition geoPos = convertToPosition(modelCoordinates, ReferenceLevel.TERRAIN);
//        if (geoPos.getLatLonAlt().getAltM() > 0)
//        {
//            return true;
//        }
//        return false;

        GeographicPosition geoPos = myModel.getCelestialBody().convertToPosition(modelCoordinates, ReferenceLevel.ELLIPSOID);
        GeographicPosition flatPos = new GeographicPosition(
                LatLonAlt.createFromDegrees(geoPos.getLatLonAlt().getLatD(), geoPos.getLatLonAlt().getLonD()));
        Vector3d terrain = myModel.getTerrainModelPosition(flatPos, Vector3d.ORIGIN);

        // The length squared can be used here since we are only interested
        // in
        // relative size.
        return modelCoordinates.getLengthSquared() > terrain.getLengthSquared();
    }

    @Override
    public void setModelCenter(Vector3d modelCenter)
    {
        throw new UnsupportedOperationException("The model center cannot be changed for immutable projections.");
    }

    /**
     * Get the model.
     *
     * @return the model
     */
    protected GeographicProjectionModel getModel()
    {
        return myModel;
    }
}
