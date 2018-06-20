package io.opensphere.wfs;

import java.net.MalformedURLException;
import java.net.URL;

import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.server.services.OGCServiceValidationResponse;
import io.opensphere.server.services.OGCServiceValidator;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.util.OGCServerConnector;
import io.opensphere.server.util.OGCServerException;
import io.opensphere.wfs.envoy.WFSEnvoyHelper;
import net.opengis.wfs._110.WFSCapabilitiesType;

/**
 * Validator for the WFS service.
 */
public class WFSValidator implements OGCServiceValidator
{
    /**
     * The registry of server providers.
     */
    private final ServerProviderRegistry myServerRegistry;

    /** The envoy helper. */
    private final WFSEnvoyHelper myEnvoyHelper;

    /**
     * Get a connector for the URL.
     *
     * @param url The URL.
     * @param params the params
     * @param serverProvider The registry of server providers.
     * @return The connector.
     */
    private static OGCServerConnector getConnector(URL url, ServerConnectionParams params, ServerProviderRegistry serverProvider)
    {
        return new OGCServerConnector(url, serverProvider);
    }

    /**
     * Get the Server title (name) from an OGC Server's WFS GetCapabilities doc.
     * As long as the server returns a valid capabilities doc, return a valid
     * string (even if it's empty).
     *
     * @param serverCfg the server configuration
     * @param serverId the ID that uniquely identifies this server
     * @param serverProvider The registry of server providers.
     * @param envoyHelper the helper instance with which to build the
     *            GetCapabilties URL.
     * @return the server title from the capabilities doc
     * @throws OGCServerException If there is a problem communicating with the
     *             server.
     */
    private static OGCServiceValidationResponse getTitleFromOGCCapabilities(ServerConnectionParams serverCfg, String serverId,
            ServerProviderRegistry serverProvider, WFSEnvoyHelper envoyHelper)
        throws OGCServerException
    {
        try
        {
            URL url = envoyHelper.buildGetCapabilitiesURL(serverCfg);
            OGCServerConnector connector = getConnector(url, serverCfg, serverProvider);
            WFSCapabilitiesType caps = connector.requestObject(WFSCapabilitiesType.class);
            if (caps != null && caps.getServiceIdentification() != null && caps.getServiceIdentification().getTitle() != null)
            {
                OGCServiceValidationResponse response = new OGCServiceValidationResponse(serverId);
                response.setServerTitle(caps.getServiceIdentification().getTitle());
                response.setValid(true);
                return response;
            }
        }
        catch (MalformedURLException e)
        {
            return null;
        }
        return null;
    }

    /**
     * Test for valid WFS URL against known interfaces.
     *
     * @param servConf the server config
     * @param serverId the ID that uniquely identifies this server
     * @param serverProvider The registry of server providers.
     * @param envoyHelper the helper class used to build the GetCapabilities
     *            URL, if needed.
     * @return the WFS server validation information
     * @throws OGCServerException If there is a problem communicating with the
     *             server.
     */
    private static OGCServiceValidationResponse testForValidUrl(ServerConnectionParams servConf, String serverId,
            ServerProviderRegistry serverProvider, WFSEnvoyHelper envoyHelper)
        throws OGCServerException
    {
        OGCServiceValidationResponse response = null;
        OGCServerException exception = null;

        // Try the OGC WFS interface first
        try
        {
            response = getTitleFromOGCCapabilities(servConf, serverId, serverProvider, envoyHelper);
        }
        catch (OGCServerException e)
        {
            exception = e;
        }

        // Throw the server exception if it didn't work out
        if (response == null && exception != null)
        {
            throw exception;
        }

        return response;
    }

    /**
     * Constructs a new WFSValidator.
     *
     * @param serverRegistry The registry of server providers.
     * @param envoyHelper the envoy helper used to make remote calls, if needed.
     */
    public WFSValidator(ServerProviderRegistry serverRegistry, WFSEnvoyHelper envoyHelper)
    {
        myServerRegistry = serverRegistry;
        myEnvoyHelper = envoyHelper;
    }

    @Override
    public String getService()
    {
        return OGCServerSource.WFS_SERVICE;
    }

    /**
     * Validate a connection to a server. This requests a capabilities document
     * and verifies if the return is non-null.
     *
     * @param servConf the server configuration
     * @return the WFS server validation information
     */
    @Override
    public OGCServiceValidationResponse validate(ServerConnectionParams servConf)
    {
        final String serverId = servConf.getServerId(OGCServerSource.WFS_SERVICE);
        OGCServiceValidationResponse response = null;
        String error = null;
        try
        {
            response = testForValidUrl(servConf, serverId, myServerRegistry, myEnvoyHelper);
            if (response == null)
            {
                error = "Failed to validate URL using WFS and ARC Rest interfaces:\n" + serverId;
            }
        }
        catch (OGCServerException e)
        {
            error = StringUtilities.concat("Connection to server failed for ", serverId, " - ", e.getMessage());
        }
        catch (Error e)
        {
            error = StringUtilities.concat("Authentication failed for ", serverId, " - ", e.getMessage());
        }

        if (response == null)
        {
            response = new OGCServiceValidationResponse(serverId);
            response.setErrorMessage(error);
        }

        return response;
    }
}
