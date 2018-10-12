package io.opensphere.core.viewer.impl;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/**
 * Simple map context.
 *
 * @param <T> The type for the standard viewer.
 */
public class SimpleMapContext<T extends Viewer> implements MapContext<T>
{
    /** The screen viewer. */
    private final ScreenViewer myScreenViewer;

    /** The standard viewer. */
    private final T myStandardViewer;

    /** Support for view change notifications. */
    private final ViewChangeSupport myViewChangeSupport;

    /** The observer I use to know when the view has changed. */
    private final DynamicViewer.Observer myViewerObserver;

    /**
     * Construct the viewer set.
     *
     * @param screenViewer The viewer for screen position geometries.
     * @param standardViewer The viewer for standard geometries.
     */
    public SimpleMapContext(ScreenViewer screenViewer, T standardViewer)
    {
        myScreenViewer = screenViewer;
        myStandardViewer = standardViewer;

        myViewChangeSupport = new ViewChangeSupport();
        if (screenViewer != null || standardViewer != null)
        {
            myViewerObserver = type -> myViewChangeSupport.notifyViewChangeListeners(myStandardViewer, null, type);
            if (screenViewer != null)
            {
                screenViewer.addObserver(myViewerObserver);
            }
            if (standardViewer != null)
            {
                standardViewer.addObserver(myViewerObserver);
            }
        }
        else
        {
            myViewerObserver = null;
        }
    }

    @Override
    public void close()
    {
        if (myScreenViewer != null)
        {
            myScreenViewer.removeObserver(myViewerObserver);
        }
    }

    @Override
    public Collection<ViewControlTranslator> getAllControlTranslators()
    {
        return null;
    }

    @Override
    public ViewControlTranslator getCurrentControlTranslator()
    {
        return null;
    }

    @Override
    public DrawEnableSupport getDrawEnableSupport()
    {
        return null;
    }

    @Override
    public Projection getProjection()
    {
        return null;
    }

    @Override
    public Projection getProjection(Class<? extends AbstractDynamicViewer> viewerType)
    {
        return null;
    }

    @Override
    public ProjectionChangeSupport getProjectionChangeSupport()
    {
        return null;
    }

    @Override
    public Map<Projection, Class<? extends AbstractDynamicViewer>> getProjections()
    {
        return null;
    }

    @Override
    public Projection getRawProjection()
    {
        return null;
    }

    @Override
    public ScreenViewer getScreenViewer()
    {
        return myScreenViewer;
    }

    @Override
    public T getStandardViewer()
    {
        return myStandardViewer;
    }

    @Override
    public ViewChangeSupport getViewChangeSupport()
    {
        return myViewChangeSupport;
    }

    @Override
    public Class<? extends AbstractDynamicViewer> getViewerTypeForProjection(Projection proj)
    {
        return null;
    }

    @Override
    public void reshape(int width, int height)
    {
        if (myScreenViewer != null)
        {
            myScreenViewer.reshape(width, height);
        }
        if (myStandardViewer != null)
        {
            myStandardViewer.reshape(width, height);
        }
    }

    @Override
    public void setProjection(Class<? extends AbstractDynamicViewer> viewer)
    {
    }
}
