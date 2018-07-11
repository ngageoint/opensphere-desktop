package io.opensphere.core.map;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import io.opensphere.core.MapManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.config.ViewerConfigurations;
import io.opensphere.core.config.ViewerConfigurations.ViewControlTranslatorConfiguration;
import io.opensphere.core.config.ViewerConfigurations.ViewerConfiguration;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.projection.AbstractProjection;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangeSupport;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.projection.ProjectionManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.ref.VolatileReference;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistry;
import io.opensphere.core.viewbookmark.impl.ViewBookmarkRegistryImpl;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.impl.AbstractDynamicViewer;
import io.opensphere.core.viewer.impl.DrawEnableSupport;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.core.viewer.impl.ViewControlTranslator;
import io.opensphere.core.viewer.impl.Viewer2D;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.core.viewer.impl.ViewerManager;

/**
 * Implementation of the {@link MapManager} interface.
 */
@SuppressWarnings("PMD.GodClass")
public class MapManagerImpl implements MapManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MapManagerImpl.class);

    private final Toolbox myToolbox;

    /** Map of projections to viewer types. */
    private static final Map<Projection, Class<? extends AbstractDynamicViewer>> ourProjectionMap = New.insertionOrderMap();

    /** Key for the projection preference. */
    private static final String PROJECTION_PREF_KEY = "projection";

    /** A reference to the current viewer. */
    private final VolatileReference<DynamicViewer> myCurrentViewer = new VolatileReference<>();

    /** Support for draw enable/disable. */
    private final DrawEnableSupportExtension myDrawEnableSupport;

    /** The map menu controller. */
    private final MapMenuControl myMapMenuControl;

    /** Context menu provider for the map manager. */
    private final MapManagerMenuProvider myMenuProvider;

    /** Helper for managing preference settings settings. */
    private final MapManagerPreferenceHelper myPreferenceHelper;

    /** The preferences for the map manager. */
    private final Preferences myPreferences;

    /** The lead projection change listener. */
    private final ProjectionChangeSupport.ProjectionChangeListener myProjectionChangeListener = new ProjectionChangeSupport.ProjectionChangeListener()
    {
        @Override
        public void projectionChanged(ProjectionChangedEvent evt)
        {
            myProjectionManager.notifyProjectionChangeListeners(evt, myUpdateExecutor);
            myCurrentViewer.get().validateViewerPosition();
        }
    };

    /** The current index into {@link #ourProjectionMap}. */
    private int myProjectionIndex = -1;

    /** Lock to manage concurrent access to the current projection. */
    private final ReentrantReadWriteLock myProjectionLock = new ReentrantReadWriteLock();

    /** Manager for projection stuff. */
    private final ProjectionManager myProjectionManager = new ProjectionManager();

    /** The viewer for screen position geometries. */
    private final ScreenViewer myScreenViewer = new ScreenViewer();

    /** The controller for saving and activating map manager states. */
    private MapManagerStateController myStateController;

    /** The manager for application wide state management. */
    private final ModuleStateManager myStateManager;

    /** The executor used for launching updates. */
    private final Executor myUpdateExecutor;

    /** The View book mark registry. */
    private final ViewBookmarkRegistryImpl myViewBookmarkRegistry;

    /** The manager for the viewer controllers. */
    private final ViewerControlManager myViewerControlManager;

    /** Manager for view stuff. */
    private final ViewerManager myViewerManager;

    /** The observer I use to know when the view has changed. */
    private final DynamicViewer.Observer myViewerObserver = new DynamicViewer.Observer()
    {
        @Override
        public void notifyViewChanged(ViewChangeSupport.ViewChangeType type)
        {
            myViewerManager.notifyViewChangeListeners(myUpdateExecutor, type);
        }
    };

    /**
     * Initialize the mappings of projections to viewers and viewers to control
     * translators.
     *
     * @param orderManager The manager for elevation orders to be used by
     *            projections created as a result of calling this.
     */
    @SuppressWarnings("unchecked")
    private static void initializeMappings(OrderManager orderManager)
    {
        final String configFilename = "viewerConfig.xml";
        ViewerConfigurations config = ViewerConfigurations.load(MapManagerImpl.class.getResource(configFilename));
        if (config == null)
        {
            return;
        }

        for (ViewerConfiguration viewerConfiguration : config.getViewerConfigurations())
        {
            String viewerClassname = viewerConfiguration.getClassname();

            Class<? extends AbstractDynamicViewer> viewerClass;
            try
            {
                viewerClass = (Class<? extends AbstractDynamicViewer>)Class.forName(viewerClassname);
            }
            catch (ClassNotFoundException | ClassCastException e)
            {
                LOGGER.error("Failed to load viewer class specified in " + configFilename + ": " + viewerClassname + " " + e, e);
                continue;
            }

            ViewControlTranslatorConfiguration vctConfig = viewerConfiguration.getViewControlTranslatorConfiguration();
            if (vctConfig != null)
            {
                String vctClassname = vctConfig.getClassname();
                Class<? extends ViewControlTranslator> vctClass;
                try
                {
                    vctClass = (Class<? extends ViewControlTranslator>)Class.forName(vctClassname);
                }
                catch (ClassNotFoundException | ClassCastException e)
                {
                    LOGGER.error("Failed to load view control translator class specified in " + configFilename + ": "
                            + vctClassname + " " + e, e);
                    continue;
                }

                ViewerControlManager.addViewerTypeToControlTranslatorType(viewerClass, vctClass);
            }

            for (AbstractProjection projection : viewerConfiguration.getProjections(orderManager))
            {
                ourProjectionMap.put(projection, viewerClass);
            }
        }
    }

    /**
     * Construct the map manager.
     *
     * @param uiRegistry Registry from UI related items.
     * @param eventManager The event manager.
     * @param prefsRegistry The system preferences registry.
     * @param unitsRegistry The units registry.
     * @param executor The executor to use for viewer/projection updates.
     * @param orderManager The order manager.
     * @param stateManager The module state manager.
     */
    public MapManagerImpl(Toolbox toolbox, UIRegistry uiRegistry, EventManager eventManager, PreferencesRegistry prefsRegistry,
            UnitsRegistry unitsRegistry, Executor executor, OrderManager orderManager, ModuleStateManager stateManager)
    {
        initializeMappings(orderManager);

        myToolbox = toolbox;
        myUpdateExecutor = executor;
        myDrawEnableSupport = new DrawEnableSupportExtension(myUpdateExecutor);
        myViewerControlManager = new ViewerControlManager(myCurrentViewer.getReadOnly(), this)
        {
            @Override
            protected void switchProjection()
            {
                MapManagerImpl.this.switchProjection();
            }
        };

        myMenuProvider = new MapManagerMenuProvider(this, uiRegistry.getContextActionManager(), unitsRegistry, eventManager);
        myMapMenuControl = new MapMenuControl(uiRegistry.getMenuBarRegistry(), eventManager, ourProjectionMap.keySet())
        {
            @Override
            protected void setCurrentProjection(Projection projection)
            {
                MapManagerImpl.this.setCurrentProjection(projection);
            }
        };
        myProjectionManager.getProjectionChangeSupport().addProjectionChangeListener(myMapMenuControl);
        myPreferences = prefsRegistry.getPreferences(MapManager.class);
        myViewerManager = new ViewerManager(myPreferences);
        myViewBookmarkRegistry = new ViewBookmarkRegistryImpl(prefsRegistry);

        myPreferenceHelper = new MapManagerPreferenceHelperImpl(prefsRegistry, uiRegistry.getOptionsRegistry());

        myStateManager = stateManager;
    }

    /**
     * Add listeners for events sent by a control context.
     *
     * @param context The control context.
     */
    public void addControlListeners(ControlContext context)
    {
        myViewerControlManager.addControlListeners(context);
    }

    @Override
    public void close()
    {
        myViewerControlManager.close();
        myStateManager.unregisterModuleStateController("Current View", myStateController);
        myMenuProvider.close();
        getStandardViewer().removeObserver(myViewerObserver);
        myProjectionManager.getProjectionChangeSupport().removeProjectionChangeListener(myMapMenuControl);
        myPreferenceHelper.close();
    }

    @Override
    public Vector2i convertToPoint(GeographicPosition position)
    {
        // Ensure the viewer is set before locking the lock to avoid dead lock.
        getStandardViewer();
        myProjectionLock.readLock().lock();
        try
        {
            Vector3d windowCoords = getStandardViewer()
                    .modelToWindowCoords(getProjection().convertToModel(position, Vector3d.ORIGIN));
            return new Vector2i((int)Math.round(windowCoords.getX()),
                    (int)Math.round(getStandardViewer().getViewportHeight() - windowCoords.getY()));
        }
        finally
        {
            myProjectionLock.readLock().unlock();
        }
    }

    @Override
    public GeographicPosition convertToPosition(Vector2i point, ReferenceLevel altReference)
    {
        // Ensure the viewer is set before locking the lock to avoid dead lock.
        getStandardViewer();
        myProjectionLock.readLock().lock();
        try
        {
            // This does a terrain intersection so that if the view is tilted
            // off the correct position will be given when the mouse is over the
            // side of mountains and such.
            Vector3d modelCoords = getStandardViewer().getTerrainIntersection(point);
            if (modelCoords == null)
            {
                return null;
            }
            else
            {
                return getProjection().convertToPosition(modelCoords, altReference);
            }
        }
        finally
        {
            myProjectionLock.readLock().unlock();
        }
    }

    @Override
    public Collection<ViewControlTranslator> getAllControlTranslators()
    {
        return myViewerControlManager.getAllControlTranslators();
    }

    @Override
    public synchronized ViewControlTranslator getCurrentControlTranslator()
    {
        return myViewerControlManager.getCurrentControlTranslator();
    }

    @Override
    public DrawEnableSupport getDrawEnableSupport()
    {
        return myDrawEnableSupport;
    }

    @Override
    public Projection getProjection()
    {
        myProjectionLock.readLock().lock();
        try
        {
            return myProjectionManager.getCurrentProjection().getSnapshot();
        }
        finally
        {
            myProjectionLock.readLock().unlock();
        }
    }

    @Override
    public Projection getProjection(Class<? extends AbstractDynamicViewer> viewerType)
    {
        for (Entry<Projection, Class<? extends AbstractDynamicViewer>> entry : ourProjectionMap.entrySet())
        {
            if (Viewer2D.class.isAssignableFrom(viewerType))
            {
                if (Viewer2D.class.isAssignableFrom(entry.getValue()))
                {
                    return entry.getKey();
                }
            }
            else if (Viewer3D.class.isAssignableFrom(viewerType) && Viewer3D.class.isAssignableFrom(entry.getValue()))
            {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Get the map manager's projection change listener, which will distribute
     * projection change events to listeners registered using
     * {@link MapManager#getProjectionChangeSupport()}. .
     *
     * @return The projection change listener.
     */
    public final ProjectionChangeSupport.ProjectionChangeListener getProjectionChangeListener()
    {
        return myProjectionChangeListener;
    }

    @Override
    public ProjectionChangeSupport getProjectionChangeSupport()
    {
        return myProjectionManager.getProjectionChangeSupport();
    }

    @Override
    public Map<Projection, Class<? extends AbstractDynamicViewer>> getProjections()
    {
        return ourProjectionMap;
    }

    @Override
    public Projection getRawProjection()
    {
        myProjectionLock.readLock().lock();
        try
        {
            return myProjectionManager.getCurrentProjection();
        }
        finally
        {
            myProjectionLock.readLock().unlock();
        }
    }

    @Override
    public ScreenViewer getScreenViewer()
    {
        return myScreenViewer;
    }

    @Override
    public DynamicViewer getStandardViewer()
    {
        DynamicViewer currentViewer = myCurrentViewer.get();
        if (currentViewer == null)
        {
            String preferredProjection = myPreferences.getString(PROJECTION_PREF_KEY, null);
            if (preferredProjection != null)
            {
                for (Projection proj : ourProjectionMap.keySet())
                {
                    if (proj.getName().equals(preferredProjection))
                    {
                        setCurrentProjection(proj);
                        myPreferenceHelper.setDefaultZoomRate();
                        break;
                    }
                }
            }
            currentViewer = myCurrentViewer.get();
        }
        if (currentViewer == null)
        {
            switchProjection();
            currentViewer = myCurrentViewer.get();
        }
        return currentViewer;
    }

    @Override
    public ViewBookmarkRegistry getViewBookmarkRegistry()
    {
        return myViewBookmarkRegistry;
    }

    @Override
    public ViewChangeSupport getViewChangeSupport()
    {
        return myViewerManager.getViewChangeSupport();
    }

    @Override
    public Class<? extends AbstractDynamicViewer> getViewerTypeForProjection(Projection proj)
    {
        return ourProjectionMap.get(proj);
    }

    @Override
    public List<GeographicPosition> getVisibleBoundaries()
    {
        if (myCurrentViewer.get() != null)
        {
            return VisibleBoundaryHelper.getVisibleBoundaries(getStandardViewer(), getProjection());
        }
        return null;
    }

    @Override
    public GeographicBoundingBox getVisibleBoundingBox()
    {
        if (myCurrentViewer.get() != null)
        {
            return VisibleBoundaryHelper.getVisibleBoundingBox(getStandardViewer(), getProjection());
        }
        return null;
    }

    /**
     * Remove all control listeners registered by this map manager.
     */
    public void removeControlListeners()
    {
        // TODO implement this if necessary.
    }

    @Override
    public void reshape(int width, int height)
    {
        getScreenViewer().reshape(width, height);
        getStandardViewer().reshape(width, height);
    }

    @Override
    public void setProjection(Class<? extends AbstractDynamicViewer> viewer)
    {
        Projection proj = getProjection(viewer);
        if (proj != null)
        {
            setCurrentProjection(proj);
        }
    }

    /**
     * Set the current projection and notify listeners of the change.
     *
     * @param currentProjection The new projection.
     */
    protected synchronized void setCurrentProjection(Projection currentProjection)
    {
        // Send an event before changing the projection so that the screen
        // can be cleared, to avoid any visible artifacts.
        myDrawEnableSupport.notifyDrawEnableListeners(false);

        List<GeographicPosition> boundaryPoints = getVisibleBoundaries();
        GeographicPosition centroid = getViewerGeographicPosition();

        myProjectionLock.writeLock().lock();
        try
        {
            Projection oldProjection = myProjectionManager.getCurrentProjection();
            if (oldProjection != null)
            {
                if (oldProjection instanceof AbstractProjection)
                {
                    AbstractProjection old = (AbstractProjection)oldProjection;
                    old.setProjectionChangeListener(null);
                }
                if (oldProjection instanceof ViewChangeSupport.ViewChangeListener)
                {
                    myViewerManager.removeViewChangeListener((ViewChangeListener)oldProjection);
                }
            }

            if (currentProjection instanceof AbstractProjection)
            {
                ((AbstractProjection)currentProjection).setProjectionChangeListener(getProjectionChangeListener());
            }

            myProjectionManager.setCurrentProjection(currentProjection);
        }
        finally
        {
            myProjectionLock.writeLock().unlock();
        }
        if (currentProjection instanceof ViewChangeSupport.ViewChangeListener)
        {
            getViewChangeSupport().addViewChangeListener((ViewChangeListener)currentProjection);
        }
        switchViewer(currentProjection);

        myProjectionManager.notifyProjectionChangeListeners(
                new ProjectionChangedEvent(currentProjection, currentProjection.getSnapshot(), true), myUpdateExecutor);
        myDrawEnableSupport.notifyDrawEnableListeners(true);

        locateViewer(boundaryPoints, centroid);

        int index = 0;
        for (Projection projection : ourProjectionMap.keySet())
        {
            if (Utilities.sameInstance(projection, currentProjection))
            {
                myProjectionIndex = index;
                break;
            }
            ++index;
        }

        myPreferences.putString(PROJECTION_PREF_KEY, currentProjection.getName(), this);

        /* Initialization of the state controller causes the projection to be
         * set too soon. This causes color issues with the main tool menu bar.
         * For that reason, initialization of the state controller is delayed
         * until the first time the projection is set. */
        if (myStateController == null)
        {
            myStateController = new MapManagerStateController(this);
            myStateManager.registerModuleStateController("Current View", myStateController);
        }
    }

    /**
     * Switch projections to the next available one.
     */
    protected synchronized void switchProjection()
    {
        int size = ourProjectionMap.size();
        if (++myProjectionIndex >= size)
        {
            myProjectionIndex = 0;
        }
        setCurrentProjection(ourProjectionMap.keySet().toArray(new AbstractProjection[size])[myProjectionIndex]);
    }

    /**
     * Switch the viewer to match a new projection.
     *
     * @param projection The new projection.
     */
    protected synchronized void switchViewer(Projection projection)
    {
        float modelWidth = (float)projection.getSnapshot().getModelWidth();
        float modelHeight = (float)projection.getSnapshot().getModelHeight();

        Class<? extends AbstractDynamicViewer> viewerType = ourProjectionMap.get(projection);
        myViewerManager.switchViewer(viewerType, modelWidth, modelHeight, myViewerObserver);
        DynamicViewer currentViewer = myViewerManager.getCurrentViewer();
        currentViewer.setMapContext(this);
        myCurrentViewer.set(currentViewer);
        myViewerControlManager.setCurrentViewerType(currentViewer.getClass());

        myViewerManager.notifyViewChangeListeners(myUpdateExecutor, ViewChangeSupport.ViewChangeType.NEW_VIEWER);
    }

    /**
     * Get the geographic position which the viewer is over.
     *
     * @return The viewer's geographic position.
     */
    private GeographicPosition getViewerGeographicPosition()
    {
        DynamicViewer viewer = myCurrentViewer.get();
        if (viewer != null)
        {
            return getProjection().convertToPosition(viewer.getClosestModelPosition(), ReferenceLevel.ELLIPSOID);
        }
        return null;
    }

    /**
     * Move the viewer to be centered and zoomed to the boundary points.
     *
     * @param boundaryPoints The boundary points to zoom to.
     * @param centroid A hint to help center the viewer when the boundary points
     *            are geographically diverse.
     */
    private void locateViewer(List<GeographicPosition> boundaryPoints, GeographicPosition centroid)
    {
        DynamicViewer viewer = myCurrentViewer.get();
        if (boundaryPoints != null && !boundaryPoints.isEmpty() && centroid != null && viewer != null)
        {
            ViewerAnimator viewerAnimator = new ViewerAnimator(viewer, boundaryPoints, centroid, true);
            viewerAnimator.snapToPosition();
        }
    }

    /**
     * Extension to draw enable support that allows us to call notify.
     */
    private static final class DrawEnableSupportExtension extends DrawEnableSupport
    {
        /**
         * Constructor.
         *
         * @param executor The executor.
         */
        public DrawEnableSupportExtension(Executor executor)
        {
            super(executor);
        }

        @Override
        @SuppressWarnings("PMD.UselessOverridingMethod")
        protected void notifyDrawEnableListeners(boolean drawEnable)
        {
            super.notifyDrawEnableListeners(drawEnable);
        }
    }

    /**
     * Implementation of the preference helper which handles the changes to the
     * preferences.
     */
    private final class MapManagerPreferenceHelperImpl extends MapManagerPreferenceHelper
    {
        /**
         * Constructor.
         *
         * @param prefsRegistry The system preferences registry.
         * @param optionsRegistry The options registry.
         */
        public MapManagerPreferenceHelperImpl(PreferencesRegistry prefsRegistry, OptionsRegistry optionsRegistry)
        {
            super(myToolbox, prefsRegistry, optionsRegistry);
        }

        @Override
        void handleModelDensityChanged(int density)
        {
            for (Projection proj : ourProjectionMap.keySet())
            {
                proj.handleModelDensityChanged(density);
            }
        }

        @Override
        void handleZoomRateChanged(int rate)
        {
            myViewerControlManager.getCurrentControlTranslator().setZoomRate(rate);
        }
    }
}
