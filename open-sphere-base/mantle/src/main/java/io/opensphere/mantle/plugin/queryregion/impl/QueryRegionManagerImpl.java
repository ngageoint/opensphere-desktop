package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.DataRemovalEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.event.DataTypeInfoFocusEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.TypeFocusEvent.FocusType;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionListener;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.mantle.plugin.selection.SelectionCommand;
import io.opensphere.mantle.plugin.selection.SelectionCommandProcessor;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Implementation for {@link QueryRegionManager}.
 */
public class QueryRegionManagerImpl extends EventListenerService implements QueryRegionManager, SelectionCommandProcessor
{
    /** Change support for query region listeners. */
    private final ChangeSupport<QueryRegionListener> myChangeSupport = WeakChangeSupport.create();

    /** The Delete context menu provider. */
    private final ContextMenuProvider<Void> myDeleteContextMenuProvider = new ContextMenuProvider<Void>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, Void key)
        {
            List<JMenuItem> list = New.list();
            JMenuItem cancelAllQueriesMenuItem = new JMenuItem("Cancel all queries");
            cancelAllQueriesMenuItem.addActionListener(e -> cancelAllQueries());
            list.add(cancelAllQueriesMenuItem);

            JMenuItem removeQueriesMenuItem = new JMenuItem("Delete all queries and queried features");
            removeQueriesMenuItem.addActionListener(e -> removeAllQueryRegions());
            list.add(removeQueriesMenuItem);
            return list;
        }

        @Override
        public int getPriority()
        {
            return 0;
        }
    };

    /** The executor used for launching updates. */
    private final ExecutorService myDispatchExecutor = Executors
            .newSingleThreadExecutor(new NamedThreadFactory("QueryRegionManager-dispatch"));

    /** The event listener used to register the module state controller. */
    private EventListener<? super ApplicationLifecycleEvent> myListener;

    /** Controller for query region state. */
    private final ModuleStateController myModuleStateController;

    /** The query regions. */
    private final Set<QueryRegion> myQueryRegions;

    /** Event listener that triggers removal of all query regions. */
    private final EventListener<DataRemovalEvent> myRemoveAllEventListener;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new query region manager impl.
     *
     * @param tb the tb
     * @param dataGroupController the data group controller
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public QueryRegionManagerImpl(Toolbox tb, DataGroupController dataGroupController)
    {
        super(tb.getEventManager());
        myToolbox = tb;
        myModuleStateController = new QueryRegionStateController(this, tb);
        myQueryRegions = New.set();
        myRemoveAllEventListener = event -> removeAllQueryRegions();
        myToolbox.getEventManager().subscribe(DataRemovalEvent.class, myRemoveAllEventListener);
        myToolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(ContextIdentifiers.DELETE_CONTEXT,
                Void.class, myDeleteContextMenuProvider);

        addService(
                new QueryRegionPickHandler(myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT),
                        myToolbox.getEventManager(), dataGroupController, this::getQueryRegions));

        addService(new QueryRegionSelectionHandler(myToolbox.getUIRegistry().getContextActionManager(), dataGroupController,
                myToolbox.getUIRegistry().getMainFrameProvider(), myToolbox.getDataFilterRegistry(), this::getQueryRegions));

        // Register the state manager after all the other plugins so that
        // queries will get activated after other layers.
        myListener = new EventListener<ApplicationLifecycleEvent>()
        {
            @Override
            public void notify(ApplicationLifecycleEvent event)
            {
                if (event.getStage() == ApplicationLifecycleEvent.Stage.PLUGINS_INITIALIZED)
                {
                    myToolbox.getModuleStateManager().registerModuleStateController("Query Areas", myModuleStateController);
                    myToolbox.getEventManager().unsubscribe(ApplicationLifecycleEvent.class, myListener);
                    myListener = null;
                }
            }
        };
        myToolbox.getEventManager().subscribe(ApplicationLifecycleEvent.class, myListener);

        bindEvent(DataTypeInfoFocusEvent.class, this::handleDataTypeInfoFocusEvent);
    }

    /**
     * Handles a data type focus change.
     *
     * @param event the event
     */
    private void handleDataTypeInfoFocusEvent(DataTypeInfoFocusEvent event)
    {
        if (event.getSource() == this)
        {
            return;
        }
        Collection<? extends DataTypeInfo> types = event.getTypes();
        synchronized (myQueryRegions)
        {
            if (event.getFocusType() == FocusType.HOVER_GAINED && types.stream().anyMatch(t -> t.isQueryable()))
            {
                myQueryRegions.forEach(r -> r.getGeometries().forEach(
                    g -> g.getRenderProperties().setHidden(!types.stream().anyMatch(t -> r.appliesToType(t.getTypeKey())))));
            }
            else
            {
                myQueryRegions.forEach(r -> r.getGeometries().forEach(g -> g.getRenderProperties().setHidden(false)));
            }
        }
    }

    @Override
    public QueryRegion addQueryRegion(Collection<? extends PolygonGeometry> polygons, Collection<? extends String> typeKeys)
    {
        return addQueryRegion(polygons, Collections.singleton(TimeSpan.TIMELESS), getFilterMap(typeKeys));
    }

    @Override
    public QueryRegion addQueryRegion(Collection<? extends PolygonGeometry> polygons, Collection<? extends TimeSpan> validTimes,
            Map<? extends String, ? extends DataFilter> typeKeyToFilterMap)
    {
        QueryRegion region = new DefaultQueryRegion(Utilities.checkNull(polygons, "polygons"),
                Utilities.checkNull(validTimes, "validTimes"), typeKeyToFilterMap);
        addQueryRegion(region);
        return region;
    }

    @Override
    public QueryRegion addQueryRegion(Collection<? extends PolygonGeometry> polygons,
            Map<? extends String, ? extends DataFilter> typeKeyToFilterMap)
    {
        return addQueryRegion(polygons, Collections.singleton(TimeSpan.TIMELESS), typeKeyToFilterMap);
    }

    @Override
    public void addQueryRegion(QueryRegion region)
    {
        synchronized (myQueryRegions)
        {
            myQueryRegions.add(region);
        }
        Collection<PolygonGeometry> geometries = New.collection(region.getGeometries().size());
        for (PolygonGeometry polygon : region.getGeometries())
        {
            if (polygon.getRenderProperties().isDrawable() || polygon.getRenderProperties().isPickable())
            {
                geometries.add(polygon);
            }
        }
        myToolbox.getGeometryRegistry().addGeometriesForSource(this, geometries);

        myChangeSupport.notifyListeners(listener -> listener.queryRegionAdded(region), myDispatchExecutor);
    }

    @Override
    public void addQueryRegionListener(QueryRegionListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Get the type keys for the data types that can be queried.
     *
     * @return The type keys.
     */
    public Collection<? extends String> getQueryableDataTypeKeys()
    {
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController().getQueryableDataTypeKeys();
    }

    @Override
    public QueryRegion getQueryRegion(Geometry geom)
    {
        return getQueryRegions().stream().filter(r -> r.getGeometries().contains(geom)).findAny().orElse(null);
    }

    @Override
    public List<? extends QueryRegion> getQueryRegions()
    {
        synchronized (myQueryRegions)
        {
            return New.unmodifiableList(myQueryRegions);
        }
    }

    @Override
    public final void removeAllQueryRegions()
    {
        synchronized (myQueryRegions)
        {
            removeAllQueryBoundsFromGeometryRegistry();
            myQueryRegions.clear();
        }

        myChangeSupport.notifyListeners(listener -> listener.allQueriesRemoved(false), myDispatchExecutor);
    }

    @Override
    public void removeQueryRegion(Collection<? extends Geometry> bounds)
    {
        synchronized (myQueryRegions)
        {
            for (QueryRegion region : myQueryRegions)
            {
                if (region.getGeometries().containsAll(bounds))
                {
                    removeQueryRegion(region);
                    break;
                }
            }
        }
    }

    @Override
    public boolean removeQueryRegion(final QueryRegion region)
    {
        Utilities.checkNull(region, "region");
        boolean removed;
        synchronized (myQueryRegions)
        {
            removed = myQueryRegions.remove(region);
        }
        if (removed)
        {
            removeQueryBoundsFromGeometryRegistry(region);
            myChangeSupport.notifyListeners(listener -> listener.queryRegionRemoved(region), myDispatchExecutor);
        }
        return removed;
    }

    @Override
    public void removeQueryRegionListener(QueryRegionListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void removeQueryRegions(Collection<? extends QueryRegion> regions)
    {
        for (QueryRegion queryRegion : regions)
        {
            removeQueryRegion(queryRegion);
        }
    }

    @Override
    public void selectionOccurred(Collection<? extends PolygonGeometry> bounds, SelectionCommand cmd)
    {
        switch (cmd)
        {
            case ADD_FEATURES:
                addQueryRegion(deriveQueryPolygons(bounds, false), getFilterMap());
                break;
            case ADD_FEATURES_CURRENT_FRAME:
                addQueryRegion(deriveQueryPolygons(bounds, true), myToolbox.getTimeManager().getPrimaryActiveTimeSpans(),
                        getFilterMap());
                break;
            case LOAD_FEATURES:
                removeAllQueryRegions();
                addQueryRegion(deriveQueryPolygons(bounds, false), getFilterMap());
                break;
            case LOAD_FEATURES_CURRENT_FRAME:
                removeAllQueryRegions();
                addQueryRegion(deriveQueryPolygons(bounds, true), myToolbox.getTimeManager().getPrimaryActiveTimeSpans(),
                        getFilterMap());
                break;
            case CANCEL_QUERY:
                removeQueryRegion(bounds);
                break;
            default:
                break;
        }
    }

    /**
     * Get a map of data type keys to the data filters associated with them.
     *
     * @return The map.
     */
    protected Map<String, DataFilter> getFilterMap()
    {
        return getFilterMap(getQueryableDataTypeKeys());
    }

    /**
     * Get a map of data type keys to the data filters associated with them.
     *
     * @param keys The keys to put in the map.
     * @return The map.
     */
    protected Map<String, DataFilter> getFilterMap(Collection<? extends String> keys)
    {
        Map<String, DataFilter> filterMap = New.map();
        for (String key : keys)
        {
            filterMap.put(key, myToolbox.getDataFilterRegistry().getLoadFilter(key));
        }
        return filterMap;
    }

    /**
     * Cancel all queries.
     */
    private void cancelAllQueries()
    {
        synchronized (myQueryRegions)
        {
            removeAllQueryBoundsFromGeometryRegistry();
            myQueryRegions.clear();
        }
    }

    /**
     * Derive some orange query polygons from the input polygons.
     *
     * @param input The input polyons.
     * @param dotted If the polygons' lines should be dotted.
     * @return The output polygons.
     */
    private Collection<? extends PolygonGeometry> deriveQueryPolygons(Collection<? extends PolygonGeometry> input, boolean dotted)
    {
        // Do not derive new polygons if the input polygons are already known.
        // This used to check against getQueryRegions().containsAll(input) but
        // that makes absolutely no dang sense.
        if (getQueryRegions().stream().filter((region) -> region.getGeometries().containsAll(input)).count() > 0)
        {
            return input;
        }

        Collection<PolygonGeometry> output = New.collection(input.size());

        for (PolygonGeometry geom : input)
        {
            PolygonRenderProperties props = new DefaultPolygonRenderProperties(ZOrderRenderProperties.TOP_Z, true, true);
            props.setColor(Colors.QUERY_REGION);
            props.setWidth(geom.getRenderProperties().getWidth());
            if (dotted)
            {
                props.setStipple(StippleModelConfig.DOTTED);
            }

            output.add(geom.derive(props, (Constraints)null));
        }

        return output;
    }

    /**
     * Removes all my geometries from geometry registry.
     */
    private void removeAllQueryBoundsFromGeometryRegistry()
    {
        myToolbox.getGeometryRegistry().removeGeometriesForSource(this);
    }

    /**
     * Removes the geometries for the given region from geometry registry.
     *
     * @param region The region.
     */
    private void removeQueryBoundsFromGeometryRegistry(QueryRegion region)
    {
        myToolbox.getGeometryRegistry().removeGeometriesForSource(this, new HashSet<Geometry>(region.getGeometries()));
    }
}
