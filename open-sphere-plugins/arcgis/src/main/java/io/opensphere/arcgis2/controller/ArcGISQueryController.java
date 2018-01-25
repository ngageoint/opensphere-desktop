package io.opensphere.arcgis2.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.arcgis2.esri.Response;
import io.opensphere.arcgis2.mantle.ArcGISMantleController;
import io.opensphere.arcgis2.model.ArcGISDataGroupInfo;
import io.opensphere.arcgis2.util.ArcGISRegistryUtils;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animationhelper.TimeSpanGovernor;
import io.opensphere.core.animationhelper.TimeSpanGovernorManager;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.matcher.GeneralPropertyMatcher;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.CompositeService;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.rangeset.DefaultRangedLongSet;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionListener;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;

/** ArcGIS query controller. */
public class ArcGISQueryController extends CompositeService
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ArcGISQueryController.class);

    /** The time manager. */
    private final TimeManager myTimeManager;

    /** The query region manager. */
    private final QueryRegionManager myQueryRegionManager;

    /** The mantle controller. */
    private final ArcGISMantleController myMantleController;

    /** The time span governor manager. */
    private final TimeSpanGovernorManager<Pair<ArcGISDataGroupInfo, QueryRegion>> myGovernorManager;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param mantleController the mantle controller
     */
    public ArcGISQueryController(Toolbox toolbox, ArcGISMantleController mantleController)
    {
        super(2);

        myTimeManager = toolbox.getTimeManager();
        myQueryRegionManager = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class).getQueryRegionManager();
        myMantleController = mantleController;
        myGovernorManager = new ArcGISGovernorManager(toolbox.getDataRegistry(), mantleController);

        addService(myTimeManager.getPrimaryTimeSpanListenerService(new ArcPrimaryTimeSpanChangeListener()));
        addService(myQueryRegionManager.getQueryRegionListenerService(new ArcQueryRegionListener()));
    }

    /**
     * Requests data.
     *
     * @param layers the layers to request
     * @param regions the regions to request
     * @param spans the time spans to request
     */
    private void requestData(Collection<? extends ArcGISDataGroupInfo> layers, Collection<? extends QueryRegion> regions,
            Collection<? extends TimeSpan> spans)
    {
        for (ArcGISDataGroupInfo layer : layers)
        {
            boolean isTimeAware = layer.getFeatureType().getTimeExtents() != null;
            Collection<? extends TimeSpan> spansToQuery = isTimeAware ? spans : Collections.singleton(TimeSpan.TIMELESS);
            for (QueryRegion region : regions)
            {
                myGovernorManager.requestData(new Pair<>(layer, region), spansToQuery);
            }
        }
    }

    /** Time change listener. */
    private class ArcPrimaryTimeSpanChangeListener implements PrimaryTimeSpanChangeListener
    {
        @Override
        public void primaryTimeSpansChanged(TimeSpanList spans)
        {
            ThreadUtilities.runCpu(() ->
            {
                // Query time-aware active feature layers
                Collection<ArcGISDataGroupInfo> layers = myMantleController
                        .getActiveLayers(t -> ArcGISDataGroupInfo.isFeatureType(t) && t.getTimeExtents() != null);
                requestData(layers, myQueryRegionManager.getQueryRegions(), spans);
            });
        }

        @Override
        public void primaryTimeSpansCleared()
        {
        }
    }

    /** Query region listener. */
    private class ArcQueryRegionListener implements QueryRegionListener
    {
        @Override
        public void queryRegionAdded(QueryRegion region)
        {
            // Query all active feature layers
            // if layer is not time aware, query all time
            // make sure all time works in manager
            Collection<ArcGISDataGroupInfo> layers = myMantleController.getActiveLayers(ArcGISDataGroupInfo::isFeatureType);
            requestData(layers, Collections.singleton(region), myTimeManager.getPrimaryActiveTimeSpans());
        }

        @Override
        public void queryRegionRemoved(QueryRegion region)
        {
            myGovernorManager.clearData(new Pair<>(null, region));
        }

        @Override
        public void allQueriesRemoved(boolean animationPlanCancelled)
        {
            myGovernorManager.clearData(new Pair<>(null, null));
        }
    }

    /** ArcGIS time span governor manager. */
    private static class ArcGISGovernorManager extends TimeSpanGovernorManager<Pair<ArcGISDataGroupInfo, QueryRegion>>
    {
        /**
         * Constructor.
         *
         * @param dataRegistry The data registry
         * @param mantleController The mantle controller
         */
        public ArcGISGovernorManager(DataRegistry dataRegistry, ArcGISMantleController mantleController)
        {
            super(p -> new ArcGISTimeSpanGovernor(dataRegistry, mantleController, p));
        }

        @Override
        public void clearData(Pair<ArcGISDataGroupInfo, QueryRegion> context)
        {
            Map<Pair<ArcGISDataGroupInfo, QueryRegion>, TimeSpanGovernor> governors = getTimeSpanGovernors();
            synchronized (governors)
            {
                // Find wildcards
                if (context.getFirstObject() == null || context.getSecondObject() == null)
                {
                    for (Map.Entry<Pair<ArcGISDataGroupInfo, QueryRegion>, TimeSpanGovernor> entry : governors.entrySet())
                    {
                        boolean matches = (context.getFirstObject() == null
                                || context.getFirstObject().equals(entry.getKey().getFirstObject()))
                                && (context.getSecondObject() == null
                                        || context.getSecondObject().equals(entry.getKey().getSecondObject()));
                        if (matches)
                        {
                            TimeSpanGovernor governor = entry.getValue();
                            if (governor != null)
                            {
                                governor.clearData();
                            }
                        }
                    }
                }
                // Direct lookup in map
                else
                {
                    TimeSpanGovernor governor = governors.remove(context);
                    if (governor != null)
                    {
                        governor.clearData();
                    }
                }
            }
        }
    }

    /** ArcGIS time span governor. */
    private static class ArcGISTimeSpanGovernor extends TimeSpanGovernor
    {
        /** The data registry. */
        private final DataRegistry myDataRegistry;

        /** The mantle controller. */
        private final ArcGISMantleController myMantleController;

        /** The data type. */
        private final DataTypeInfo myDataType;

        /** The query region. */
        private final QueryRegion myRegion;

        /** The polygons to request. */
        private final List<Polygon> myPolygons;

        /** The data element IDs queried by this governor. */
        @GuardedBy("myIds")
        private final RangedLongSet myIds = new DefaultRangedLongSet();

        /**
         * Constructor.
         *
         * @param dataRegistry The data registry
         * @param mantleController The mantle controller
         * @param groupAndRegion the group and region
         */
        public ArcGISTimeSpanGovernor(DataRegistry dataRegistry, ArcGISMantleController mantleController,
                Pair<ArcGISDataGroupInfo, QueryRegion> groupAndRegion)
        {
            // TODO use a better initial time
            super(TimeSpan.get(0, Long.MAX_VALUE));
            myDataRegistry = dataRegistry;
            myMantleController = mantleController;
            myDataType = groupAndRegion.getFirstObject().getFeatureType();
            myRegion = groupAndRegion.getSecondObject();
            myPolygons = JTSCoreGeometryUtilities.convertToJTSPolygonsAndSplit(myRegion.getGeometries());
        }

        @Override
        public void clearData()
        {
            super.clearData();
            synchronized (myIds)
            {
                myMantleController.removeDataElements(myDataType, myIds.getValues());
            }
        }

        @Override
        protected boolean performRequest(List<? extends TimeSpan> timeSpans)
        {
            boolean requestComplete = true;
            for (TimeSpan timeSpan : timeSpans)
            {
                requestComplete &= performRequest(timeSpan);
            }
            return requestComplete;
        }

        /**
         * Requests the time span.
         *
         * @param timeSpan the time span to request
         * @return whether the request was completed
         */
        private boolean performRequest(TimeSpan timeSpan)
        {
            boolean requestComplete = true;

            DataModelCategory category = ArcGISRegistryUtils.newGetFeatureCategory(myDataType.getUrl());
            TimeSpanMatcher timeMatcher = new TimeSpanMatcher(TimeSpanAccessor.TIME_PROPERTY_NAME, timeSpan);

            for (Polygon polygon : myPolygons)
            {
                List<PropertyMatcher<?>> parameters = New.list(2);
                parameters.add(new GeometryMatcher(GeometryAccessor.GEOMETRY_PROPERTY_NAME,
                        GeometryMatcher.OperatorType.INTERSECTS, polygon));
                boolean isTimeAware = myDataType.getTimeExtents() != null;
                if (isTimeAware)
                {
                    parameters.add(timeMatcher);
                }
                DataFilter loadFilter = myRegion.getTypeKeyToFilterMap().get(myDataType.getTypeKey());
                if (loadFilter != null)
                {
                    parameters.add(new GeneralPropertyMatcher<>(ArcGISRegistryUtils.DATA_FILTER_PROPERTY_DESCRIPTOR, loadFilter));
                }

                SimpleQuery<Response> query = new SimpleQuery<>(category, ArcGISRegistryUtils.FEATURE_DESCRIPTOR, parameters);
                QueryTracker tracker = myDataRegistry.performQuery(query);
                if (tracker.getQueryStatus() == QueryStatus.SUCCESS)
                {
                    for (Response response : query.getResults())
                    {
                        long[] ids = myMantleController.addDataElements(myDataType, response);
                        synchronized (myIds)
                        {
                            myIds.addAll(ids);
                        }
                    }
                }
                else
                {
                    Throwable e = tracker.getException();
                    LOGGER.error(e, e);
                }
            }

            return requestComplete;
        }
    }
}
