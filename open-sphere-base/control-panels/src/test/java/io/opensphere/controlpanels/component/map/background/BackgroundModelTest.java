package io.opensphere.controlpanels.component.map.background;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;

/**
 * Tests the BackgroundModel.
 */
public class BackgroundModelTest
{
    /**
     * Tests the add function.
     */
    @Test
    public void testAdd()
    {
        EasyMockSupport support = new EasyMockSupport();

        BackgroundModel model = new BackgroundModel();
        Observer observer = createObserver(support, model);

        support.replayAll();

        model.addObserver(observer);

        TileGeometry[] geometries = createGeometries();

        model.add(New.list(geometries));

        assertEquals(2, model.getGeometries().size());

        assertTrue(model.getGeometries().contains(geometries[0]));
        assertTrue(model.getGeometries().contains(geometries[1]));

        support.verifyAll();
    }

    /**
     * Tests the remove function.
     */
    @Test
    public void testRemove()
    {
        EasyMockSupport support = new EasyMockSupport();

        BackgroundModel model = new BackgroundModel();
        Observer observer = createObserver(support, model);
        TileGeometry[] geometries = createGeometries();

        model.add(New.list(geometries));

        support.replayAll();

        model.addObserver(observer);
        model.remove(New.list(geometries[0]));

        assertEquals(1, model.getGeometries().size());
        assertEquals(geometries[1], model.getGeometries().iterator().next());

        support.verifyAll();
    }

    /**
     * Creates some test geometries.
     *
     * @return The test geometry.
     */
    private TileGeometry[] createGeometries()
    {
        TileGeometry.Builder<ScreenPosition> tileBuilder = new TileGeometry.Builder<ScreenPosition>();
        tileBuilder.setDivider(null);
        tileBuilder.setParent(null);

        tileBuilder.setImageManager(new ImageManager(null, new SingletonImageProvider(
                "/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg", Image.CompressionType.D3DFMT_DXT1)));

        final float opacityValue = .9f;
        tileBuilder.setBounds(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(300, 150)));
        TileRenderProperties props = new DefaultTileRenderProperties(1, true, true);
        props.setHighlightColorARGB(0);
        props.setOpacity(opacityValue);
        TileGeometry worldMapTile1 = new TileGeometry(tileBuilder, props, null);
        TileGeometry worldMapTile2 = new TileGeometry(tileBuilder, props, null);

        return new TileGeometry[] { worldMapTile1, worldMapTile2 };
    }

    /**
     * Creates an easy mocked observer.
     *
     * @param support Used to create the mock.
     * @param model The expected model.
     * @return The observer.
     */
    private Observer createObserver(EasyMockSupport support, BackgroundModel model)
    {
        Observer observer = support.createMock(Observer.class);

        observer.update(EasyMock.eq(model), EasyMock.cmpEq(BackgroundModel.GEOMETRIES_PROP));

        return observer;
    }
}
