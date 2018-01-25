package io.opensphere.osh.aerialimagery.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Date;

import org.junit.Test;

import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/**
 * Unit test {@link PlatformMetadataAndImage} class.
 */
public class PlatformMetadataAndImageTest
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

        ByteBuffer imageBytes = ByteBuffer.wrap(new byte[] { 1, 2, 3 });
        PlatformMetadataAndImage metadataAndImage = new PlatformMetadataAndImage(metadata, imageBytes);

        assertEquals(10.5, metadataAndImage.getCameraPitchAngle(), 0d);
        assertEquals(11.8, metadataAndImage.getCameraRollAngle(), 0d);
        assertEquals(43.2, metadataAndImage.getCameraYawAngle(), 0d);
        assertEquals(-17.3, metadataAndImage.getPitchAngle(), 0d);
        assertEquals(-43d, metadataAndImage.getRollAngle(), 0d);
        assertEquals(90.3, metadataAndImage.getYawAngle(), 0d);
        assertEquals(time, metadataAndImage.getTime());
        assertEquals(footprint, metadataAndImage.getFootprint());
        assertEquals(location, metadataAndImage.getLocation());
        assertArrayEquals(new byte[] { 1, 2, 3 }, metadataAndImage.getImageBytes().array());
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

        ByteBuffer imageBytes = ByteBuffer.wrap(new byte[] { 1, 2, 3 });
        PlatformMetadataAndImage metadataAndImage = new PlatformMetadataAndImage(metadata, imageBytes);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOutStream = new ObjectOutputStream(out);
        objOutStream.writeObject(metadataAndImage);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objInStream = new ObjectInputStream(in);

        metadataAndImage = (PlatformMetadataAndImage)objInStream.readObject();

        assertEquals(10.5, metadataAndImage.getCameraPitchAngle(), 0d);
        assertEquals(11.8, metadataAndImage.getCameraRollAngle(), 0d);
        assertEquals(43.2, metadataAndImage.getCameraYawAngle(), 0d);
        assertEquals(-17.3, metadataAndImage.getPitchAngle(), 0d);
        assertEquals(-43d, metadataAndImage.getRollAngle(), 0d);
        assertEquals(90.3, metadataAndImage.getYawAngle(), 0d);
        assertEquals(time, metadataAndImage.getTime());
        assertEquals(footprint.getVertices(), metadataAndImage.getFootprint().getVertices());
        assertEquals(location, metadataAndImage.getLocation());
        assertNull(metadataAndImage.getImageBytes());
    }
}
