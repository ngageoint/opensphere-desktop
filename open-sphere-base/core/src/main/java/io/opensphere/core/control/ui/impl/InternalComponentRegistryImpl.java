package io.opensphere.core.control.ui.impl;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import io.opensphere.core.control.ui.InternalComponentRegistry;
import io.opensphere.core.hud.awt.HUDFrame;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Registry for addition of new frames. */
public class InternalComponentRegistryImpl extends InternalComponentRegistry
{
    /** The container to which the internal frames will be added. */
    private final Container myInternalFrameContainer;

    /** True when the mouse is over Swing based HUDs only. */
    private boolean myMouseOverHUD;

    /**
     * Constructor.
     *
     * @param internalFrameContainer The container to which internal frames will
     *            be added, this will be a hidden frame.
     */
    public InternalComponentRegistryImpl(Container internalFrameContainer)
    {
        myInternalFrameContainer = internalFrameContainer;
    }

    @Override
    public Rectangle determineDefaultFramePosition(Rectangle bounds)
    {
        assert EventQueue.isDispatchThread();
        Rectangle containerBounds = getInternalContainer().getBounds();
        int left = containerBounds.width - bounds.width - 5;
        Rectangle rectangle = new Rectangle(left, 5, bounds.width, bounds.height);

        List<HUDFrame> otherFrames = getObjects();
        boolean foundOverlap;
        boolean phaseOne = true;
        do
        {
            foundOverlap = false;
            for (HUDFrame other : otherFrames)
            {
                if (other instanceof HUDJInternalFrame && other.isVisible())
                {
                    Rectangle otherBounds = ((HUDJInternalFrame)other).getInternalFrame().getBounds();
                    if (phaseOne ? otherBounds.intersects(rectangle) : otherBounds.getLocation().equals(rectangle.getLocation()))
                    {
                        int newY = otherBounds.y + (phaseOne ? otherBounds.height + 5 : 20);
                        if (newY + rectangle.getHeight() <= containerBounds.height)
                        {
                            rectangle.y = newY;
                            foundOverlap = true;
                        }
                        else
                        {
                            int newX = otherBounds.x - bounds.width - 5;
                            if (newX >= 0)
                            {
                                rectangle.y = 5;
                                rectangle.x = newX;
                                foundOverlap = true;
                            }
                            else if (phaseOne)
                            {
                                rectangle.x = left;
                                rectangle.y = 10;
                                phaseOne = false;
                            }
                            else
                            {
                                foundOverlap = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
        while (foundOverlap);

        return rectangle;
    }

    @Override
    public Container getInternalContainer()
    {
        return myInternalFrameContainer;
    }

    @Override
    public boolean isMouseOverHUD()
    {
        return myMouseOverHUD;
    }

    @Override
    public void setMouseOverHUD(boolean mouseOverHUD)
    {
        myMouseOverHUD = mouseOverHUD;
    }

    @Override
    protected boolean doAddObjectsForSource(Object source, Collection<? extends HUDFrame> objs)
    {
        for (HUDFrame frame : objs)
        {
            if (frame instanceof HUDJInternalFrame)
            {
                final HUDJInternalFrame comp = (HUDJInternalFrame)frame;
                EventQueueUtilities.runOnEDTAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myInternalFrameContainer.add(comp.getInternalFrame());

                        comp.getInternalFrame().addInternalFrameListener(new InternalFrameAdapter()
                        {
                            @Override
                            public void internalFrameClosed(InternalFrameEvent e)
                            {
                                removeObjects(Collections.singleton(comp));
                            }
                        });
                    }
                });
            }
        }
        return super.doAddObjectsForSource(source, objs);
    }

    @Override
    protected Collection<HUDFrame> doRemoveObjectsForSource(Object source)
    {
        final Collection<HUDFrame> frames = super.doRemoveObjectsForSource(source);
        if (!frames.isEmpty())
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    for (HUDFrame frame : frames)
                    {
                        if (frame instanceof HUDJInternalFrame)
                        {
                            final HUDJInternalFrame comp = (HUDJInternalFrame)frame;
                            myInternalFrameContainer.remove(comp.getInternalFrame());
                        }
                    }
                }
            });
        }
        return frames;
    }

    @Override
    protected boolean doRemoveObjectsForSource(Object source, final Collection<? extends HUDFrame> objs)
    {
        boolean removed = super.doRemoveObjectsForSource(source, objs);

        if (removed)
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    for (HUDFrame frame : objs)
                    {
                        if (frame instanceof HUDJInternalFrame)
                        {
                            HUDJInternalFrame comp = (HUDJInternalFrame)frame;
                            myInternalFrameContainer.remove(comp.getInternalFrame());
                        }
                    }
                }
            });
        }

        return removed;
    }
}
