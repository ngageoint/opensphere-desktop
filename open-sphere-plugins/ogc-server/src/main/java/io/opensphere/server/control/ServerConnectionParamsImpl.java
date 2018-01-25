package io.opensphere.server.control;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.common.connection.BasicAuthenticationConfiguration;
import io.opensphere.core.common.connection.CertificateConfiguration;
import io.opensphere.core.common.connection.ProxyConfiguration;
import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.services.ServerTimeoutChangeEvent;
import io.opensphere.server.source.AuthenticationHelper;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.util.ServerConstants;

/**
 * The Class ServerConnectionPayload.
 */
public class ServerConnectionParamsImpl implements ServerConnectionParams
{
    /** The Logger. */
    private static final Logger LOGGER = Logger.getLogger(ServerConnectionParamsImpl.class);

    /** The parameters needed to connect to the server. */
    private final ServerConfiguration myServerConfig;

    /** Special rules for how to configure/format requests to the server. */
    private final ServerCustomization myServerCustomization;

    /** My map of URLs to the service they connect with. */
    private final Map<String, String> myServerUrls;

    /** The Server title. */
    private final String myServerTitle;

    /** The Source session unique id. */
    private final int mySourceSessionUniqueId;

    /** The time budget. */
    private PausingTimeBudget myTimeBudget;

    /** The Timeout changed event listener. */
    private final EventListener<ServerTimeoutChangeEvent> myTimeoutChangedEventListener;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new server connection payload.
     *
     * @param source the source
     * @param parentComponentProvider Provider of a parent component to use for
     *            dialog parents.
     * @param tb the {@link Toolbox}
     */
    public ServerConnectionParamsImpl(OGCServerSource source, Supplier<? extends Component> parentComponentProvider, Toolbox tb)
    {
        this(source, parentComponentProvider, tb, null);
    }

    /**
     * Instantiates a new server connection payload.
     *
     * @param source the source
     * @param parentComponentProvider Provider of a parent component to use for
     *            dialog parents.
     * @param tb the {@link Toolbox}
     * @param timeBudget Optional time budget.
     */
    public ServerConnectionParamsImpl(OGCServerSource source, Supplier<? extends Component> parentComponentProvider, Toolbox tb,
            PausingTimeBudget timeBudget)
    {
        myToolbox = tb;
        myTimeBudget = timeBudget;
        mySourceSessionUniqueId = source.getSessionUniqueId();

        myServerConfig = new ServerConfiguration();
        populateServerConfig(myServerConfig, source, parentComponentProvider, timeBudget);

        myServerUrls = new HashMap<>();
        populateServerUrls(myServerUrls, source);

        myServerCustomization = getCustomization(source.getServerType());
        myServerTitle = source.getName();

        myTimeoutChangedEventListener = new EventListener<ServerTimeoutChangeEvent>()
        {
            @Override
            public void notify(ServerTimeoutChangeEvent event)
            {
                handleServerTimeoutChangeEvent(event);
            }
        };
        tb.getEventManager().subscribe(ServerTimeoutChangeEvent.class, myTimeoutChangedEventListener);
    }

    @Override
    public void failedAuthentication()
    {
        AuthenticationHelper.failedAuthentication(myServerConfig);
    }

    @Override
    public ServerConfiguration getServerConfiguration()
    {
        return myServerConfig;
    }

    @Override
    public ServerCustomization getServerCustomization()
    {
        return myServerCustomization;
    }

    @Override
    public String getServerId(String service)
    {
        return getUrl(service);
    }

    @Override
    public String getServerTitle()
    {
        return myServerTitle;
    }

    @Override
    public PausingTimeBudget getTimeBudget()
    {
        return myTimeBudget;
    }

    /**
     * Gets the URL for a specified service.
     *
     * @param service the service (e.g. WMS, WFS, or WPS)
     * @return the URL for the specified service
     */
    public String getUrl(String service)
    {
        return myServerUrls.get(service);
    }

    @Override
    public String getWfsUrl()
    {
        return getUrl(OGCServerSource.WFS_SERVICE);
    }

    @Override
    public String getWmsGetMapOverride()
    {
        return getUrl(OGCServerSource.WMS_GETMAP_SERVICE);
    }

    @Override
    public String getWmsUrl()
    {
        return getUrl(OGCServerSource.WMS_SERVICE);
    }

    @Override
    public String getWpsUrl()
    {
        return getUrl(OGCServerSource.WPS_SERVICE);
    }

    @Override
    public void setTimeBudget(PausingTimeBudget timeBudget)
    {
        myTimeBudget = timeBudget;
    }

    /**
     * Gets the server customization based on the provided server type.
     *
     * @param serverType the server type
     * @return the customization for the specified server type
     */
    private ServerCustomization getCustomization(String serverType)
    {
        ServerToolbox serverToolbox = ServerToolboxUtils.getServerToolbox(myToolbox);
        ServerSourceControllerManager mgr = serverToolbox.getServerSourceControllerManager();
        ServerSourceController ctrl = mgr.getServerSourceController(serverType);
        return ctrl == null ? new DefaultCustomization() : ctrl.getServerCustomization(serverType);
    }

