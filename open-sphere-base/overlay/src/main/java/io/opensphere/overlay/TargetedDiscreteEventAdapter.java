package io.opensphere.overlay;

import io.opensphere.core.control.DiscreteEventAdapter;

/**
 * Abstract class which establishes the target priority and that the listener
 * must be targeted.
 */
public abstract class TargetedDiscreteEventAdapter extends DiscreteEventAdapter
{
    /** Constructor. */
    public TargetedDiscreteEventAdapter()
    {
        super("Selection Controls", "Draw Selection Region", "Used for drawing selection regions.");
    }

    @Override
    public int getTargetPriority()
    {
        return 2000;
    }

    @Override
    public boolean mustBeTargeted()
    {
        return true;
    }
}
