package io.opensphere.search.googleplaces;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.search.ResultsSearchProvider;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.search.googleplaces.json.GeoJsonObject;

/** Class that facilitates a search for places from google. */
public class GooglePlacesSearch implements ResultsSearchProvider
{
    /** The logger reference. */
    private static final Logger LOG = Logger.getLogger(GooglePlacesSearch.class);

    /**
     * The api key to use for search.
     */
    private String myAPIKey;

    /**
     * Takes the google results and translates them to {@link SearchResult}.
     */
    private final SearchResultCreator myResultCreator = new SearchResultCreator();

    /** The toolbox through which application interaction occurs. */
    private final Toolbox myToolbox;

    /**
     * Builds the appropriate search url.
     */
    private final GoogleUrlBuilder myUrlBuilder = new GoogleUrlBuilder();

    /**
     * Creates a new GooglePlaces search provider.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     */
    public GooglePlacesSearch(Toolbox pToolbox)
    {
        myToolbox = pToolbox;
    }

    @Override
    public String getName()
    {
        return "Google Places Search";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.search.SearchProvider#getType()
     */
    @Override
    public String getType()
    {
        return "Google Places";
    }

    @Override
    public List<SearchResult> performSearch(String keyword, LatLonAlt lowerLeft, LatLonAlt upperRight, TimeSpan span)
    {
        List<SearchResult> results = New.list();

        if (!StringUtils.isBlank(keyword))
        {
            try
            {
                URL url = myUrlBuilder.buildUrl(keyword, lowerLeft, upperRight, myAPIKey);
                ServerProvider<HttpServer> provider = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class);
                HttpServer server = provider.getServer(url);

                ResponseValues response = new ResponseValues();
                CancellableInputStream responseStream = server.sendGet(url, response);

                GeoJsonObject googleResults = readJson(responseStream);
                googleResults.setSearchTerm(keyword);

                results.addAll(myResultCreator.createResult(googleResults));
            }
            catch (IOException | URISyntaxException e)
            {
                Notify.error("Error searching Google, check logs for details.", Method.TOAST);
                LOG.error(e, e);
            }
        }

        return results;
    }

    /**
     * Sets the api key to use for search.
     *
     * @param key The key to use for search.
     */
    public void setAPIKey(String key)
    {
        myAPIKey = key;
    }

    /**
     * Data binding JSON response to java objects.
     *
     * @param pInput inputStream
     * @throws IOException -- error reading in JSON response.
     * @return object bindings
     */
    private GeoJsonObject readJson(InputStream pInput) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(pInput, GeoJsonObject.class);
    }
}
