package io.opensphere.core.util;

/**
 * Listener handle for ObservableList.
 *
 * @param <E> the type of elements in the list
 */
public class ObservableListListenerHandle<E> implements Service
{
    /** The observable list. */
    private final ObservableList<E> myObservable;

    /** The listener. */
    private final ListDataListener<E> myListener;

    /**
     * Constructor.
     *
     * @param observable the observable list
     * @param listener the listener
     */
    public ObservableListListenerHandle(ObservableList<E> observable, ListDataListener<E> listener)
    {
        myObservable = observable;
        myListener = listener;
    }

    @Override
    public void open()
    {
        myObservable.addChangeListener(myListener);
    }

    @Override
    public void close()
    {
        myObservable.removeChangeListener(myListener);
    }
}
