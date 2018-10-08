package io.opensphere.core.util;

/**
 * Listener handle for ObservableValue.
 *
 * @param <T> The type of the value
 */
public class ObservableValueListenerHandle<T> implements Service
{
    /** The observable value. */
    private final ObservableValue<T> myObservable;

    /** The listener. */
    private final ChangeListener<? super T> myListener;

    /** Whether to handle the event. */
    private volatile boolean myHandleEvent = true;

    /**
     * Constructor.
     *
     * @param observable the observable value
     * @param listener the listener
     */
    public ObservableValueListenerHandle(ObservableValue<T> observable, final ChangeListener<? super T> listener)
    {
        myObservable = observable;
        myListener = new ChangeListener<>()
        {
            @Override
            public void changed(ObservableValue<? extends T> value, T oldValue, T newValue)
            {
                if (myHandleEvent)
                {
                    listener.changed(value, oldValue, newValue);
                }
            }
        };
    }

    /**
     * Access the observable.
     *
     * @return The observable.
     */
    protected final ObservableValue<T> getObservable()
    {
        return myObservable;
    }

    @Override
    public void open()
    {
        myObservable.addListener(myListener);
    }

    @Override
    public void close()
    {
        myObservable.removeListener(myListener);
    }

    /**
     * Causes events to be suppressed until resume is called.
     */
    public void pause()
    {
        myHandleEvent = false;
    }

    /**
     * Causes events to be handled.
     */
    public void resume()
    {
        myHandleEvent = true;
    }
}
