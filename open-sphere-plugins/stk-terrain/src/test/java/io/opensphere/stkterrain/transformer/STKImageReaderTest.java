package io.opensphere.stkterrain.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ServiceLoader;

import org.junit.Test;

import io.opensphere.core.image.ImageReader;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;
import io.opensphere.stkterrain.model.mesh.QuantizedMeshTest;

/**
 * Unit test for {@link STKImageReader}.
 */
public class STKImageReaderTest
{
    /**
     * Tests that it returns the correct image format.
     */
    @Test
    public void testGetImageFormat()
    {
        STKImageReader reader = new STKImageReader();
        STKElevationImageReader elevationReader = new STKElevationImageReader(
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0), LatLonAlt.createFromDegrees(10, 10)), "EPSG:4283",
                "order");
        assertEquals(elevationReader.getImageFormat(), reader.getImageFormat());
    }

    /**
     * Tests reading a quantized mesh.
     */
    @Test
    public void testReadImage()
    {
        STKImageReader reader = new STKImageReader();
        QuantizedMesh mesh = (QuantizedMesh)reader.readImage(ByteBuffer.wrap(QuantizedMeshTest.createMeshByes()));
        assertEquals(5, mesh.getHeader().getMaxHeight(), 0);
    }

    /**
     * Tests this class can be loaded from a service loader.
     */
    @Test
    public void testServiceLoader()
    {
        ServiceLoader<ImageReader> loader = ServiceLoader.load(ImageReader.class);
        boolean hasSTKImageReader = false;
        for (ImageReader reader : loader)
        {
            if (reader instanceof STKImageReader)
            {
                hasSTKImageReader = true;
                break;
            }
        }

        assertTrue(hasSTKImageReader);
    }
}
