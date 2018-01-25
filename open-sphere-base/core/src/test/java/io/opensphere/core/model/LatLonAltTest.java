package io.opensphere.core.model;

import org.junit.Test;

import org.junit.Assert;

/** Test for {@link LatLonAlt}. */
public class LatLonAltTest
{
    /**
     * Helper function to test LatLonAlt are the same.
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

    /** Test for {@link LatLonAlt#crossesAntimeridian(LatLonAlt, LatLonAlt)}. */
    @Test
    public void testCrossesAntimeridian()
    {
        LatLonAlt e179 = LatLonAlt.createFromDegrees(0, 179.);
        LatLonAlt e180 = LatLonAlt.createFromDegrees(0, 180.);
        LatLonAlt e181 = LatLonAlt.createFromDegrees(0, 181.);
        LatLonAlt w179 = LatLonAlt.createFromDegrees(0, -179.);
        LatLonAlt w180 = LatLonAlt.createFromDegrees(0, -180.);
        LatLonAlt w181 = LatLonAlt.createFromDegrees(0, -181.);

        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e179, e179));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e179, e180));
        Assert.assertTrue(LatLonAlt.crossesAntimeridian(e179, e181));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e180, e179));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e180, e180));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e180, e181));
        Assert.assertTrue(LatLonAlt.crossesAntimeridian(e181, e179));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e181, e180));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e181, e181));

        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w181, e179));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w181, e180));
        Assert.assertTrue(LatLonAlt.crossesAntimeridian(w181, e181));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w180, e179));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w180, e180));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w180, e181));
        Assert.assertTrue(LatLonAlt.crossesAntimeridian(w179, e179));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w179, e180));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w179, e181));

        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e179, w181));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e179, w180));
        Assert.assertTrue(LatLonAlt.crossesAntimeridian(e179, w179));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e180, w181));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e180, w180));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e180, w179));
        Assert.assertTrue(LatLonAlt.crossesAntimeridian(e181, w181));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e181, w180));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(e181, w179));

        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w181, w181));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w181, w180));
        Assert.assertTrue(LatLonAlt.crossesAntimeridian(w181, w179));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w180, w181));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w180, w180));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w180, w179));
        Assert.assertTrue(LatLonAlt.crossesAntimeridian(w179, w181));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w179, w180));
        Assert.assertFalse(LatLonAlt.crossesAntimeridian(w179, w179));
    }

    /**
     * Test that {@link LatLonAlt#interpolate(LatLonAlt, double)} picks the
     * right side of the earth.
     */
    @Test
    public void testInterpolate()
    {
        checkInterpolateLongitude(177., -179, .25, false, 178.);
        checkInterpolateLongitude(177., -179, .25, true, 88.);
        checkInterpolateLongitude(177., -179, .75, false, 180.);
        checkInterpolateLongitude(177., -179, .75, true, -90);
        checkInterpolateLongitude(-179., 177, .25, false, 180.);
        checkInterpolateLongitude(-179., 177, .25, true, -90.);
        checkInterpolateLongitude(-179., 177, .75, false, 178.);
        checkInterpolateLongitude(-179., 177, .75, true, 88.);
        checkInterpolateLongitude(-90., 90., .25, false, -45.);
        checkInterpolateLongitude(-90., 90., .25, true, -135.);
        checkInterpolateLongitude(-90., 90., .75, false, 45.);
        checkInterpolateLongitude(-90., 90., .75, true, 135.);
        checkInterpolateLongitude(-90.001, 90.001, .5, false, 180.);
        checkInterpolateLongitude(-90.001, 90.001, .5, true, 0.);
        checkInterpolateLongitude(-89., 89., .5, false, 0.);
        checkInterpolateLongitude(-89., 89., .5, true, 180.);
    }

    /**
     * Test for {@link LatLonAlt#isValidDecimalLat(String, char...)}.
     */
    @Test
    public void testIsValidDecimalLat()
    {
        Assert.assertFalse(LatLonAlt.isValidDecimalLat(""));

        Assert.assertTrue(LatLonAlt.isValidDecimalLat("0"));

        Assert.assertTrue(LatLonAlt.isValidDecimalLat("90"));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-90"));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat(" 90 "));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat(" -90 "));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("90°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-90°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("90N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-90N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("90S", 'S'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-90S", 'S'));

        // With a decimal point.
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("90."));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-90."));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("90.°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-90.°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("90.N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-90.N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("90.S", 'S'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-90.S", 'S'));

        // With a decimal portion.
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("89.5"));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-89.5"));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("89.5°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-89.5°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("89.5N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-89.5N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("89.5S", 'S'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLat("-89.5S", 'S'));

        // Check out of range.
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("90.1"));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("-90.1"));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("90.1°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("-90.1°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("90.1N", 'N'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("-90.1N", 'N'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("90.1S", 'S'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("-90.1S", 'S'));

        // Check bad characters.
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("A90"));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("-A90"));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("90.°1", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("-90.°1", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("90S", 'N'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("-90S", 'N'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("90N", 'S'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLat("-90N", 'S'));
    }

    /**
     * Test for {@link LatLonAlt#isValidDecimalLon(String, char...)}.
     */
    @Test
    public void testIsValidDecimalLon()
    {
        Assert.assertFalse(LatLonAlt.isValidDecimalLon(""));

        Assert.assertTrue(LatLonAlt.isValidDecimalLon("0"));

        Assert.assertTrue(LatLonAlt.isValidDecimalLon("180"));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-180"));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon(" 180 "));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon(" -180 "));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("180°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-180°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("180N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-180N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("180S", 'S'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-180S", 'S'));

        // With a decimal point.
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("180."));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-180."));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("180.°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-180.°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("180.N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-180.N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("180.S", 'S'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-180.S", 'S'));

        // With a decimal portion.
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("179.5"));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-179.5"));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("179.5°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-179.5°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("179.5N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-179.5N", 'N'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("179.5S", 'S'));
        Assert.assertTrue(LatLonAlt.isValidDecimalLon("-179.5S", 'S'));

        // Check out of range.
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("180.1"));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("-180.1"));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("180.1°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("-180.1°", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("180.1N", 'N'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("-180.1N", 'N'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("180.1S", 'S'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("-180.1S", 'S'));

        // Check bad characters.
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("A180"));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("-A180"));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("180.°1", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("-180.°1", LatLonAlt.DEGREE_SYMBOL));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("180S", 'N'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("-180S", 'N'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("180N", 'S'));
        Assert.assertFalse(LatLonAlt.isValidDecimalLon("-180N", 'S'));
    }

    /**
     * Test for {@link LatLonAlt#latToDMSString(double, int)}.
     */
    @Test
    public void testLatToDMSString()
    {
        String zeroNorth = "0°0'0\"N";
        Assert.assertEquals(zeroNorth, LatLonAlt.latToDMSString(0., 2));
        Assert.assertEquals("0°1'0\"N", LatLonAlt.latToDMSString(1. / 60, 2));
        Assert.assertEquals("0°0'1\"N", LatLonAlt.latToDMSString(1. / 3600, 2));
        Assert.assertEquals("0°0'0.99\"N", LatLonAlt.latToDMSString(.99 / 3600, 2));
        Assert.assertEquals("0°0'0.99\"N", LatLonAlt.latToDMSString(.994 / 3600, 2));
        Assert.assertEquals("0°0'59.99\"N", LatLonAlt.latToDMSString(59.995 / 3600, 2));
        Assert.assertEquals("0°1'0\"N", LatLonAlt.latToDMSString(59.996 / 3600, 2));
        Assert.assertEquals("1°0'0\"N", LatLonAlt.latToDMSString(3599.996 / 3600, 2));

        Assert.assertEquals(zeroNorth, LatLonAlt.latToDMSString(-0., 2));
        Assert.assertEquals("0°1'0\"S", LatLonAlt.latToDMSString(-1. / 60, 2));
        Assert.assertEquals("0°0'1\"S", LatLonAlt.latToDMSString(-1. / 3600, 2));
        Assert.assertEquals("0°0'0.99\"S", LatLonAlt.latToDMSString(-.99 / 3600, 2));
        Assert.assertEquals("0°0'0.99\"S", LatLonAlt.latToDMSString(-.994 / 3600, 2));
        Assert.assertEquals("0°0'59.99\"S", LatLonAlt.latToDMSString(-59.995 / 3600, 2));
        Assert.assertEquals("0°1'0\"S", LatLonAlt.latToDMSString(-59.996 / 3600, 2));
        Assert.assertEquals("1°0'0\"S", LatLonAlt.latToDMSString(-3599.996 / 3600, 2));

        Assert.assertEquals("0°0'0\"N", LatLonAlt.latToDMSString(.1 / 3600, 0));
        Assert.assertEquals("0°0'0.1\"N", LatLonAlt.latToDMSString(.1 / 3600, 1));
        Assert.assertEquals(zeroNorth, LatLonAlt.latToDMSString(.01 / 3600, 1));
        Assert.assertEquals("0°0'0.01\"N", LatLonAlt.latToDMSString(.01 / 3600, 2));
        Assert.assertEquals(zeroNorth, LatLonAlt.latToDMSString(.001 / 3600, 2));
        Assert.assertEquals("0°0'0.001\"N", LatLonAlt.latToDMSString(.001 / 3600, 3));
    }

    /**
     * Test for {@link LatLonAlt#longitudeDifference(double, double)}.
     */
    @Test
    public void testLongitudeDifference()
    {
        Assert.assertEquals(180., LatLonAlt.longitudeDifference(0., 180.), 0.);
        Assert.assertEquals(20., LatLonAlt.longitudeDifference(10., -10.), 0.);
        Assert.assertEquals(20., LatLonAlt.longitudeDifference(-10., 10.), 0.);
        Assert.assertEquals(20., LatLonAlt.longitudeDifference(-370., 370.), 0.);
        Assert.assertEquals(15., LatLonAlt.longitudeDifference(-175., 170.), 0.);
        Assert.assertEquals(0., LatLonAlt.longitudeDifference(-180., 180.), 0.);
        Assert.assertEquals(0., LatLonAlt.longitudeDifference(-190., 170.), 0.);
    }

    /**
     * Test for {@link LatLonAlt#lonToDMSString(double, int)}.
     */
    @Test
    public void testLonToDMSString()
    {
        String zeroEast = "0°0'0\"E";
        Assert.assertEquals(zeroEast, LatLonAlt.lonToDMSString(0., 2));
        Assert.assertEquals("0°1'0\"E", LatLonAlt.lonToDMSString(1. / 60, 2));
        Assert.assertEquals("0°0'1\"E", LatLonAlt.lonToDMSString(1. / 3600, 2));
        Assert.assertEquals("0°0'0.99\"E", LatLonAlt.lonToDMSString(.99 / 3600, 2));
        Assert.assertEquals("0°0'0.99\"E", LatLonAlt.lonToDMSString(.994 / 3600, 2));
        Assert.assertEquals("0°0'59.99\"E", LatLonAlt.lonToDMSString(59.995 / 3600, 2));
        Assert.assertEquals("0°1'0\"E", LatLonAlt.lonToDMSString(59.996 / 3600, 2));
        Assert.assertEquals("1°0'0\"E", LatLonAlt.lonToDMSString(3599.996 / 3600, 2));

        Assert.assertEquals(zeroEast, LatLonAlt.lonToDMSString(-0., 2));
        Assert.assertEquals("0°1'0\"W", LatLonAlt.lonToDMSString(-1. / 60, 2));
        Assert.assertEquals("0°0'1\"W", LatLonAlt.lonToDMSString(-1. / 3600, 2));
        Assert.assertEquals("0°0'0.99\"W", LatLonAlt.lonToDMSString(-.99 / 3600, 2));
        Assert.assertEquals("0°0'0.99\"W", LatLonAlt.lonToDMSString(-.994 / 3600, 2));
        Assert.assertEquals("0°0'59.99\"W", LatLonAlt.lonToDMSString(-59.995 / 3600, 2));
        Assert.assertEquals("0°1'0\"W", LatLonAlt.lonToDMSString(-59.996 / 3600, 2));
        Assert.assertEquals("1°0'0\"W", LatLonAlt.lonToDMSString(-3599.996 / 3600, 2));

        Assert.assertEquals("0°0'0\"E", LatLonAlt.lonToDMSString(.1 / 3600, 0));
        Assert.assertEquals("0°0'0.1\"E", LatLonAlt.lonToDMSString(.1 / 3600, 1));
        Assert.assertEquals(zeroEast, LatLonAlt.lonToDMSString(.01 / 3600, 1));
        Assert.assertEquals("0°0'0.01\"E", LatLonAlt.lonToDMSString(.01 / 3600, 2));
        Assert.assertEquals(zeroEast, LatLonAlt.lonToDMSString(.001 / 3600, 2));
        Assert.assertEquals("0°0'0.001\"E", LatLonAlt.lonToDMSString(.001 / 3600, 3));
    }

    /**
     * Test {@link LatLonAlt#normalizeLatitude(double)} with various strange
     * latitudes and verifying that the result is normalized between -90 and 90
     * (inclusive).
     */
    @Test
    public void testNormalizeLatitude()
    {
        Assert.assertEquals(-90., LatLonAlt.normalizeLatitude(-90.), 0.);
        Assert.assertEquals(-45., LatLonAlt.normalizeLatitude(-45.), 0.);
        Assert.assertEquals(0., LatLonAlt.normalizeLatitude(0.), 0.);
        Assert.assertEquals(45., LatLonAlt.normalizeLatitude(45.), 0.);
        Assert.assertEquals(90., LatLonAlt.normalizeLatitude(90.), 0.);

        Assert.assertEquals(89.999, LatLonAlt.normalizeLatitude(90.001), 0.);
        Assert.assertEquals(-89.999, LatLonAlt.normalizeLatitude(-90.001), 0.);

        Assert.assertEquals(1., LatLonAlt.normalizeLatitude(179.), 0.);
        Assert.assertEquals(-1., LatLonAlt.normalizeLatitude(-179.), 0.);

        Assert.assertEquals(0., LatLonAlt.normalizeLatitude(180.), 0.);
        Assert.assertEquals(0., LatLonAlt.normalizeLatitude(-180.), 0.);

        Assert.assertEquals(-1., LatLonAlt.normalizeLatitude(181.), 0.);
        Assert.assertEquals(1., LatLonAlt.normalizeLatitude(-181.), 0.);

        Assert.assertEquals(-89., LatLonAlt.normalizeLatitude(269.), 0.);
        Assert.assertEquals(89, LatLonAlt.normalizeLatitude(-269.), 0.);

        Assert.assertEquals(-90., LatLonAlt.normalizeLatitude(270.), 0.);
        Assert.assertEquals(90., LatLonAlt.normalizeLatitude(-270.), 0.);

        Assert.assertEquals(-89., LatLonAlt.normalizeLatitude(271.), 0.);
        Assert.assertEquals(89., LatLonAlt.normalizeLatitude(-271.), 0.);

        Assert.assertEquals(0., LatLonAlt.normalizeLatitude(360.), 0.);
        Assert.assertEquals(0, LatLonAlt.normalizeLatitude(-360.), 0.);

        Assert.assertEquals(1., LatLonAlt.normalizeLatitude(361.), 0.);
        Assert.assertEquals(-1, LatLonAlt.normalizeLatitude(-361.), 0.);

        Assert.assertEquals(-1., LatLonAlt.normalizeLatitude(541.), 0.);
        Assert.assertEquals(1., LatLonAlt.normalizeLatitude(-541.), 0.);

        Assert.assertEquals(-89., LatLonAlt.normalizeLatitude(629.), 0.);
        Assert.assertEquals(89, LatLonAlt.normalizeLatitude(-629.), 0.);

        Assert.assertEquals(-90., LatLonAlt.normalizeLatitude(630.), 0.);
        Assert.assertEquals(90, LatLonAlt.normalizeLatitude(-630.), 0.);

        Assert.assertEquals(-89., LatLonAlt.normalizeLatitude(631.), 0.);
        Assert.assertEquals(89, LatLonAlt.normalizeLatitude(-631.), 0.);

        Assert.assertEquals(1., LatLonAlt.normalizeLatitude(721.), 0.);
        Assert.assertEquals(-1., LatLonAlt.normalizeLatitude(-721.), 0.);

        Assert.assertEquals(89., LatLonAlt.normalizeLatitude(811.), 0.);
        Assert.assertEquals(-89, LatLonAlt.normalizeLatitude(-811.), 0.);
    }

    /**
     * Test {@link LatLonAlt#normalizeLongitude(double)} with various strange
     * longitudes and verifying that the result is normalized between -180 and
     * 180 (inclusive).
     */
    @Test
    public void testNormalizeLongitude()
    {
        Assert.assertEquals(-1., LatLonAlt.normalizeLongitude(-361.), 0.);
        Assert.assertEquals(0., LatLonAlt.normalizeLongitude(-360.), 0.);
        Assert.assertEquals(1., LatLonAlt.normalizeLongitude(-359.), 0.);
        Assert.assertEquals(45., LatLonAlt.normalizeLongitude(-315.), 0.);
        Assert.assertEquals(90., LatLonAlt.normalizeLongitude(-270.), 0.);
        Assert.assertEquals(135., LatLonAlt.normalizeLongitude(-225.), 0.);
        Assert.assertEquals(-180., LatLonAlt.normalizeLongitude(-180.), 0.);
        Assert.assertEquals(-135., LatLonAlt.normalizeLongitude(-135.), 0.);
        Assert.assertEquals(-90., LatLonAlt.normalizeLongitude(-90.), 0.);
        Assert.assertEquals(-45., LatLonAlt.normalizeLongitude(-45.), 0.);
        Assert.assertEquals(0., LatLonAlt.normalizeLongitude(0.), 0.);
        Assert.assertEquals(45., LatLonAlt.normalizeLongitude(45.), 0.);
        Assert.assertEquals(90., LatLonAlt.normalizeLongitude(90.), 0.);
        Assert.assertEquals(135., LatLonAlt.normalizeLongitude(135.), 0.);
        Assert.assertEquals(180., LatLonAlt.normalizeLongitude(180.), 0.);
        Assert.assertEquals(-135., LatLonAlt.normalizeLongitude(225.), 0.);
        Assert.assertEquals(-90., LatLonAlt.normalizeLongitude(270.), 0.);
        Assert.assertEquals(-45., LatLonAlt.normalizeLongitude(315.), 0.);
        Assert.assertEquals(-1., LatLonAlt.normalizeLongitude(359.), 0.);
        Assert.assertEquals(0., LatLonAlt.normalizeLongitude(360.), 0.);
        Assert.assertEquals(1., LatLonAlt.normalizeLongitude(361.), 0.);
    }

    /**
     * Test for {@link LatLonAlt#positionsCrossLongitudeBoundary(LatLonAlt)}.
     */
    @Test
    public void testPositionsCrossLongitudeBoundary()
    {
        LatLonAlt original = LatLonAlt.createFromDegrees(0., 175.);

        LatLonAlt lla = LatLonAlt.createFromDegrees(0., 160.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., 180.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., 75.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., 0.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., -5.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));

        lla = LatLonAlt.createFromDegrees(0., -6.);
        Assert.assertEquals(true, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., -175.);
        Assert.assertEquals(true, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., -180.);
        Assert.assertEquals(true, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., -50.);
        Assert.assertEquals(true, original.positionsCrossLongitudeBoundary(lla));

        original = LatLonAlt.createFromDegrees(0., -175.);

        lla = LatLonAlt.createFromDegrees(0., 180.);
        Assert.assertEquals(true, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., 179.);
        Assert.assertEquals(true, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., 160.);
        Assert.assertEquals(true, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., 75.);
        Assert.assertEquals(true, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., 6.);
        Assert.assertEquals(true, original.positionsCrossLongitudeBoundary(lla));

        lla = LatLonAlt.createFromDegrees(0., 5.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., 0.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., 5.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., -6.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., -175.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
        lla = LatLonAlt.createFromDegrees(0., -180.);
        Assert.assertEquals(false, original.positionsCrossLongitudeBoundary(lla));
    }

    /**
     * Helper method that tests interpolating longitude.
     *
     * @param a The first longitude.
     * @param b The second longitude.
     * @param fraction The fraction of the distance to the other point.
     * @param longway Flag indicating if the interpolation should be done the
     *            long way around the globe.
     * @param expected The expected result.
     */
    private void checkInterpolateLongitude(double a, double b, double fraction, boolean longway, double expected)
    {
        LatLonAlt lla1 = LatLonAlt.createFromDegreesMeters(0., a, 0., Altitude.ReferenceLevel.TERRAIN);
        LatLonAlt lla2 = LatLonAlt.createFromDegreesMeters(0., b, 0., Altitude.ReferenceLevel.TERRAIN);
        LatLonAlt result = lla1.interpolate(lla2, fraction, longway);
        Assert.assertEquals(expected, result.getLonD(), 0.);
    }
}
