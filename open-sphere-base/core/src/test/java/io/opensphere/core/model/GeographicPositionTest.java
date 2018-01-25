package io.opensphere.core.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.junit.Assert;

/** Test for {@link GeographicPosition}. */
public class GeographicPositionTest
{
    /**
     * Test for {@link GeographicPosition#findCentroid(List)}.
     */
    @Test
    public void testCalculateCentroid()
    {
        List<GeographicPosition> positions = new ArrayList<>();

        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(5, 5)));

        // Test simple case when there is a single point.
        GeographicPosition geoPos = GeographicPosition.findCentroid(positions);
        Assert.assertEquals(geoPos, positions.get(0));

        // Test simple case for two points.
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5)));
        geoPos = GeographicPosition.findCentroid(positions);
        GeographicPosition expected = new GeographicPosition(LatLonAlt.createFromDegrees(0, 0));
        Assert.assertEquals(geoPos, expected);

        positions.clear();

        // Test case for weighted average
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(0, 10)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(0, 8)));
        geoPos = GeographicPosition.findCentroid(positions);
        expected = new GeographicPosition(LatLonAlt.createFromDegrees(0, 6));
        Assert.assertEquals(geoPos, expected);

        // Another test case for weighted average
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(20, 20)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(20, -5)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(5, 3)));
        geoPos = GeographicPosition.findCentroid(positions);
        expected = new GeographicPosition(LatLonAlt.createFromDegrees(7.5, 6));
        Assert.assertEquals(geoPos, expected);

        positions.clear();

        // Test case for points crossing 180 longitude (west side).
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(40, 175)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(40, 179)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(40, -178)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(40, -176)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(40, -173)));
        geoPos = GeographicPosition.findCentroid(positions);
        expected = new GeographicPosition(LatLonAlt.createFromDegrees(40, -178.6));
        Assert.assertEquals(geoPos, expected);

        // Another test case for points crossing 180 longitude (east side)
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(40, 176)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(40, 175)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(40, 177)));
        geoPos = GeographicPosition.findCentroid(positions);
        expected = new GeographicPosition(LatLonAlt.createFromDegrees(40, 179.375));
        Assert.assertEquals(geoPos, expected);

        positions.clear();

        // Test the case where the centroid falls right on the boundary
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(-10, 177)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(-20, 175)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(-13, -176)));
        positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(-23, -176)));
        geoPos = GeographicPosition.findCentroid(positions);
        expected = new GeographicPosition(LatLonAlt.createFromDegrees(-16.5, 180));
        Assert.assertEquals(geoPos, expected);
    }
}
