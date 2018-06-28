package io.opensphere.wfs;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.jcip.annotations.GuardedBy;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.AnimationChangeAdapter;
import io.opensphere.core.AnimationChangeListener;
import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.TimeManager.ActiveTimeSpans;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.matcher.GeneralIntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.data.util.QueryTracker.QueryTrackerListener;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanArrayList;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.ConcurrentLazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.core.util.time.DateDurationKey;
import io.opensphere.core.util.time.TimelineUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionListener;
import io.opensphere.mantle.plugin.queryregion.impl.DefaultQueryRegion;
import io.opensphere.wfs.envoy.AbstractWFSEnvoy;
import io.opensphere.wfs.envoy.WFSDataRegistryHelper;
import io.opensphere.wfs.layer.SingleLayerRequeryEvent;
import io.opensphere.wfs.layer.SingleLayerRequeryEvent.RequeryType;
import io.opensphere.wfs.layer.WFSDataType;

/**
 * Responsible for listening for query regions and performing the necessary
 * requests against the data registry.
 */
@SuppressWarnings("PMD.GodClass")
public class WFSControls
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WFSControls.class);

    /** Listener for changes to the animation plan. */
    private final AnimationChangeListener myAnimationListener = new AnimationChangeAdapter()
    {
        @Override
        public void animationPlanCancelled()
        {
        }

        @Override
        public void animationPlanEstablished(io.opensphere.core.animation.AnimationPlan plan)
        {
            // If the plan coverage was bigger when the active spans were
            // changed, clear the envoy features here.
            TimeSpanList coverageBefore = myAnimationPlanCoverage;
            TimeSpanList coverageNow = new TimeSpanArrayList(plan.getTimeCoverage());
            myAnimationPlanCoverage = coverageNow;
            if (coverageBefore == null || coverageNow.equals(myTimeManager.getActiveTimeSpans().getPrimary())
                    && !coverageNow.getExtent().contains(coverageBefore.getExtent()))
            {
                clearEnvoyFeatures(true);
                requestAndActivateFeatures(false);
            }
        }

        @Override
        public boolean prepare(AnimationState pendingState, Phaser phaser)
        {
            synchronized (myQueryStatusListeners)
            {
                if (!myQueryStatusListeners.isEmpty())
                {
                    phaser.register();
                    // set the phaser ...
                    myWaitForQueriesPhaser = phaser;
                    // ... on "stun".
                }
            }
            return true;
        }
    };

    /** Listener for QueryRegion callbacks. */
    private final QueryRegionListener regionEar = new QueryRegionListener()
    {
        @Override
        public void allQueriesRemoved(boolean cancelled)
        {
            clearEnvoyFeatures(cancelled);
        }

        @Override
        public void queryRegionAdded(QueryRegion region)
        {
            requestAndActivateFeatures(region);
        }

        @Override
        public void queryRegionRemoved(QueryRegion region)
        {
            Set<QueryTracker> trackerSet = New.set();
            synchronized (myQueryStatusListeners)
            {
                myQueryStatusListeners.stream().filter(x -> x.getQueryRegion().equals(region))
                        .forEach(x -> trackerSet.add(x.getTracker()));
            }
            for (QueryTracker tracker : trackerSet)
            {
                tracker.cancel(true);
            }
        }
    };

    /** Listener for ... see below. */
    private final EventListener<SingleLayerRequeryEvent> requeryEar = this::notifyRequery;

    /** The animation manager. */
    private final AnimationManager myAnimationManager;

    /** Animation plan coverage the last time the active spans were changed. */
    private volatile TimeSpanList myAnimationPlanCoverage;

    /** The data filter registry. */
    private final DataFilterRegistry myDataFilterRegistry;

    /** The registry for keeping data models. */
    private final DataRegistry myDataRegistry;

    /** Listener for changes to the WFS Types. */
    private final DataRegistryListener<WFSDataType> myDataRegistryListener = new DataRegistryListenerAdapter<WFSDataType>()
    {
        @Override
        public void allValuesRemoved(Object source)
        {
            myWFSTypes.clear();
        }

        @Override
        public boolean isIdArrayNeeded()
        {
            return false;
        }

        @Override
        public boolean isWantingRemovedObjects()
        {
            return true;
        }

        @Override
        public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends WFSDataType> newValues,
                Object source)
        {
            newValues.spliterator().forEachRemaining(t -> myWFSTypes.add(t));
        }

        @Override
        public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends WFSDataType> removedValues,
                Object source)
        {
            // Cancel any active queries for the types that have been removed.
            Map<String, QueryTracker> trackerMap = New.map();
            synchronized (myQueryStatusListeners)
            {
                for (ControlsTrackerListener listener : myQueryStatusListeners)
                {
                    trackerMap.put(listener.getTracker().getQuery().getDataModelCategory().getCategory(), listener.getTracker());
                }
            }

            for (WFSDataType type : removedValues)
            {
                QueryTracker tracker = trackerMap.get(type.getTypeKey());
                if (tracker != null)
                {
                    tracker.cancel(true);
                }
                myQueryMap.remove(typeCat(type));
                myWFSTypes.remove(type);
            }
        }
    };

    /** Listener for display interval changes. */
    private final ActiveTimeSpanChangeListener myDisplayIntervalChangeListener = new ActiveTimeSpanChangeListener()
    {
        @Override
        public void activeTimeSpansChanged(ActiveTimeSpans active)
        {
            AnimationPlan plan = myAnimationManager.getCurrentPlan();
            if (plan != null)
            {
                myAnimationPlanCoverage = new TimeSpanArrayList(plan.getTimeCoverage());
                if (myAnimationPlanCoverage.equals(active.getPrimary()))
                {
                    clearEnvoyFeatures(true);
                }
            }
            requestAndActivateFeatures(plan == null);
        }
    };

    /** My envoy registry. */
    private final GenericRegistry<Envoy> myEnvoyRegistry;

    /** This executes the time change tasks. */
    private final ScheduledExecutorService myExecutor = Executors
            .newSingleThreadScheduledExecutor(new NamedThreadFactory("WFSControls"));

    /** Future to keep track of the task scheduled on {@link #myExecutor}. */
    private volatile ScheduledFuture<?> myFuture;

    /** My mantle tb. */
    private final MantleToolbox myMantleTb;

    /** A map to keep track of the queries. */
    private final Map<DataModelCategory, Set<Query>> myQueryMap = ConcurrentLazyMap.create(
            new ConcurrentHashMap<DataModelCategory, Set<Query>>(), DataModelCategory.class, k -> Collections.synchronizedSet(New.<Query>set()));

    /** Listener for changes to the status of queries. */
    private final Collection<ControlsTrackerListener> myQueryStatusListeners = New.collection();

    /** A task to request and activate features for the current time. */
    private final Runnable myRequestAndActivateFeaturesTask = new Runnable()
    {
        @Override
        public void run()
        {
            for (QueryRegion region : myMantleTb.getQueryRegionManager().getQueryRegions())
            {
                requestAndActivateFeatures(region);
            }

            // Query whole-world layers for the current time
            synchronized (myWFSTypes)
            {
                for (WFSDataType t : myWFSTypes)
                {
                    if (t.isVisible() && !t.isQueryable() && !t.isAnimationSensitive())
                    {
                        requestAndActivateFeatures(t, new WorldQueryRegion(t,
                                myDataFilterRegistry.getLoadFilter(t.getTypeKey())));
                    }
                }
            }
        }
    };

    /** The time manager. */
    private final TimeManager myTimeManager;

    /** For waiting on queries to be completed. */
    @GuardedBy("myQueryStatusListeners")
    private Phaser myWaitForQueriesPhaser;

    /** The current collection of WFSTypes. */
    private final Collection<WFSDataType> myWFSTypes = Collections.synchronizedCollection(New.<WFSDataType>collection());

    /**
     * Construct the WFSControls.
     *
     * @param toolbox the core toolbox
     */
    public WFSControls(Toolbox toolbox)
    {
        myDataRegistry = toolbox.getDataRegistry();
        myTimeManager = toolbox.getTimeManager();
        myEnvoyRegistry = toolbox.getEnvoyRegistry();
        myAnimationManager = toolbox.getAnimationManager();
        myDataFilterRegistry = toolbox.getDataFilterRegistry();
        myMantleTb = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);

        myAnimationManager.addAnimationChangeListener(myAnimationListener);
        myTimeManager.addActiveTimeSpanChangeListener(myDisplayIntervalChangeListener);
        myMantleTb.getQueryRegionManager().addQueryRegionListener(regionEar);

        toolbox.getEventManager().subscribe(SingleLayerRequeryEvent.class, requeryEar);
        myDataRegistry.addChangeListener(myDataRegistryListener, new DataModelCategory(null, WFSDataType.class.getName(), null),
                WFSDataType.WFS_PROPERTY_DESCRIPTOR);
    }

    /**
     * Clean up my registered listeners.
     */
    public void close()
    {
        if (myTimeManager != null)
        {
            myTimeManager.removeActiveTimeSpanChangeListener(myDisplayIntervalChangeListener);
        }
        if (myMantleTb != null)
        {
            myMantleTb.getQueryRegionManager().removeQueryRegionListener(regionEar);
        }
    }

    /**
     * Event handler method for occurrences of SingleLayerRequeryEvent.
     * @param event bla
     */
    private void notifyRequery(SingleLayerRequeryEvent event)
    {
        WFSDataType type = event.getDataTypeInfo();
        if (type.isAnimationSensitive())
        {
            return;
        }
        String key = type.getTypeKey();

        // If the request was for a FULL_REQUERY, remove existing data first.
        if (event.getRequeryType().equals(RequeryType.FULL_REQUERY))
        {
            myMantleTb.getDataTypeController().removeDataType(key, this);
            DataModelCategory queryCat = typeCat(type);
            getDataRegistry().removeModels(queryCat, false);
            myQueryMap.remove(queryCat);
        }

        DataFilter fil = myDataFilterRegistry.getLoadFilter(key);
        if (type.isQueryable())
        {
            // Create new regions with updated filters for the layer
            for (QueryRegion qr : myMantleTb.getQueryRegionManager().getQueryRegions())
            {
                if (qr.appliesToType(key))
                {
                    requestAndActivateFeatures(type, new DefaultQueryRegion(qr.getGeometries(),
                            qr.getValidTimes(), Collections.singletonMap(key, fil)));
                }
            }
        }
        else if (type.isVisible())
        {
            requestAndActivateFeatures(type, new WorldQueryRegion(type, fil));
        }
    }

    /**
     * Iterates through all WFS Envoys and clears the features that they've
     * queried.
     *
     * @param timedOnly If true, only remove layers that have a time extent
     */
    private void clearEnvoyFeatures(boolean timedOnly)
    {
        // Get the set of Active timed queries that need to be cancelled
        List<QueryTracker> trackers;
        synchronized (myQueryStatusListeners)
        {
            if (timedOnly)
            {
                trackers = myQueryStatusListeners.stream().filter(x -> isTimed(x))
                    .map(x -> x.getTracker()).collect(Collectors.toList());
            }
            else
            {
                trackers = myQueryStatusListeners.stream()
                    .map(x -> x.getTracker()).collect(Collectors.toList());
            }
        }

        for (QueryTracker tracker : trackers)
        {
            tracker.cancel(true);
        }
        for (AbstractWFSEnvoy envoy : getEnvoys())
        {
            envoy.dumpFeatures(timedOnly, false);
        }

        myQueryMap.clear();
    }

    /**
     * See if the argument contains any PropertyMatcher with a time bound.
     * @param ctl bla
     * @return bla
     */
    private static boolean isTimed(ControlsTrackerListener ctl)
    {
        return ctl.getTracker().getQuery().getParameters().stream().anyMatch(p -> isTimed(p));
    }

    /**
     * See if a PropertyMatcher has a time bound.
     * @param pm bla
     * @return bla
     */
    private static boolean isTimed(PropertyMatcher<?> pm)
    {
        return pm instanceof TimeSpanMatcher && (TimeSpan)pm.getOperand() != TimeSpan.TIMELESS;
    }

    /**
     * For test support only:  provide access to the dataRegistry.
     *
     * @return The dataRegistry.
     */
    public DataRegistry getDataRegistry()
    {
        return myDataRegistry;
    }

    /**
     * Gets the envoys.
     *
     * @return The envoys.
     */
    private Collection<AbstractWFSEnvoy> getEnvoys()
    {
        return myEnvoyRegistry.getObjectsAssignableToClass(AbstractWFSEnvoy.class);
    }

    /**
     * Execute the request and activate features task on my executor.
     *
     * @param procrastinate If we should wait a bit.
     */
    private void requestAndActivateFeatures(boolean procrastinate)
    {
        if (myFuture != null)
        {
            myFuture.cancel(false);
        }

        if (procrastinate)
        {
            myFuture = myExecutor.schedule(myRequestAndActivateFeaturesTask, 1, TimeUnit.SECONDS);
        }
        else
        {
            myExecutor.execute(myRequestAndActivateFeaturesTask);
        }
    }

    /**
     * Request features for a single geometry, time span, and WFS type from the
     * registry.
     *
     * @param geom The geometry.
     * @param ivl The time span.
     * @param type The WFS type.
     * @param reg The query region that the geometry came from.
     */
    private void requestAndActivateFeatures(Polygon geom, TimeSpan ivl,
            WFSDataType type, QueryRegion reg)
    {
        if (!new TimeSpanArrayList(reg.getValidTimes()).intersects(ivl))
        {
            return;
        }
        DataModelCategory cat = typeCat(type);
        doStuff(myQueryMap.get(cat), ivl, geom, regionFilter(reg, type), cat, reg);
    }

    /**
     * Issue queries for any portions of the requested interval that have not
     * previously been loaded and update the query cache to reflect the change.
     * @param cache list of Queries for this data type
     * @param ivl the requested time interval
     * @param geom the requested geographical region
     * @param fil the applicable filter
     * @param cat DataModelCategory
     * @param reg also the requested geographical region (with bells on)
     */
    private void doStuff(Set<Query> cache, TimeSpan ivl, Polygon geom,
            DataFilter fil, DataModelCategory cat, QueryRegion reg)
    {
        // filter down to the set of those matching both geometry and filter
        // and intersecting the time span
        List<Query> seq = cache.stream()
                .filter(q -> equiv(geom, qGeom(q)) && equiv(fil, qFilter(q)))
                .filter(q -> ivl.overlaps(qTimeSpan(q)))
                .collect(Collectors.toList());
        // the new one is already covered, no action is required
        for (Query cq :  seq)
        {
            if (qTimeSpan(cq).contains(ivl))
            {
                return;
            }
        }

        // calculate the union (for the cache) and difference (to fetch)
        TimeSpan u = ivl;
        List<TimeSpan> diff = new LinkedList<>();
        diff.add(ivl);
        for (Query q :  seq)
        {
            TimeSpan qIvl = qTimeSpan(q);
            u = u.union(qIvl);
            diff = subtract(diff, qIvl);
        }

        // update the query cache
        cache.removeAll(seq);
        cache.add(formQuery(geom, u, cat, fil));

        // construct and submit queries for the required intervals
        for (TimeSpan qIvl :  diff)
        {
            fetchData(formQuery(geom, qIvl, cat, fil), reg);
        }
    }

    /**
     * Issue a single Query and register a callback for results.
     * @param q the Query
     * @param qReg the geographical region (with bells on)
     */
    private void fetchData(Query q, QueryRegion qReg)
    {
        QueryTracker qt = myDataRegistry.submitQuery(q);
        ControlsTrackerListener ear = new ControlsTrackerListener(qt, qReg);
        qt.addListener(ear);
        synchronized (myQueryStatusListeners)
        {
            myQueryStatusListeners.add(ear);
        }
    }

    /**
     * Perform interval subtraction.  Here, <i>times</i> is presumed to be a
     * collection of non-trivial, non-overlapping, non-contiguous intervals
     * representing a subset of the timeline.  The result has the same
     * structure and represents the set difference <i>times</i> - <i>cut</i>.
     * If <i>times</i> is arranged in chronological order, then that property
     * is preserved in the result.
     * @param times the initial set
     * @param cut the set to be removed
     * @return the set difference
     */
    private static List<TimeSpan> subtract(List<TimeSpan> times, TimeSpan cut)
    {
        TimeInstant c0 = cut.getStartInstant();
        TimeInstant c1 = cut.getEndInstant();
        LinkedList<TimeSpan> ret = new LinkedList<>();
        for (TimeSpan ivl :  times)
        {
            // trivial case of cut containing the interval => gone, daddy, gone
            if (cut.contains(ivl))
            {
                continue;
            }
            // trivial case of cut not intersecting the interval => no change
            if (!cut.overlaps(ivl))
            {
                ret.add(ivl);
                continue;
            }
            // non-trivial cases:  keep the start, keep the end, keep both
            TimeInstant i0 = ivl.getStartInstant();
            TimeInstant i1 = ivl.getEndInstant();
            if (i0.isBefore(c0))
            {
                ret.add(TimeSpan.get(i0, c0));
            }
            if (c1.isBefore(i1))
            {
                ret.add(TimeSpan.get(c1, i1));
            }
        }
        return ret;
    }

    /**
     * Extract the time span from a Query.
     * @param q bla
     * @return bla
     */
    private static TimeSpan qTimeSpan(Query q)
    {
        return ((TimeSpanMatcher)getParam(
                q, TimeSpanAccessor.TIME_PROPERTY_NAME)).getOperand();
    }

    /**
     * Extract the geometry from a Query.
     * @param q bla
     * @return bla
     */
    private static Polygon qGeom(Query q)
    {
        return (Polygon)((GeometryMatcher)getParam(
                q, GeometryAccessor.GEOMETRY_PROPERTY_NAME)).getOperand();
    }

    /**
     * Extract the filter from a Query, if present.
     * @param q bla
     * @return bla
     */
    private static DataFilter qFilter(Query q)
    {
        PropertyMatcher<?> pm = getParam(q, WFSDataRegistryHelper.FILTER_PROP_KEY);
        if (pm == null)
        {
            return null;
        }
        return (DataFilter)pm.getOperand();
    }

    /**
     * Extract a named property "matcher" from a Query, if present.
     * @param q the Query
     * @param key the property name
     * @return the "matcher", if found, or null
     */
    private static PropertyMatcher<?> getParam(Query q, String key)
    {
        for (PropertyMatcher<?> p :  q.getParameters())
        {
            if (key.equals(p.getPropertyDescriptor().getPropertyName()))
            {
                return p;
            }
        }
        return null;
    }

    /**
     * Get the filter applicable to a WFSDataType for the specified
     * geographical region (with bells on).
     * @param reg bla
     * @param type bla
     * @return the DataFilter, if any, or null
     */
    private static DataFilter regionFilter(QueryRegion reg, WFSDataType type)
    {
        return reg.getTypeKeyToFilterMap().get(type.getTypeKey());
    }

    /**
     * Construct a Query as a DefaultQuery from the basic parameters thereof.
     * @param geom the geographical region
     * @param ivl the time interval
     * @param cat the DataModelCategory
     * @param fil the applicable filter, which may be null
     * @return the Query
     */
    private Query formQuery(Polygon geom, TimeSpan ivl,
            DataModelCategory cat, DataFilter fil)
    {
        List<PropertyMatcher<?>> p = new LinkedList<>();
        p.add(geomParam(geom));
        p.add(timeParam(ivl));
        addNonNull(p, filterParam(fil));
        return new DefaultQuery(cat, Collections.emptyList(), p, null);
    }

    /**
     * Construct the DataModelCategory for the specified WFSDataType.
     * @param t bla
     * @return bla
     */
    private static DataModelCategory typeCat(WFSDataType t)
    {
        return new DataModelCategory(null, MapDataElement.class.getName(), t.getTypeKey());
    }

    /**
     * Handle the irritating work of constructing a "matcher" for a geometry.
     * @param jtsGeom bla
     * @return bla
     */
    private static GeometryMatcher geomParam(Polygon jtsGeom)
    {
        return new GeometryMatcher(GeometryAccessor.GEOMETRY_PROPERTY_NAME,
                GeometryMatcher.OperatorType.INTERSECTS, jtsGeom);
    }

    /**
     * Handle the irritating work of constructing a "Matcher" for a timespan.
     * @param timeSpan bla
     * @return bla
     */
    private static TimeSpanMatcher timeParam(TimeSpan timeSpan)
    {
        return new TimeSpanMatcher(
                TimeSpanAccessor.TIME_PROPERTY_NAME, timeSpan);
    }

    /**
     * Handle the irritating work of constructing a "Matcher" for a filter.
     * @param f bla
     * @return bla
     */
    private static PropertyMatcher<DataFilter> filterParam(DataFilter f)
    {
        if (f == null)
        {
            return null;
        }
        return new GeneralIntervalPropertyMatcher<>(
                WFSDataRegistryHelper.DATA_FILTER_PROPERTY_DESCRIPTOR, f);
    }

    /**
     * Insert an element into a collection, ignoring nulls.
     * @param c bla
     * @param e bla
     * @param <E> bla
     */
    private static <E> void addNonNull(Collection<E> c, E e)
    {
        if (e != null)
        {
            c.add(e);
        }
    }

    /**
     * Null-tolerant comparison for equivalence.
     * @param left bla
     * @param right bla
     * @return bla
     */
    private static boolean equiv(Object left, Object right)
    {
        if (left == null)
        {
            return right == null;
        }
        if (right == null)
        {
            return false;
        }
        return left.equals(right);
    }

    /**
     * Retrieve WFS features for all active layers and inject them into the
     * system.
     *
     * @param reg The query region.
     */
    private void requestAndActivateFeatures(QueryRegion reg)
    {
        synchronized (myWFSTypes)
        {
            for (WFSDataType t : myWFSTypes)
            {
                if (t.isQueryable() && reg.appliesToType(t.getTypeKey()))
                {
                    requestAndActivateFeatures(t, reg);
                }
            }
        }
    }

    /**
     * Retrieve WFS features for a single layer and inject them into the system.
     *
     * @param type The WFS layer whose features should be retrieved.
     * @param region The query region.
     */
    private void requestAndActivateFeatures(WFSDataType type, QueryRegion region)
    {
        Collection<TimeSpan> queryTimeSpans = getQueryTimeSpans(type);
        if (queryTimeSpans.isEmpty())
        {
            return;
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Requesting features for " + type.getTypeKey() + " " + queryTimeSpans);
        }
        for (Polygon poly : JTSCoreGeometryUtilities.convertToJTSPolygonsAndSplit(region.getGeometries()))
        {
            for (TimeSpan timeSpan : queryTimeSpans)
            {
                requestAndActivateFeatures(poly, timeSpan, type, region);
            }
        }
    }

    /**
     * Get the time spans that should be queried for the given type.
     *
     * @param type The data type.
     * @return The time spans.
     */
    private Collection<TimeSpan> getQueryTimeSpans(WFSDataType type)
    {
        if (type.isTimeless())
        {
            return Collections.singleton(TimeSpan.TIMELESS);
        }

        TimeSpanList timeSpans = myTimeManager.getPrimaryActiveTimeSpans();
        DataGroupInfo dgi = type.getParent();
        Collection<? extends TimeSpan> secondary = dgi == null ? null
                : myTimeManager.getSecondaryActiveTimeSpans(dgi.getId());
        if (secondary != null && !secondary.isEmpty())
        {
            timeSpans = new TimeSpanArrayList(CollectionUtilities.concat(timeSpans, secondary));
        }
        timeSpans = trimSpansToExtent(timeSpans, type.getTimeExtents());

        TimeSpanList timeCoverage = getTimeCoverage(type);
        if (timeCoverage == null)
        {
            return timeSpans;
        }

        Collection<TimeSpan> queryTimeSpans = New.set(timeSpans.size());
        for (TimeSpan timeSpan : timeSpans)
        {
            // See if there is a larger interval we should
            // request in order to avoid excessive downloads.
            Collection<? extends TimeSpan> coverageSpans = StreamUtilities.filter(timeCoverage,
                coverage -> coverage.overlaps(timeSpan));
            if (coverageSpans.isEmpty())
            {
                queryTimeSpans.add(timeSpan);
            }
            else
            {
                queryTimeSpans.addAll(coverageSpans);
            }
        }
        return queryTimeSpans;
    }

    /**
     * Get the time spans from the plan trimmed to the extent of the layer.
     *
     * @param type The layer.
     * @return The time span list.
     */
    private TimeSpanList getTimeCoverage(WFSDataType type)
    {
        AnimationPlan curPlan = myAnimationManager.getCurrentPlan();
        if (curPlan == null)
        {
            return null;
        }
        Duration dataDur = myTimeManager.getDataLoadDuration();
        if (dataDur == null)
        {
            return null;
        }

        TimeSpanArrayList cov = new TimeSpanArrayList(curPlan.getTimeCoverage());
        List<DateDurationKey> keys = TimelineUtilities.getTableOfLegalDates(
                cov.getExtent(), dataDur).get(dataDur);
        List<TimeSpan> spans = keys.stream().map(k -> k.getAsTimeSpan()).collect(Collectors.toList());
        return trimSpansToExtent(new TimeSpanArrayList(spans), type.getTimeExtents());
    }

    /**
     * Trim a TimeSpanList down to just what fits inside a TimeExtent.
     *
     * @param timeSpans the time spans to trim
     * @param extents the time extents that the resultant list should not exceed
     * @return the trimmed list of time spans
     */
    private TimeSpanList trimSpansToExtent(TimeSpanList timeSpans, TimeExtents extents)
    {
        if (extents == null)
        {
            return timeSpans;
        }
        return timeSpans.intersection(new TimeSpanArrayList(extents.getTimespans()));
    }

    /** Listener for changes to the status of queries. */
    private class ControlsTrackerListener implements QueryTrackerListener
    {
        /** The region associated with the query. */
        private final QueryRegion myQueryRegion;

        /** The Query tracker. */
        private final QueryTracker myQueryTracker;

        /**
         * Instantiates a new controls tracker listener.
         *
         * @param tracker the tracker
         * @param region the region associated with the query
         */
        public ControlsTrackerListener(QueryTracker tracker, QueryRegion region)
        {
            myQueryTracker = tracker;
            myQueryRegion = region;
        }

        @Override
        public void fractionCompleteChanged(QueryTracker tracker, float fractionComplete)
        {
        }

        /**
         * Get the region associated with the query.
         *
         * @return The region.
         */
        public QueryRegion getQueryRegion()
        {
            return myQueryRegion;
        }

        /**
         * Gets the tracker.
         *
         * @return the tracker
         */
        public QueryTracker getTracker()
        {
            return myQueryTracker;
        }

        @Override
        @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
        public void statusChanged(QueryTracker tracker, QueryStatus status)
        {
            tracker.logException();
            synchronized (myQueryStatusListeners)
            {
                switch (status)
                {
                    case FAILED:
                        // --> Commented out so that failed requests don't get
                        // > re-requested each iteration through a timeline
                        // Set<Query> set =
                        // myQueryMap.get(tracker.getQuery().getDataModelCategory());
                        // if (set != null)
                        // {
                        // set.remove(tracker.getQuery());
                        // }
                        //$FALL-THROUGH$
                    case CANCELLED:
                    case SUCCESS:
                        myQueryStatusListeners.remove(this);
                        if (myWaitForQueriesPhaser != null && myQueryStatusListeners.isEmpty())
                        {
                            myWaitForQueriesPhaser.arriveAndDeregister();
                            myWaitForQueriesPhaser = null;
                        }
                        break;
                    case RUNNING:
                        break;
                    default:
                        LOGGER.error("Unexpected enum value for query status : " + status);
                        break;
                }
            }
        }
    }
}
