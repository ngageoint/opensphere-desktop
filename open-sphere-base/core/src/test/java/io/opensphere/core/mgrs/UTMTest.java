package io.opensphere.core.mgrs;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.model.LatLonAltParser;

/** Test class. */
public class UTMTest
{
    /** The tolerance for conversions to UTM and back. */
    private static final double ourPreservationTolerance = .00001;

    /** The tolerance for conversions compared to truth data. */
    private static final double ourTruthTolerance = .001;

    /**
     * The values for these are taken from the conversion tool on NOAA's web
     * page.
     */
    @Test
    public void testTruthGeoToUtm()
    {
        double lon = LatLonAltParser.parseLon("123°17'12.23\"E", CoordFormat.DMS);
        double lat = LatLonAltParser.parseLat("42°16'8.32\"N", CoordFormat.DMS);
        UTM utm = new UTM(lat, lon);
        Assert.assertEquals(51, utm.getZone());
        Assert.assertEquals("NORTH", utm.getHemisphere().toString());
        Assert.assertEquals(4679681.018, utm.getNorthing(), ourTruthTolerance);
        Assert.assertEquals(523646.086, utm.getEasting(), ourTruthTolerance);

        lon = LatLonAltParser.parseLon("10°5'33.46\"W", CoordFormat.DMS);
        lat = LatLonAltParser.parseLat("60°12'31.457\"N", CoordFormat.DMS);
        utm = new UTM(lat, lon);
        Assert.assertEquals(29, utm.getZone());
        Assert.assertEquals("NORTH", utm.getHemisphere().toString());
        Assert.assertEquals(6675159.373, utm.getNorthing(), ourTruthTolerance);
        Assert.assertEquals(439441.942, utm.getEasting(), ourTruthTolerance);

        lon = LatLonAltParser.parseLon("45°12'22.37842\"W", CoordFormat.DMS);
        lat = LatLonAltParser.parseLat("13°19'47\"N", CoordFormat.DMS);
        utm = new UTM(lat, lon);
        Assert.assertEquals(23, utm.getZone());
        Assert.assertEquals("NORTH", utm.getHemisphere().toString());
        Assert.assertEquals(1473608.192, utm.getNorthing(), ourTruthTolerance);
        Assert.assertEquals(477667.478, utm.getEasting(), ourTruthTolerance);
    }

    /**
     * Test utm lat/lon conversions.
     */
    @Test
    public void testUTM()
    {
        UTM testUTM = new UTM(new GeographicPosition(LatLonAlt.createFromDegrees(25, 35)));
        GeographicPosition result = testUTM.convertToLatLon();
        Assert.assertEquals(25, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(35, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        UTM testUTM2 = new UTM(testUTM.getZone(), testUTM.getHemisphere(), testUTM.getEasting(), testUTM.getNorthing());
        GeographicPosition geoPos = testUTM2.convertToLatLon();
        Assert.assertEquals(25, geoPos.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(35, geoPos.getLatLonAlt().getLonD(), ourPreservationTolerance);
    }

    /**
     * Test utm lat/lon conversions.
     */
    @Test
    public void testUTMLatLonConversions()
    {
        UTM testUTM = new UTM(0, 0);
        GeographicPosition result = testUTM.convertToLatLon();
        Assert.assertEquals(0, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(0, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(84, 5);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(84, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(5, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(-80.5, 5);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(-80.5, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(5, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(-95, 185);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(-85, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(-175, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(95, -190);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(85, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(170, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(45, 0);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(45, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(0, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(89.999, 0);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(89.999, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(0, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(-89.999, 0);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(-89.999, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(0, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        // Longitude values are converted to be in the range -180 to 179.999...
        // so 180 should be converted to -180.
        testUTM = new UTM(0, 180);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(0, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(-180, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(-45.678, 179.999);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(-45.678, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(179.999, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(45.678, -179.999);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(45.678, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(-179.999, result.getLatLonAlt().getLonD(), ourPreservationTolerance);

        testUTM = new UTM(.0001, -180);
        result = testUTM.convertToLatLon();
        Assert.assertEquals(.0001, result.getLatLonAlt().getLatD(), ourPreservationTolerance);
        Assert.assertEquals(-180, result.getLatLonAlt().getLonD(), ourPreservationTolerance);
    }
}