    /**
     * Handle server timeout change event.
     *
     * @param event the event
     */
    private void handleServerTimeoutChangeEvent(ServerTimeoutChangeEvent event)
    {
        if (event != null && event.getOGCServerSourceSessionUniqueId() == mySourceSessionUniqueId)
        {
            LOGGER.info("Changing Timeouts for Payload[" + myServerTitle + "]UqId[" + mySourceSessionUniqueId + "] Read(ms)["
                    + event.getReadTimeoutMS() + "] Connect(ms)[" + event.getConnectTimeoutMS() + "]");
            myServerConfig.setReadTimeout(event.getReadTimeoutMS());
            myServerConfig.setConnectTimeout(event.getConnectTimeoutMS());
            updateServerConnectionTimeouts(event);
        }
    }

    /**
     * Populate server config.
     *
     * @param server the server
     * @param source the source
     * @param parentComponentProvider Provider of a parent component to use for
     *            dialog parents.
     * @param timeBudget the time budget
     */
    private void populateServerConfig(ServerConfiguration server, OGCServerSource source,
            Supplier<? extends Component> parentComponentProvider, PausingTimeBudget timeBudget)
    {
        if (server != null)
        {
            // Set these even though they're not used, so that clone doesn't
            // fail.
            server.setProxyConfiguration(new ProxyConfiguration());
            server.setBasicAuthenticationConfiguration(new BasicAuthenticationConfiguration());
            server.setCertificateConfiguration(new CertificateConfiguration());

            // Set the protocol, host, and port from WMS or WFS. WPS relies on
            // WFS layers, so don't use that.
            String baseUrl = null;
            if (source.getWMSServerURL() != null && !source.getWMSServerURL().isEmpty())
            {
                baseUrl = source.getWMSServerURL();
            }
            else if (source.getWFSServerURL() != null && !source.getWFSServerURL().isEmpty())
            {
                baseUrl = source.getWFSServerURL();
            }
            if (baseUrl != null)
            {
                try
                {
                    URL tempUrl = new URL(baseUrl);
                    server.setProtocol(tempUrl.getProtocol());
                    server.setHost(tempUrl.getHost());

                    int port = tempUrl.getPort() < 0 ? tempUrl.getDefaultPort() : tempUrl.getPort();
                    server.setPort(port);
                }
                catch (MalformedURLException e)
                {
                    // URLs should be validated before this class is built, but
                    // log a warning just in case.
                    LOGGER.info("Failed to encode URL while building server configuration.", e);
                }
            }

            populateTimeouts(server, source);
        }
    }

    /**
     * Populate the server URLs from a configured source.
     *
     * @param urls the map of URLs to populate
     * @param source the source to populate from
     */
    private void populateServerUrls(Map<String, String> urls, OGCServerSource source)
    {
        if (StringUtils.isNotEmpty(source.getWMSServerURL()))
        {
            urls.put(OGCServerSource.WMS_SERVICE, source.getWMSServerURL());
        }

        if (StringUtils.isNotEmpty(source.getWMSGetMapServerUrlOverride()))
        {
            urls.put(OGCServerSource.WMS_GETMAP_SERVICE, source.getWMSGetMapServerUrlOverride());
        }

        if (StringUtils.isNotEmpty(source.getWFSServerURL()))
        {
            urls.put(OGCServerSource.WFS_SERVICE, source.getWFSServerURL());
        }

        if (StringUtils.isNotEmpty(source.getWPSServerURL()))
        {
            urls.put(OGCServerSource.WPS_SERVICE, source.getWPSServerURL());
        }
    }

    /**
     * Populate timeouts.
     *
     * @param server the server
     * @param source the source
     */
    private void populateTimeouts(ServerConfiguration server, OGCServerSource source)
    {
        int sourceConnectTimeout = source.getConnectTimeoutMillis();
        if (sourceConnectTimeout <= 0)
        {
            sourceConnectTimeout = ServerConstants.getDefaultServerConnectTimeoutFromPrefs(myToolbox.getPreferencesRegistry());
        }
        int sourceReadTimeout = source.getReadTimeoutMillis();
        if (sourceReadTimeout <= 0)
        {
            sourceReadTimeout = ServerConstants.getDefaultServerReadTimeoutFromPrefs(myToolbox.getPreferencesRegistry());
        }
        server.setConnectTimeout(sourceConnectTimeout);
        server.setReadTimeout(sourceReadTimeout);
    }

    /**
     * Update server connection timeouts.
     *
     * @param event the event
     */
    private void updateServerConnectionTimeouts(ServerTimeoutChangeEvent event)
    {
        ServerProvider<HttpServer> provider = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class);

        HttpServer server = provider.getServer(myServerConfig.getHost(), myServerConfig.getProtocol(), myServerConfig.getPort());
        server.setTimeouts(myServerConfig.getReadTimeout(), myServerConfig.getConnectTimeout());
    }
}
