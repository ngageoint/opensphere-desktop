package io.opensphere.controlpanels.component.map.model;

import static org.junit.Assert.assertEquals;

import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;

/**
 * Tests the MapModel class.
 *
 */
public class MapModelTest
{
    /**
     * Tests the MapModel class.
     */
    @Test
    public void testSetHeightWidth()
    {
        EasyMockSupport support = new EasyMockSupport();

        MapModel model = new MapModel();
        Observer observer = createObserver(support, model, MapModel.SIZE_PROP, 3);

        model.addObserver(observer);

        support.replayAll();

        model.setHeightWidth(100, 200);
        assertEquals(100, model.getHeight());
        assertEquals(200, model.getWidth());

        model.setHeightWidth(100, 200);

        model.setHeightWidth(200, 200);
        assertEquals(200, model.getHeight());
        assertEquals(200, model.getWidth());

        model.setHeightWidth(200, 100);
        assertEquals(200, model.getHeight());
        assertEquals(100, model.getWidth());

        support.verifyAll();
    }

    /**
     * Tests the set region method.
     */
    @Test
    public void testSetRegion()
    {
        EasyMockSupport support = new EasyMockSupport();

        GeographicBoundingBox region = new GeographicBoundingBox(LatLonAlt.createFromDegrees(-45d, -90d),
                LatLonAlt.createFromDegrees(0, 0));
        MapModel model = new MapModel();
        Observer observer = createObserver(support, model, MapModel.REGION_PROP, 1);

        model.addObserver(observer);

        support.replayAll();

        model.setRegion(region);
        assertEquals(region, model.getRegion());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked observer.
     *
     * @param support Used to create the mock.
     * @param model The expected model.
     * @param expectedProperty The expected property for the update call.
     * @param times The number of times to expect the update call.
     * @return the observer.
     */
    private Observer createObserver(EasyMockSupport support, MapModel model, String expectedProperty, int times)
    {
        Observer observer = support.createMock(Observer.class);

        observer.update(EasyMock.eq(model), EasyMock.cmpEq(expectedProperty));
        EasyMock.expectLastCall().times(times);

        return observer;
    }
}
