package io.opensphere.mantle.infinity;

import java.util.List;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.api.adapter.SimpleEnvoy;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.matcher.GeneralPropertyMatcher;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.infinity.QueryParameters.GeometryType;

/** Performs queries. */
public class InfinityQuerier
{
    /** The data registry. */
    private final DataRegistry myDataRegistry;

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry
     */
    public InfinityQuerier(DataRegistry dataRegistry)
    {
        myDataRegistry = dataRegistry;
    }

    /**
     * Helper method for a client to query this envoy.
     *
     * @param dataType the data type to query
     * @param polygon the polygon to query
     * @param timeSpan the time span to query
     * @param binField the bin field
     * @param binParams the query binning parameters
     * @return the search response, or null
     * @throws QueryException if something goes wrong with the query
     */
    public QueryResults query(DataTypeInfo dataType, Polygon polygon, TimeSpan timeSpan, String binField, QueryBinParameters binParams)
        throws QueryException
    {
        QueryResults result = null;

        String url = InfinityUtilities.getUrl(dataType);
        String geomField = InfinityUtilities.getTagValue(InfinityUtilities.POINT, dataType);
        GeometryType geometryType = GeometryType.POINT;
        if (geomField == null)
        {
            geomField = InfinityUtilities.getTagValue(InfinityUtilities.SHAPE, dataType);
            geometryType = GeometryType.SHAPE;
        }
        String timeField = InfinityUtilities.getTagValue(InfinityUtilities.START, dataType);
        String endTimeField = null;
        if (timeField == null)
        {
            timeField = InfinityUtilities.getTagValue(InfinityUtilities.TIME, dataType);
        }
        else
        {
            endTimeField = InfinityUtilities.getTagValue(InfinityUtilities.END, dataType);
        }

        QueryParameters queryParameters = new QueryParameters();
        queryParameters.setBinField(binField);
        if (binField != null)
        {
            queryParameters.setBinFieldType(dataType.getMetaDataInfo().getKeyClassType(binField));
        }

        if (binParams != null)
        {
            queryParameters.setBinWidth(binParams.getBinWidth());
            queryParameters.setBinOffset(binParams.getBinOffset());
            queryParameters.setDateFormat(binParams.getDateFormat());
            queryParameters.setDateInterval(binParams.getDateInterval());
            queryParameters.setDayOfWeek(binParams.getDayOfWeek());
        }

        queryParameters.setGeomField(geomField);
        queryParameters.setTimeField(timeField);
        queryParameters.setEndTimeField(endTimeField);
        queryParameters.setGeometryType(geometryType);

        if (geomField != null)
        {
            DataModelCategory category = new DataModelCategory(null, QueryResults.FAMILY, url);
            List<PropertyMatcher<?>> parameters = New.list(3);
            parameters.add(new GeometryMatcher(GeometryAccessor.GEOMETRY_PROPERTY_NAME, GeometryMatcher.OperatorType.INTERSECTS,
                    polygon));
            parameters.add(new TimeSpanMatcher(TimeSpanAccessor.TIME_PROPERTY_NAME, timeSpan));
            parameters.add(new GeneralPropertyMatcher<>(QueryParameters.PROPERTY_DESCRIPTOR, queryParameters));
            SimpleQuery<QueryResults> query = new SimpleQuery<>(category, QueryResults.PROPERTY_DESCRIPTOR, parameters);
            List<QueryResults> results = SimpleEnvoy.performQuery(myDataRegistry, query);
            result = results.iterator().next();
        }

        return result;
    }
}
