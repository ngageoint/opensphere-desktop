package io.opensphere.core.geometry;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.math.VectorUtilities;

/**
 * Given an ellipsoids location, orientation, pitch and roll, this class
 * provides a transform matrix to be used for rendering so that it shows
 * correctly on the globe.
 */
public class EllipsoidTransformProvider
{
    /**
     * Used to convert geographic coordinates to model coordinates.
     */
    private final MapManager myMapManager;

    /**
     * Constructs a new transform provider.
     *
     * @param mapManager Used to convert geographic coordinates to model
     *            coordinates.
     */
    public EllipsoidTransformProvider(MapManager mapManager)
    {
        myMapManager = mapManager;
    }

    /**
     * Gets the necessary transform matrix so that the ellipsoid is presented
     * correctly on the globe.
     *
     * @param location The location of the center of the ellipsoid.
     * @param heading The orientation of the ellipsoid in degrees. 0 is north,
     *            90 is east, 180 is south, 270 west.
     * @param pitch The pitch of the ellipsoid in degrees.
     * @param roll The roll of the ellipsoid in degrees. This will only be
     *            noticed if axisB and axisC are different values in the
     *            ellipsoid.
     * @return The transform matrix to use when rendering the ellipsoid.
     */
    public Matrix4d provideTransform(LatLonAlt location, double heading, double pitch, double roll)
    {
        Vector3d modelVector = myMapManager.getProjection().convertToModel(new GeographicPosition(location), Vector3d.ORIGIN);
        return provideTransform(modelVector, heading, pitch, roll);
    }

    /**
     * Gets the necessary transform matrix so that the ellipsoid is presented
     * correctly on the globe.
     *
     * @param modelVector The location of the center of the ellipsoid in model
     *            coordinates.
     * @param heading The orientation of the ellipsoid in degrees. 0 is north,
     *            90 is east, 180 is south, 270 west.
     * @param pitch The pitch of the ellipsoid in degrees.
     * @param roll The roll of the ellipsoid in degrees. This will only be
     *            noticed if axisB and axisC are different values in the
     *            ellipsoid.
     * @return The transform matrix to use when rendering the ellipsoid.
     */
    public Matrix4d provideTransform(Vector3d modelVector, double heading, double pitch, double roll)
    {
        Vector3d scaleVector = new Vector3d(1, 1, 1);
        return VectorUtilities.getModelTransform(modelVector, heading, pitch, roll, scaleVector);
    }
}
