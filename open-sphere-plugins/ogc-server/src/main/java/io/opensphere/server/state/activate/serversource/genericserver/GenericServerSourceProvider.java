package io.opensphere.server.state.activate.serversource.genericserver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.LayerType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.state.activate.serversource.ServerSourceProvider;
import io.opensphere.server.state.utilities.ServerStateUtilities;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.WFSLayerConfigurationManager;

/**
 * Constructs a list of OGCServerSource that are contained within the state
 * node. This server source provider will provide servers necessary for WMS and
 * WFS layers.
 *
 */
public class GenericServerSourceProvider implements ServerSourceProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GenericServerSourceProvider.class);

    /**
     * Retrieves the NRT URLs from the state node.
     */
    private final NRTUrlRetriever myNrtRetriever;

    /**
     * Retrieves the WFS URLs from the state node.
     */
    private final WfsUrlRetriever myWfsRetriever;

    /**
     * Retrieves the WMS URLs from the state node.
     */
    private final WmsUrlRetriever myWmsRetriever;

    /**
     * The configuration manager through which WFS layer configurations are
     * accessed.
     */
    private final WFSLayerConfigurationManager myLayerConfigurationManager;

    /**
     * Creates a new server source provider, using the supplied toolbox to
     * access application state.
     *
     * @param toolbox the toolbox through which application state is accessed.
     */
    public GenericServerSourceProvider(Toolbox toolbox)
    {
        myNrtRetriever = new NRTUrlRetriever(toolbox);
        myWfsRetriever = new WfsUrlRetriever(toolbox);
        myWmsRetriever = new WmsUrlRetriever();

        myLayerConfigurationManager = ServerToolboxUtils.getServerToolbox(toolbox).getLayerConfigurationManager();
    }

    @Override
    public List<IDataSource> getServersInNode(Node node)
    {
        List<URL> wfsUrls = myWfsRetriever.getUrls(node);
        List<URL> wmsUrls = myWmsRetriever.getUrls(node);
        List<URL> nrtUrls = myNrtRetriever.getUrls(node);
        return getServers(wfsUrls, wmsUrls, nrtUrls);
    }

    @Override
    public List<IDataSource> getServersInNode(StateType state)
    {
        List<URL> wfsUrls = getUrls(ServerStateUtilities.getWfsLayers(myLayerConfigurationManager, state));
        List<URL> wmsUrls = getUrls(CollectionUtilities.concat(ServerStateUtilities.getWmsLayers(state, true),
                ServerStateUtilities.getWmsLayers(state, false)));
        List<URL> nrtUrls = Collections.emptyList();
        return getServers(wfsUrls, wmsUrls, nrtUrls);
    }

    /**
     * Gets the servers to be activated.
     *
     * @param wfsUrls the WFS URLs
     * @param wmsUrls the WMS URLs
     * @param nrtUrls the NRT URLs
     * @return the servers to be activated
     */
    private List<IDataSource> getServers(Collection<URL> wfsUrls, Collection<URL> wmsUrls, Collection<URL> nrtUrls)
    {
        List<IDataSource> servers = New.list();

        Map<String, URL> hostToWfs = mapHostToUrl(wfsUrls);
        Map<String, URL> hostToWms = mapHostToUrl(wmsUrls);
        Map<String, URL> hostToNrt = mapHostToUrl(nrtUrls);

        Set<String> hosts = New.set(hostToWfs.keySet());
        hosts.addAll(hostToWms.keySet());
        hosts.addAll(hostToNrt.keySet());

        for (String host : hosts)
        {
            OGCServerSource serverSource = createNewServerSource();
            serverSource.setName(host);

            URL wfsUrl = hostToWfs.get(host);
            if (wfsUrl != null)
            {
                serverSource.setWFSServerURL(wfsUrl.toString());
            }

            URL wmsUrl = hostToWms.get(host);
            if (wmsUrl != null)
            {
                serverSource.setWMSServerURL(wmsUrl.toString());
            }

            URL nrtUrl = hostToNrt.get(host);
            if (nrtUrl != null)
            {
                String nrtUrlString = nrtUrl.toString();

                if (StringUtils.isEmpty(serverSource.getWFSServerURL()))
                {
                    serverSource.setWFSServerURL(nrtUrlString.replace("wps", "wfs"));
                }

                if (StringUtils.isEmpty(serverSource.getWMSServerURL()))
                {
                    serverSource.setWMSServerURL(nrtUrlString.replace("wfs", "wms").replace("wps", "wms"));
                }

                serverSource.setWPSServerURL(nrtUrlString.replace("wfs", "wps"));
            }

            servers.add(serverSource);
        }

        return servers;
    }

    /**
     * Generates a new {@link OGCServerSource} for the servers to be activated.
     *
     * @return a new blank server source.
     */
    protected OGCServerSource createNewServerSource()
    {
        return new OGCServerSource();
    }

    /**
     * Converts the layers to URLs.
     *
     * @param layers the layers
     * @return the URLs
     */
    private static List<URL> getUrls(Collection<LayerType> layers)
    {
        return layers.stream().map(l -> toUrl(l.getUrl())).filter(u -> u != null).collect(Collectors.toList());
    }

    /**
     * Converts a string into a URL.
     *
     * @param s the string
     * @return the URL
     */
    private static URL toUrl(String s)
    {
        URL url;
        try
        {
            url = new URI(s).normalize().toURL();
        }
        catch (MalformedURLException | URISyntaxException e)
        {
            url = null;
            LOGGER.error(e.getMessage(), e);
        }
        return url;
    }

    /**
     * Map the hosts to URLs.
     *
     * @param urls The URLs to map.
     * @return A map of hosts to URLs.
     */
    private Map<String, URL> mapHostToUrl(Collection<URL> urls)
    {
        Map<String, URL> map = New.map();
        for (URL url : urls)
        {
            String host = url.getHost();
            map.put(host, url);
        }
        return map;
    }
}
