package io.opensphere.core.geometry;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;

/**
 * Unit test for the {@link EllipsoidTransformProvider}.
 */
public class EllipsoidTransformProviderTest
{
    /**
     * Tests the transform provider.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        MapManager mapManager = EllipsoidTestUtils.createMapManager(support);

        support.replayAll();

        LatLonAlt location = LatLonAlt.createFromDegreesMeters(10, 11, 5280, ReferenceLevel.TERRAIN);
        EllipsoidTransformProvider provider = new EllipsoidTransformProvider(mapManager);
        Matrix4d transform = provider.provideTransform(location, 14, 15, 16);
        Matrix4d expected = new Matrix4d(new double[] { -0.5187182308412952, 0.8122809550672292, -.26670404389125235, 0.0,
            -0.8501440620567355, -0.45705076232245867, 0.2614568308729626, 0.0, 0.0904791177153618, 0.36235928399246203,
            0.9276363935087497, 0.0, 11.0, 10.0, 5280.0, 1.0 });

        assertEquals(expected, transform);

        support.verifyAll();
    }
}
