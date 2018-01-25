package io.opensphere.controlpanels.component.map.boundingbox;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.util.collections.New;

/**
 * Tests the BoundingBoxModel class.
 */
public class BoundingBoxModelTest
{
    /**
     * Tests the BoundingBoxModel.
     */
    @Test
    public void testSetBoundingBox()
    {
        BoundingBoxModel model = new BoundingBoxModel();
        List<LineSegment2d> boundingBox = New.list(new LineSegment2d(new Vector2d(0, 0), new Vector2d(10, 10)));

        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, model);

        support.replayAll();

        model.addObserver(observer);
        model.setBoundingBox(boundingBox);

        assertEquals(boundingBox.get(0), model.getBoundingBox().iterator().next());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked observer.
     *
     * @param support Used to create the mock.
     * @param model The expected model.
     * @return The observer.
     */
    private Observer createObserver(EasyMockSupport support, BoundingBoxModel model)
    {
        Observer observer = support.createMock(Observer.class);

        observer.update(EasyMock.eq(model), EasyMock.cmpEq(BoundingBoxModel.BOUNDING_BOX_PROP));

        return observer;
    }
}
