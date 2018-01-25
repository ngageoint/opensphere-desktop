package io.opensphere.geopackage.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.lang.Pair;

/**
 * Unit test for {@link TileKeyGenerator}.
 */
public class TileKeyGeneratorTest
{
    /**
     * Tests generating a key and parsing that key.
     */
    @Test
    public void test()
    {
        long zoomLevel = 12;
        GeographicBoundingBox box = new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 0),
                LatLonAlt.createFromDegrees(10, 10));

        String tileKey = TileKeyGenerator.getInstance().generateTileKey(zoomLevel, box);
        Pair<Long, GeographicBoundingBox> tile = TileKeyGenerator.getInstance().parseKeyString(tileKey);

        assertEquals(zoomLevel, tile.getFirstObject().longValue());
        assertEquals(box, tile.getSecondObject());
    }
}
