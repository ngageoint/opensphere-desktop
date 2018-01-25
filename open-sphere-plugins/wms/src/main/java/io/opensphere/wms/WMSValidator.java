package io.opensphere.wms;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.server.services.OGCServiceValidationResponse;
import io.opensphere.server.services.OGCServiceValidator;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wms.capabilities.WMSServerCapabilities;
import io.opensphere.wms.util.WMSEnvoyHelper;

/**
 * Validator for the WMS service.
 */
public class WMSValidator implements OGCServiceValidator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WMSValidator.class);

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a WMS validator.
     *
     * @param toolbox The system toolbox.
     */
    public WMSValidator(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public String getService()
    {
        return OGCServerSource.WMS_SERVICE;
    }

    /**
     * Validate a connection to a server. This requests a capabilities document
     * and verifies if the return is non-null.
     *
     * @param pServConf the server configuration
     *
     * @return Some information about the server relevant to validation
     */
    @Override
    public OGCServiceValidationResponse validate(ServerConnectionParams pServConf)
    {
        String serverId = pServConf.getServerId(OGCServerSource.WMS_SERVICE);
        final OGCServiceValidationResponse response = new OGCServiceValidationResponse(serverId);
        String error;
        try
        {
            // Clear out any existing connections, then get capabilities
            WMSServerCapabilities caps = WMSEnvoyHelper.requestCapabilitiesFromServer(pServConf, myToolbox);
            if (caps == null)
            {
                error = "Server did not provide a 1.3.0 or 1.1.1 compliant capabilities document.";
            }
            else
            {
                response.setServerTitle(caps.getTitle());
                error = null;
            }
        }
        catch (MalformedURLException | URISyntaxException e)
        {
            error = StringUtilities.concat("WMS URL is not properly formatted for [", serverId, "] - ", e.getMessage());
        }
        catch (SSLHandshakeException e)
        {
            error = StringUtilities.concat("Failed to negotiate SSL handshake. URL: ", serverId);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(error, e);
            }
        }
        catch (IOException e)
        {
            error = StringUtilities.concat("Connection to server failed. URL: ", serverId, " - ", e.getMessage());
        }
        catch (GeneralSecurityException e)
        {
            error = StringUtilities.concat("Authentication failed for ", serverId, " - ", e.getMessage());
        }
        catch (Error e)
        {
            error = StringUtilities.concat("Authentication failed for ", serverId, " - ", e.getMessage());
        }
        catch (InterruptedException e)
        {
            error = StringUtilities.concat("Request was cancelled to validate ", serverId);
        }
        if (error != null && !error.isEmpty())
        {
            response.setErrorMessage(error);
        }
        else
        {
            response.setValid(true);
        }

        return response;
    }
}
