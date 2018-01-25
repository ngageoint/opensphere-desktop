package io.opensphere.wps;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.server.services.OGCServiceValidationResponse;
import io.opensphere.server.services.OGCServiceValidator;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wps.envoy.WpsGetCapabilitiesEnvoy;
import net.opengis.wps._100.WPSCapabilitiesType;

/**
 * A validator implementation used to verify the state of the WPS Server to which connections are being attempted.
 */
public class WpsServerValidator implements OGCServiceValidator
{
    /**
     * A dash surrounded by spaces.
     */
    private static final String DASH = " - ";

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new WPSServerValidator.
     *
     * @param toolbox The system toolbox.
     */
    public WpsServerValidator(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.services.OGCServiceValidator#getService()
     */
    @Override
    public String getService()
    {
        return OGCServerSource.WPS_SERVICE;
    }

    /**
     * Validate a connection to a server. This requests a capabilities document and verifies if the return is non-null.
     *
     * @param configParams the server configuration
     * @return the WPS server validation information
     */
    @Override
    public OGCServiceValidationResponse validate(ServerConnectionParams configParams)
    {
        OGCServiceValidationResponse response = new OGCServiceValidationResponse(
                configParams.getServerId(OGCServerSource.WPS_SERVICE));
        String error = null;
        try
        {
            WpsGetCapabilitiesEnvoy getCapabilitiesEnvoy = new WpsGetCapabilitiesEnvoy(myToolbox, configParams, null);
            WPSCapabilitiesType wpsCaps = getCapabilitiesEnvoy.getCapabilities();

            response.setValid(wpsCaps != null);
        }
        catch (QueryException e)
        {
            error = StringUtilities.concat("Connection to server failed. URL: ", configParams.getWpsUrl(), DASH, e.getMessage());
        }
        catch (Error e)
        {
            error = StringUtilities.concat("Authentication failed for ", configParams.getWpsUrl(), DASH, e.getMessage());
        }

        if (error != null && !error.isEmpty())
        {
            response.setErrorMessage(error);
        }

        return response;
    }
}
