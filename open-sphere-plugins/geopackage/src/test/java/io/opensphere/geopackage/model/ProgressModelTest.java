package io.opensphere.geopackage.model;

import static org.junit.Assert.assertEquals;

import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

/**
 * Unit test for the {@link ProgressModel} class.
 */
public class ProgressModelTest
{
    /**
     * Tests the completed count property.
     */
    @Test
    public void testCompletedImportCount()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, ProgressModel.COMPLETED_COUNT_PROP);

        support.replayAll();

        ProgressModel model = new ProgressModel();
        model.addObserver(observer);

        model.setCompletedCount(34);
        assertEquals(34, model.getCompletedCount());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link Observer}.
     *
     * @param support Used to create the mock.
     * @param expectedProp The expected property on the update call.
     * @return The mocked observer.
     */
    private Observer createObserver(EasyMockSupport support, String expectedProp)
    {
        Observer observer = support.createMock(Observer.class);

        observer.update(EasyMock.isA(ProgressModel.class), EasyMock.cmpEq(expectedProp));

        return observer;
    }
}
