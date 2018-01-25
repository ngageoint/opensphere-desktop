package io.opensphere.arcgis2.envoy.tile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;

/**
 * Unit test for {@link XYZUrlBuilder}.
 */
public class XYZUrlBuilderTest
{
    /**
     * Tests building the url.
     */
    @Test
    public void testBuildUrl()
    {
        ZYXImageKey key = new ZYXImageKey(0, 1, 2,
                new GeographicBoundingBox(LatLonAlt.createFromDegrees(0, 1), LatLonAlt.createFromDegrees(2, 3)));
        DataModelCategory category = new DataModelCategory(null, null, "http://somehost/layer1/MapServer/5");

        XYZUrlBuilder builder = new XYZUrlBuilder();
        String urlString = builder.buildUrl(category, key);

        assertEquals("http://somehost/layer1/MapServer/tile/0/1/2", urlString);
    }
}
