package io.opensphere.core.projection;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.LatLonAlt;

/** Test for {@link GeographicBody3D}. */
@SuppressWarnings("boxing")
public class GeographicBody3DTest
{
    /**
     * Helper function to test a LatLonAlt against the expected values.
     *
     * @param lla The LatLonAlt point.
     * @param expectedLat The expected latitude value (in degrees).
     * @param expectedLon The expected longitude value (in degrees).
     */
    public void compareLatLonAlt(LatLonAlt lla, double expectedLat, double expectedLon)
    {
        Assert.assertEquals(lla.getLatD(), expectedLat, 0.0000001);
        Assert.assertEquals(lla.getLonD(), expectedLon, 0.0000001);
    }

    /**
     * Test for
     * {@link GeographicBody3D#greatCircleEndPosition(LatLonAlt, double, double)}
     * .
     */
    @Test
    public void testGreatCircleEndPosition()
    {
        // Use a starting point of lat = 0 , lon = 0
        LatLonAlt orgin = LatLonAlt.createFromDegrees(0., 0.);

        // If the distance is zero, the passed in LatLonAlt is returned.
        Assert.assertEquals(orgin, GeographicBody3D.greatCircleEndPosition(orgin, 0., 0.));

        // Azimuth angle is measured clockwise from north so an azimuth
        // angle 0 and distance 45 should give us lat = 45, lon = 0
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(0.), Math.toRadians(45.)), 45., 0.);

        // angle 0 and distance 89 should give us lat = 89, lon = 0
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(0.), Math.toRadians(89.)), 89., 0.);
        // angle 0 and distance 90 should give us lat = 90, lon = 0
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(0.), Math.toRadians(90.)), 90., 0.);
        // angle 0 and distance 91 should give us lat = 89, lon = 180
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(0.), Math.toRadians(91.)), 89., 180.);
        // angle 0 and distance 135 should give us lat = 45, lon = 180
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(0.), Math.toRadians(135.)), 45., 180.);

        // angle 90 and distance 45 should give us lat = 0, lon = 45
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(90.), Math.toRadians(45.)), 0., 45.);
        // angle 90 and distance 90 should give us lat = 0, lon = 90
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(90.), Math.toRadians(90.)), 0., 90.);
        // angle 90 and distance 135 should give us lat = 0 lon = 135
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(90.), Math.toRadians(135.)), 0., 135.);
        // angle 90 and distance 180 should give us lat 0 lon = 180
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(90.), Math.toRadians(180.)), 0., 180.);
        // angle 90 and distance 190 should give us lat 0 lon = -170
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(90.), Math.toRadians(190.)), 0., -170.);

        // angle 270 and distance 45 should give us lat = 0, lon = -45
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(270.), Math.toRadians(45.)), 0., -45.);
        // angle 270 and distance 90 should give us lat = 0, lon = -90
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(270.), Math.toRadians(90.)), 0., -90.);
        // angle 270 and distance 135 should give us lat = 0 lon = -135
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(270.), Math.toRadians(135.)), 0., -135.);
        // angle 270 and distance 180 should give us lat 0 lon = -180
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(270.), Math.toRadians(180.)), 0., -180.);
        // angle 270 and distance 190 should give us lat 0 lon = 170
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(270.), Math.toRadians(190.)), 0., 170.);

        // angle -90 and distance 45 should give us lat = 0, lon = -45
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(-90.), Math.toRadians(45.)), 0., -45.);
        // angle -90 and distance 90 should give us lat = 0, lon = -90
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(-90.), Math.toRadians(90.)), 0., -90.);
        // angle -90 and distance 135 should give us lat = 0 lon = -135
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(-90.), Math.toRadians(135.)), 0., -135.);
        // angle -90 and distance 180 should give us lat 0 lon = -180
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(-90.), Math.toRadians(180.)), 0., -180.);
        // angle -90 and distance 190 should give us lat 0 lon = 170
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(-90.), Math.toRadians(190.)), 0., 170.);

        // angle 360 and distance 45 should give us lat = 45, lon = 0
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(360.), Math.toRadians(45.)), 45., 0.);
        // angle 180 and distance 45 should give us lat = -45, lon = 0
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(orgin, Math.toRadians(180.), Math.toRadians(45.)), -45., 0.);

        // Use a starting point just left of 180 meridian which is crossed.
        LatLonAlt left = LatLonAlt.createFromDegrees(0., 170.);
        // angle 90 and distance 70 should give us lat 0 lon = -120
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(left, Math.toRadians(90.), Math.toRadians(70.)), 0., -120.);

        // Use a starting point just right of 180 meridian which is crossed.
        LatLonAlt right = LatLonAlt.createFromDegrees(0., -170.);
        // angle 270 and distance 70 should give us lat 0 lon = 120
        compareLatLonAlt(GeographicBody3D.greatCircleEndPosition(right, Math.toRadians(270.), Math.toRadians(70.)), 0., 120.);

        LatLonAlt end = GeographicBody3D.greatCircleEndPosition(orgin, 1., WGS84EarthConstants.RADIUS_MEAN_M, 500.);
        double arcDistanceM = GeographicBody3D.greatCircleDistanceM(orgin, end, WGS84EarthConstants.RADIUS_MEAN_M);
        Assert.assertEquals(500., arcDistanceM, 0.0000001);
    }

    /** Test interpolation when the start and end points are the same. */
    @Test
    public void testMatchingStartAndEnd()
    {
        LatLonAlt start = LatLonAlt.createFromDegrees(0., 0.);
        LatLonAlt end = LatLonAlt.createFromDegrees(0., 0.);
        double percent = 0.5;

        LatLonAlt interp = GeographicBody3D.greatCircleInterpolate(start, end, percent);
        Assert.assertEquals(interp.equals(start), true);
    }
}
