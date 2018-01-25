package io.opensphere.core.viewbookmark;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * The Class ViewBookmarkRegistry.
 */
public interface ViewBookmarkRegistry
{
    /**
     * Adds the listener to the registry. Note that listeners are held as weak
     * references and the caller should continue to hold a strong reference.
     *
     * @param listener the {@link ViewBookmarkRegistryListener} to add.
     */
    void addListener(ViewBookmarkRegistryListener listener);

    /**
     * Adds the view book mark.
     *
     * @param view the view
     * @param source the source that made the change.
     * @return true, if successful false if that book mark name is already in
     *         the registry.
     */
    boolean addViewBookmark(ViewBookmark view, Object source);

    /**
     * Checks for book mark with name.
     *
     * @param viewName the view name
     * @return the bookmark
     */
    ViewBookmark getBookmarkByName(String viewName);

    /**
     * Gets the view book mark by name.
     *
     * @param bookmarkName the book mark name
     * @return the {@link ViewBookmark} or null if not found.
     */
    ViewBookmark getViewBookmarkByName(String bookmarkName);

    /**
     * Gets the view book mark names.
     *
     * @return the view book mark names
     */
    List<String> getViewBookmarkNames();

    /**
     * Gets full {@link Set} of {@link ViewBookmark} in the registry.
     *
     * @return the view book marks.
     */
    Set<ViewBookmark> getViewBookmarks();

    /**
     * Gets the full set of the {@link ViewBookmark} in the registry sorted
     * using the provided sortComparator.
     *
     * @param sortComparator the sort comparator (if null returns sorted
     *            alphabetically by name).
     * @return the list of {@link ViewBookmark}
     */
    List<ViewBookmark> getViewBookmarks(Comparator<ViewBookmark> sortComparator);

    /**
     * Removes the listener.
     *
     * @param listener the {@link ViewBookmarkRegistryListener} to remove.
     */
    void removeListener(ViewBookmarkRegistryListener listener);

    /**
     * Removes the view book mark.
     *
     * @param viewName the view name
     * @param source the source that made the change.
     * @return true, if successful
     */
    boolean removeViewBookmark(String viewName, Object source);

    /**
     * Removes the view book mark.
     *
     * @param view the view
     * @param source the source that made the change.
     * @return true, if successful
     */
    boolean removeViewBookmark(ViewBookmark view, Object source);
}
