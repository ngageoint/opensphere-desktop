package io.opensphere.core.control;

import java.awt.event.ActionEvent;

import javax.swing.event.PopupMenuEvent;

/**
 * The Class ContextMenuSelectionAdapter.
 *
 * Adapter for {@link ContextMenuSelectionListener} that allows implementers to
 * override only those methods needed.
 */
public class ContextMenuSelectionAdapter implements ContextMenuSelectionListener
{
    @Override
    public void actionPerformed(ActionEvent e)
    {
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e)
    {
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
    {
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e)
    {
    }
}
