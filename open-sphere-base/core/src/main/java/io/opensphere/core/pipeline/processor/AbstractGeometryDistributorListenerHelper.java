package io.opensphere.core.pipeline.processor;

import java.util.concurrent.ExecutorService;

import io.opensphere.core.projection.ProjectionChangeSupport;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;

/** Helper for event listening for the {@link GeometryDistributor}. */
abstract class AbstractGeometryDistributorListenerHelper
{
    /** The map context with which I register for events. */
    private final MapContext<?> myMapContext;

    /** Listener for projection change events. */
    private ProjectionChangeSupport.ProjectionChangeListener myProjectionChangeListener = new ProjectionChangeSupport.ProjectionChangeListener()
    {
        @Override
        public void projectionChanged(ProjectionChangedEvent evt)
        {
            handleProjectionChanged(evt);
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append(ProjectionChangeSupport.ProjectionChangeListener.class.getSimpleName())
                    .append(" [GeometryDistributorListenerHelper]").toString();
        }
    };

    /** Listener for draw enable events. */
    private MapContext.DrawEnableListener myDrawEnableListener = new MapContext.DrawEnableListener()
    {
        @Override
        public void drawEnabled(boolean flag)
        {
            handleDrawEnabled(flag);
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append(MapContext.DrawEnableListener.class.getSimpleName())
                    .append(" [GeometryDistributorListenerHelper]").toString();
        }
    };

    /** Executor to handle view changes. */
    private final ProcrastinatingExecutor myViewChangeExecutor;

    /** Listener for view change events. */
    private ViewChangeSupport.ViewChangeListener myViewChangeListener = new ViewChangeSupport.ViewChangeListener()
    {
        @Override
        public String toString()
        {
            return new StringBuilder().append(ViewChangeSupport.ViewChangeListener.class.getSimpleName())
                    .append(" [GeometryDistributorListenerHelper]").toString();
        }

        @Override
        public void viewChanged(final Viewer view, final ViewChangeSupport.ViewChangeType type)
        {
            myViewChangeExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    handleViewChanged(view, type);
                }
            });
        }
    };

    /**
     * Constructor. This class should only be created from AbstractProcessor.
     *
     * @param mapContext The processor's map context.
     * @param executorService The processor's executor service.
     */
    public AbstractGeometryDistributorListenerHelper(MapContext<?> mapContext, ExecutorService executorService)
    {
        myMapContext = mapContext;

        if (myMapContext.getDrawEnableSupport() != null)
        {
            myMapContext.getDrawEnableSupport().addDrawEnableListener(myDrawEnableListener);
        }

        if (myMapContext.getProjectionChangeSupport() != null)
        {
            myMapContext.getProjectionChangeSupport().addProjectionChangeListener(myProjectionChangeListener);
        }

        ViewChangeSupport viewChangeSupport = myMapContext.getViewChangeSupport();
        if (viewChangeSupport != null)
        {
            viewChangeSupport.addViewChangeListener(myViewChangeListener);
        }

        myViewChangeExecutor = new ProcrastinatingExecutor(executorService);
    }

    /** Handle any required cleanup. */
    public void close()
    {
        removeDrawEnableListener();
        removeProjectionChangeListener();
        removeViewChangeListener();
    }

    /**
     * Callback for a draw enabled event.
     *
     * @param flag Flag indicating if drawing is enabled.
     */
    protected abstract void handleDrawEnabled(boolean flag);

    /**
     * Callback for a projection changed event. Model coordinates must be
     * invalidated in any regions that the projection changed.
     *
     * @param evt The event.
     */
    protected abstract void handleProjectionChanged(ProjectionChangedEvent evt);

    /**
     * Call-back for a view changed event. When the view changes, the geometries
     * must be reviewed to determine which ones are on-screen.
     *
     * @param view The viewer.
     * @param type The viewer update type.
     */
    protected abstract void handleViewChanged(Viewer view, ViewChangeType type);

    /** Unsubscribe the draw enable listener. */
    protected void removeDrawEnableListener()
    {
        if (myDrawEnableListener != null && myMapContext.getDrawEnableSupport() != null)
        {
            myMapContext.getDrawEnableSupport().removeDrawEnableListener(myDrawEnableListener);
            myDrawEnableListener = null;
        }
    }

    /** Unsubscribe the projection change listener. */
    protected void removeProjectionChangeListener()
    {
        if (myProjectionChangeListener != null && myMapContext.getProjectionChangeSupport() != null)
        {
            myMapContext.getProjectionChangeSupport().removeProjectionChangeListener(myProjectionChangeListener);
            myProjectionChangeListener = null;
        }
    }

    /** Unsubscribe the view change listener. */
    protected void removeViewChangeListener()
    {
        if (myViewChangeListener != null && myMapContext.getViewChangeSupport() != null)
        {
            myMapContext.getViewChangeSupport().removeViewChangeListener(myViewChangeListener);
            myViewChangeListener = null;
        }
    }
}
