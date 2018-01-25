package io.opensphere.wfs.mantle;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.animationhelper.TimeSpanGovernor;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.matcher.GeneralIntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.data.util.QueryTracker.QueryTrackerListener;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.wfs.envoy.WFSDataRegistryHelper;

/**
 * Responsible for querying wfs features for a given query area and time
 * periods.
 */
public class WFSGovernor extends TimeSpanGovernor implements QueryTrackerListener
{
    /**
     * All of eternity, well according to a computer.
     */
    private static final TimeSpan ourAllTime = TimeSpan.get(0, Long.MAX_VALUE);

    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(WFSGovernor.class);

    /**
     * Removes the appropriate wfs feature layers when clearData is called on
     * governor.
     */
    private final WFSDataClearer myDataClearer;

    /**
     * Used to query for data.
     */
    private final DataRegistry myDataRegistry;

    /**
     * The region to query for.
     */
    private final QueryRegion myRegion;

    /**
     * The layer to retrieve wfs features for.
     */
    private final DataTypeInfo myWfsLayer;

    /**
     * Constructs a new {@link TimeSpanGovernor} that retrieves wfs features
     * from an OGC server.
     *
     * @param mantle The mantle toolbox.
     * @param dataRegistry Used to query for data.
     * @param wfsLayer The layer to retrieve the features for.
     * @param region The area to retrieve the features for.
     */
    public WFSGovernor(MantleToolbox mantle, DataRegistry dataRegistry, DataTypeInfo wfsLayer, QueryRegion region)
    {
        super(ourAllTime);
        myDataRegistry = dataRegistry;
        myWfsLayer = wfsLayer;
        myRegion = region;
        myDataClearer = new WFSDataClearer(mantle, myWfsLayer, myRegion);
    }

    @Override
    public void clearData()
    {
        try
        {
            myDataClearer.removeAll();
            super.clearData();
        }
        catch (DataElementLookupException e)
        {
            LOGGER.error(e, e);
        }
    }

    @Override
    public void clearData(Collection<? extends TimeSpan> spans)
    {
        for (TimeSpan span : spans)
        {
            try
            {
                myDataClearer.removeFeatures(span);
            }
            catch (DataElementLookupException e)
            {
                LOGGER.error(e, e);
            }
        }
        super.clearData(spans);
    }

    @Override
    public void fractionCompleteChanged(QueryTracker tracker, float fractionComplete)
    {
    }

    @Override
    public void statusChanged(QueryTracker tracker, QueryStatus status)
    {
        tracker.logException();
    }

    @Override
    protected boolean performRequest(List<? extends TimeSpan> spans)
    {
        DataModelCategory category = new DataModelCategory(null, MapDataElement.class.getName(), myWfsLayer.getTypeKey());

        GeneralIntervalPropertyMatcher<DataFilter> filter = null;
        if (myRegion.getTypeKeyToFilterMap().get(myWfsLayer.getTypeKey()) != null)
        {
            filter = new GeneralIntervalPropertyMatcher<>(WFSDataRegistryHelper.DATA_FILTER_PROPERTY_DESCRIPTOR,
                    myRegion.getTypeKeyToFilterMap().get(myWfsLayer.getTypeKey()));
        }

        for (Polygon poly : JTSCoreGeometryUtilities.convertToJTSPolygonsAndSplit(myRegion.getGeometries()))
        {
            GeometryMatcher geometry = new GeometryMatcher(GeometryAccessor.GEOMETRY_PROPERTY_NAME,
                    GeometryMatcher.OperatorType.INTERSECTS, poly);

            for (TimeSpan span : spans)
            {
                TimeSpan toUse = span;

                // When span is all of eternity, this means the layer we are querying is timeless so we want to send timeless in the query.
                if (span.equals(ourAllTime))
                {
                    toUse = TimeSpan.TIMELESS;
                }

                TimeSpanMatcher time = new TimeSpanMatcher(TimeSpanAccessor.TIME_PROPERTY_NAME, toUse);

                List<PropertyMatcher<?>> matchers = New.list(geometry, time);
                if (filter != null)
                {
                    matchers.add(filter);
                }
                DefaultQuery query = new DefaultQuery(category, Collections.emptyList(), matchers, null);
                QueryTracker tracker = myDataRegistry.submitQuery(query);
                tracker.addListener(this);
            }
        }

        return true;
    }
}
