package io.opensphere.core.util;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;

/**
 * A service that simplifies creating a controller that listens to observable
 * values.
 */
public class ObservableValueService extends CompositeService
{
    /**
     * Constructor.
     */
    public ObservableValueService()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param size the expected number of services (for memory savings)
     */
    public ObservableValueService(int size)
    {
        super(size);
    }

    /**
     * Adds a listener handle to this service that will bind the listener to the
     * given observable model when the service is opened.
     *
     * @param <T> The type of the observable.
     * @param observable The observable model.
     * @param listener The listener instance.
     * @return the listener handle that was created
     */
    public final <T> ObservableValueListenerHandle<T> bindModel(ObservableValue<T> observable, ChangeListener<? super T> listener)
    {
        return addService(new ObservableValueListenerHandle<>(observable, listener));
    }

    /**
     * Adds a listener handle to this service that will bind the listener to the
     * given observable model when the service is opened.
     *
     * @param <T> The type of the observable.
     * @param observable The observable model.
     * @param listener The listener instance.
     * @return the service that was created
     */
    public final <T> Service bindModel(javafx.beans.value.ObservableValue<T> observable,
            javafx.beans.value.ChangeListener<? super T> listener)
    {
        return addService(new Service()
        {
            @Override
            public void open()
            {
                observable.addListener(listener);
            }

            @Override
            public void close()
            {
                observable.removeListener(listener);
            }
        });
    }

    /**
     * Adds a listener handle to this service that will bind the listener to the
     * given observable model when the service is opened. The binding is done on
     * the JavaFX thread
     *
     * @param <T> The type of the observable.
     * @param observable The observable model.
     * @param listener The listener instance.
     * @return the service that was created
     */
    public final <T> Service bindModelFX(javafx.beans.value.ObservableValue<T> observable,
            javafx.beans.value.ChangeListener<? super T> listener)
    {
        return addService(new Service()
        {
            @Override
            public void open()
            {
                Platform.runLater(() -> observable.addListener(listener));
            }

            @Override
            public void close()
            {
                Platform.runLater(() -> observable.removeListener(listener));
            }
        });
    }

    /**
     * Adds a listener handle to this service that will bind the listener to the
     * given observable model when the service is opened.
     *
     * @param <E> the type of elements in the list
     * @param observable The observable model.
     * @param listener The listener instance.
     * @return the listener handle that was created
     */
    public final <E> ObservableListListenerHandle<E> bindModel(ObservableList<E> observable, ListDataListener<E> listener)
    {
        return addService(new ObservableListListenerHandle<>(observable, listener));
    }

    /**
     * Adds a listener handle to this service that will bind the listener to the
     * given observable model when the service is opened.
     *
     * @param <E> the type of elements in the list
     * @param observable The observable model.
     * @param listener The listener instance.
     * @return the service that was created
     */
    public final <E> Service bindModel(javafx.collections.ObservableList<E> observable, ListChangeListener<? super E> listener)
    {
        return addService(new Service()
        {
            @Override
            public void open()
            {
                observable.addListener(listener);
            }

            @Override
            public void close()
            {
                observable.removeListener(listener);
            }
        });
    }
}
