package io.opensphere.core.control.action.context;

import io.opensphere.core.model.ScreenPosition;

/** The context key for actions with only a screen position. */
public class ScreenPositionContextKey
{
    /** The position of the action on-screen. */
    private final ScreenPosition myPosition;

    /**
     * Constructor.
     *
     * @param position The position of the action.
     */
    public ScreenPositionContextKey(ScreenPosition position)
    {
        myPosition = position;
    }

    /**
     * Get the screen position of the action.
     *
     * @return The screen position.
     */
    public ScreenPosition getPosition()
    {
        return myPosition;
    }
}
