package io.opensphere.core.control;

import java.awt.event.MouseEvent;

/**
 * Internal binding used to translate mouse events to external bindings.
 */
class DiscreteMouseBind extends MouseBindingAbs
{
    /**
     * Construct the binding.
     *
     * @param binding The external binding.
     * @param listener The listener.
     * @param controlContext The control context.
     */
    DiscreteMouseBind(DefaultMouseBinding binding, DiscreteEventListener listener, ControlContextImpl controlContext)
    {
        super(binding, listener, controlContext);
    }

    /**
     * Construct the binding from a mouse event.
     *
     * @param e The mouse event.
     * @param listener The listener.
     */
    DiscreteMouseBind(MouseEvent e, DiscreteEventListener listener)
    {
        super(e, listener, null);
    }

    @Override
    public DiscreteEventListener getListener()
    {
        return (DiscreteEventListener)super.getListener();
    }

    @Override
    void mouseClicked(MouseEvent e)
    {
        getListener().eventOccurred(e);
    }

    @Override
    void mouseDragged(MouseEvent e)
    {
        getListener().eventOccurred(e);
    }

    @Override
    void mouseEntered(MouseEvent e)
    {
        getListener().eventOccurred(e);
    }

    @Override
    void mouseExited(MouseEvent e)
    {
        getListener().eventOccurred(e);
    }

    @Override
    void mouseMoved(MouseEvent e)
    {
        getListener().eventOccurred(e);
    }

    @Override
    void mousePressed(MouseEvent e)
    {
        getListener().eventOccurred(e);
    }

    @Override
    void mouseReleased(MouseEvent e)
    {
        getListener().eventOccurred(e);
    }
}
