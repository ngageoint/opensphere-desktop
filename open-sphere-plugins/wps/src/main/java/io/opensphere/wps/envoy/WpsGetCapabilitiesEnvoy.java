package io.opensphere.wps.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.QueryException;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wps.util.ServerException;
import io.opensphere.wps.util.WpsServerConnectionHelper;
import net.opengis.wps._100.WPSCapabilitiesType;

/**
 * An envoy implementation used to execute a get capabilities request against a single server.
 */
public class WpsGetCapabilitiesEnvoy extends AbstractWpsDataRegistryEnvoy<WPSCapabilitiesType>
{
    /**
     * The data group for the server.
     */
    private final DataGroupInfo myServerDataGroup;

    /**
     * Creates a new envoy through which a WPS Get Capabilities request is executed for a single server.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     * @param pServer the configuration of the server to which the query will be made.
     * @param pServerDataGroup The data group for the server.
     */
    public WpsGetCapabilitiesEnvoy(Toolbox pToolbox, ServerConnectionParams pServer, DataGroupInfo pServerDataGroup)
    {
        super(pToolbox, pServer, WpsRequestType.GET_CAPABLITIES, WpsPropertyDescriptors.WPS_GET_CAPABILITIES);

        myServerDataGroup = pServerDataGroup;
    }

    /**
     * Requests a capabilities document from a remote server, but does so when the server has not yet been connected. This occurs
     * primarily when the user has added an entry for the server, but not yet connected to it, and clicks "validate".
     *
     * @return a {@link WPSCapabilitiesType} instance retrieved from the remote server.
     * @throws QueryException if the remote server cannot be accessed.
     */
    public WPSCapabilitiesType getCapabilities() throws QueryException
    {
        return executeQuery(
                new WpsServerConnectionHelper(WpsUrlHelper.buildGetCapabilitiesUrl(getServer().getWpsUrl()), getToolbox()),
                getParameterMap());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#executeQuery(io.opensphere.wps.util.WpsServerConnectionHelper,
     *      java.util.Map)
     */
    @Override
    protected WPSCapabilitiesType executeQuery(WpsServerConnectionHelper pHelper, Map<String, String> pParameters)
        throws QueryException
    {
        WPSCapabilitiesType responseObject;
        try (InputStream stream = executeStreamQuery(pHelper, pParameters))
        {
            responseObject = pHelper.parseStream(WPSCapabilitiesType.class, stream);
        }
        catch (ServerException e)
        {
            throw new QueryException(
                    "Unable to parse WPS get capabilities response from server '" + getServer().getWpsUrl() + "'", e);
        }
        catch (IOException e)
        {
            throw new QueryException("Unable to query remote server '" + getServer().getWpsUrl() + "' for WPS Get Capabilities",
                    e);
        }
        return responseObject;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#executeStreamQuery(io.opensphere.wps.util.WpsServerConnectionHelper,
     *      java.util.Map)
     */
    @Override
    protected InputStream executeStreamQuery(WpsServerConnectionHelper pHelper, Map<String, String> pParameters)
        throws QueryException
    {
        try
        {
            return pHelper.requestStream();
        }
        catch (ServerException e)
        {
            throw new QueryException("Unable to query remote server '" + getServer().getWpsUrl() + "' for WPS Get Capabilities",
                    e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsDataRegistryEnvoy#getParameterMap(java.util.List)
     */
    @Override
    protected Map<String, String> getParameterMap(List<? extends PropertyMatcher<?>> pParameters)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#fireStateEvent(java.lang.String)
     */
    @Override
    protected void fireStateEvent(String errorMessage)
    {
        fireStateEvent(getServer(), errorMessage != null, errorMessage, getToolbox().getEventManager(), myServerDataGroup);
    }
}
