package io.opensphere.core.viewbookmark.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistry;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistryListener;
import io.opensphere.core.viewbookmark.config.v1.JAXBViewBookmark;
import io.opensphere.core.viewbookmark.config.v1.JAXBViewBookmarkList;

/**
 * The Class ViewBookmarkRegistryImpl.
 */
public class ViewBookmarkRegistryImpl implements ViewBookmarkRegistry
{
    /** The Constant REGISTRY_PREFS_KEY. */
    public static final String REGISTRY_PREFS_KEY = "ViewBookmarkRegistry";

    /** The Change support. */
    private final WeakChangeSupport<ViewBookmarkRegistryListener> myChangeSupport;

    /** The preferences for view bookmarks. */
    private final Preferences myPreferences;

    /** The View book mark read write lock. */
    private final ReentrantReadWriteLock myViewBookmarkReadWriteLock;

    /** The View book marks. */
    private final Map<String, ViewBookmark> myViewBookmarks;

    /**
     * Instantiates a new view book mark registry.
     *
     * @param preferencesRegistry The preferences registry.
     */
    public ViewBookmarkRegistryImpl(PreferencesRegistry preferencesRegistry)
    {
        myViewBookmarkReadWriteLock = new ReentrantReadWriteLock();
        myChangeSupport = new WeakChangeSupport<>();
        myViewBookmarks = New.map();
        myPreferences = preferencesRegistry.getPreferences(ViewBookmarkRegistry.class);
        loadFromPreferences();
    }

    @Override
    public void addListener(ViewBookmarkRegistryListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public boolean addViewBookmark(ViewBookmark view, final Object source)
    {
        Utilities.checkNull(view, "view");
        Utilities.checkNull(view.getViewName(), "view name");
        ViewBookmark addedView = null;
        myViewBookmarkReadWriteLock.writeLock().lock();
        try
        {
            if (!myViewBookmarks.containsKey(view.getViewName()))
            {
                addedView = new DefaultViewBookmark(view);
                myViewBookmarks.put(view.getViewName(), addedView);
            }
        }
        finally
        {
            myViewBookmarkReadWriteLock.writeLock().unlock();
        }
        if (addedView != null)
        {
            final ViewBookmark fAddedView = addedView;
            saveToPreferences();
            myChangeSupport.notifyListeners(new Callback<ViewBookmarkRegistryListener>()
            {
                @Override
                public void notify(ViewBookmarkRegistryListener listener)
                {
                    listener.viewBookmarkAdded(fAddedView, source);
                }
            });
        }
        return addedView != null;
    }

    @Override
    public ViewBookmark getBookmarkByName(String viewName)
    {
        ViewBookmark vbm = null;
        myViewBookmarkReadWriteLock.readLock().lock();
        try
        {
            if (myViewBookmarks.containsKey(viewName))
            {
                vbm = myViewBookmarks.get(viewName);
            }
        }
        finally
        {
            myViewBookmarkReadWriteLock.readLock().unlock();
        }
        return vbm;
    }

    @Override
    public ViewBookmark getViewBookmarkByName(String bookmarkName)
    {
        ViewBookmark result = null;
        myViewBookmarkReadWriteLock.readLock().lock();
        try
        {
            result = myViewBookmarks.get(bookmarkName);
        }
        finally
        {
            myViewBookmarkReadWriteLock.readLock().unlock();
        }
        return result;
    }

    @Override
    public List<String> getViewBookmarkNames()
    {
        List<String> resultList = null;
        myViewBookmarkReadWriteLock.readLock().lock();
        try
        {
            resultList = New.list(myViewBookmarks.keySet());
        }
        finally
        {
            myViewBookmarkReadWriteLock.readLock().unlock();
        }
        if (resultList != null)
        {
            Collections.sort(resultList);
        }
        return resultList == null ? Collections.<String>emptyList() : Collections.unmodifiableList(resultList);
    }

    @Override
    public Set<ViewBookmark> getViewBookmarks()
    {
        Set<ViewBookmark> resultSet;
        myViewBookmarkReadWriteLock.readLock().lock();
        try
        {
            resultSet = Collections.unmodifiableSet(New.set(myViewBookmarks.values()));
        }
        finally
        {
            myViewBookmarkReadWriteLock.readLock().unlock();
        }
        return resultSet;
    }

    @Override
    public List<ViewBookmark> getViewBookmarks(Comparator<ViewBookmark> sortComparator)
    {
        List<ViewBookmark> resultList = null;
        myViewBookmarkReadWriteLock.readLock().lock();
        try
        {
            resultList = New.list(myViewBookmarks.values());
        }
        finally
        {
            myViewBookmarkReadWriteLock.readLock().unlock();
        }
        if (resultList != null)
        {
            Collections.sort(resultList, sortComparator == null ? new ViewBookmark.NameComparator() : sortComparator);
        }
        return resultList == null ? Collections.<ViewBookmark>emptyList() : Collections.unmodifiableList(resultList);
    }

    @Override
    public void removeListener(ViewBookmarkRegistryListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public boolean removeViewBookmark(String viewName, final Object source)
    {
        Utilities.checkNull(viewName, "viewName");
        ViewBookmark removedView = null;
        myViewBookmarkReadWriteLock.writeLock().lock();
        try
        {
            removedView = myViewBookmarks.remove(viewName);
        }
        finally
        {
            myViewBookmarkReadWriteLock.writeLock().unlock();
        }
        if (removedView != null)
        {
            saveToPreferences();
            final ViewBookmark fRemoved = removedView;
            myChangeSupport.notifyListeners(new Callback<ViewBookmarkRegistryListener>()
            {
                @Override
                public void notify(ViewBookmarkRegistryListener listener)
                {
                    listener.viewBookmarkRemoved(fRemoved, source);
                }
            });
        }
        return removedView != null;
    }

    @Override
    public boolean removeViewBookmark(ViewBookmark view, Object source)
    {
        Utilities.checkNull(view, "view");
        return removeViewBookmark(view.getViewName(), source);
    }

    /**
     * Load from preferences.
     */
    private void loadFromPreferences()
    {
        JAXBViewBookmarkList list = myPreferences.getJAXBObject(JAXBViewBookmarkList.class, REGISTRY_PREFS_KEY, null);
        if (list != null && list.getBookmarkList() != null && !list.getBookmarkList().isEmpty())
        {
            myViewBookmarkReadWriteLock.writeLock().lock();
            try
            {
                myViewBookmarks.clear();
                for (JAXBViewBookmark bm : list.getBookmarkList())
                {
                    myViewBookmarks.put(bm.getViewName(), new DefaultViewBookmark(bm));
                }
            }
            finally
            {
                myViewBookmarkReadWriteLock.writeLock().unlock();
            }
        }
    }

    /**
     * Save to preferences.
     */
    private void saveToPreferences()
    {
        JAXBViewBookmarkList list = new JAXBViewBookmarkList(getViewBookmarks(null));
        myPreferences.putJAXBObject(REGISTRY_PREFS_KEY, list, false, this);
    }
}
