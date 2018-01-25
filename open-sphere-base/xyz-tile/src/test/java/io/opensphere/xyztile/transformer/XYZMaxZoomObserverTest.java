package io.opensphere.xyztile.transformer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZDataTypeInfo;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Unit test for {@link XYZMaxZoomObserver}.
 */
public class XYZMaxZoomObserverTest
{
    /**
     * Tests the observer.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void test() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        XYZTileLayerInfo layer = new XYZTileLayerInfo("theid", "A Name", Projection.EPSG_4326, 2, true, 4,
                new XYZServerInfo("serverName", "http://somehost"));
        XYZDataTypeInfo xyzType = new XYZDataTypeInfo(null, layer);

        CountDownLatch latch = new CountDownLatch(1);

        LayerActivationListener listener = createListener(support, xyzType, latch);

        support.replayAll();

        XYZMaxZoomObserver observer = new XYZMaxZoomObserver(xyzType, listener);
        layer.setMaxLevelsUser(10);
        latch.await(1, TimeUnit.SECONDS);
        observer.close();
        layer.setMaxLevelsUser(13);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link LayerActivationListener}.
     *
     * @param support Used to create the mock.
     * @param layer The expected layer to be refreshed.
     * @param latch Used to synchronize test.
     * @return The mocked {@link LayerActivationListener}.
     */
    private LayerActivationListener createListener(EasyMockSupport support, XYZDataTypeInfo layer, CountDownLatch latch)
    {
        LayerActivationListener listener = support.createMock(LayerActivationListener.class);

        listener.layerDeactivated(EasyMock.eq(layer));
        listener.layerActivated(EasyMock.eq(layer));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            latch.countDown();
            return null;
        });

        return listener;
    }
}
