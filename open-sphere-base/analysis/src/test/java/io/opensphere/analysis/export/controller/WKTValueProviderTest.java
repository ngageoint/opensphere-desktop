package io.opensphere.analysis.export.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;

/**
 * Unit test for the {@link WKTValueProvider} class.
 */
public class WKTValueProviderTest
{
    /**
     * The test feature id.
     */
    private static final long ourFeatureId = 10011L;

    /**
     * Tests getting wkt value for a given column.
     */
    @Test
    public void testGetWKTValue()
    {
        EasyMockSupport support = new EasyMockSupport();

        DefaultMapPointGeometrySupport point = new DefaultMapPointGeometrySupport(LatLonAlt.createFromDegrees(10.1, 11.1));
        DefaultMapDataElement element = new DefaultMapDataElement(ourFeatureId, point);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();
        model.setAddWkt(true);
        WKTValueProvider provider = new WKTValueProvider(model);
        String wktPoint = provider.getWKTValue(element);

        assertEquals("POINT (11.1 10.1)", wktPoint);

        support.verifyAll();
    }

    /**
     * Verifies null is returned when user does not want to add a wkt column.
     */
    @Test
    public void testNoAddWkt()
    {
        EasyMockSupport support = new EasyMockSupport();

        DefaultMapPointGeometrySupport point = new DefaultMapPointGeometrySupport(LatLonAlt.createFromDegrees(10.1, 11.1));
        DefaultMapDataElement element = new DefaultMapDataElement(ourFeatureId, point);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();
        WKTValueProvider provider = new WKTValueProvider(model);
        String wktPoint = provider.getWKTValue(element);

        assertNull(wktPoint);

        support.verifyAll();
    }

    /**
     * Verifies null is returned when map geometry support isn't found.
     */
    @Test
    public void testNoGeometry()
    {
        EasyMockSupport support = new EasyMockSupport();

        DefaultDataElement element = new DefaultDataElement(ourFeatureId);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();
        model.setAddWkt(true);
        WKTValueProvider provider = new WKTValueProvider(model);
        String wktPoint = provider.getWKTValue(element);

        assertNull(wktPoint);

        support.verifyAll();
    }
}
