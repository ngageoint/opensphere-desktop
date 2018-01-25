package io.opensphere.osh.aerialimagery.results;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Given a platforms location and orientation and a camera's orientation, this
 * class knows how to calculate the camera's footprint on the earth.
 */
public class FootprintCalculator
{
    /**
     * Calculates the footprint of the camera.
     *
     * @param metadata Contains the camera information.
     * @param fieldOfViewWidth The field of view width.
     * @param fieldOfViewHeight The field of view height.
     * @return The footprint.
     */
    public GeographicConvexQuadrilateral calculateFootprint2(PlatformMetadata metadata, double fieldOfViewWidth,
            double fieldOfViewHeight)
    {
        double altitude = metadata.getLocation().getAltM();

        double widthAngleDeg = fieldOfViewWidth / 2;
        double heightAngleDeg = fieldOfViewHeight / 2;
        double widthAngle = Math.toRadians(widthAngleDeg);
        double heightAngle = Math.toRadians(heightAngleDeg);

        double yawAngle = metadata.getCameraYawAngle() + metadata.getYawAngle();
        double pitchAngle = metadata.getPitchAngle() + metadata.getCameraPitchAngle();

        double pitchRads = Math.toRadians(90 + pitchAngle);
        double yawRads = Math.toRadians(-yawAngle);

        double groundDistanceTop = altitude * Math.tan(pitchRads + heightAngle);
        double groundDistanceBottom = altitude * Math.tan(pitchRads - heightAngle);

        double aircraftToTopDistance = Math.sqrt(Math.pow(groundDistanceTop, 2) + Math.pow(altitude, 2));
        double aircraftToBottomDistance = Math.sqrt(Math.pow(groundDistanceBottom, 2) + Math.pow(altitude, 2));

        double lonDeltaTopM = aircraftToTopDistance * Math.tan(widthAngle);
        double lonDeltaBottomM = aircraftToBottomDistance * Math.tan(widthAngle);

        LatLonAlt pos = LatLonAlt.createFromDegrees(metadata.getLocation().getLatD(), metadata.getLocation().getLonD());

        LatLonAlt bottomLat = calculateIntersectionPoint(pos, altitude, 0, pitchAngle - heightAngleDeg);
        LatLonAlt topLat = calculateIntersectionPoint(pos, altitude, 0, pitchAngle + heightAngleDeg);

        LatLonAlt topLeft = GeographicBody3D.greatCircleEndPosition(topLat, Math.toRadians(270),
                WGS84EarthConstants.RADIUS_EQUATORIAL_M, lonDeltaTopM);
        LatLonAlt topRight = GeographicBody3D.greatCircleEndPosition(topLat, Math.toRadians(90),
                WGS84EarthConstants.RADIUS_EQUATORIAL_M, lonDeltaTopM);
        LatLonAlt bottomLeft = GeographicBody3D.greatCircleEndPosition(bottomLat, Math.toRadians(270),
                WGS84EarthConstants.RADIUS_EQUATORIAL_M, lonDeltaBottomM);
        LatLonAlt bottomRight = GeographicBody3D.greatCircleEndPosition(bottomLat, Math.toRadians(90),
                WGS84EarthConstants.RADIUS_EQUATORIAL_M, lonDeltaBottomM);

        Vector2d posVector = pos.asVec2d();
        Vector2d topLeftVector = topLeft.asVec2d().subtract(posVector).rotateAroundOrigin(yawRads).add(posVector);
        Vector2d bottomLeftVector = bottomLeft.asVec2d().subtract(posVector).rotateAroundOrigin(yawRads).add(posVector);
        Vector2d bottomRightVector = bottomRight.asVec2d().subtract(posVector).rotateAroundOrigin(yawRads).add(posVector);
        Vector2d topRightVector = topRight.asVec2d().subtract(posVector).rotateAroundOrigin(yawRads).add(posVector);

        topLeft = LatLonAlt.createFromDegrees(topLeftVector.getY(), topLeftVector.getX());
        bottomLeft = LatLonAlt.createFromDegrees(bottomLeftVector.getY(), bottomLeftVector.getX());
        bottomRight = LatLonAlt.createFromDegrees(bottomRightVector.getY(), bottomRightVector.getX());
        topRight = LatLonAlt.createFromDegrees(topRightVector.getY(), topRightVector.getX());

        return new GeographicConvexQuadrilateral(new GeographicPosition(topLeft), new GeographicPosition(topRight),
                new GeographicPosition(bottomRight), new GeographicPosition(bottomLeft));
    }

    /**
     * Calculates the geographic point at which the ray intersects with the
     * earth.
     *
     * @param cameraPos The position of the camera.
     * @param terrainRelativeAlt The altitude of the camera relative to the
     *            terrain.
     * @param yawAngle The yaw angle of the camera.
     * @param pitchAngle The pitch angle of the camera.
     * @return The geographic position.
     */
    private LatLonAlt calculateIntersectionPoint(LatLonAlt cameraPos, double terrainRelativeAlt, double yawAngle,
            double pitchAngle)
    {
        double angle = Math.toRadians(90 + pitchAngle);
        double yawRads = Math.toRadians(yawAngle);

        double groundDistance = terrainRelativeAlt * Math.tan(angle);

        LatLonAlt pos = LatLonAlt.createFromDegrees(cameraPos.getLatD(), cameraPos.getLonD());

        LatLonAlt groundPos = GeographicBody3D.greatCircleEndPosition(pos, yawRads, WGS84EarthConstants.RADIUS_EQUATORIAL_M,
                groundDistance);

        return groundPos;
    }
}
