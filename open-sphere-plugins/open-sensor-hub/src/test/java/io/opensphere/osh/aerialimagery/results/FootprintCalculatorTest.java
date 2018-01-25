package io.opensphere.osh.aerialimagery.results;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Unit test for the {@link FootprintCalculator}.
 */
public class FootprintCalculatorTest
{
    /**
     * A small number.
     */
    public static final float SMALL_NUM = 0.00000001f;

    /**
     * Tests calculating the footprint.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        LatLonAlt cameraPos = LatLonAlt.createFromDegreesMeters(10, 10, 10, ReferenceLevel.ELLIPSOID);

        support.replayAll();

        PlatformMetadata metadata = new PlatformMetadata();
        metadata.setLocation(cameraPos);
        metadata.setCameraPitchAngle(-45);
        metadata.setCameraYawAngle(45);

        FootprintCalculator calculator = new FootprintCalculator();
        GeographicConvexQuadrilateral footprint = calculator.calculateFootprint2(metadata, 20, 20);

        LatLonAlt expectedBoresightCenter = LatLonAlt.createFromDegrees(10.000067597085383, 10.000067597085385);

        assertEquals(expectedBoresightCenter, footprint.getCenter().getLatLonAlt());

        LatLonAlt expectedTopRight = LatLonAlt.createFromDegrees(10.000030593454609, 10.000058361586499);
        LatLonAlt expectedBottomRight = LatLonAlt.createFromDegrees(10.00007088814512, 10.000110545155312);
        LatLonAlt expectedTopLeft = LatLonAlt.createFromDegrees(10.000058361586499, 10.000030593454609);
        LatLonAlt expectedBottomLeft = LatLonAlt.createFromDegrees(10.000110545155312, 10.00007088814512);

        assertEquals(4, footprint.getVertices().size());

        assertEquals(expectedBottomLeft, footprint.getVertices().get(0).getLatLonAlt());
        assertEquals(expectedBottomRight, footprint.getVertices().get(1).getLatLonAlt());
        assertEquals(expectedTopRight, footprint.getVertices().get(2).getLatLonAlt());
        assertEquals(expectedTopLeft, footprint.getVertices().get(3).getLatLonAlt());

        support.verifyAll();
    }
}
