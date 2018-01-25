package io.opensphere.osh.aerialimagery.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.junit.Test;

import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/**
 * Unit test for {@link PlatformMetadata}.
 */
public class PlatformMetadataTest
{
    /**
     * Tests the {@link PlatformMetadata}.
     */
    @Test
    public void test()
    {
        PlatformMetadata metadata = new PlatformMetadata();

        metadata.setCameraPitchAngle(10.5);
        metadata.setCameraRollAngle(11.8);
        metadata.setCameraYawAngle(43.2);
        GeographicConvexQuadrilateral footprint = new GeographicConvexQuadrilateral(
                new GeographicPosition(LatLonAlt.createFromDegrees(-10, -10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, -10)));
        metadata.setFootprint(footprint);
        LatLonAlt location = LatLonAlt.createFromDegreesMeters(5, 5, Altitude.createFromMeters(101, ReferenceLevel.ELLIPSOID));
        metadata.setLocation(location);
        metadata.setPitchAngle(-17.3);
        metadata.setRollAngle(-43);
        Date time = new Date(System.currentTimeMillis());
        metadata.setTime(time);
        metadata.setYawAngle(90.3);

        assertEquals(10.5, metadata.getCameraPitchAngle(), 0d);
        assertEquals(11.8, metadata.getCameraRollAngle(), 0d);
        assertEquals(43.2, metadata.getCameraYawAngle(), 0d);
        assertEquals(-17.3, metadata.getPitchAngle(), 0d);
        assertEquals(-43d, metadata.getRollAngle(), 0d);
        assertEquals(90.3, metadata.getYawAngle(), 0d);
        assertEquals(time, metadata.getTime());
        assertEquals(footprint, metadata.getFootprint());
        assertEquals(location, metadata.getLocation());
    }

    /**
     * Tests the {@link PlatformMetadata}.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        PlatformMetadata metadata = new PlatformMetadata();

        metadata.setCameraPitchAngle(10.5);
        metadata.setCameraRollAngle(11.8);
        metadata.setCameraYawAngle(43.2);
        GeographicConvexQuadrilateral footprint = new GeographicConvexQuadrilateral(
                new GeographicPosition(LatLonAlt.createFromDegrees(-10, -10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(-10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, -10)));
        metadata.setFootprint(footprint);
        LatLonAlt location = LatLonAlt.createFromDegreesMeters(5, 5, Altitude.createFromMeters(101, ReferenceLevel.ELLIPSOID));
        metadata.setLocation(location);
        metadata.setPitchAngle(-17.3);
        metadata.setRollAngle(-43);
        Date time = new Date(System.currentTimeMillis());
        metadata.setTime(time);
        metadata.setYawAngle(90.3);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOutStream = new ObjectOutputStream(out);
        objOutStream.writeObject(metadata);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objInStream = new ObjectInputStream(in);

        metadata = (PlatformMetadata)objInStream.readObject();

        assertEquals(10.5, metadata.getCameraPitchAngle(), 0d);
        assertEquals(11.8, metadata.getCameraRollAngle(), 0d);
        assertEquals(43.2, metadata.getCameraYawAngle(), 0d);
        assertEquals(-17.3, metadata.getPitchAngle(), 0d);
        assertEquals(-43d, metadata.getRollAngle(), 0d);
        assertEquals(90.3, metadata.getYawAngle(), 0d);
        assertEquals(time, metadata.getTime());
        assertEquals(footprint.getVertices(), metadata.getFootprint().getVertices());
        assertEquals(location, metadata.getLocation());
    }
}
