package io.opensphere.core.util.jts;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.model.GeographicUtilities.PolygonWinding;

/**
 * Test for {@link JTSUtilities}.
 */
public class JTSUtilitiesTest
{
    /** A generic geometry factory. */
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Test
     * {@link JTSUtilities#removeColinearCoordinates(com.vividsolutions.jts.geom.CoordinateSequence)}
     * .
     *
     * @throws ParseException If the test fails.
     */
    @Test
    public void testRemoveColinearCoordinates() throws ParseException
    {
        WKTReader wktReader = new WKTReader();
        Polygon poly = (Polygon)wktReader
                .read("POLYGON ((39.112988931854005 29.81059452656339, 39.04409144256399 29.90228152435198, 39.09796458962061 "
                        + "29.830588532837407, 39.09796458962061 29.830588532837403, 39.112988931854005 29.81059452656339))");
        CoordinateSequence result = JTSUtilities.removeColinearCoordinates(poly.getExteriorRing().getCoordinateSequence());
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(poly.getExteriorRing().getCoordinateN(0), result.getCoordinate(0));

        poly = (Polygon)wktReader.read(
                "POLYGON ((38.47284670237692 29.10299964527892, 39.83903968752042 30.693142647959093, 39.97976598538794 29.857629062172062, "
                        + "39.740147373341685 29.857629062172062, 39.740147373341685 29.572671728413983, 39.656981275688324 29.56916980952858, "
                        + "39.53212882282782 29.582821413469514, 39.41183202611622 29.615009550133816, 39.299687464259364 29.66477332836825, "
                        + "39.19906068114374 29.730625450190097, 39.112988931854005 29.81059452656339, 39.09796458962061 29.830588532837403, "
                        + "38.47284670237692 29.10299964527892))");

        result = JTSUtilities.removeColinearCoordinates(poly.getExteriorRing().getCoordinateSequence());
        Polygon expected = (Polygon)wktReader.read(
                "POLYGON ((39.83903968752042 30.693142647959093, 39.97976598538794 29.857629062172062, 39.740147373341685 29.857629062172062, "
                        + "39.740147373341685 29.572671728413983, 39.656981275688324 29.56916980952858, 39.53212882282782 29.582821413469514, "
                        + "39.41183202611622 29.615009550133816, 39.299687464259364 29.66477332836825, 39.19906068114374 29.730625450190097, "
                        + "39.112988931854005 29.81059452656339, 39.09796458962061 29.830588532837403, 39.83903968752042 30.693142647959093))");
        Assert.assertArrayEquals(expected.getExteriorRing().getCoordinates(), result.toCoordinateArray());

        poly = (Polygon)wktReader.read("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))");
        result = JTSUtilities.removeColinearCoordinates(poly.getExteriorRing().getCoordinateSequence());
        Assert.assertNull(result);
    }

    /**
     * Test
     * {@link JTSUtilities#splitOnAntimeridian(Polygon, GeographicBoundingBox, PolygonWinding)}
     * .
     */
    @Test
    public void testSplitOnAntiMeridianByBounds()
    {
        GeographicPosition lowerLeftCorner = new GeographicPosition(LatLonAlt.createFromDegrees(-50., -10));
        GeographicPosition upperRightCorner = new GeographicPosition(LatLonAlt.createFromDegrees(50., -160));
        GeographicBoundingBox bbox = new GeographicBoundingBox(lowerLeftCorner, upperRightCorner);

        // an exterior ring with segments longer than 180 degrees
        // longitudinally.
        LinearRing shell = (LinearRing)readGeometryFromJTSString("LINEARRING (-10 -50, -160 -50, -160 50, -10 50, -10 -50)");

        LinearRing[] holes = new LinearRing[2];

        // a hole which crosses the antimeridian
        holes[0] = (LinearRing)readGeometryFromJTSString("LINEARRING (170 10, -170 10, -170 40, 170 40, 170 10)");

        // a hole which does not cross the antimeridian
        holes[1] = (LinearRing)readGeometryFromJTSString("LINEARRING (170 -10, 170 -40, 175 -40, 175 -10, 170 -10)");

        Polygon polygon = new Polygon(shell, holes, GEOMETRY_FACTORY);

        List<Polygon> split = JTSUtilities.splitOnAntimeridian(polygon, bbox, PolygonWinding.UNKNOWN);

        Assert.assertTrue("There must be 2 polygons after dividing. ", split.size() == 2);

        for (Polygon part : split)
        {
            if (part.getCentroid().getCoordinate().x > 0)
            {
                Assert.assertTrue("There must be one hole in the left side polygon.", part.getNumInteriorRing() == 1);

                // Check to see if the shell matches what we expect.
                LinearRing splitShell = (LinearRing)readGeometryFromJTSString(
                        "LINEARRING (180 50, 180 40, 170 40, 170 10, " + "180 10, 180 -50, -10 -50, -10 50, 180 50)");
                Assert.assertEquals(part.getExteriorRing(), splitShell);

                // Check to see if the hole matches what we expect.
                Assert.assertEquals(part.getInteriorRingN(0), holes[1]);
            }
            else
            {
                Assert.assertTrue("There must be no holes in the right side polygon", part.getNumInteriorRing() == 0);

                LinearRing splitShell = (LinearRing)readGeometryFromJTSString(
                        "LINEARRING (-180 -50, -180 10, -170 10, -170 40, " + "-180 40, -180 50, -160 50, -160 -50, -180 -50)");
                Assert.assertEquals(part.getExteriorRing(), splitShell);
            }
        }
    }

    /**
     * Test
     * {@link JTSUtilities#splitOnAntimeridian(Polygon, GeographicBoundingBox, PolygonWinding)}
     * .
     */
    @Test
    public void testSplitOnAntimeridianBySegmentLength()
    {
        LinearRing shell = (LinearRing)readGeometryFromJTSString("LINEARRING (160 -50, -160 -50, -160 50, 160 50, 160 -50)");

        LinearRing[] holes = new LinearRing[2];

        // a hole which crosses the antimeridian
        holes[0] = (LinearRing)readGeometryFromJTSString("LINEARRING (170 10, -170 10, -170 40, 170 40, 170 10)");

        // a hole which does not cross the antimeridian
        holes[1] = (LinearRing)readGeometryFromJTSString("LINEARRING (170 -10, 170 -40, 175 -40, 175 -10, 170 -10)");

        Polygon polygon = new Polygon(shell, holes, GEOMETRY_FACTORY);

        List<Polygon> split = JTSUtilities.splitOnAntimeridian(polygon, null, PolygonWinding.UNKNOWN);

        Assert.assertTrue("There must be 2 polygons after dividing. ", split.size() == 2);

        for (Polygon part : split)
        {
            if (part.getCentroid().getCoordinate().x > 0)
            {
                Assert.assertTrue("There must be one hole in the left side polygon.", part.getNumInteriorRing() == 1);

                // Check to see if the shell matches what we expect.
                LinearRing splitShell = (LinearRing)readGeometryFromJTSString(
                        "LINEARRING (180 50, 180 40, 170 40, 170 10, " + "180 10, 180 -50, 160 -50, 160 50, 180 50)");
                Assert.assertEquals(part.getExteriorRing(), splitShell);

                // Check to see if the hole matches what we expect.
                Assert.assertEquals(part.getInteriorRingN(0), holes[1]);
            }
            else
            {
                Assert.assertTrue("There must be no holes in the right side polygon", part.getNumInteriorRing() == 0);

                LinearRing splitShell = (LinearRing)readGeometryFromJTSString(
                        "LINEARRING (-180 -50, -180 10, -170 10, -170 40, " + "-180 40, -180 50, -160 50, -160 -50, -180 -50)");
                Assert.assertEquals(part.getExteriorRing(), splitShell);
            }
        }
    }

    /**
     * Test
     * {@link JTSUtilities#splitOnAntimeridian(Polygon, GeographicBoundingBox, PolygonWinding)}
     * .
     */
    @Test
    public void testSplitOnAntimeridianNoSplit()
    {
        LinearRing shell = (LinearRing)readGeometryFromJTSString("LINEARRING (100 0, 150 0, 150 50, 100 50, 100 0)");

        LinearRing[] holes = new LinearRing[1];
        holes[0] = (LinearRing)readGeometryFromJTSString("LINEARRING (110 10, 140 10, 140 40, 110 40, 110 10)");

        Polygon polygon = new Polygon(shell, holes, GEOMETRY_FACTORY);

        List<Polygon> split = JTSUtilities.splitOnAntimeridian(polygon, null, PolygonWinding.UNKNOWN);

        Assert.assertEquals(polygon, split.get(0));
    }

    /**
     * Generate a JTS geometry from a string which describes a JTS geometry. For
     * Example : "POLYGON ((160 -50, -160 -50, -160 50, 160 50, 160 -50))"
     *
     * @param geomString the string which describes the geometry
     * @return the coordinates which describe the geometry.
     */
    private Geometry readGeometryFromJTSString(String geomString)
    {
        WKTReader reader = new WKTReader();
        try
        {
            return reader.read(geomString);
        }
        catch (ParseException e)
        {
            // just return null, this will break the test.
            return null;
        }
    }
}
