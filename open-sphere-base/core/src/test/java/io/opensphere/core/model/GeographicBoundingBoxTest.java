package io.opensphere.core.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.junit.Assert;

/** Test for {@link GeographicBoundingBox}. */
public class GeographicBoundingBoxTest
{
    /**
     * Test for {@link GeographicBoundingBox#contains(Position, double)}.
     */
    @Test
    public void testContains()
    {
        GeographicPosition lowerLeft1 = new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, -5.0));
        GeographicPosition upperRight1 = new GeographicPosition(LatLonAlt.createFromDegrees(10.0, 10.0));
        GeographicBoundingBox box1 = new GeographicBoundingBox(lowerLeft1, upperRight1);

        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, 0.0)), 0));

        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, -5.0)), 0));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(10.0, 10.0)), 0));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, 10.0)), 0));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(10.0, -5.0)), 0));

        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, 10.0)), 0));
        Assert.assertFalse(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, 11.0)), 0));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, 11.0)), 1.0));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, 12.0)), 3.0));
        Assert.assertFalse(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, 12.0)), 1.0));

        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, -4.0)), 0));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, -5.0)), 0));
        Assert.assertFalse(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, -6.0)), 0));
        Assert.assertFalse(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, -7.0)), 1));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, -7.0)), 2));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, -7.0)), 3));

        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-4.0, 0.0)), 0));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, 0.0)), 0));
        Assert.assertFalse(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-6.0, 0.0)), 0));
        Assert.assertFalse(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-7, 0.0)), 0));
        Assert.assertFalse(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-7.0, 0.0)), 1));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-7.0, 0.0)), 2));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-7.0, 0.0)), 3));
        Assert.assertTrue(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-7.0, 0.0)), 50));
        Assert.assertFalse(box1.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-10.0, 0.0)), 0));

        GeographicPosition lowerLeft2 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 165.0));
        GeographicPosition upperRight2 = new GeographicPosition(LatLonAlt.createFromDegrees(10, 175.0));
        GeographicBoundingBox box2 = new GeographicBoundingBox(lowerLeft2, upperRight2);

        Assert.assertTrue(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 170.0)), 3));
        Assert.assertTrue(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, 170.0)), 5));
        Assert.assertFalse(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, 170.0)), 4));

        Assert.assertFalse(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 180.0)), 4));
        Assert.assertTrue(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 180.0)), 5));
        Assert.assertTrue(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 180.0)), 6));

        // Test across 180 boundary
        Assert.assertFalse(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -175.0)), 0));
        Assert.assertFalse(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -175.0)), 9));
        Assert.assertTrue(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -175.0)), 10));
        Assert.assertTrue(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -175.0)), 11));
        Assert.assertTrue(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 185.0)), 10));

        Assert.assertFalse(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-1.0, 176.0)), 0));
        Assert.assertTrue(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-1.0, 176.0)), 2));
        Assert.assertTrue(box2.contains(new GeographicPosition(LatLonAlt.createFromDegrees(-1.0, 176.0)), 100));
    }

    /**
     * Test for {@link GeographicBoundingBox#getCenter()}.
     */
    @Test
    public void testGetCenter()
    {
        // Center over the origin
        GeographicPosition lowerLeft1 = new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, -5.0));
        GeographicPosition upperRight1 = new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 5.0));
        GeographicBoundingBox box1 = new GeographicBoundingBox(lowerLeft1, upperRight1);

        GeographicPosition center1 = box1.getCenter();
        Assert.assertEquals(center1, new GeographicPosition(LatLonAlt.createFromDegrees(0.0, 0.0)));

        // Center on the 180 longitude boundary line.
        GeographicPosition lowerLeft2 = new GeographicPosition(LatLonAlt.createFromDegrees(-10, 170));
        GeographicPosition upperRight2 = new GeographicPosition(LatLonAlt.createFromDegrees(10, -170));
        GeographicBoundingBox box2 = new GeographicBoundingBox(lowerLeft2, upperRight2);

        GeographicPosition center2 = box2.getCenter();
        Assert.assertEquals(center2, new GeographicPosition(LatLonAlt.createFromDegrees(0.0, -180.0)));

        // Center with negative longitudes
        GeographicPosition lowerLeft3 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -30));
        GeographicPosition upperRight3 = new GeographicPosition(LatLonAlt.createFromDegrees(15, -10));
        GeographicBoundingBox box3 = new GeographicBoundingBox(lowerLeft3, upperRight3);

        GeographicPosition center3 = box3.getCenter();
        Assert.assertEquals(center3, new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -20.0)));

        // Center near the 180 longitude boundary.
        GeographicPosition lowerLeft4 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 175));
        GeographicPosition upperRight4 = new GeographicPosition(LatLonAlt.createFromDegrees(15, -165));
        GeographicBoundingBox box4 = new GeographicBoundingBox(lowerLeft4, upperRight4);

        GeographicPosition center4 = box4.getCenter();
        Assert.assertEquals(center4, new GeographicPosition(LatLonAlt.createFromDegrees(10.0, -175.0)));
    }

    /**
     * Test for
     * {@link GeographicBoundingBox#getMinimumBoundingBox(java.util.Collection)}
     * .
     */
    @Test
    public void testGetMinimumBoundingBox()
    {
        List<GeographicPosition> testPoints = new ArrayList<>();

        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(20.058525870251344, 46.50066830594939)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(32.64288024782886, 61.661855183778314)));

        GeographicBoundingBox bBox = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.058525870251344, 46.50066830594939)),
                bBox.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(32.64288024782886, 61.661855183778314)),
                bBox.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(32.64288024782886, 46.50066830594939)),
                bBox.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.058525870251344, 61.661855183778314)),
                bBox.getLowerRight());

        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(20.5, 46.5)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(32.0, 62.0)));

        GeographicBoundingBox bBox2 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.5, 46.5)), bBox2.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(32.0, 62.0)), bBox2.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(32.0, 46.5)), bBox2.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.5, 62.0)), bBox2.getLowerRight());

        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(-25.0, 25.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, -35.0)));

        GeographicBoundingBox bBox3 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-25.0, -35.0)), bBox3.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, 25.0)), bBox3.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, -35.0)), bBox3.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-25.0, 25.0)), bBox3.getLowerRight());

        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(-85.0, -175.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(85.0, 175.0)));

        GeographicBoundingBox bBox4 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-85.0, 175.0)), bBox4.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(85.0, -175.0)), bBox4.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(85.0, 175.0)), bBox4.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-85.0, -175.0)), bBox4.getLowerRight());

        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, -185.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, 185.0)));

        GeographicBoundingBox bBox5 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, 175.0)), bBox5.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, -175.0)), bBox5.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, 175.0)), bBox5.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, -175.0)), bBox5.getLowerRight());

        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(-10.0, -20.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, 10.0)));

        GeographicBoundingBox bBox6 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-10.0, -20.0)), bBox6.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, 10.0)), bBox6.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, -20.0)), bBox6.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-10.0, 10.0)), bBox6.getLowerRight());

        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, 0.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, -10.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(-10.0, 25.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 10.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, -5.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, 0.0)));

        GeographicBoundingBox bBox7 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-10.0, -10.0)), bBox7.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, 25.0)), bBox7.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, -10.0)), bBox7.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-10.0, 25.0)), bBox7.getLowerRight());

        double lat = 5.0;
        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(lat, -20.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(lat, 10.0)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(lat, -5.0)));

        GeographicBoundingBox bBox8 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        // When the latitude values are the same, a calculation is done to add
        // a little padding on either side to create a box. This is taken from
        // the code in GeographicBoudingBox::createBoxFromEdges.

        double deltaLat = 0;
        double deltaLon = 10.0 - -20.0;
        double minDeltaLat = deltaLon * GeographicBoundingBox.MAX_BOX_FLATTENING;
        double shift = (minDeltaLat - deltaLat) * 0.5;
        double adjustedMinLat = lat - shift;
        double adjustedMaxLat = lat + shift;

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(adjustedMinLat, -20.0)), bBox8.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(adjustedMaxLat, 10.0)), bBox8.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(adjustedMaxLat, -20.0)), bBox8.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(adjustedMinLat, 10.0)), bBox8.getLowerRight());

        double lon = 10.0;
        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, lon)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(0.0, lon)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(17.0, lon)));

        GeographicBoundingBox bBox9 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        // When the longitude values are the same, a calculation is done to add
        // a little padding on either side to create a box. This is taken from
        // the code in GeographicBoudingBox::createBoxFromEdges.

        deltaLat = 17.0 - -5.0;
        deltaLon = 0;
        double minDeltaLon = deltaLat * GeographicBoundingBox.MAX_BOX_FLATTENING;
        shift = (minDeltaLon - deltaLon) * 0.5;
        double adjustedMinLon = lon - shift;
        double adjustedMaxLon = lon + shift;

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, adjustedMinLon)), bBox9.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(17.0, adjustedMaxLon)), bBox9.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(17.0, adjustedMinLon)), bBox9.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, adjustedMaxLon)), bBox9.getLowerRight());

        double latlon = 5.0;
        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(latlon, latlon)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(latlon, latlon)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(latlon, latlon)));

        GeographicBoundingBox bBox10 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        // When both the latitude and longitude values are the same, no special
        // processing
        // is done and they remain the same values.
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(latlon, latlon)), bBox10.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(latlon, latlon)), bBox10.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(latlon, latlon)), bBox10.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(latlon, latlon)), bBox10.getLowerRight());

        // Test potential floating point error case
        testPoints.clear();
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(21.46666667, 101.5833333)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(23.634722222222223, 105.62222222222222)));
        testPoints.add(new GeographicPosition(LatLonAlt.createFromDegrees(29.326944444444443, 85.23388888888888)));

        GeographicBoundingBox bBox11 = GeographicBoundingBox.getMinimumBoundingBox(testPoints);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(21.46666667, 85.23388888888888)),
                bBox11.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(29.326944444444443, 105.62222222222222)),
                bBox11.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(29.326944444444443, 85.23388888888888)),
                bBox11.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(21.46666667, 105.62222222222222)),
                bBox11.getLowerRight());
    }

    /**
     * Test {@link GeographicBoundingBox}.intersection.
     */
    @Test
    public void testIntersection()
    {
        // Two boxes that do not intersect
        GeographicPosition lowerLeft1 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 0));
        GeographicPosition upperRight1 = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicBoundingBox box1 = new GeographicBoundingBox(lowerLeft1, upperRight1);

        GeographicPosition lowerLeft2 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 25));
        GeographicPosition upperRight2 = new GeographicPosition(LatLonAlt.createFromDegrees(25, 35));
        GeographicBoundingBox box2 = new GeographicBoundingBox(lowerLeft2, upperRight2);

        Assert.assertNull(box2.intersection(box1));

        // Two boxes that intersect
        GeographicPosition lowerLeft3 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight3 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 5));
        GeographicBoundingBox box3 = new GeographicBoundingBox(lowerLeft3, upperRight3);

        GeographicBoundingBox bggI1 = (GeographicBoundingBox)box1.intersection(box3);
        Assert.assertNotNull(bggI1);
        Assert.assertEquals(bggI1.getLowerLeft(), lowerLeft1);
        Assert.assertEquals(bggI1.getUpperRight(), upperRight3);

        // One box is contained within the other
        GeographicPosition lowerLeft4 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight4 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 15));
        GeographicBoundingBox box4 = new GeographicBoundingBox(lowerLeft4, upperRight4);

        GeographicBoundingBox bggI2 = (GeographicBoundingBox)box1.intersection(box4);
        Assert.assertNotNull(bggI2);
        Assert.assertEquals(bggI2.getLowerLeft(), lowerLeft1);
        Assert.assertEquals(bggI2.getUpperRight(), upperRight1);

        // One box has shared side with the other
        GeographicPosition lowerLeft5 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 10));
        GeographicPosition upperRight5 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 20));
        GeographicBoundingBox box5 = new GeographicBoundingBox(lowerLeft5, upperRight5);

        GeographicBoundingBox bggI3 = (GeographicBoundingBox)box1.intersection(box5);
        Assert.assertNotNull(bggI3);
        Assert.assertEquals(bggI3.getLowerLeft(), lowerLeft5);
        Assert.assertEquals(bggI3.getUpperRight(), upperRight1);

        // One box shares a single point with the other.
        GeographicPosition lowerLeft6 = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicBoundingBox box6 = new GeographicBoundingBox(lowerLeft6, upperRight2);
        GeographicBoundingBox bggI4 = (GeographicBoundingBox)box1.intersection(box6);
        Assert.assertNotNull(bggI4);
        Assert.assertEquals(bggI4.getLowerLeft(), lowerLeft6);
        Assert.assertEquals(bggI4.getUpperRight(), lowerLeft6);

        // Boxes that cross 180 longitude boundary.
        GeographicPosition lowerLeft7 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 175));
        GeographicPosition upperRight7 = new GeographicPosition(LatLonAlt.createFromDegrees(15, -170));
        GeographicBoundingBox box7 = new GeographicBoundingBox(lowerLeft7, upperRight7);

        GeographicPosition lowerLeft8 = new GeographicPosition(LatLonAlt.createFromDegrees(10, -175));
        GeographicPosition upperRight8 = new GeographicPosition(LatLonAlt.createFromDegrees(25, -160));
        GeographicBoundingBox box8 = new GeographicBoundingBox(lowerLeft8, upperRight8);

        GeographicBoundingBox bggI5 = (GeographicBoundingBox)box7.intersection(box8);
        Assert.assertNotNull(bggI5);
        Assert.assertEquals(bggI5.getLowerLeft(), new GeographicPosition(LatLonAlt.createFromDegrees(10, -170)));
        Assert.assertEquals(bggI5.getUpperRight(), new GeographicPosition(LatLonAlt.createFromDegrees(15, -160)));

