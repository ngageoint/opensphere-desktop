package io.opensphere.core.viewer.control;

import io.opensphere.core.control.CompoundEventMouseAdapter;

/**
 * Class which handles targeting and target priority for view control listeners.
 */
public abstract class AbstractTargetedViewAdapter extends CompoundEventMouseAdapter
{
    /**
     * Construct the listener.
     *
     * @param category The category to present to the user.
     * @param title The title to present to the user.
     * @param description The description to present to the user.
     */
    public AbstractTargetedViewAdapter(String category, String title, String description)
    {
        super(category, title, description);
    }

    @Override
    public int getTargetPriority()
    {
        return 1000;
    }

    @Override
    public boolean isTargeted()
    {
        return true;
    }

    @Override
    public boolean mustBeTargeted()
    {
        return true;
    }
}
