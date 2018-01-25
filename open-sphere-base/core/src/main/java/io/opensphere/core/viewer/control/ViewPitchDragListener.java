package io.opensphere.core.viewer.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import io.opensphere.core.util.ref.VolatileReference;
import io.opensphere.core.viewer.impl.ViewControlTranslator;

/**
 * A listener that handles viewer pitching movements.
 */
public class ViewPitchDragListener extends AbstractTargetedViewAdapter
{
    /** A reference to the view control translator. */
    private final VolatileReference<ViewControlTranslator> myViewControlTranslator;

    /**
     * Construct the listener for view pitch movements.
     *
     * @param viewControlTranslator A reference to the current view control
     *            translator.
     */
    public ViewPitchDragListener(VolatileReference<ViewControlTranslator> viewControlTranslator)
    {
        super("View", "Pitch the Camera", "This causes the camera to pitch up/down with mouse movement");
        myViewControlTranslator = viewControlTranslator;
    }

    @Override
    public void eventEnded(InputEvent event)
    {
        myViewControlTranslator.get().compoundViewPitchEnd(event);
    }

    @Override
    public void eventStarted(InputEvent event)
    {
        myViewControlTranslator.get().compoundViewPitchStart(event);
    }

    @Override
    public void mouseDragged(MouseEvent event)
    {
        myViewControlTranslator.get().compoundViewPitchDrag(event);
    }

    @Override
    public void mouseMoved(MouseEvent event)
    {
        myViewControlTranslator.get().compoundViewPitchDrag(event);
    }
}
