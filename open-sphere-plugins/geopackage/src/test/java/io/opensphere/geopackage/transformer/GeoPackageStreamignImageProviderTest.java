package io.opensphere.geopackage.transformer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;

/**
 * Unit test for {@link GeoPackageStreamingImageProvider}.
 */
public class GeoPackageStreamignImageProviderTest
{
    /**
     * Tests getting the image stream.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testGetImageStream() throws IOException
    {
        GeoPackageStreamingImageProvider provider = new GeoPackageStreamingImageProvider(
                ByteBuffer.wrap(new byte[] { 0, 1, 2, 3 }));
        InputStream stream = provider.getImageStream(new ZYXImageKey(0, 0, 0,
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10), LatLonAlt.createFromDegrees(11, 11))));

        assertEquals(4, stream.available());
        byte[] actual = new byte[4];
        stream.read(actual);
        assertArrayEquals(new byte[] { 0, 1, 2, 3 }, actual);

        InputStream anotherStream = provider.getImageStream(new ZYXImageKey(0, 0, 0,
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10), LatLonAlt.createFromDegrees(11, 11))));

        assertNotSame(stream, anotherStream);
    }
}
