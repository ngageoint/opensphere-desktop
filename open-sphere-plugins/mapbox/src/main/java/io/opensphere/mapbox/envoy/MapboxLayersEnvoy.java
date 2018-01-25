package io.opensphere.mapbox.envoy;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.HttpUtilities;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mapbox.util.MapboxUtil;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/** Mapbox layers envoy. */
public class MapboxLayersEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /**
     * The set of available servers that we will add to every layers query.
     */
    private final Set<String> myAvailableServers;

    /** The server provider. */
    private final ServerProvider<HttpServer> myServerProvider;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param availableServers The set of available servers that we will add to
     *            every layers query.
     */
    public MapboxLayersEnvoy(Toolbox toolbox, Set<String> availableServers)
    {
        super(toolbox);
        myServerProvider = toolbox.getServerProviderRegistry().getProvider(HttpServer.class);
        myAvailableServers = availableServers;
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
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return XYZTileUtils.LAYERS_FAMILY.equals(category.getFamily()) && MapboxUtil.PROVIDER.equals(category.getCategory());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws QueryException
    {
        try (CancellableTaskActivity ta = CancellableTaskActivity.createActive("Querying Mapbox layers"))
        {
            getToolbox().getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);

            String baseUrl = category.getSource();
            baseUrl = baseUrl.replaceFirst(":\\d+", "");
            String url = StringUtilities.concat(baseUrl, "/js/leaflet-embed.js");

            List<XYZTileLayerInfo> layers = queryLayers(baseUrl, url);
            myAvailableServers.add(baseUrl);
            queryReceiver.receive(new SimpleSessionOnlyCacheDeposit<>(category, XYZTileUtils.LAYERS_DESCRIPTOR, layers));
        }
        catch (IOException | CacheException e)
        {
            throw new QueryException(e);
        }
    }

    /**
     * Query for the list of available layers.
     *
     * @param baseUrl The base url to the server.
     * @param url the URL
     * @return the layers
     * @throws IOException If something went wrong
     */
    private List<XYZTileLayerInfo> queryLayers(String baseUrl, String url) throws IOException
    {
        List<XYZTileLayerInfo> layers = New.list();
        try (CancellableInputStream stream = HttpUtilities.sendGet(UrlUtilities.toURL(url), myServerProvider))
        {
            String javaScript = new StreamReader(stream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);

            Map<String, String> layerToVar1Map = New.map();
            Matcher matcher = Pattern.compile("var (.+?)=.+?v4/(.+?)/").matcher(javaScript);
            while (matcher.find())
            {
                layerToVar1Map.put(matcher.group(2), matcher.group(1));
            }

            Map<String, String> var1ToVar2Map = New.map();
            matcher = Pattern.compile("var (.+?) = new L.TileLayer\\((.+?),").matcher(javaScript);
            while (matcher.find())
            {
                var1ToVar2Map.put(matcher.group(2), matcher.group(1));
            }

            Map<String, String> var2ToDisplayNameMap = New.map();
            matcher = Pattern.compile("\"(.+?)\"\\s*:\\s*(\\w+)").matcher(javaScript);
            while (matcher.find())
            {
                var2ToDisplayNameMap.put(matcher.group(2), matcher.group(1));
            }

            XYZServerInfo serverInfo = new XYZServerInfo("Mapbox", baseUrl);
            for (Map.Entry<String, String> entry : layerToVar1Map.entrySet())
            {
                boolean added = false;
                String layerName = entry.getKey();
                String var1 = entry.getValue();
                String var2 = var1ToVar2Map.get(var1);
                if (var2 != null)
                {
                    String displayName = var2ToDisplayNameMap.get(var2);
                    if (displayName != null)
                    {
                        XYZTileLayerInfo layerInfo = new XYZTileLayerInfo(layerName, displayName, Projection.EPSG_3857, 1, false,
                                5, serverInfo);
                        layers.add(layerInfo);
                        added = true;
                    }
                }

                if (!added)
                {
                    XYZTileLayerInfo layerInfo = new XYZTileLayerInfo(layerName, layerName, Projection.EPSG_3857, 1, false, 4,
                            serverInfo);
                    layers.add(layerInfo);
                }
            }
        }
        return layers;
    }
}
