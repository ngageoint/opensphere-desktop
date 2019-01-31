package io.opensphere.view.picker.controller;

import io.opensphere.core.Toolbox;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistry;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistryListener;
import io.opensphere.view.picker.model.ViewPickerModel;
import io.opensphere.view.picker.view.ViewPickerPanel;

/** The controller managing interactions between the model and the view. */
public class ViewPickerController implements ViewBookmarkRegistryListener
{
    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /** The registry through which bookmarks are read. */
    private ViewBookmarkRegistry myViewBookmarkRegistry;

    /** The model in which saved views are stored for the view. */
    private final ViewPickerModel myModel;

    /** The panel on which view buttons are rendered. */
    private final ViewPickerPanel myPanel;

    /**
     * Creates a new controller.
     * 
     * @param toolbox the toolbox through which application state is accessed.
     */
    public ViewPickerController(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myViewBookmarkRegistry = myToolbox.getMapManager().getViewBookmarkRegistry();
        myViewBookmarkRegistry.addListener(this);
        myModel = new ViewPickerModel();
        myPanel = new ViewPickerPanel(toolbox, myModel);

        myViewBookmarkRegistry.getViewBookmarks().forEach(myModel::addView);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.viewbookmark.ViewBookmarkRegistryListener#viewBookmarkAdded(io.opensphere.core.viewbookmark.ViewBookmark,
     *      java.lang.Object)
     */
    @Override
    public void viewBookmarkAdded(ViewBookmark view, Object source)
    {
        myModel.addView(view);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.viewbookmark.ViewBookmarkRegistryListener#viewBookmarkRemoved(io.opensphere.core.viewbookmark.ViewBookmark,
     *      java.lang.Object)
     */
    @Override
    public void viewBookmarkRemoved(ViewBookmark view, Object source)
    {
        myModel.removeView(view);
    }

    /**
     * Gets the value of the {@link #myPanel} field.
     *
     * @return the value of the myPanel field.
     */
    public ViewPickerPanel getPanel()
    {
        return myPanel;
    }
}
