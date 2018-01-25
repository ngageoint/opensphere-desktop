package io.opensphere.overlay;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.SelectionMode;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.concurrent.EventQueueExecutor;

/**
 * The Class SelectionModeControllerImpl.
 */
public class SelectionModeControllerImpl implements SelectionModeController
{
    /** The Constant OUR_SELECTION_MODE_PREFERENCE. */
    private static final String OUR_SELECTION_MODE_PREFERENCE = "DefaultSelectionMode";

    /** The Change support. */
    private final WeakChangeSupport<SelectionModeChangeListener> myChangeSupport;

    /** The Default selection mode. */
    private SelectionMode myDefaultSelectionMode = SelectionMode.BOUNDING_BOX;

    /** The Preferences. */
    private Preferences myPreferences;

    /** The Default selection mode. */
    private SelectionMode mySelectionMode = SelectionMode.NONE;

    /**
     * Instantiates a new selection mode controller impl.
     *
     * @param tb the {@link Toolbox}
     */
    public SelectionModeControllerImpl(Toolbox tb)
    {
        myChangeSupport = new WeakChangeSupport<>();
        if (tb != null)
        {
            myPreferences = tb.getPreferencesRegistry().getPreferences(SelectionModeControllerImpl.class);
            myDefaultSelectionMode = SelectionMode
                    .valueOf(myPreferences.getString(OUR_SELECTION_MODE_PREFERENCE, SelectionMode.BOUNDING_BOX.toString()));
        }
    }

    @Override
    public void addSelectionModeChangeListener(SelectionModeChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public SelectionMode getDefaultSelectionMode()
    {
        return myDefaultSelectionMode;
    }

    @Override
    public SelectionMode getSelectionMode()
    {
        return mySelectionMode;
    }

    @Override
    public void removeSelectionModeChangeListener(SelectionModeChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void setSelectionMode(final SelectionMode mode)
    {
        if (mode != null && !mySelectionMode.equals(mode))
        {
            mySelectionMode = mode;
            if (mode != SelectionMode.NONE)
            {
                myDefaultSelectionMode = mode;
                // The saved default mode should never be NONE.
                if (myPreferences != null)
                {
                    myPreferences.putString(OUR_SELECTION_MODE_PREFERENCE, mode.toString(), this);
                }
            }

            myChangeSupport.notifyListeners(new Callback<SelectionModeChangeListener>()
            {
                @Override
                public void notify(SelectionModeChangeListener listener)
                {
                    listener.selectionModeChanged(mode);
                }
            }, new EventQueueExecutor());
        }
    }
}
