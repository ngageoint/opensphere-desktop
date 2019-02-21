package io.opensphere.core.viewer.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/**
 * Manager for the viewers in the application.
 */
public class ViewerManager
{
    /**
     * The current viewer. This is synchronized using the ViewerManager monitor.
     */
    private AbstractDynamicViewer myCurrentViewer;

    /** The support for view changes. */
    private final ViewChangeSupport myViewChangeSupport = new ViewChangeSupport();

    /** Map of {@link Viewer} types to viewer instances. */
    private final Map<Class<? extends AbstractDynamicViewer>, AbstractDynamicViewer> myViewers;

    /** The preferences for the viewer manager. */
    private final Preferences myPreferences;

    /**
     * The {@link UIRegistry}.
     */
    private final UIRegistry myRegistry;

    /**
     * Constructor.
     *
     * @param preferences The preferences for the viewer manager.
     * @param uiRegistry The {@link UIRegistry}.
     */
    public ViewerManager(Preferences preferences, UIRegistry uiRegistry)
    {
        myPreferences = preferences;
        myRegistry = uiRegistry;
        myViewers = new HashMap<>();
    }

    /**
     * Add a listener for view changes.
     *
     * @param listener The listener.
     */
    public void addViewChangeListener(ViewChangeSupport.ViewChangeListener listener)
    {
        myViewChangeSupport.addViewChangeListener(listener);
    }

    /**
     * Create a new viewer viewer.
     *
     * @param viewerType The type of viewer which to create.
     * @param modelWidth The width of the model in model coordinates.
     * @param modelHeight The height of the model in model coordinates.
     * @return the newly created viewer.
     */
    public synchronized AbstractDynamicViewer addViewer(Class<? extends AbstractDynamicViewer> viewerType, float modelWidth,
            float modelHeight)
    {
        final double minZoom = 1.;
        final double maxZoom = 1000000.;
        AbstractDynamicViewer.Builder builder = new AbstractDynamicViewer.Builder().maxZoom(maxZoom).minZoom(minZoom);
        builder.modelWidth(modelWidth);
        builder.modelHeight(modelHeight);
        builder.preferences(myPreferences);
        builder.uiRegistry(myRegistry);

        AbstractDynamicViewer viewer = myViewers.get(viewerType);
        if (viewer == null)
        {
            viewer = AbstractDynamicViewer.create(viewerType, builder);
            myViewers.put(viewerType, viewer);
        }
        else
        {
            viewer.reset(builder);
        }

        return viewer;
    }

    /**
     * Get the current viewer, used for transforming from model coordinates to
     * window coordinates.
     *
     * @return The current viewer.
     */
    public synchronized DynamicViewer getCurrentViewer()
    {
        return myCurrentViewer;
    }

    /**
     * Access the view change support.
     *
     * @return The view change support.
     */
    public ViewChangeSupport getViewChangeSupport()
    {
        return myViewChangeSupport;
    }

    /**
     * Get the viewer for this type.
     *
     * @param viewerType Class type of the viewer desired.
     * @return viewer to get.
     */
    public Viewer getViewer(Class<? extends AbstractViewer> viewerType)
    {
        return myViewers.get(viewerType);
    }

    /**
     * Notify the view-change listeners that the view has changed.
     *
     * @param executor Call the view changed methods using this executor.
     * @param type The type of viewer update.
     */
    public synchronized void notifyViewChangeListeners(Executor executor, ViewChangeSupport.ViewChangeType type)
    {
        myViewChangeSupport.notifyViewChangeListeners(myCurrentViewer, executor, type);
    }

    /**
     * Remove a view-changed listener.
     *
     * @param listener The listener to be removed.
     */
    public void removeViewChangeListener(ViewChangeSupport.ViewChangeListener listener)
    {
        myViewChangeSupport.removeViewChangeListener(listener);
    }

    /**
     * Switch the viewer.
     *
     * @param viewerType The type of viewer to which to switch.
     * @param modelWidth The width of the model in model coordinates.
     * @param modelHeight The height of the model in model coordinates.
     * @param viewerObserver An observer to be notified when the view changes.
     */
    public synchronized void switchViewer(Class<? extends AbstractDynamicViewer> viewerType, float modelWidth, float modelHeight,
            AbstractDynamicViewer.Observer viewerObserver)
    {
        AbstractDynamicViewer viewer = addViewer(viewerType, modelWidth, modelHeight);

        if (!Utilities.sameInstance(myCurrentViewer, viewer))
        {
            if (myCurrentViewer != null)
            {
                myCurrentViewer.removeObserver(viewerObserver);
                if (viewer.getViewportWidth() != myCurrentViewer.getViewportWidth()
                        || viewer.getViewportHeight() != myCurrentViewer.getViewportHeight())
                {
                    viewer.reshape(myCurrentViewer.getViewportWidth(), myCurrentViewer.getViewportHeight());
                }
            }
            viewer.addObserver(viewerObserver);
            myCurrentViewer = viewer;
        }
    }
}
