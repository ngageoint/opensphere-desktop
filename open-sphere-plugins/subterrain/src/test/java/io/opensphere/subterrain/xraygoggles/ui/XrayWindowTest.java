package io.opensphere.subterrain.xraygoggles.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Tests the {@link XrayWindow} class.
 */
public class XrayWindowTest
{
    /**
     * The published geometries.
     */
    private final List<Geometry> myGeometries = New.list();

    /**
     * Tests showing the xray window.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataRegistry registry = support.createMock(DataRegistry.class);
        GenericSubscriber<Geometry> receiver = createSubscriber(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        XrayWindow window = new XrayWindow(registry, model);

        model.setScreenPosition(new ScreenPosition(10, 11), new ScreenPosition(11, 11), new ScreenPosition(10, 10),
                new ScreenPosition(11, 10));

        window.addSubscriber(receiver);
        window.open();

        assertEquals(1, myGeometries.size());
        assertEquals(model.getWindowGeometry(), myGeometries.iterator().next());

        PolylineGeometry geometry = (PolylineGeometry)myGeometries.get(0);

        assertEquals(5, geometry.getVertices().size());
        assertEquals(model.getUpperLeft().asVector2d(), geometry.getVertices().get(0).asVector2d());
        assertEquals(model.getUpperRight().asVector2d(), geometry.getVertices().get(1).asVector2d());
        assertEquals(model.getLowerRight().asVector2d(), geometry.getVertices().get(2).asVector2d());
        assertEquals(model.getLowerLeft().asVector2d(), geometry.getVertices().get(3).asVector2d());
        assertEquals(model.getUpperLeft().asVector2d(), geometry.getVertices().get(4).asVector2d());

        model.setScreenPosition(new ScreenPosition(20, 21), new ScreenPosition(21, 21), new ScreenPosition(20, 20),
                new ScreenPosition(21, 20));

        assertEquals(1, myGeometries.size());
        assertEquals(model.getWindowGeometry(), myGeometries.iterator().next());

        geometry = (PolylineGeometry)myGeometries.get(0);

        assertEquals(5, geometry.getVertices().size());
        assertEquals(model.getUpperLeft().asVector2d(), geometry.getVertices().get(0).asVector2d());
        assertEquals(model.getUpperRight().asVector2d(), geometry.getVertices().get(1).asVector2d());
        assertEquals(model.getLowerRight().asVector2d(), geometry.getVertices().get(2).asVector2d());
        assertEquals(model.getLowerLeft().asVector2d(), geometry.getVertices().get(3).asVector2d());
        assertEquals(model.getUpperLeft().asVector2d(), geometry.getVertices().get(4).asVector2d());

        model.setScreenPosition(null, null, null, null);

        assertTrue(myGeometries.isEmpty());
        assertNull(model.getWindowGeometry());

        support.verifyAll();
    }

    /**
     * Creates the subscriber.
     *
     * @param support Used to create the mock.
     * @return The mocked subscriber.
     */
    @SuppressWarnings("unchecked")
    private GenericSubscriber<Geometry> createSubscriber(EasyMockSupport support)
    {
        GenericSubscriber<Geometry> subscriber = support.createMock(GenericSubscriber.class);

        subscriber.receiveObjects(EasyMock.isA(Object.class), EasyMock.isA(Collection.class), EasyMock.isA(Collection.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myGeometries.addAll((Collection<? extends Geometry>)EasyMock.getCurrentArguments()[1]);
            myGeometries.removeAll((Collection<?>)EasyMock.getCurrentArguments()[2]);
            return null;
        }).atLeastOnce();

        return subscriber;
    }
}
