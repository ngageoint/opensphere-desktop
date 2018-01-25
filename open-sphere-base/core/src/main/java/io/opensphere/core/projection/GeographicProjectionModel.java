package io.opensphere.core.projection;

import java.util.Collection;

import org.apache.log4j.Logger;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicConvexPolygon;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.viewer.Viewer;

/** A geographic projection which is a model of the body it represents. */
public abstract class GeographicProjectionModel extends AbstractMutableGeographicProjection
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeographicProjectionModel.class);

    /** Generate a snapshot of the current state of the model. */
    public abstract void generateModelSnapshot();

    /**
     * Celestial body.
     *
     * @return the celestialBody to get.
     */
    public abstract GeographicBody3D getCelestialBody();

    /**
     * Get the model point for the given location. Since this gets the model
     * coordinates from the celestial body, the altitude should be based on
     * either the ellipsoidal model or the origin.
     *
     * @param inPos location for which to get the model point
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return the model point.
     */
    public abstract Vector3d getCelestialBodyModelPosition(GeographicPosition inPos, Vector3d modelCenter);

    @Override
    public ElevationManager getElevationManager()
    {
        return getCelestialBody().getElevationManager();
    }

    /**
     * Get the approximate minimum distance from the view plane to the terrain.
     * This value must not be more than the actual distance.
     *
     * @param view The viewer from which the distance is desired.
     * @return The approximate minimum distance from the view plane to the
     *         terrain.
     */
    public abstract double getMinimumInviewDistance(Viewer view);

    /**
     * Get the current snapshot of the model. Mutable models should copy
     * themselves, immutable models may return themselves.
     *
     * @return The projection model.
     */
    public abstract GeographicProjectionModel getModelSnapshot();

    @Override
    public Projection getSnapshot()
    {
        LOGGER.error("Attempting to get projection snapshot for a model");
        return null;
    }

    /**
     * Use the current tessellation to get a model position which is on the
     * terrain.
     *
     * @param inPos the geographic position.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return model point of the position based on the current tessellation.
     */
    public abstract Vector3d getTerrainModelPosition(GeographicPosition inPos, Vector3d modelCenter);

    /**
     * Generate the tesserae that overlap the polygon defined by the vertices.
     * The polygon must be convex and the vertices must be given in
     * counter-clockwise order.
     *
     * @param polygon The vertices of the convex polygon (counter-clockwise).
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return generated tesserae
     */
    public abstract TesseraList<? extends GeographicProjectedTesseraVertex> getTesserae(GeographicConvexPolygon polygon,
            Vector3d modelCenter);

    /**
     * Make any necessary adjustments when switching between high and low
     * accuracy.
     *
     * @param highAccuracy The new high accuracy value.
     */
    public abstract void setHighAccuracy(boolean highAccuracy);

    /**
     * Update the model for the viewer position.
     *
     * @param view The viewer.
     * @return The bounds which enclose any updates to the model.
     */
    public abstract Collection<GeographicBoundingBox> updateModelForView(Viewer view);
}
