package io.opensphere.view.picker.model;

import io.opensphere.core.viewbookmark.ViewBookmark;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** The model backing the view picker. */
public class ViewPickerModel
{
    /** The observable list in which views are stored. */
    private final ObservableList<ViewBookmark> myViews;

    /** Creates a new view picker model. */
    public ViewPickerModel()
    {
        myViews = FXCollections.observableArrayList();
    }

    /**
     * Adds the supplied view bookmark to the model.
     *
     * @param view the view to add to the model.
     */
    public void addView(final ViewBookmark view)
    {
        myViews.add(view);
    }

    /**
     * Removes the supplied view bookmark from the model.
     *
     * @param view the view to remove from the model.
     */
    public void removeView(final ViewBookmark view)
    {
        myViews.remove(view);
    }

    /**
     * Gets the value of the {@link #myViews} field.
     *
     * @return the value of the myViews field.
     */
    public ObservableList<ViewBookmark> getViews()
    {
        return myViews;
    }
}
