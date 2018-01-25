package io.opensphere.csvcommon.ui.columndefinition.model;

import java.util.Observable;
import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;

/**
 * Contains utility methods for the model tests.
 *
 */
public final class ModelTestUtil
{
    /**
     * Creates an easy mocked observer.
     *
     * @param support The easy mock support object.
     * @param expectedProperty The expected property to be passed to the
     *            observer.
     * @return The observer.
     */
    public static Observer createObserver(EasyMockSupport support, String expectedProperty)
    {
        Observer observer = support.createMock(Observer.class);
        observer.update(EasyMock.isA(Observable.class), EasyMock.cmpEq(expectedProperty));

        return observer;
    }

    /**
     * Not constructible.
     */
    private ModelTestUtil()
    {
    }
}
