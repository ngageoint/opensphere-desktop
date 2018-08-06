package io.opensphere.arcgis2.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import io.opensphere.arcgis2.model.ArcGISLayer;
import io.opensphere.arcgis2.model.FolderInfo;
import io.opensphere.arcgis2.model.Service;
import io.opensphere.arcgis2.model.TileInfo;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.server.util.JsonUtils;

/** Envoy that talks to an ArcGIS server to get its layer list via json. */
public class ArcGISLayerListEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ArcGISLayerListEnvoy.class);

    /** A set of server endpoint types recognized by the envoy. */
    private static final Set<String> ARCGIS_SERVER_ENDPOINTS = New.set("MapServer", "FeatureServer", "GPServer", "GlobeServer",
            "ImageServer");

    /**
     * A regular expression pattern used to determine if the user has entered a
     * URL that is a subpath of the main ArcGIS server (so as to avoid
     * configuring the entire server).
     */
    private static final Pattern SUBPATH_PATTERN = Pattern.compile("^.*rest/services(/.+)$");

    /**
     * A regular expression pattern used to determine if the user has entered a
     * URL that contains the ArcGIS server component.
     */
    private static final Pattern SERVER_SUBPATH_PATTERN = Pattern.compile("^.*/(Map|Feature|GP|Globe|Image)(Server)$");

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public ArcGISLayerListEnvoy(Toolbox toolbox)
    {
        super(toolbox);
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfaction(DataModelCategory dataModelCategory,
            Collection<? extends IntervalPropertyValueSet> intervalSets)
    {
        return SingleSatisfaction.generateSatisfactions(intervalSets);
    }

    @Override
    public String getThreadPoolName()
    {
        return getClass().getSimpleName();
    }

    @Override
    public void open()
    {
        /* intentionally blank */
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return category.getFamily() == null || ArcGISLayer.class.getName().equals(category.getFamily());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, final CacheDepositReceiver queryReceiver)
        throws InterruptedException, QueryException
    {
        if (category.getCategory() == null)
        {
            throw new QueryException("Server URL must be provided in category.");
        }

        DataModelCategory localCategory = new DataModelCategory(category.getSource(), category.getFamily(),
                category.getCategory());

        String includedSubpath = null;
        Matcher matcher = SUBPATH_PATTERN.matcher(localCategory.getCategory());
        if (matcher.matches())
        {
            String subpath = matcher.group(1);
            localCategory = localCategory.withCategory(localCategory.getCategory().replace(subpath, ""));

            // strip the leading slash from the subpath, and if there's a server
            // component, remove it from the subpath:
            includedSubpath = removeServerComponent(subpath.substring(1));
        }

        queryImpl(localCategory, queryReceiver, UrlUtilities.toURL(localCategory.getCategory()), includedSubpath);
    }

    /**
     * Examines the supplied subpath, looking for an ArcGIS server component. If
     * found, it is removed and the truncated string is returned. If not found,
     * the original string is returned unmolested.
     *
     * @param subpath the subpath to examine.
     * @return the supplied subpath, stripped of any ArcGIS Server components,
     *         if present.
     */
    protected String removeServerComponent(String subpath)
    {
        String returnValue = subpath;
        Matcher serverMatcher = SERVER_SUBPATH_PATTERN.matcher(subpath);
        if (serverMatcher.matches())
        {
            returnValue = subpath.replace("/" + serverMatcher.group(1) + serverMatcher.group(2), "");
        }
        return returnValue;
    }

    /**
     * Recursively executes a series HTTP GET queries against the supplied URL,
     * and processes the results. the included subpath is used to determine if
     * only a subset of the folders should be retrieved. The subpath is used to
     * limit the number of recursions to only those items which match it or a
     * substring of it.
     *
     * @param category the {@link DataModelCategory} for which to query.
     * @param queryReceiver the receiver to process the results.
     * @param url the URL to which the query is sent.
     * @param includedSubpath the optional subpath to use for restricting the
     *            number of recursions.
     * @throws QueryException if the query cannot be executed.
     * @throws InterruptedException if the processing of the results is
     *             interrupted.
     */
    private void queryImpl(DataModelCategory category, final CacheDepositReceiver queryReceiver, URL url, String includedSubpath)
        throws QueryException, InterruptedException
    {
        CancellableInputStream is = getJsonStream(url);

        if (is != null)
        {
            try
            {
                FolderInfo info = parseResponse(url, is);

                Collection<Map<String, Object>> layers = CollectionUtilities.concat(info.getLayers(), info.getTables());
                if (!layers.isEmpty())
                {
                    List<ArcGISLayer> arcLayers = createLayers(layers, url, info);
                    depositLayers(arcLayers, queryReceiver, url);
                }

                for (String folder : info.getFolders())
                {
                    if (StringUtils.startsWith(folder, includedSubpath) || includedSubpath == null)
                    {
                        String folderUrl = UrlUtilities.concatUrlFragments(category.getCategory(), folder);
                        queryFolder(category, folderUrl, queryReceiver, includedSubpath);
                    }
                }

                for (Service service : info.getServices())
                {
                    String name = service.getName();
                    if (StringUtils.startsWith(name, includedSubpath) || includedSubpath == null)
                    {
                        int indexOfSlash = name.indexOf('/');
                        if (indexOfSlash >= 0 && indexOfSlash + 1 < name.length())
                        {
                            name = name.substring(indexOfSlash + 1);
                        }
                        String type = service.getType();
                        String folderUrl = UrlUtilities.concatUrlFragments(category.getCategory(), name, type);

                        queryFolder(category, folderUrl, queryReceiver, includedSubpath);
                    }
                }
            }
            catch (JsonParseException e)
            {
                LOGGER.error("Unable to add layer " + url, e);
            }
            catch (IOException | ClassCastException e)
            {
                throw new QueryException("Failed to read JSON response from server for url [" + url + "]: " + e, e);
            }
            catch (CacheException e)
            {
                throw new QueryException("Failed to deposit layer: " + e, e);
            }
            finally
            {
                Utilities.close(is);
            }
        }
    }

    /**
     * Reads the response from the supplied URL from the supplied input stream.
     * If debug is enabled, the response is logged before parsing.
     *
     * @param url the URL from which the response was received.
     * @param is the input stream to read.
     * @return a {@link FolderInfo} object containing the unmarshalled JSON
     *         response.
     * @throws IOException if the input stream cannot be read.
     * @throws JsonParseException if the contents of the input stream cannot be
     *             processed as JSON data.
     * @throws JsonMappingException if the contents of the input stream cannot
     *             be processed as JSON data.
     */
    private FolderInfo parseResponse(URL url, InputStream is) throws IOException, JsonParseException, JsonMappingException
    {
        FolderInfo info;
        if (LOGGER.isDebugEnabled())
        {
            String jsonResponse = IOUtils.toString(is, StringUtilities.DEFAULT_CHARSET);
            LOGGER.debug(url + ": " + jsonResponse);
            info = JsonUtils.createMapper().readValue(jsonResponse, FolderInfo.class);
        }
        else
        {
            info = JsonUtils.createMapper().readValue(is, FolderInfo.class);
        }
        return info;
    }

    /**
     * Start a query for a subfolder on the server.
     *
     * @param category The category for the current folder.
     * @param folderUrl The URL for the subfolder.
     * @param queryReceiver The receiver for the layers.
     * @param includedSubpath the subpath to include in requests.
     * @throws QueryException If the query fails.
     * @throws InterruptedException If the thread is interrupted.
     */
    private void queryFolder(DataModelCategory category, String folderUrl, CacheDepositReceiver queryReceiver,
            String includedSubpath)
        throws InterruptedException, QueryException
    {
        queryImpl(category.withCategory(folderUrl), queryReceiver, UrlUtilities.toURL(folderUrl), includedSubpath);
    }

    /**
     * Get an input stream from the json content at the given URL.
     *
     * @param url The URL.
     * @return The input stream.
     * @throws QueryException If there's an error.
     */
    private CancellableInputStream getJsonStream(URL url) throws QueryException
    {
        ServerProvider<HttpServer> provider = getToolbox().getServerProviderRegistry().getProvider(HttpServer.class);
        HttpServer serverConnection = provider.getServer(url);
        ResponseValues response = new ResponseValues();

        CancellableInputStream is;
        try
        {
            URL actualUrl = new URL(url.toString() + "?f=json");
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("GET: " + actualUrl);
            }
            is = serverConnection.sendGet(actualUrl, response);
            if (MimeType.HTML.toString().equals(response.getContentType()))
            {
                // They are sending us an html page instead of json.
                StreamReader reader = new StreamReader(is);
                String html = reader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
                LOGGER.warn("Unable to get details for " + url + " " + html);
                is.close();
                is = null;
            }
        }
        catch (IOException | URISyntaxException e)
        {
            String message = "Failed to get layers from server at url [" + url + "]: " + e;
            throw new QueryException(message, e);
        }
        if (response.getResponseCode() != HttpURLConnection.HTTP_OK && response.getResponseCode() != 0)
        {
            throw new QueryException(
                    "Received " + response.getResponseCode() + " response from server: " + response.getResponseMessage());
        }
        return is;
    }

    /**
     * Creates ArcGISLayer objects.
     *
     * @param layers the layers received from the server
     * @param url the URL
     * @param info the folder info
     * @return the ArcGISLayer objects
     */
    private List<ArcGISLayer> createLayers(Collection<Map<String, Object>> layers, URL url, FolderInfo info)
    {
        // Initialize the builder
        ArcGISLayer.Builder builder = new ArcGISLayer.Builder();
        List<String> split = Arrays.asList(url.toString().split("/"));
        int servicesIndex = split.indexOf("services");

        if (servicesIndex >= 0)
        {
            int serverIndex;
            for (String serverType : ARCGIS_SERVER_ENDPOINTS)
            {
                if ((serverIndex = split.indexOf(serverType)) >= 0)
                {
                    builder.setPath(split.subList(servicesIndex + 1, serverIndex));
                    break;
                }
            }
        }
        builder.setURL(url);

        // Create the layers
        List<ArcGISLayer> arcLayers = New.list(layers.size());
        boolean isFirst = true;
        for (Map<String, Object> layer : layers)
        {
            // as long as the layer doesn't declare itself invisible by default,
            // process it (changed from only processing layers that declared
            // themselves visible, and some servers just omit this tag
            // entirely):
            if (!Boolean.FALSE.equals(layer.get("defaultVisibility")))
            {
                populateBuilder(builder, info, layer);
                ArcGISLayer arcLayer = new ArcGISLayer(builder);
                arcLayers.add(arcLayer);
                if (isFirst)
                {
                    isFirst = false;
                    // If this layer is a tile layer only add the first
                    // layer, we are unable to retrieve images from the
                    // other layers and adding them screws up mantle.
                    if (arcLayer.isSingleFusedMapCache())
                    {
                        // Since there is only one layer for a single fused map
                        // cache, make id 0 so saved preferences will work.
                        arcLayers.remove(arcLayer);
                        builder.setId("0");
                        if (layers.size() > 1 && !StringUtils.isEmpty(info.getDocumentInfo().getTitle()))
                        {
                            builder.setLayerName(info.getDocumentInfo().getTitle());
                        }
                        arcLayer = new ArcGISLayer(builder);
                        arcLayers.add(arcLayer);
                        break;
                    }
                }
            }
        }
        return arcLayers;
    }

    /**
     * Populate the builder with values from the folder info.
     *
     * @param builder The builder.
     * @param info The info.
     * @param layer The layer property from the map.
     */
    private void populateBuilder(ArcGISLayer.Builder builder, FolderInfo info, Map<String, Object> layer)
    {
        builder.setId(String.valueOf(layer.get("id")));
        builder.setLayerName(String.valueOf(layer.get("name")));
        builder.setDescription(info.getDescription());
        builder.setServiceDescription(info.getServiceDescription());
        builder.setSingleFusedMapCache(info.isSingleFusedMapCache());
        if (info.getFullExtent() != null && info.getFullExtent().getSpatialReference() != null)
        {
            builder.setSpatialReference(info.getFullExtent().getSpatialReference().getWkid());
        }
        TileInfo tileInfo = info.getTileInfo();
        if (tileInfo != null)
        {
            builder.setMaxLevels(tileInfo.getLods().size() - 1);
            builder.setRows(tileInfo.getRows());
            builder.setCols(tileInfo.getCols());
            LatLonAlt lowerLeft = LatLonAlt.createFromDegrees(info.getFullExtent().getMinY(), tileInfo.getOrigin().getX());
            LatLonAlt upperRight = LatLonAlt.createFromDegrees(tileInfo.getOrigin().getY(), info.getFullExtent().getMaxX());
            GeographicBoundingBox bounds = new GeographicBoundingBox(lowerLeft, upperRight);
            builder.setBounds(bounds);

            if (info.getDocumentInfo() != null)
            {
                String keywords = info.getDocumentInfo().getKeywords();
                String[] split = keywords.split(",");
                builder.setKeywords(Arrays.asList(split));
            }
        }
    }

    /**
     * Deposits the layers in the data registry.
     *
     * @param arcLayers the layers
     * @param queryReceiver the query receiver
     * @param url the URL
     * @throws CacheException if it couldn't be deposited
     */
    private void depositLayers(Collection<ArcGISLayer> arcLayers, CacheDepositReceiver queryReceiver, URL url)
        throws CacheException
    {
        Collection<UnserializableAccessor<ArcGISLayer, ?>> accessors = New.collection();
        accessors.add(UnserializableAccessor.getHomogeneousAccessor(ArcGISLayer.LAYER_PROPERTY));
        accessors.add(UnserializableAccessor.getSingletonAccessor(ArcGISLayer.ACTIVE_PROPERTY, Boolean.FALSE));

        DataModelCategory depositCategory = new DataModelCategory(getClass().getName(), ArcGISLayer.class.getName(),
                url.toString());
        DefaultCacheDeposit<ArcGISLayer> deposit = new DefaultCacheDeposit<>(depositCategory, accessors, arcLayers, true,
                CacheDeposit.SESSION_END, true);
        queryReceiver.receive(deposit);
    }
}
