package io.opensphere.core.viewer.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import io.opensphere.core.util.ref.VolatileReference;
import io.opensphere.core.viewer.impl.ViewControlTranslator;

/**
 * A listener that handles moving the globe with a mouse drag.
 */
public class EarthDragListener extends AbstractTargetedViewAdapter
{
    /** A reference to the view control translator. */
    private final VolatileReference<ViewControlTranslator> myViewControlTranslator;

    /**
     * Construct the listener.
     *
     * @param viewControlTranslator A reference to the current view control
     *            translator.
     */
    public EarthDragListener(VolatileReference<ViewControlTranslator> viewControlTranslator)
    {
        super("View", "Move the Earth", "This causes the earth to move with the mouse.");
        myViewControlTranslator = viewControlTranslator;
    }

    @Override
    public void eventEnded(InputEvent event)
    {
        myViewControlTranslator.get().compoundEarthDragEnd(event);
    }

    @Override
    public void eventStarted(InputEvent event)
    {
        myViewControlTranslator.get().compoundEarthDragStart(event);
    }

    @Override
    public void mouseDragged(MouseEvent event)
    {
        myViewControlTranslator.get().compoundEarthDrag(event);
    }

    @Override
    public void mouseMoved(MouseEvent event)
    {
        myViewControlTranslator.get().compoundEarthDrag(event);
    }
}
