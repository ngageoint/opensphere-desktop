package io.opensphere.wps.ui.detail;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import io.opensphere.core.util.Visitor;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.util.WPSConstants;
import net.opengis.wps._100.ProcessDescriptionType;

/**
 * A visitor implementation used to gather data from individual components.
 */
public class WpsProcessConfigurationVisitor implements Visitor<WpsProcessConfiguration>
{
    /**
     * The process configuration populated during visiting hours.
     */
    private final WpsProcessConfiguration myResultContainer;

    /**
     * The description of the process being visited.
     */
    private final ProcessDescriptionType myProcessDescriptionType;

    /**
     * Creates a new visitor, configured to accept parameters for the supplied process.
     *
     * @param pServerId the unique identifier of the server on which the process will be executed.
     * @param pProcessDescriptionType The description of the process being visited.
     */
    public WpsProcessConfigurationVisitor(String pServerId, ProcessDescriptionType pProcessDescriptionType)
    {
        myProcessDescriptionType = pProcessDescriptionType;
        myResultContainer = new WpsProcessConfiguration(pServerId, myProcessDescriptionType);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Visitor#setValue(java.lang.String, java.lang.String)
     */
    @Override
    public void setValue(String pParameterName, String pValue)
    {
        myResultContainer.getInputs().put(pParameterName, pValue);
        if (StringUtils.equals(pParameterName, WPSConstants.PROCESS_INSTANCE_NAME))
        {
            myResultContainer.setProcessTitle(pValue);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Visitor#getValues()
     */
    @Override
    public Map<String, String> getValues()
    {
        return myResultContainer.getInputs();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Visitor#getResult()
     */
    @Override
    public WpsProcessConfiguration getResult()
    {
        return myResultContainer;
    }
}
