package io.opensphere.core.util.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;

/**
 * Test for {@link GeographicUtilities}.
 */
public class GeographicUtilitiesTest
{
    /**
     * Test
     * {@link GeographicUtilities#decomposePositionsToPolygons(java.util.List)}.
     */
    @Test
    public void testDecomposePositionsToPolygons()
    {
        List<LatLonAlt> exteriorRing1 = createRings(0., 0., 0., 50., 50., 50., 50., 0., 0., 0.);
        List<LatLonAlt> exteriorRing2 = createRings(1., 1., 1., 50., 50., 50., 50., 1., 1., 1.);
        List<LatLonAlt> exteriorRing3 = createRings(2., 2., 2., 50., 50., 50., 50., 2., 2., 2.);

        List<LatLonAlt> interiorRing1 = createRings(10., 10., 20., 10., 20., 20., 10., 20., 10., 10.);
        List<LatLonAlt> interiorRing2 = createRings(25., 25., 35., 25., 35., 35., 25., 35., 25., 25.);

        List<LatLonAlt> all = New.list();

        // Polygon 1
        all.addAll(exteriorRing1);

        // Polygon 2
        all.addAll(exteriorRing2);
        all.addAll(interiorRing1);

        // Polygon 3
        all.addAll(exteriorRing3);
        all.addAll(interiorRing1);
        all.addAll(interiorRing2);

        Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> decomposition = GeographicUtilities.decomposePositionsToPolygons(all);

        Assert.assertTrue("There must be one hole in the left side polygon.", decomposition.size() == 3);

        for (Entry<List<LatLonAlt>, Collection<List<LatLonAlt>>> entry : decomposition.entrySet())
        {
            if (entry.getKey().get(0).getLonD() == 0.)
            {
                Assert.assertTrue("The fist polygon should have no holes.", entry.getValue().isEmpty());
            }
            else if (entry.getKey().get(0).getLonD() == 1.)
            {
                Assert.assertTrue("The second polygon should have 1 holes.", entry.getValue().size() == 1);
            }
            else if (entry.getKey().get(0).getLonD() == 2.)
            {
                Assert.assertTrue("The third polygon should have 2 holes.", entry.getValue().size() == 2);
            }
        }
    }

    /**
     * Tests the toScreenPositions.
     */
    @Test
    public void testToScreenPositions()
    {
        List<GeographicPosition> geoPositions = New.list(new GeographicPosition(LatLonAlt.createFromDegrees(90, -180)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-90, -180)),
                new GeographicPosition(LatLonAlt.createFromDegrees(90, 180)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-90, 180)),
                new GeographicPosition(LatLonAlt.createFromDegrees(0, 0)),
                new GeographicPosition(LatLonAlt.createFromDegrees(45, -90)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-45, -90)),
                new GeographicPosition(LatLonAlt.createFromDegrees(45, 90)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-45, 90)));

        ScreenBoundingBox screenBounds = new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150));

        List<ScreenPosition> positions = GeographicUtilities.toScreenPositions(geoPositions, screenBounds);

        Assert.assertEquals(9, positions.size());

        Assert.assertEquals(0d, positions.get(0).getX(), 0d);
        Assert.assertEquals(0d, positions.get(0).getY(), 0d);
        Assert.assertEquals(0d, positions.get(1).getX(), 0d);
        Assert.assertEquals(150d, positions.get(1).getY(), 0d);
        Assert.assertEquals(300d, positions.get(2).getX(), 0d);
        Assert.assertEquals(0d, positions.get(2).getY(), 0d);
        Assert.assertEquals(300d, positions.get(3).getX(), 0d);
        Assert.assertEquals(150d, positions.get(3).getY(), 0d);
        Assert.assertEquals(150d, positions.get(4).getX(), 0d);
        Assert.assertEquals(75d, positions.get(4).getY(), 0d);
        Assert.assertEquals(75d, positions.get(5).getX(), 0d);
        Assert.assertEquals(38d, positions.get(5).getY(), 0d);
        Assert.assertEquals(75d, positions.get(6).getX(), 0d);
        Assert.assertEquals(113d, positions.get(6).getY(), 0d);
        Assert.assertEquals(225d, positions.get(7).getX(), 0d);
        Assert.assertEquals(38d, positions.get(7).getY(), 0d);
        Assert.assertEquals(225d, positions.get(8).getX(), 0d);
        Assert.assertEquals(113d, positions.get(8).getY(), 0d);
    }

    /**
     * Utility method for creating the list of LatLonAlt to make the code easier
     * to read.
     *
     * @param lonLat Interleaved pairs of longitude followed by latitude.
     * @return the newly created list
     */
    private List<LatLonAlt> createRings(double... lonLat)
    {
        List<LatLonAlt> ring = New.list(lonLat.length / 2);

        for (int i = 0; i < lonLat.length; i += 2)
        {
            ring.add(LatLonAlt.createFromDegrees(lonLat[i + 1], lonLat[i]));
        }

        return ring;
    }
}