//        // Boxes that are on opposite sides of 180 longitude boundary.
        GeographicPosition lowerLeft9 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, 170));
        GeographicPosition upperRight9 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 175));
        GeographicBoundingBox box9 = new GeographicBoundingBox(lowerLeft9, upperRight9);

        GeographicPosition lowerLeft10 = new GeographicPosition(LatLonAlt.createFromDegrees(0, -175));
        GeographicPosition upperRight10 = new GeographicPosition(LatLonAlt.createFromDegrees(10, -170));
        GeographicBoundingBox box10 = new GeographicBoundingBox(lowerLeft10, upperRight10);
        GeographicBoundingBox bggI6 = (GeographicBoundingBox)box9.intersection(box10);
        Assert.assertNull(bggI6);
    }

    /**
     * Test {@link GeographicBoundingBox}.intersects.
     */
    @Test
    public void testIntersects()
    {
        // Two boxes that do not intersect
        GeographicPosition lowerLeft1 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 0));
        GeographicPosition upperRight1 = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicBoundingBox box1 = new GeographicBoundingBox(lowerLeft1, upperRight1);

        GeographicPosition lowerLeft2 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 25));
        GeographicPosition upperRight2 = new GeographicPosition(LatLonAlt.createFromDegrees(25, 35));
        GeographicBoundingBox box2 = new GeographicBoundingBox(lowerLeft2, upperRight2);

        Assert.assertFalse(box2.intersects(box1));

        // Two boxes that intersect
        GeographicPosition lowerLeft3 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight3 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 5));
        GeographicBoundingBox box3 = new GeographicBoundingBox(lowerLeft3, upperRight3);

        Assert.assertTrue(box1.intersects(box3));

        // One box is contained within the other
        GeographicPosition lowerLeft4 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight4 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 15));
        GeographicBoundingBox box4 = new GeographicBoundingBox(lowerLeft4, upperRight4);

        Assert.assertTrue(box1.intersects(box4));

        // One box has shared side with the other
        GeographicPosition lowerLeft5 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 10));
        GeographicPosition upperRight5 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 20));
        GeographicBoundingBox box5 = new GeographicBoundingBox(lowerLeft5, upperRight5);

        Assert.assertTrue(box1.intersects(box5));

        // Boxes that cross 180 longitude boundary.
        GeographicPosition lowerLeft6 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 175));
        GeographicPosition upperRight6 = new GeographicPosition(LatLonAlt.createFromDegrees(15, -170));
        GeographicBoundingBox box6 = new GeographicBoundingBox(lowerLeft6, upperRight6);

        GeographicPosition lowerLeft7 = new GeographicPosition(LatLonAlt.createFromDegrees(10, -175));
        GeographicPosition upperRight7 = new GeographicPosition(LatLonAlt.createFromDegrees(25, -160));
        GeographicBoundingBox box7 = new GeographicBoundingBox(lowerLeft7, upperRight7);

        Assert.assertTrue(box6.intersects(box7));

        // Boxes that are on opposite sides of 180 longitude boundary.
        GeographicPosition lowerLeft8 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, 170));
        GeographicPosition upperRight8 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 175));
        GeographicBoundingBox box8 = new GeographicBoundingBox(lowerLeft8, upperRight8);

        GeographicPosition lowerLeft9 = new GeographicPosition(LatLonAlt.createFromDegrees(0, -175));
        GeographicPosition upperRight9 = new GeographicPosition(LatLonAlt.createFromDegrees(10, -170));
        GeographicBoundingBox box9 = new GeographicBoundingBox(lowerLeft9, upperRight9);

        Assert.assertFalse(box8.intersects(box9));
    }

    /**
     * Test for
     * {@link GeographicBoundingBox#merge(GeographicBoundingBox, GeographicBoundingBox)}
     * .
     */
    @Test
    public void testMerge()
    {
        // Two boxes that do not intersect
        GeographicPosition lowerLeft1 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 0));
        GeographicPosition upperRight1 = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicBoundingBox box1 = new GeographicBoundingBox(lowerLeft1, upperRight1);

        GeographicPosition lowerLeft2 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 25));
        GeographicPosition upperRight2 = new GeographicPosition(LatLonAlt.createFromDegrees(25, 35));
        GeographicBoundingBox box2 = new GeographicBoundingBox(lowerLeft2, upperRight2);

        GeographicBoundingBox mergedBox = GeographicBoundingBox.merge(box1, box2);

        Assert.assertEquals(lowerLeft1, mergedBox.getLowerLeft());
        Assert.assertEquals(upperRight2, mergedBox.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(25, 0)), mergedBox.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(0, 35)), mergedBox.getLowerRight());

        // Two boxes that intersect
        GeographicPosition lowerLeft3 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight3 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 5));
        GeographicBoundingBox box3 = new GeographicBoundingBox(lowerLeft3, upperRight3);

        GeographicBoundingBox mergedBox2 = GeographicBoundingBox.merge(box1, box3);

        Assert.assertEquals(lowerLeft3, mergedBox2.getLowerLeft());
        Assert.assertEquals(upperRight1, mergedBox2.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(10, -5)), mergedBox2.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-5, 10)), mergedBox2.getLowerRight());

        // One box is contained within the other
        GeographicPosition lowerLeft4 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight4 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 15));
        GeographicBoundingBox box4 = new GeographicBoundingBox(lowerLeft4, upperRight4);

        GeographicBoundingBox mergedBox3 = GeographicBoundingBox.merge(box1, box4);

        Assert.assertEquals(lowerLeft4, mergedBox3.getLowerLeft());
        Assert.assertEquals(upperRight4, mergedBox3.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15, -5)), mergedBox3.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-5, 15)), mergedBox3.getLowerRight());

        // Boxes that cross 180 longitude boundary
        GeographicPosition lowerLeft5 = new GeographicPosition(LatLonAlt.createFromDegrees(15.0, 170.0));
        GeographicPosition upperRight5 = new GeographicPosition(LatLonAlt.createFromDegrees(20.0, -175.0));
        GeographicBoundingBox box5 = new GeographicBoundingBox(lowerLeft5, upperRight5);

        GeographicPosition lowerLeft6 = new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 175.0));
        GeographicPosition upperRight6 = new GeographicPosition(LatLonAlt.createFromDegrees(15.0, -170.0));
        GeographicBoundingBox box6 = new GeographicBoundingBox(lowerLeft6, upperRight6);

        GeographicBoundingBox mergedBox4 = GeographicBoundingBox.merge(box5, box6);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 170.0)), mergedBox4.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, -170.0)), mergedBox4.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, 170.0)), mergedBox4.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -170.0)), mergedBox4.getLowerRight());

        GeographicPosition lowerLeft7 = new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -175.0));
        GeographicPosition upperRight7 = new GeographicPosition(LatLonAlt.createFromDegrees(15.0, -165.0));
        GeographicBoundingBox box7 = new GeographicBoundingBox(lowerLeft7, upperRight7);

        GeographicBoundingBox mergedBox5 = GeographicBoundingBox.merge(box5, box7);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 170.0)), mergedBox5.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, -165.0)), mergedBox5.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, 170.0)), mergedBox5.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -165.0)), mergedBox5.getLowerRight());

        GeographicPosition lowerLeft10 = new GeographicPosition(LatLonAlt.createFromDegrees(10.0, 165.0));
        GeographicPosition upperRight10 = new GeographicPosition(LatLonAlt.createFromDegrees(25.0, 175.0));
        GeographicBoundingBox box10 = new GeographicBoundingBox(lowerLeft10, upperRight10);

        GeographicBoundingBox mergedBox7 = GeographicBoundingBox.merge(box10, box7);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 165.0)), mergedBox7.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(25.0, -165.0)), mergedBox7.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(25.0, 165.0)), mergedBox7.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -165.0)), mergedBox7.getLowerRight());

        // Boxes that share common side of 180 longitude boundary
        GeographicPosition lowerLeft8 = new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 170.0));
        GeographicPosition upperRight8 = new GeographicPosition(LatLonAlt.createFromDegrees(15.0, 180.0));
        GeographicBoundingBox box8 = new GeographicBoundingBox(lowerLeft8, upperRight8);

        GeographicPosition lowerLeft9 = new GeographicPosition(LatLonAlt.createFromDegrees(10.0, -180.0));
        GeographicPosition upperRight9 = new GeographicPosition(LatLonAlt.createFromDegrees(20.0, -170.0));
        GeographicBoundingBox box9 = new GeographicBoundingBox(lowerLeft9, upperRight9);

        GeographicBoundingBox mergedBox6 = GeographicBoundingBox.merge(box8, box9);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 170.0)), mergedBox6.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, -170.0)), mergedBox6.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, 170.0)), mergedBox6.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -170.0)), mergedBox6.getLowerRight());

        // Boxes that cross 0 longitude boundary
        GeographicPosition lowerLeft12 = new GeographicPosition(LatLonAlt.createFromDegrees(5, -15));
        GeographicPosition upperRight12 = new GeographicPosition(LatLonAlt.createFromDegrees(15, -5));
        GeographicBoundingBox box12 = new GeographicBoundingBox(lowerLeft12, upperRight12);

        GeographicPosition lowerLeft11 = new GeographicPosition(LatLonAlt.createFromDegrees(-10, 5));
        GeographicPosition upperRight11 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 15));
        GeographicBoundingBox box11 = new GeographicBoundingBox(lowerLeft11, upperRight11);

        GeographicBoundingBox mergedBox8 = GeographicBoundingBox.merge(box12, box11);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-10.0, -15.0)), mergedBox8.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, 15.0)), mergedBox8.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, -15.0)), mergedBox8.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-10.0, 15.0)), mergedBox8.getLowerRight());

        GeographicPosition lowerLeft13 = new GeographicPosition(LatLonAlt.createFromDegrees(5, -10));
        GeographicPosition upperRight13 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 10));
        GeographicBoundingBox box13 = new GeographicBoundingBox(lowerLeft13, upperRight13);

        GeographicPosition lowerLeft14 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight14 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 15));
        GeographicBoundingBox box14 = new GeographicBoundingBox(lowerLeft14, upperRight14);

        GeographicBoundingBox mergedBox9 = GeographicBoundingBox.merge(box13, box14);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, -10.0)), mergedBox9.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, 15.0)), mergedBox9.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(15.0, -10.0)), mergedBox9.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(-5.0, 15.0)), mergedBox9.getLowerRight());

        // Boxes that share common side of 0 longitude boundary
        GeographicPosition lowerLeft15 = new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -10.0));
        GeographicPosition upperRight15 = new GeographicPosition(LatLonAlt.createFromDegrees(15.0, 0.0));
        GeographicBoundingBox box15 = new GeographicBoundingBox(lowerLeft15, upperRight15);

        GeographicPosition lowerLeft16 = new GeographicPosition(LatLonAlt.createFromDegrees(10.0, -0.0));
        GeographicPosition upperRight16 = new GeographicPosition(LatLonAlt.createFromDegrees(20.0, 10.0));
        GeographicBoundingBox box16 = new GeographicBoundingBox(lowerLeft16, upperRight16);

        GeographicBoundingBox mergedBox10 = GeographicBoundingBox.merge(box15, box16);

        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, -10.0)), mergedBox10.getLowerLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, 10.0)), mergedBox10.getUpperRight());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(20.0, -10.0)), mergedBox10.getUpperLeft());
        Assert.assertEquals(new GeographicPosition(LatLonAlt.createFromDegrees(5.0, 10.0)), mergedBox10.getLowerRight());
    }

    /**
     * Test for {@link GeographicBoundingBox#overlaps(Quadrilateral, double)}.
     */
    @Test
    public void testOverlaps()
    {
        // Two boxes that do not intersect
        GeographicPosition lowerLeft1 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 0));
        GeographicPosition upperRight1 = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicBoundingBox box1 = new GeographicBoundingBox(lowerLeft1, upperRight1);

        GeographicPosition lowerLeft2 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 25));
        GeographicPosition upperRight2 = new GeographicPosition(LatLonAlt.createFromDegrees(25, 35));
        GeographicBoundingBox box2 = new GeographicBoundingBox(lowerLeft2, upperRight2);

        Assert.assertFalse(box2.overlaps(box1, 0));
        Assert.assertFalse(box2.overlaps(box1, 10));
        Assert.assertTrue(box2.overlaps(box1, 20));

        // Two boxes that intersect
        GeographicPosition lowerLeft3 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight3 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 5));
        GeographicBoundingBox box3 = new GeographicBoundingBox(lowerLeft3, upperRight3);

        Assert.assertTrue(box1.overlaps(box3, 0));
        Assert.assertTrue(box1.overlaps(box3, 10));

        // One box is contained within the other
        GeographicPosition lowerLeft4 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight4 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 15));
        GeographicBoundingBox box4 = new GeographicBoundingBox(lowerLeft4, upperRight4);

        Assert.assertTrue(box1.overlaps(box4, 0));
        Assert.assertTrue(box1.overlaps(box4, 10));

        // One box has shared side with the other
        GeographicPosition lowerLeft5 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 10));
        GeographicPosition upperRight5 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 20));
        GeographicBoundingBox box5 = new GeographicBoundingBox(lowerLeft5, upperRight5);

        Assert.assertTrue(box1.overlaps(box5, 0));
        Assert.assertTrue(box1.overlaps(box5, 5));

        // Boxes that cross 180 longitude boundary.
        GeographicPosition lowerLeft6 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 175));
        GeographicPosition upperRight6 = new GeographicPosition(LatLonAlt.createFromDegrees(15, -170));
        GeographicBoundingBox box6 = new GeographicBoundingBox(lowerLeft6, upperRight6);

        GeographicPosition lowerLeft7 = new GeographicPosition(LatLonAlt.createFromDegrees(10, -175));
        GeographicPosition upperRight7 = new GeographicPosition(LatLonAlt.createFromDegrees(25, -160));
        GeographicBoundingBox box7 = new GeographicBoundingBox(lowerLeft7, upperRight7);

        Assert.assertTrue(box6.overlaps(box7, 0.0));
        Assert.assertTrue(box6.overlaps(box7, 25.0));

        // Boxes that are on opposite sides of 180 longitude boundary.
        GeographicPosition lowerLeft8 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, 170));
        GeographicPosition upperRight8 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 175));
        GeographicBoundingBox box8 = new GeographicBoundingBox(lowerLeft8, upperRight8);

        GeographicPosition lowerLeft9 = new GeographicPosition(LatLonAlt.createFromDegrees(0, -175));
        GeographicPosition upperRight9 = new GeographicPosition(LatLonAlt.createFromDegrees(10, -170));
        GeographicBoundingBox box9 = new GeographicBoundingBox(lowerLeft9, upperRight9);

        Assert.assertFalse(box8.overlaps(box9, 0.0));
        Assert.assertFalse(box8.overlaps(box9, 1.0));
        Assert.assertFalse(box8.overlaps(box9, 5.0));
        Assert.assertFalse(box8.overlaps(box9, 9.0));
        Assert.assertTrue(box8.overlaps(box9, 10.0));
        Assert.assertTrue(box8.overlaps(box9, 11.0));
        Assert.assertTrue(box8.overlaps(box9, 15.0));
        Assert.assertTrue(box8.overlaps(box9, 25.0));
    }

    /** Test for {@link GeographicBoundingBox#quadSplit()}. */
    @Test
    public void testQuadSplit()
    {
        GeographicBoundingBox box = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-20., -20.),
                LatLonAlt.createFromDegrees(20., 20.));
        List<GeographicBoundingBox> result = box.quadSplit();
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(-20., -20.), LatLonAlt.createFromDegrees(0., 0.)),
                result.get(0));
        Assert.assertEquals(
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(-20., 0.), LatLonAlt.createFromDegrees(0., 20.)),
                result.get(1));
        Assert.assertEquals(
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(0., -20.), LatLonAlt.createFromDegrees(20., 0.)),
                result.get(2));
        Assert.assertEquals(new GeographicBoundingBox(LatLonAlt.createFromDegrees(0., 0.), LatLonAlt.createFromDegrees(20., 20.)),
                result.get(3));
    }

    /** Test for {@link GeographicBoundingBox#toGridString(int)}. */
    @Test
    public void testToGridString()
    {
        Assert.assertEquals("0./0.",
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(-45, -135))
                        .toGridString(45));
        Assert.assertEquals("0.0/0.0",
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(-67.5, -157.5))
                        .toGridString(45));
        Assert.assertEquals("0.0/0.1",
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(-67.5, -180), LatLonAlt.createFromDegrees(-45, -157.5))
                        .toGridString(45));

        StringBuilder sbx = new StringBuilder();
        StringBuilder sby = new StringBuilder();
        for (int x = 0; x < 8; ++x)
        {
            for (int y = 0; y < 4; ++y)
            {
                double lonD = x * 45. - 180.;
                double latD = y * 45. - 90.;

                sbx.setLength(0);
                sbx.append(x).append('.');
                sby.setLength(0);
                sby.append(y).append('.');
                Assert.assertEquals(sbx + "/" + sby, new GeographicBoundingBox(LatLonAlt.createFromDegrees(latD, lonD),
                        LatLonAlt.createFromDegrees(latD + 45., lonD + 45.)).toGridString(45));

                testGrid(latD, lonD, sbx, sby, 22.5);
                Assert.assertEquals(x + ".0/" + y + ".0", new GeographicBoundingBox(LatLonAlt.createFromDegrees(latD, lonD),
                        LatLonAlt.createFromDegrees(latD + 22.5, lonD + 22.5)).toGridString(45));
                Assert.assertEquals(x + ".00/" + y + ".00", new GeographicBoundingBox(LatLonAlt.createFromDegrees(latD, lonD),
                        LatLonAlt.createFromDegrees(latD + 11.25, lonD + 11.25)).toGridString(45));
            }
        }
    }

    /**
     * Test {@link GeographicBoundingBox}.union.
     */
    @Test
    public void testUnion()
    {
        // Two boxes that do not intersect
        GeographicPosition lowerLeft1 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 0));
        GeographicPosition upperRight1 = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicBoundingBox box1 = new GeographicBoundingBox(lowerLeft1, upperRight1);

        GeographicPosition lowerLeft2 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 25));
        GeographicPosition upperRight2 = new GeographicPosition(LatLonAlt.createFromDegrees(25, 35));
        GeographicBoundingBox box2 = new GeographicBoundingBox(lowerLeft2, upperRight2);

        GeographicBoundingBox union1 = (GeographicBoundingBox)box1.union(box2);
        Assert.assertNotNull(union1);
        Assert.assertEquals(union1.getLowerLeft(), lowerLeft1);
        Assert.assertEquals(union1.getUpperRight(), upperRight2);

        // Two boxes that intersect
        GeographicPosition lowerLeft3 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight3 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 5));
        GeographicBoundingBox box3 = new GeographicBoundingBox(lowerLeft3, upperRight3);

        GeographicBoundingBox union2 = (GeographicBoundingBox)box1.union(box3);
        Assert.assertNotNull(union2);
        Assert.assertEquals(union2.getLowerLeft(), lowerLeft3);
        Assert.assertEquals(union2.getUpperRight(), upperRight1);

        // One box is contained within the other
        GeographicPosition lowerLeft4 = new GeographicPosition(LatLonAlt.createFromDegrees(-5, -5));
        GeographicPosition upperRight4 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 15));
        GeographicBoundingBox box4 = new GeographicBoundingBox(lowerLeft4, upperRight4);

        GeographicBoundingBox union3 = (GeographicBoundingBox)box1.union(box4);
        Assert.assertNotNull(union3);
        Assert.assertEquals(union3.getLowerLeft(), lowerLeft4);
        Assert.assertEquals(union3.getUpperRight(), upperRight4);

        // One box has shared side with the other
        GeographicPosition lowerLeft5 = new GeographicPosition(LatLonAlt.createFromDegrees(5, 10));
        GeographicPosition upperRight5 = new GeographicPosition(LatLonAlt.createFromDegrees(15, 20));
        GeographicBoundingBox box5 = new GeographicBoundingBox(lowerLeft5, upperRight5);
        GeographicBoundingBox union4 = (GeographicBoundingBox)box1.union(box5);
        Assert.assertNotNull(union4);
        Assert.assertEquals(union4.getLowerLeft(), lowerLeft1);
        Assert.assertEquals(union4.getUpperRight(), upperRight5);

        // One box shares a single point with the other.
        GeographicPosition lowerLeft6 = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicBoundingBox box6 = new GeographicBoundingBox(lowerLeft6, upperRight2);
        GeographicBoundingBox union5 = (GeographicBoundingBox)box1.union(box6);
        Assert.assertNotNull(union5);
        Assert.assertEquals(union5.getLowerLeft(), lowerLeft1);
        Assert.assertEquals(union5.getUpperRight(), upperRight2);

        // Boxes that cross 180 longitude boundary.
