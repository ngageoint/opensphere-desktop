package io.opensphere.controlpanels.viewbookmark.controller;

import java.awt.Point;
import java.util.List;

import javax.swing.JOptionPane;

import io.opensphere.core.Toolbox;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistry;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistryListener;
import io.opensphere.core.viewbookmark.impl.DefaultViewBookmark;
import io.opensphere.core.viewbookmark.util.ViewBookmarkUtil;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.Viewer2D;
import io.opensphere.core.viewer.impl.Viewer2D.ViewerPosition2D;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * The Class ViewBookmarkController.
 */
public class ViewBookmarkController implements ViewBookmarkRegistryListener
{
    /** The Change support. */
    private final WeakChangeSupport<ViewBookmarkRegistryListener> myChangeSupport;

    /** The my toolbox. */
    private final Toolbox myToolbox;

    /** The View bookmark menu provider. */
    private final ViewBookmarkMenuItemProvider myViewBookmarkMenuProvider;

    /** The View bookmark registry. */
    private final ViewBookmarkRegistry myViewBookmarkRegistry;

    /**
     * Instantiates a new view bookmark controller.
     *
     * @param toolbox the toolbox
     */
    public ViewBookmarkController(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myViewBookmarkRegistry = myToolbox.getMapManager().getViewBookmarkRegistry();
        myViewBookmarkRegistry.addListener(this);
        myViewBookmarkMenuProvider = new ViewBookmarkMenuItemProvider(this);
        myChangeSupport = new WeakChangeSupport<>();
    }

    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    public void addListener(ViewBookmarkRegistryListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Convert a point to a geographic position.
     *
     * @param point The point.
     * @return The geographic position, or {@code null} if there is no
     *         geographic position at this point.
     */
    public final GeographicPosition convertPointToGeographicPosition(Point point)
    {
        return myToolbox.getMapManager().convertToPosition(new Vector2i(point), ReferenceLevel.ELLIPSOID);
    }

    /**
     * Gets the bookmark by name.
     *
     * @param name the name
     * @return the bookmark by name
     */
    public ViewBookmark getBookmarkByName(String name)
    {
        return myViewBookmarkRegistry.getBookmarkByName(name);
    }

    /**
     * Gets the bookmark names.
     *
     * @return the bookmark names
     */
    public List<String> getBookmarkNames()
    {
        return myViewBookmarkRegistry.getViewBookmarkNames();
    }

    /**
     * Gets the book marks.
     *
     * @return the book marks
     */
    public List<ViewBookmark> getBookmarks()
    {
        return myViewBookmarkRegistry.getViewBookmarks(new ViewBookmark.TypeAndNameComparator());
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Gets the view bookmark menu provider.
     *
     * @return the view bookmark menu provider
     */
    public ViewBookmarkMenuItemProvider getViewBookmarkMenuProvider()
    {
        return myViewBookmarkMenuProvider;
    }

    /**
     * Goto view.
     *
     * @param viewName the view name
     */
    public void gotoView(String viewName)
    {
        ViewBookmark vbm = myViewBookmarkRegistry.getBookmarkByName(viewName);
        ViewBookmarkUtil.gotoView(myToolbox, vbm);
    }

    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    public void removeListener(ViewBookmarkRegistryListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Removes the viewer position.
     *
     * @param view the view
     */
    public void removeViewerPosition(ViewBookmark view)
    {
        myViewBookmarkRegistry.removeViewBookmark(view, this);
    }

    /**
     * Save viewer position.
     */
    public void saveViewerPosition()
    {
        int index = 0;
        String viewName;
        do
        {
            viewName = "View-" + ++index;
        }
        while (myViewBookmarkRegistry.getBookmarkByName(viewName) != null);

        viewName = (String)JOptionPane.showInputDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                "Please enter a name for this look angle.", "New Saved Look Angle", JOptionPane.QUESTION_MESSAGE, null, null,
                viewName);

        if (viewName != null)
        {
            Viewer privateViewer = myToolbox.getMapManager().getStandardViewer();
            if (privateViewer instanceof Viewer3D)
            {
                Viewer3D view3d = (Viewer3D)privateViewer;
                ViewerPosition3D newView = new ViewerPosition3D(view3d.getPosition().getLocation(), view3d.getPosition().getDir(),
                        view3d.getPosition().getUp());
                DefaultViewBookmark bookmark = new DefaultViewBookmark(viewName, newView);
                myViewBookmarkRegistry.addViewBookmark(bookmark, this);
            }
            else if (privateViewer instanceof Viewer2D)
            {
                Viewer2D view2d = (Viewer2D)privateViewer;
                ViewerPosition2D newView = new ViewerPosition2D(view2d.getPosition().getLocation(),
                        view2d.getPosition().getScale());
                DefaultViewBookmark bookmark = new DefaultViewBookmark(viewName, newView);
                myViewBookmarkRegistry.addViewBookmark(bookmark, this);
            }
        }
    }

    @Override
    public void viewBookmarkAdded(final ViewBookmark view, final Object source)
    {
        myChangeSupport.notifyListeners(new Callback<ViewBookmarkRegistryListener>()
        {
            @Override
            public void notify(ViewBookmarkRegistryListener listener)
            {
                listener.viewBookmarkAdded(view, source);
            }
        });
    }

    @Override
    public void viewBookmarkRemoved(final ViewBookmark view, final Object source)
    {
        myChangeSupport.notifyListeners(new Callback<ViewBookmarkRegistryListener>()
        {
            @Override
            public void notify(ViewBookmarkRegistryListener listener)
            {
                listener.viewBookmarkRemoved(view, source);
            }
        });
    }
}
