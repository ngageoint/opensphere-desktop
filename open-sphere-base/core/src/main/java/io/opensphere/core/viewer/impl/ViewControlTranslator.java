package io.opensphere.core.viewer.impl;

import java.awt.event.InputEvent;

/**
 * Interface for classes that translate from user control events to viewer
 * control events.
 */
public interface ViewControlTranslator
{
    /**
     * Continue an earth drag event.
     *
     * @param event The input user event.
     */
    void compoundEarthDrag(InputEvent event);

    /**
     * End an earth drag event.
     *
     * @param event The input user event.
     */
    void compoundEarthDragEnd(InputEvent event);

    /**
     * Begin an earth drag event.
     *
     * @param event The input user event.
     */
    void compoundEarthDragStart(InputEvent event);

    /**
     * Continue an axis move.
     *
     * @param event The input user event.
     */
    void compoundMoveAxisDrag(InputEvent event);

    /**
     * End an axis move.
     *
     * @param event The input user event.
     */
    void compoundMoveAxisEnd(InputEvent event);

    /**
     * Begin an axis move.
     *
     * @param event The input user event.
     */
    void compoundMoveAxisStart(InputEvent event);

    /**
     * Continue a view pitch.
     *
     * @param event The input user event.
     */
    void compoundViewPitchDrag(InputEvent event);

    /**
     * End a view pitch.
     *
     * @param event The input user event.
     */
    void compoundViewPitchEnd(InputEvent event);

    /**
     * Begin a view pitch.
     *
     * @param event The input user event.
     */
    void compoundViewPitchStart(InputEvent event);

    /**
     * End a view yaw.
     *
     * @param event The input user event.
     */
    void compoundViewYawDrag(InputEvent event);

    /**
     * Continue a view yaw.
     *
     * @param event The input user event.
     */
    void compoundViewYawEnd(InputEvent event);

    /**
     * Begin a view yaw.
     *
     * @param event The input user event.
     */
    void compoundViewYawStart(InputEvent event);

    /**
     * Continue a zoom event.
     *
     * @param event The input user event.
     */
    void compoundZoomAction(InputEvent event);

    /**
     * End a zoom event.
     *
     * @param event The input user event.
     */
    void compoundZoomEnd(InputEvent event);

    /**
     * Begin a zoom event.
     *
     * @param event The input user event.
     */
    void compoundZoomStart(InputEvent event);

    /**
     * Pitch the view down.
     *
     * @param event The input user event.
     */
    void pitchViewDown(InputEvent event);

    /**
     * Pitch the view up.
     *
     * @param event The input user event.
     */
    void pitchViewUp(InputEvent event);

    /**
     * Reset the view to a normalized position.
     *
     * @param event The input user event.
     */
    void resetView(InputEvent event);

    /**
     * Roll the view left.
     *
     * @param event The input user event.
     */
    void rollViewLeft(InputEvent event);

    /**
     * Roll the view right.
     *
     * @param event The input user event.
     */
    void rollViewRight(InputEvent event);

    /**
     * Set the control to be enabled or disabled.
     *
     * @param enable true to enable and false to disable.
     */
    void setControlEnabled(boolean enable);

    /**
     * Sets the view zoom rate.
     *
     * @param rate the new zoom rate
     */
    void setZoomRate(int rate);

    /**
     * Gets the view zoom rate.
     *
     * @return the view zoom rate.
     */
    int getZoomRate();

    /**
     * Pan the view down.
     *
     * @param event The input user event.
     * @param microMovement move the view in very small increments rather than
     *            the regular movements
     */
    void viewDown(InputEvent event, boolean microMovement);

    /**
     * Pan the view left.
     *
     * @param event The input user event.
     * @param microMovement move the view in very small increments rather than
     *            the regular movements
     */
    void viewLeft(InputEvent event, boolean microMovement);

    /**
     * Pan the view right.
     *
     * @param event The input user event.
     * @param microMovement move the view in very small increments rather than
     *            the regular movements
     */
    void viewRight(InputEvent event, boolean microMovement);

    /**
     * Pan the view up.
     *
     * @param event The input user event.
     * @param microMovement move the view in very small increments rather than
     *            the regular movements
     */
    void viewUp(InputEvent event, boolean microMovement);

    /**
     * Yaw the view left.
     *
     * @param event The input user event.
     */
    void yawViewLeft(InputEvent event);

    /**
     * Yaw the view right.
     *
     * @param event The input user event.
     */
    void yawViewRight(InputEvent event);

    /**
     * Zoom the view an arbitrary amount.
     *
     * @param delta The amount to zoom the view.
     */
    void zoomView(double delta);

    /**
     * Zoom the view in.
     *
     * @param event The input user event.
     */
    void zoomInView(InputEvent event);

    /**
     * Zoom the view out.
     *
     * @param event The input user event.
     */
    void zoomOutView(InputEvent event);
}
