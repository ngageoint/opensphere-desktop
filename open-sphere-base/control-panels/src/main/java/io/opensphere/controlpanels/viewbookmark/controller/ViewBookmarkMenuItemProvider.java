package io.opensphere.controlpanels.viewbookmark.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.ScreenPositionContextKey;
import io.opensphere.core.model.GeographicPosition;

/**
 * The Class ViewBookmarkMenuItemProvider.
 */
public class ViewBookmarkMenuItemProvider
{
    /** The Controller. */
    private final ViewBookmarkController myController;

    /** The default context menu provider. */
    private final ContextMenuProvider<ScreenPositionContextKey> myViewBookmarkMenuProvider = new ContextMenuProvider<ScreenPositionContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, ScreenPositionContextKey key)
        {
            final GeographicPosition pos = myController.convertPointToGeographicPosition(key.getPosition().asPoint());
            if (pos != null)
            {
                JMenuItem mi = new JMenuItem("Save the current viewer position...");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        myController.saveViewerPosition();
                    }
                });
                return Collections.singletonList(mi);
            }
            else
            {
                return null;
            }
        }

        @Override
        public int getPriority()
        {
            return 11000;
        }
    };

    /**
     * Instantiates a new view bookmark menu item provider.
     *
     * @param controller the controller
     */
    public ViewBookmarkMenuItemProvider(ViewBookmarkController controller)
    {
        myController = controller;
        myController.getToolbox().getUIRegistry().getContextActionManager().registerContextMenuItemProvider(
                ContextIdentifiers.SCREEN_POSITION_CONTEXT, ScreenPositionContextKey.class, myViewBookmarkMenuProvider);
    }

    /**
     * Gets the view bookmark menu provider.
     *
     * @return the view bookmark menu provider
     */
    public ContextMenuProvider<ScreenPositionContextKey> getViewBookmarkMenuProvider()
    {
        return myViewBookmarkMenuProvider;
    }
}
