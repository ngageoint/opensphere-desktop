package io.opensphere.core.util.swing;

import java.awt.Point;

/**
 * The Class GhostDropEvent.
 */
public abstract class GhostDropEvent
{
    /** The action. */
    private DropAction myAction;

    /** The point. */
    private Point myPoint;

    /**
     * Instantiates a new ghost drop event.
     *
     * @param action the action
     * @param point the point
     */
    public GhostDropEvent(DropAction action, Point point)
    {
        myAction = action;
        myPoint = point;
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public DropAction getAction()
    {
        return myAction;
    }

    /**
     * Gets the drop location.
     *
     * @return the drop location
     */
    public Point getDropLocation()
    {
        return myPoint;
    }

    /**
     * Sets the action.
     *
     * @param action the new action
     */
    public void setAction(DropAction action)
    {
        myAction = action;
    }

    /**
     * Sets the drop location.
     *
     * @param p the new drop location
     */
    public void setDropLocation(Point p)
    {
        myPoint = p;
    }

    /**
     * The Enum Action.
     */
    public enum DropAction
    {
        /** Move down. */
        MOVE_DOWN("Move Down"),

        /** Move up. */
        MOVE_UP("Move Up"),

        /** Released. */
        RELEASED("Released");

        /** The Action str. */
        private String myActionStr;

        /**
         * Instantiates a new action.
         *
         * @param action the action
         */
        DropAction(String action)
        {
            myActionStr = action;
        }

        @Override
        public String toString()
        {
            return myActionStr;
        }
    }
}
