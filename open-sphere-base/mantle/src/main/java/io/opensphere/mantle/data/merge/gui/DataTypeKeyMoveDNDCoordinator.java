package io.opensphere.mantle.data.merge.gui;

import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.concurrent.EventQueueExecutor;

/**
 * The Class DataTypeKeyMoveDNDCoordinator.
 */
public class DataTypeKeyMoveDNDCoordinator
{
    /** The Key move listeners. */
    private final WeakChangeSupport<KeyMoveListener> myChangeSupport;

    /** The Current move entry. */
    private TypeKeyEntry myCurrentMoveEntry;

    /** The Current source panel. */
    private TypeKeyPanel myCurrentSourcePanel;

    /**
     * Instantiates a new data type key move dnd coordinator.
     */
    public DataTypeKeyMoveDNDCoordinator()
    {
        myChangeSupport = new WeakChangeSupport<>();
    }

    /**
     * Adds the key move listener.
     *
     * @param listener the listener
     */
    public void addKeyMoveListener(KeyMoveListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Move complete.
     */
    public void moveComplete()
    {
        final TypeKeyEntry entry = myCurrentMoveEntry;
        final TypeKeyPanel origPanel = myCurrentSourcePanel;
        myCurrentMoveEntry = null;
        myCurrentSourcePanel = null;
        myChangeSupport.notifyListeners(new Callback<KeyMoveListener>()
        {
            @Override
            public void notify(KeyMoveListener listener)
            {
                listener.keyMoveCompleted(entry, origPanel);
            }
        }, new EventQueueExecutor());
    }

    /**
     * Move initiated.
     *
     * @param entry the entry
     * @param sourcePanel the source panel
     * @param source the source
     */
    public void moveInitiated(final TypeKeyEntry entry, final TypeKeyPanel sourcePanel, final Object source)
    {
        myCurrentMoveEntry = entry;
        myCurrentSourcePanel = sourcePanel;
        myChangeSupport.notifyListeners(new Callback<KeyMoveListener>()
        {
            @Override
            public void notify(KeyMoveListener listener)
            {
                listener.keyMoveInitiated(entry, sourcePanel, source);
            }
        }, new EventQueueExecutor());
    }

    /**
     * Removes the key move listener.
     *
     * @param listener the listener
     */
    public void removeKeyMoveListener(KeyMoveListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * The listener interface.
     *
     **/
    public interface KeyMoveListener
    {
        /**
         * Key move completed.
         *
         * @param entry the entry
         * @param origPanel the orig panel
         */
        void keyMoveCompleted(TypeKeyEntry entry, TypeKeyPanel origPanel);

        /**
         * Key move initiated.
         *
         * @param entry the entry
         * @param sourcePanel the source panel
         * @param source the source
         */
        void keyMoveInitiated(TypeKeyEntry entry, TypeKeyPanel sourcePanel, Object source);
    }
}
