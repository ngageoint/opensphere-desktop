package io.opensphere.stkterrain.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ServiceLoader;

import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.terrain.util.ElevationImageReader;

/**
 * Unit test for {@link STKElevationImageReader}.
 */
public class STKElevationImageReaderTest
{
    /**
     * Tests loading a {@link STKElevationImageReader} via {@link ServiceLoader}
     * .
     */
    @Test
    public void testInit()
    {
        ServiceLoader<ElevationImageReader> loader = ServiceLoader.load(ElevationImageReader.class);
        STKElevationImageReader reader = null;
        for (ElevationImageReader aReader : loader)
        {
            if (aReader instanceof STKElevationImageReader)
            {
                reader = (STKElevationImageReader)aReader;
            }
        }

        assertNotNull(reader);

        GeographicBoundingBox box = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0),
                LatLonAlt.createFromDegrees(10, 10));
        reader.init(box, 0, "proj", "IamId");

        assertEquals(box, reader.getBoundingBox());
        assertEquals(-Short.MIN_VALUE, reader.getMissingDataValue(), 0);
        assertEquals("proj", reader.getCRS());
        assertEquals("IamId", reader.getElevationOrderId());
    }
}
