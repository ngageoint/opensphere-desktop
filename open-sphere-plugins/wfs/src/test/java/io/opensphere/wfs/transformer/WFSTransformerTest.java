package io.opensphere.wfs.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.common.geospatial.model.DataPoint;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistryImpl;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Test for {@link WFSTransformer}.
 */
public class WFSTransformerTest
{
    /**
     * Test that when the transformer receives {@link DataPoint}s, it will
     * publish matching {@link PointGeometry}s.
     */
    @Test
    public void testTransform()
    {
        final double lat1 = 24.4f;
        final double lon1 = 10.1f;
        final double lat2 = 25.2f;
        final double lon2 = 11.0f;
        GeographicPosition[] positions = { new GeographicPosition(LatLonAlt.createFromDegrees(lat1, lon1)),
            new GeographicPosition(LatLonAlt.createFromDegrees(lat2, lon2)), };

        Color[] colors = { Color.BLUE, Color.green, };

        WFSTransformer transformer = new WFSTransformer(getTestToolbox());

        Collection<Geometry> geometries = new ArrayList<>();
        int zOrder = 0;
        transformer.transform(new long[] { 1L, 2L }, TimeSpan.ZERO, positions, colors, zOrder, geometries);

        assertEquals(2, geometries.size());

        boolean found1 = false;
        boolean found2 = false;
        for (Geometry geom : geometries)
        {
            PointGeometry ptGeom = (PointGeometry)geom;
            LatLonAlt lla = ((GeographicPosition)ptGeom.getPosition()).getLatLonAlt();
            if (lla.getLatD() == lat1 && lla.getLonD() == lon1 && ptGeom.getRenderProperties().getColor().equals(Color.BLUE))
            {
                found1 = true;
            }
            else if (lla.getLatD() == lat2 && lla.getLonD() == lon2
                    && ptGeom.getRenderProperties().getColor().equals(Color.GREEN))
            {
                found2 = true;
            }
        }

        assertTrue(found1 && found2);
    }

    /**
     * Gets a core toolbox for test purposes.
     *
     * @return the test toolbox
     */
    private Toolbox getTestToolbox()
    {
        DataRegistry dataRegistry = EasyMock.createMock(DataRegistry.class);
        GeometryRegistryImpl geomRegistry = new GeometryRegistryImpl(null);
        EventManager eventMgr = EasyMock.createMock(EventManager.class);
        TimeManager timeMgr = EasyMock.createMock(TimeManager.class);

        Toolbox toolbox = EasyMock.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry).anyTimes();
        EasyMock.expect(toolbox.getGeometryRegistry()).andReturn(geomRegistry).anyTimes();
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventMgr).anyTimes();
        EasyMock.expect(toolbox.getTimeManager()).andReturn(timeMgr).anyTimes();
        EasyMock.replay(toolbox);
        return toolbox;
    }
}
