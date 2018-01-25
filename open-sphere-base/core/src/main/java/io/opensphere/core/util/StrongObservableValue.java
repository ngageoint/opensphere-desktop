package io.opensphere.core.util;

/**
 * Strong change support implementation of ObservableValue.
 *
 * @param <T> The type of the value
 */
public class StrongObservableValue<T> extends AbstractObservableValue<T>
{
    /** The change support. */
    private final transient AbstractChangeSupport<ChangeListener<? super T>> myChangeSupport = new StrongChangeSupport<>();

    @Override
    protected AbstractChangeSupport<ChangeListener<? super T>> getChangeSupport()
    {
        return myChangeSupport;
    }
}
