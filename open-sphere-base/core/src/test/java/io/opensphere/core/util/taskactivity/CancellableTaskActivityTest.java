package io.opensphere.core.util.taskactivity;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

/**
 * Tests the {@link CancellableTaskActivity} class.
 */
public class CancellableTaskActivityTest
{
    /**
     * Tests the {@link CancellableTaskActivity} class.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        ChangeListener<? super Double> listener = createListener(support);

        support.replayAll();

        CancellableTaskActivity activity = new CancellableTaskActivity();
        activity.progressProperty().addListener(listener);
        activity.setProgress(.5);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked listener.
     *
     * @param support Used to create the mock.
     * @return The mocked listener.
     */
    @SuppressWarnings("unchecked")
    private ChangeListener<? super Double> createListener(EasyMockSupport support)
    {
        ChangeListener<Double> runnable = support.createMock(ChangeListener.class);

        runnable.changed(EasyMock.isA(ObservableValue.class), EasyMock.eq(Double.valueOf(0)), EasyMock.eq(Double.valueOf(.5)));

        return runnable;
    }
}
