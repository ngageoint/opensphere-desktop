package io.opensphere.core.viewbookmark;

/**
 * The listener interface for the {@link ViewBookmarkRegistry}.
 */
public interface ViewBookmarkRegistryListener
{
    /**
     * View book mark added.
     *
     * @param view the {@link ViewBookmark} added.
     * @param source the source of the change.
     */
    void viewBookmarkAdded(ViewBookmark view, Object source);

    /**
     * View book mark removed.
     *
     * @param view the {@link ViewBookmark} removed.
     * @param source the source of the change.
     */
    void viewBookmarkRemoved(ViewBookmark view, Object source);
}
