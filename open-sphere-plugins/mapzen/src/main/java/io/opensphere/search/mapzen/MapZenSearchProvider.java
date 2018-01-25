package io.opensphere.search.mapzen;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.search.ResultsSearchProvider;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.net.UrlBuilder;
import io.opensphere.search.mapzen.model.MapZenSettingsModel;
import io.opensphere.search.mapzen.model.geojson.Feature;
import io.opensphere.search.mapzen.model.geojson.GeoJsonObject;

/**
 * A search provider implementation that interacts with MapZen.
 */
public class MapZenSearchProvider implements ResultsSearchProvider
{
    /** The {@link Logger} instance used to capture output. */
    private static final Logger LOG = Logger.getLogger(MapZenSearchProvider.class);

    /** The search URL. */
    private static final String SEARCH_URL_TEMPLATE = "https://search.mapzen.com/v1/search?text=%1$s&api_key=%2$s";

    /** The toolbox through which application interaction occurs. */
    private final Toolbox myToolbox;

    /** The plugin model from which preferences are read. */
    private final MapZenSettingsModel myModel;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param model The plugin model from which preferences are read.
     */
    public MapZenSearchProvider(Toolbox toolbox, MapZenSettingsModel model)
    {
        myToolbox = toolbox;
        myModel = model;
        toolbox.getDataRegistry();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.search.SearchProvider#getName()
     */
    @Override
    public String getName()
    {
        return "MapZen Search";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.search.SearchProvider#getType()
     */
    @Override
    public String getType()
    {
        return "MapZen Places";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.search.ResultsSearchProvider#performSearch(java.lang.String,
     *      io.opensphere.core.model.LatLonAlt,
     *      io.opensphere.core.model.LatLonAlt,
     *      io.opensphere.core.model.time.TimeSpan)
     */
    @Override
    public List<SearchResult> performSearch(String keyword, LatLonAlt lowerLeft, LatLonAlt upperRight, TimeSpan span)
    {
        List<SearchResult> searchResults = New.list();
        try
        {
            String encodedSearchString = URLEncoder.encode(keyword, "UTF-8");
            String searchUrl = String.format(myModel.searchUrlTemplateProperty().get(), encodedSearchString,
                    myModel.apiKeyProperty().get());
            ServerProvider<HttpServer> provider = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class);

            URL url = new UrlBuilder(searchUrl).toURL();
            HttpServer server = provider.getServer(url);

            ResponseValues response = new ResponseValues();
            CancellableInputStream responseStream = server.sendGet(url, response);
            GeoJsonObject results = readJson(responseStream);

            searchResults.addAll(Arrays.stream(results.getFeatures()).map(f -> createResult(f)).collect(Collectors.toList()));
        }
        catch (URISyntaxException | IOException e)
        {
            LOG.error(e, e);
        }
        return searchResults;
    }

    /**
     * Converts a feature to a search result.
     *
     * @param feature the feature to convert to a search result.
     * @return a search result generated from the supplied feature.
     */
    protected SearchResult createResult(Feature feature)
    {
        SearchResult result = new SearchResult();
        result.setText(feature.getProperties().getName());
        result.setConfidence((float)feature.getProperties().getConfidence());

        StringBuilder builder = new StringBuilder(feature.getProperties().getLabel());
        if (StringUtils.isNotBlank(feature.getProperties().getRegion()))
        {
            builder.append(", ").append(feature.getProperties().getRegion());
        }
        if (StringUtils.isNotBlank(feature.getProperties().getCounty()))
        {
            builder.append(", ").append(feature.getProperties().getCounty());
        }

        result.setDescription(builder.toString());

        result.getLocations().addAll(createPoints(feature.getGeometry()));

        return result;
    }

    /**
     * Creates a collection of OpenSphere positions from the supplied geometry.
     *
     * @param geometry the geometry to interpret.
     * @return a collection of positions (which may be empty, but never null)
     *         created from the supplied geometry.
     */
    private Collection<? extends LatLonAlt> createPoints(io.opensphere.search.mapzen.model.geojson.Geometry geometry)
    {
        Collection<LatLonAlt> returnValue = New.list(1);
        switch (geometry.getTypeString())
        {
            case "Point":
                returnValue.add(LatLonAlt.createFromDegrees(geometry.getCoordinates()[1], geometry.getCoordinates()[0]));
                break;
            default:
                LOG.error("Geometry type not supported: " + geometry.getTypeString());
                break;
        }

        return returnValue;
    }

    /**
     * Generates a query URL from the supplied parameters.
     *
     * @param pParameters the set of parameters to encode into the query string.
     * @return A {@link URL} generated from the supplied parameters.
     * @throws QueryException if the URL cannot be generated because it is
     *             malformed.
     */
    protected URL getQueryUrl(List<? extends PropertyMatcher<?>> pParameters) throws QueryException
    {
        Map<String, String> propertyMap = New.map(pParameters.size());
        for (PropertyMatcher<?> parameter : pParameters)
        {
            String propertyName = parameter.getPropertyDescriptor().getPropertyName();
            String value = (String)parameter.getOperand();
            propertyMap.put(propertyName, value);
        }
        StrSubstitutor substitutor = new StrSubstitutor(propertyMap);
        URL url;
        try
        {
            url = new URL(substitutor.replace(SEARCH_URL_TEMPLATE));
        }
        catch (MalformedURLException e)
        {
            throw new QueryException("Unable to generate URL by substituting parameters.", e);
        }
        return url;
    }

    /**
     * Gets the geometry.
     *
     * @param pObject The object.
     * @return The geometries.
     */
    public List<Geometry> getGeometry(GeoJsonObject pObject)
    {
        GeometryFactory factory = new GeometryFactory();

        List<Geometry> returnValue = New.list();
        for (Feature feature : pObject.getFeatures())
        {
            double[] coordinates = feature.getGeometry().getCoordinates();
            Coordinate coordinate = new Coordinate(coordinates[1], coordinates[0]);
            returnValue.add(factory.createPoint(coordinate));
        }

        return returnValue;
    }

    /**
     * Reads the JSON.
     *
     * @param pInput The JSON.
     * @return The parsed JSON.
     * @throws IOException If issues reading the JSON stream.
     */
    public GeoJsonObject readJson(InputStream pInput) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();

        String response = IOUtils.toString(pInput, Charset.defaultCharset());

        return mapper.readValue(response, GeoJsonObject.class);
    }
}
