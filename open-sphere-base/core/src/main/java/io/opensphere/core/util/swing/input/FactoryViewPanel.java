package io.opensphere.core.util.swing.input;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.ObservableListListenerHandle;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ObservableValueListenerHandle;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.swing.input.controller.ControllerFactoryService;

/**
 * A panel that can be used for most views. It takes care of opening and closing
 * any controllers created by its factory assuming that {@link #open()} and
 * {@link #close()} are called.
 */
public class FactoryViewPanel extends ViewPanel implements Service
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The controller factory. */
    private final transient ControllerFactoryService myFactory = new ControllerFactoryService();

    @Override
    public void open()
    {
        myFactory.open();
    }

    @Override
    public void close()
    {
        myFactory.close();
    }

    /**
     * Gets the factory.
     *
     * @return the factory
     */
    protected final ControllerFactoryService getFactory()
    {
        return myFactory;
    }

    /**
     * Adds a listener handle to this service that will bind the listener to the
     * given observable model when the service is opened.
     *
     * @param <T> The type of the observable.
     * @param observable The observable model.
     * @param listener The listener instance.
     */
    protected final <T> void bindModel(ObservableValue<T> observable, ChangeListener<? super T> listener)
    {
        myFactory.addService(new ObservableValueListenerHandle<>(observable, listener));
    }

    /**
     * Adds a listener handle to this service that will bind the listener to the
     * given observable model when the service is opened.
     *
     * @param <E> the type of elements in the list
     * @param observable The observable model.
     * @param listener The listener instance.
     */
    protected final <E> void bindModel(ObservableList<E> observable, ListDataListener<E> listener)
    {
        myFactory.addService(new ObservableListListenerHandle<>(observable, listener));
    }
}
