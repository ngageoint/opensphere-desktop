package io.opensphere.stkterrain.model.mesh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.test.core.testutils.TestUtils;

/** Unit test for {@link GeographicQuantizedMeshReader}. */
public class GeographicQuantizedMeshReaderTestFunctional
{
    /**
     * Tests
     * {@link GeographicQuantizedMeshReader#getElevationM(GeographicPosition, QuantizedMesh)}
     * .
     *
     * @throws IOException if bad stuff happens
     */
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    @Test
    public void testGetElevationM() throws IOException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(TestUtils.convertDataPath("/data/sample_data/quantized_mesh/western.mesh")));
        QuantizedMesh mesh = new QuantizedMesh(ByteBuffer.wrap(bytes));
        GeographicQuantizedMeshReader geoMesh = new GeographicQuantizedMeshReader(
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(90, 0)));

        Assert.assertEquals(1276.74, geoMesh.getElevationM(new GeographicPosition(LatLonAlt.createFromDegrees(40, -105)), mesh),
                .01);
    }
}
