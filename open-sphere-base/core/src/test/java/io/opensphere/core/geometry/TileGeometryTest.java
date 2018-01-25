package io.opensphere.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.geometry.TileGeometry.Builder;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.ProxyTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangeListener;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangedEvent;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.GeographicQuadrilateral;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link TileGeometry}.
 */
public class TileGeometryTest
{
    /**
     * Tests the individual render properties.
     */
    @Test
    public void testIndividualRenderProperties()
    {
        EasyMockSupport support = new EasyMockSupport();

        RenderPropertyChangeListener listener = createListener(support);

        support.replayAll();

        Builder<GeographicPosition> builder = new Builder<>();
        builder.setBounds(new GeographicQuadrilateral(New.list(new GeographicPosition(LatLonAlt.createFromDegrees(10, 10)),
                new GeographicPosition(LatLonAlt.createFromDegrees(10, 11)),
                new GeographicPosition(LatLonAlt.createFromDegrees(11, 11)),
                new GeographicPosition(LatLonAlt.createFromDegrees(11, 10)))));
        builder.setImageManager(new ImageManager("image", null));
        DefaultTileRenderProperties props = new DefaultTileRenderProperties(10, true, true);
        props.addListener(listener);
        TileGeometry geometry = new TileGeometry(builder, props, null);

        assertEquals(props, geometry.getRenderProperties());
        geometry.getRenderPropertiesIndividual().setOpacity(.5f);

        TileRenderProperties individual = geometry.getRenderProperties();
        assertTrue(individual instanceof ProxyTileRenderProperties);
        assertEquals(127f, individual.getOpacity(), 0f);
        assertEquals(255f, props.getOpacity(), 0f);

        geometry.clearIndividualRenderProperties();
        assertEquals(255f, individual.getOpacity(), 0f);
        assertEquals(props, geometry.getRenderProperties());

        geometry.clearIndividualRenderProperties();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link RenderPropertyChangeListener}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link RenderPropertyChangeListener}.
     */
    private RenderPropertyChangeListener createListener(EasyMockSupport support)
    {
        RenderPropertyChangeListener listener = support.createMock(RenderPropertyChangeListener.class);

        listener.propertyChanged(EasyMock.isA(RenderPropertyChangedEvent.class));
        EasyMock.expectLastCall().atLeastOnce();

        return listener;
    }
}
