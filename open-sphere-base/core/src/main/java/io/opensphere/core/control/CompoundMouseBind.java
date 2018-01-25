package io.opensphere.core.control;

import java.awt.event.MouseEvent;

/**
 * Internal binding used to translate mouse events to external bindings.
 */
class CompoundMouseBind extends MouseBindingAbs
{
    /** Flag indicating if the mouse button is currently pressed. */
    private volatile boolean myIsPressed;

    /**
     * Construct the binding.
     *
     * @param binding The external binding.
     * @param listener The listener.
     * @param controlContext The control context.
     */
    CompoundMouseBind(DefaultMouseBinding binding, CompoundEventListener listener, ControlContextImpl controlContext)
    {
        super(binding, listener, controlContext);
    }

    /**
     * Construct the binding from a mouse event.
     *
     * @param e The mouse event.
     * @param listener The listener.
     * @param controlContext The control context.
     */
    CompoundMouseBind(MouseEvent e, CompoundEventListener listener, ControlContext controlContext)
    {
        super(e, listener, controlContext);
    }

    @Override
    public CompoundEventMouseAdapter getListener()
    {
        return (CompoundEventMouseAdapter)super.getListener();
    }

    @Override
    void mouseDragged(MouseEvent e)
    {
        if (myIsPressed)
        {
            getListener().mouseDragged(e);
        }
    }

    @Override
    void mousePressed(MouseEvent e)
    {
        myIsPressed = true;
        getListener().eventStarted(e);
    }

    @Override
    void mouseReleased(MouseEvent e)
    {
        if (myIsPressed)
        {
            myIsPressed = false;
            getListener().eventEnded(e);
        }
    }
}