//        GeographicPosition lowerLeft7 = new GeographicPosition(LatLonAlt.createFromDegrees(0, 175));
//        GeographicPosition upperRight7 = new GeographicPosition(LatLonAlt.createFromDegrees(15, -170));
//        GeographicBoundingBox box7 = new GeographicBoundingBox(lowerLeft7, upperRight7);

//        GeographicPosition lowerLeft8 = new GeographicPosition(LatLonAlt.createFromDegrees(10, -175));
//        GeographicPosition upperRight8 = new GeographicPosition(LatLonAlt.createFromDegrees(25, -160));
//        GeographicBoundingBox box8 = new GeographicBoundingBox(lowerLeft8, upperRight8);
//        GeographicBoundingBox union6 = (GeographicBoundingBox)box7.union(box8);

        // This will not work as we would like for now. It will not merge across
        // the meridian correctly.

//        Assert.assertNotNull(union6);
//        Assert.assertEquals(union6.getLowerLeft(), new GeographicPosition(LatLonAlt.createFromDegrees(10, -170)));
//        Assert.assertEquals(union6.getUpperRight(), new GeographicPosition(LatLonAlt.createFromDegrees(15, -160)));
    }

    /**
     * Recursively test a square of the grid.
     *
     * @param latD The southern latitude.
     * @param lonD The western longitude.
     * @param sbx The string builder for the X coordinate.
     * @param sby The string builder for the Y coordinate.
     * @param div The current grid size in degrees.
     */
    private void testGrid(double latD, double lonD, StringBuilder sbx, StringBuilder sby, double div)
    {
        sbx.append('0');
        sby.append('0');
        Assert.assertEquals(sbx + "/" + sby, new GeographicBoundingBox(LatLonAlt.createFromDegrees(latD, lonD),
                LatLonAlt.createFromDegrees(latD + div, lonD + div)).toGridString(45));
        if (div > .3515625)
        {
            testGrid(latD, lonD, sbx, sby, div * .5);
        }

        sbx.setCharAt(sbx.length() - 1, '1');
        Assert.assertEquals(sbx + "/" + sby, new GeographicBoundingBox(LatLonAlt.createFromDegrees(latD, lonD + div),
                LatLonAlt.createFromDegrees(latD + div, lonD + div * 2)).toGridString(45));
        if (div > .3515625)
        {
            testGrid(latD, lonD + div, sbx, sby, div * .5);
        }

        sby.setCharAt(sby.length() - 1, '1');
        Assert.assertEquals(sbx + "/" + sby, new GeographicBoundingBox(LatLonAlt.createFromDegrees(latD + div, lonD + div),
                LatLonAlt.createFromDegrees(latD + div * 2, lonD + div * 2)).toGridString(45));
        if (div > .3515625)
        {
            testGrid(latD + div, lonD + div, sbx, sby, div * .5);
        }

        sbx.setCharAt(sbx.length() - 1, '0');
        Assert.assertEquals(sbx + "/" + sby, new GeographicBoundingBox(LatLonAlt.createFromDegrees(latD + div, lonD),
                LatLonAlt.createFromDegrees(latD + div * 2, lonD + div)).toGridString(45));
        if (div > .3515625)
        {
            testGrid(latD + div, lonD, sbx, sby, div * .5);
        }

        sbx.setLength(sbx.length() - 1);
        sby.setLength(sby.length() - 1);
    }
}
