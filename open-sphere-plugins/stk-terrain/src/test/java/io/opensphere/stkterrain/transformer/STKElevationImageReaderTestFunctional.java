package io.opensphere.stkterrain.transformer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.terrain.util.ElevationImageReaderException;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;
import io.opensphere.test.core.testutils.TestUtils;

/**
 * Unit test for {@link STKElevationImageReader}.
 */
public class STKElevationImageReaderTestFunctional
{
    /**
     * Tests the reading elevations.
     *
     * @throws IOException Bad IO.
     * @throws ElevationImageReaderException Bad elevation.
     */
    @Test
    public void test() throws IOException, ElevationImageReaderException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(TestUtils.convertDataPath("/data/sample_data/quantized_mesh/western.mesh")));
        QuantizedMesh mesh = new QuantizedMesh(ByteBuffer.wrap(bytes));

        GeographicBoundingBox bounds = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180),
                LatLonAlt.createFromDegrees(90, 0));
        STKElevationImageReader reader = new STKElevationImageReader(bounds, "proj", "I am order");

        assertEquals(bounds, reader.getBoundingBox());
        assertEquals("proj", reader.getCRS());
        assertEquals("I am order", reader.getElevationOrderId());
        assertEquals(-Short.MIN_VALUE, reader.getMissingDataValue(), 0d);

        assertEquals(1276.74,
                reader.readElevation(new GeographicPosition(LatLonAlt.createFromDegrees(40, -105)), mesh, bounds, true), 0.01d);
        assertEquals(-Short.MIN_VALUE,
                reader.readElevation(new GeographicPosition(LatLonAlt.createFromDegrees(40, -105)), null, bounds, true), 0);
    }
}
