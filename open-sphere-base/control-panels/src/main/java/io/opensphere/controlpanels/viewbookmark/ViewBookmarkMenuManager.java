package io.opensphere.controlpanels.viewbookmark;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import io.opensphere.controlpanels.viewbookmark.controller.ViewBookmarkController;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.SplitButton;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistryListener;

/**
 * This class will manage adding/removing view position menu items to the Viewer
 * Book marks menu.
 */
public class ViewBookmarkMenuManager implements ViewBookmarkRegistryListener
{
    /** The menu item for creating a view bookmark. */
    private JMenuItem myAddMenuItem;

    /** The Controller. */
    private final ViewBookmarkController myController;

    /** The menu item for bringing up the manager dialog. */
    private JMenuItem myManageMenuItem;

    /** The Splitbutton panel. */
    private SplitButton mySplitbuttonPanel;

    /**
     * Instantiates a new view book mark menu manager.
     *
     * @param controller the controller
     */
    public ViewBookmarkMenuManager(ViewBookmarkController controller)
    {
        myController = controller;
        myController.addListener(this);
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                rebuildMenu();
            }
        });
    }

    /**
     * Instantiates a new view bookmark menu manager.
     *
     * @param controller the controller
     * @param buttonPanel the button panel
     * @param addMenuItem The menu item for adding a bookmark.
     * @param manageMenuItem The menu item for bringing up the manager dialog.
     */
    public ViewBookmarkMenuManager(ViewBookmarkController controller, SplitButton buttonPanel, JMenuItem addMenuItem,
            JMenuItem manageMenuItem)
    {
        this(controller);
        mySplitbuttonPanel = buttonPanel;
        myAddMenuItem = addMenuItem;
        myManageMenuItem = manageMenuItem;
    }

    @Override
    public void viewBookmarkAdded(ViewBookmark view, Object source)
    {
        rebuildMenu();
    }

    @Override
    public void viewBookmarkRemoved(ViewBookmark view, Object source)
    {
        if (mySplitbuttonPanel != null)
        {
            mySplitbuttonPanel.removeAll();
        }
        rebuildMenu();
    }

    /**
     * Adds the menu item.
     *
     * @param view the view
     */
    private void addMenuItem(ViewBookmark view)
    {
        ViewBookmarkMenuItem item = new ViewBookmarkMenuItem(view);
        item.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                if (evt.getSource() instanceof ViewBookmarkMenuItem)
                {
                    ViewBookmarkMenuItem item = (ViewBookmarkMenuItem)evt.getSource();
                    myController.gotoView(item.getViewBookmark().getViewName());
                }
            }
        });
        if (mySplitbuttonPanel != null)
        {
            mySplitbuttonPanel.addMenuItem(item);
        }
    }

    /**
     * Rebuild menu.
     */
    private void rebuildMenu()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                if (mySplitbuttonPanel != null)
                {
                    mySplitbuttonPanel.removeAll();
                    if (myAddMenuItem != null || myManageMenuItem != null)
                    {
                        if (myAddMenuItem != null)
                        {
                            mySplitbuttonPanel.addMenuItem(myAddMenuItem);
                        }
                        if (myManageMenuItem != null)
                        {
                            mySplitbuttonPanel.addMenuItem(myManageMenuItem);
                        }
                        mySplitbuttonPanel.add(new JSeparator());
                    }
                    for (ViewBookmark name : myController.getBookmarks())
                    {
                        addMenuItem(name);
                    }
                }
            }
        });
    }

    /**
     * The Class ViewBookmarkMenuItem.
     */
    private static class ViewBookmarkMenuItem extends JMenuItem
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The my view book mark. */
        private final ViewBookmark myViewBookmark;

        /**
         * Instantiates a new view book mark menu item.
         *
         * @param vbm the {@link ViewBookmark}
         */
        public ViewBookmarkMenuItem(ViewBookmark vbm)
        {
            super((vbm.is3D() ? "3D: " : "2D: ") + vbm.getViewName());
            myViewBookmark = vbm;
        }

        /**
         * Gets the view bookmark.
         *
         * @return the view bookmark
         */
        private ViewBookmark getViewBookmark()
        {
            return myViewBookmark;
        }
    }
}
