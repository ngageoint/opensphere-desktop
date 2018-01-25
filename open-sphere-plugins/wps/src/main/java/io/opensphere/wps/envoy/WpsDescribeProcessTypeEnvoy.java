package io.opensphere.wps.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wps.util.ServerException;
import io.opensphere.wps.util.WpsServerConnectionHelper;
import net.opengis.wps._100.ProcessDescriptionType;
import net.opengis.wps._100.ProcessDescriptions;

/**
 * An envoy implementation used to execute a WPS Describe Process Type request against a single server.
 */
public class WpsDescribeProcessTypeEnvoy extends AbstractWpsDataRegistryEnvoy<ProcessDescriptionType>
{
    /**
     * Creates a new envoy through which a WPS Describe Process Type request is executed for a single server.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     * @param pServer the configuration of the server to which the query will be made.
     */
    public WpsDescribeProcessTypeEnvoy(Toolbox pToolbox, ServerConnectionParams pServer)
    {
        super(pToolbox, pServer, WpsRequestType.DESCRIBE_PROCESS_TYPE, WpsPropertyDescriptors.WPS_DESCRIBE_PROCESS);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.AbstractEnvoy#open()
     */
    @Override
    public void open()
    {
        // Overridden in order to avoid unnecessary query at startup
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsDataRegistryEnvoy#getParameterMap(java.util.List)
     */
    @Override
    protected Map<String, String> getParameterMap(List<? extends PropertyMatcher<?>> pParameters)
    {
        Map<String, String> parameters = New.map();

        if (pParameters != null)
        {
            for (PropertyMatcher<?> propertyMatcher : pParameters)
            {
                parameters.put(propertyMatcher.getPropertyDescriptor().getPropertyName(), (String)propertyMatcher.getOperand());
            }
        }

        return parameters;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#executeQuery(java.util.Map)
     */
    @Override
    protected ProcessDescriptionType executeQuery(Map<String, String> pParameters) throws QueryException
    {
        return executeQuery(new WpsServerConnectionHelper(WpsUrlHelper.buildDescribeProcessUrl(getServer().getWpsUrl(),
                pParameters.get(WpsParameter.PROCESS_ID.getVariableName())), getToolbox()), pParameters);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#executeQuery(io.opensphere.wps.util.WpsServerConnectionHelper,
     *      java.util.Map)
     */
    @Override
    protected ProcessDescriptionType executeQuery(WpsServerConnectionHelper pHelper, Map<String, String> pParameters)
        throws QueryException
    {
        ProcessDescriptionType responseObject = null;
        try (InputStream stream = executeStreamQuery(pHelper, pParameters))
        {
            ProcessDescriptions descriptions = pHelper.parseStream(ProcessDescriptions.class, stream);
            if (!descriptions.getProcessDescription().isEmpty())
            {
                responseObject = descriptions.getProcessDescription().get(0);
            }
        }
        catch (ServerException | IOException e)
        {
            throw new QueryException(
                    "Unable to query remote server '" + getServer().getWpsUrl() + "' for WPS Describe Process Type, for process '"
                            + pParameters.get(WpsParameter.PROCESS_ID.getVariableName()) + "'",
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
        InputStream returnValue = null;
        try
        {
            returnValue = pHelper.requestStream();
        }
        catch (ServerException e)
        {
            throw new QueryException(
                    "Unable to query remote server '" + getServer().getWpsUrl() + "' for WPS Describe Process Type, for process '"
                            + pParameters.get(WpsParameter.PROCESS_ID.getVariableName()) + "'",
                    e);
        }

        return returnValue;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.envoy.AbstractWpsEnvoy#executeStreamQuery(java.util.Map)
     */
    @Override
    protected InputStream executeStreamQuery(Map<String, String> pParameters) throws QueryException
    {
        WpsServerConnectionHelper helper = new WpsServerConnectionHelper(WpsUrlHelper.buildDescribeProcessUrl(
                getServer().getWpsUrl(), pParameters.get(WpsParameter.PROCESS_ID.getVariableName())), getToolbox());
        return executeStreamQuery(helper, pParameters);
    }
}
