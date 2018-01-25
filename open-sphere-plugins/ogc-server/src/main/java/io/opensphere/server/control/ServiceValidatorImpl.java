package io.opensphere.server.control;

import java.awt.Component;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.DefaultComponentSupplier;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.server.display.OGCServerValidationEnvoy;
import io.opensphere.server.display.ServiceValidator;
import io.opensphere.server.services.OGCServiceValidationResponse;
import io.opensphere.server.services.OGCServiceValidator;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.ServerValidatorRegistry;

/**
 * Validates an OGC service.
 */
public class ServiceValidatorImpl extends ServiceValidator<OGCServerSource>
{
    /** The toolbox. */
    private final transient Toolbox myToolbox;

    /** The parent component. */
    private Component myParentComponent;

    /** The data source. */
    private OGCServerSource mySource;

    /** The service. */
    private String myService;

    /** Envoy used to execute "validate" commands. */
    private transient OGCServerValidationEnvoy myValidationEnvoy;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public ServiceValidatorImpl(Toolbox toolbox)
    {
        super();
        myToolbox = toolbox;
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        validateService();
        return super.getValidationStatus();
    }

    @Override
    public void setParent(Component parent)
    {
        myParentComponent = parent;
    }

    @Override
    public void setService(String service)
    {
        myService = service;
    }

    @Override
    public void setSource(OGCServerSource source)
    {
        mySource = source;
    }

    /**
     * Gets the validation envoy.
     *
     * @return the validation envoy
     */
    private OGCServerValidationEnvoy getValidationEnvoy()
    {
        if (myValidationEnvoy == null)
        {
            Collection<OGCServerValidationEnvoy> envoys = myToolbox.getEnvoyRegistry()
                    .getObjectsOfClass(OGCServerValidationEnvoy.class);
            if (CollectionUtilities.hasContent(envoys))
            {
                myValidationEnvoy = envoys.iterator().next();
            }
        }
        return myValidationEnvoy;
    }

    /**
     * Validate the URL for a given service.
     */
    private void validateService()
    {
        ValidationStatus isValid = ValidationStatus.ERROR;
        String displayError = null;
        String url = mySource.getURL(myService);
        if (StringUtils.isNotEmpty(url))
        {
            ServerValidatorRegistry validationRegistry = ServerToolboxUtils.getServerValidatorRegistry(myToolbox);
            OGCServiceValidator validator = validationRegistry.retrieve(myService);
            if (validator != null)
            {
                final PausingTimeBudget timeBudget = PausingTimeBudget.startMillisecondsPaused(30000L);
                ServerConnectionParams config = new ServerConnectionParamsImpl(mySource,
                        new DefaultComponentSupplier(myParentComponent), myToolbox, timeBudget);

                OGCServiceValidationResponse response = getValidationEnvoy().validate(validator, config, timeBudget);
                if (response.isValid())
                {
                    isValid = ValidationStatus.VALID;
                }
                else
                {
                    String message = StringUtils.isEmpty(response.getErrorMessage()) ? "Unknown error"
                            : response.getErrorMessage();

                    displayError = "Error validating " + myService + " Server: \n\n" + message;
                }
            }
            else
            {
                displayError = "Error validating " + myService + " Server: \n\nNo plugin is registered for " + myService
                        + " validation";
            }
        }
        else
        {
            displayError = "Error validating " + myService + " Server: \n\nEmpty URL for " + myService + " validation";
        }
        setValidationResult(isValid, displayError);
    }
}
