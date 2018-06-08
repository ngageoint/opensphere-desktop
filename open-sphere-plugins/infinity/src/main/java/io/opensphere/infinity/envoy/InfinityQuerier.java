package io.opensphere.infinity.envoy;

import java.util.List;

import com.vividsolutions.jts.geom.Polygon;

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
import io.opensphere.infinity.json.SearchResponse;
import io.opensphere.infinity.util.InfinityUtilities;
import io.opensphere.mantle.data.DataTypeInfo;

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
     * @return the search response
     * @throws QueryException if something goes wrong with the query
     */
    public SearchResponse query(DataTypeInfo dataType, Polygon polygon, TimeSpan timeSpan, String binField) throws QueryException
    {
        SearchResponse response = null;

        String url = InfinityUtilities.getUrl(dataType);
        String geomField = InfinityUtilities.getTagValue(".es-geopoint", dataType);
        if (geomField == null)
        {
            geomField = InfinityUtilities.getTagValue(".es-geoshape", dataType);
        }
        String timeField = InfinityUtilities.getTagValue(".es-starttime", dataType);
        if (timeField == null)
        {
            timeField = InfinityUtilities.getTagValue(".es-datetime", dataType);
        }
        else
        {
            String endTimeField = InfinityUtilities.getTagValue(".es-endtime", dataType);
            if (endTimeField != null)
            {
                timeField += "," + endTimeField;
            }
        }

        if (geomField != null)
        {
            DataModelCategory category = new DataModelCategory(null, InfinityEnvoy.FAMILY, url);
            List<PropertyMatcher<?>> parameters = New.list(5);
            parameters.add(new GeometryMatcher(GeometryAccessor.GEOMETRY_PROPERTY_NAME, GeometryMatcher.OperatorType.INTERSECTS,
                    polygon));
            parameters.add(new TimeSpanMatcher(TimeSpanAccessor.TIME_PROPERTY_NAME, timeSpan));
            parameters.add(new GeneralPropertyMatcher<>(InfinityEnvoy.GEOM_FIELD_DESCRIPTOR, geomField));
            parameters.add(new GeneralPropertyMatcher<>(InfinityEnvoy.TIME_FIELD_DESCRIPTOR, timeField));
            if (binField != null)
            {
                parameters.add(new GeneralPropertyMatcher<>(InfinityEnvoy.BIN_FIELD_DESCRIPTOR, binField));
            }
            SimpleQuery<SearchResponse> query = new SimpleQuery<>(category, InfinityEnvoy.RESULTS_DESCRIPTOR, parameters);
            List<SearchResponse> results = InfinityEnvoy.performQuery(myDataRegistry, query);
            response = results.iterator().next();
        }

        return response;
    }
}
