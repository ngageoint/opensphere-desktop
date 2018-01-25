package io.opensphere.core.mgrs;

import org.junit.Test;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import org.junit.Assert;

/** Test class for MGRSConverter. */
public class MGRSConverterTest
{
    /** The delta for floats and doubles. */
    private static final double ourDelta = .00001;

    /**
     * Test the convertToUTMMethod().
     */
    @Test
    public void testConvertToUTM()
    {
        // MGRS strings to convert and test.
        String mgrsString1 = "17SLE0021200316";
        // Should produce results of Lat = 39.729349 Lon = -83.331246

        String mgrsString2 = "59GLK3261970959";
        // Should produce results of Lat = -45.394862 Lon = 168.861543

        String mgrsString3 = "31MBV3072055630";
        // Should produce results of Lat = -.401072 Lon = .580710

        String mgrsString4 = "17SLE0000100001";
        String mgrsString5 = "17SLE1000010000";

        MGRSConverter converter = new MGRSConverter();

        GeographicPosition geoPos1 = converter.convertToLatLon(mgrsString1);
        Assert.assertEquals(39.729349, geoPos1.getLatLonAlt().getLatD(), ourDelta);
        Assert.assertEquals(-83.331246, geoPos1.getLatLonAlt().getLonD(), ourDelta);

        // Convert back to MGRS string from lat/lon to make sure we match.
        UTM utm1 = new UTM(geoPos1);
        String mgrsStr1 = converter.createString(utm1);
        Assert.assertEquals(mgrsString1, mgrsStr1);

        GeographicPosition geoPos2 = converter.convertToLatLon(mgrsString2);
        Assert.assertEquals(-45.394862, geoPos2.getLatLonAlt().getLatD(), ourDelta);
        Assert.assertEquals(168.861543, geoPos2.getLatLonAlt().getLonD(), ourDelta);

        // Convert back to MGRS string from lat/lon to make sure we match.
        UTM utm2 = new UTM(geoPos2);
        String mgrsStr2 = converter.createString(utm2);
        Assert.assertEquals(mgrsString2, mgrsStr2);

        GeographicPosition geoPos3 = converter.convertToLatLon(mgrsString3);
        Assert.assertEquals(-.401072, geoPos3.getLatLonAlt().getLatD(), ourDelta);
        Assert.assertEquals(.580710, geoPos3.getLatLonAlt().getLonD(), ourDelta);

        // Convert back to MGRS string from lat/lon to make sure we match.
        UTM utm3 = new UTM(geoPos3);
        String mgrsStr3 = converter.createString(utm3);
        Assert.assertEquals(mgrsString3, mgrsStr3);

        GeographicPosition geoPos4 = converter.convertToLatLon(mgrsString4);

        // Convert back to MGRS string from lat/lon to make sure we match.
        UTM utm4 = new UTM(geoPos4);
        String mgrsStr4 = converter.createString(utm4);
        Assert.assertEquals(mgrsString4, mgrsStr4);

        GeographicPosition geoPos5 = converter.convertToLatLon(mgrsString5);

        // Convert back to MGRS string from lat/lon to make sure we match.
        UTM utm5 = new UTM(geoPos5);
        String mgrsStr5 = converter.createString(utm5);
        Assert.assertEquals(mgrsString5, mgrsStr5);

        // Start shortening the easting / northing values
        String mgrsString = "17SLE00210031";
        // Should produce results of Lat = ~39.729349 Lon = -83.331246

        GeographicPosition geoPos = converter.convertToLatLon(mgrsString);
        Assert.assertEquals(39.729349, geoPos.getLatLonAlt().getLatD(), .0001);
        Assert.assertEquals(-83.331246, geoPos.getLatLonAlt().getLonD(), .0001);

        mgrsString = "17SLE002003";
        geoPos = converter.convertToLatLon(mgrsString);
        Assert.assertEquals(39.729349, geoPos.getLatLonAlt().getLatD(), .001);
        Assert.assertEquals(-83.331246, geoPos.getLatLonAlt().getLonD(), .001);

        mgrsString = "17SLE0000";
        geoPos = converter.convertToLatLon(mgrsString);
        Assert.assertEquals(39.729349, geoPos.getLatLonAlt().getLatD(), .01);
        Assert.assertEquals(-83.331246, geoPos.getLatLonAlt().getLonD(), .01);

        mgrsString = "17SLE00";
        geoPos = converter.convertToLatLon(mgrsString);
        Assert.assertEquals(39.729349, geoPos.getLatLonAlt().getLatD(), .1);
        Assert.assertEquals(-83.331246, geoPos.getLatLonAlt().getLonD(), .1);

        mgrsString = "42S XD 90929 66948";
        geoPos = converter.convertToLatLon(mgrsString);
        Assert.assertEquals(34.927051865184275, geoPos.getLatLonAlt().getLatD(), .1);
        Assert.assertEquals(71.09032003859397, geoPos.getLatLonAlt().getLonD(), .1);
    }

    /**
     * Test the convertToUTMMethod().
     */
    @Test
    public void testLatLonConversions()
    {
        UTM testUTM = new UTM(new GeographicPosition(LatLonAlt.createFromDegrees(38, -105)));

///        UTM testUTM = new UTM();
///        testUTM.createFromLatLon(38, -105);

        MGRSConverter converter = new MGRSConverter();

        String mgrsString = converter.createString(testUTM);

        GeographicPosition geoPos = converter.convertToLatLon(mgrsString);

        Assert.assertEquals(38, geoPos.getLatLonAlt().getLatD(), ourDelta);
        Assert.assertEquals(-105, geoPos.getLatLonAlt().getLonD(), ourDelta);
    }
}
