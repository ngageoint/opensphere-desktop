package io.opensphere.controlpanels.component.image;

import java.awt.image.BufferedImage;
import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

/**
 * Tests the {@link ScaledImageModel} class.
 */
public class ScaledImageModelTest
{
    /**
     * Test image.
     */
    private static final BufferedImage ourImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

    /**
     * Tests the {@link ScaledImageModel}.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support);

        support.replayAll();

        ScaledImageModel model = new ScaledImageModel();
        model.addObserver(observer);

        model.setWidthAndHeight(100, 100);
        model.setWidthAndHeight(100, 200);
        model.setWidthAndHeight(200, 200);
        model.setWidthAndHeight(200, 200);

        model.setImage(ourImage);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link Observer}.
     *
     * @param support Used to create the mock.
     * @return The observer.
     */
    private Observer createObserver(EasyMockSupport support)
    {
        Observer observer = support.createMock(Observer.class);

        observer.update(EasyMock.isA(ScaledImageModel.class), EasyMock.cmpEq(ScaledImageModel.IMAGE_PROP));
        observer.update(EasyMock.isA(ScaledImageModel.class), EasyMock.cmpEq(ScaledImageModel.WIDTH_HEIGHT_PROP));
        EasyMock.expectLastCall().times(3);

        return observer;
    }
}
