package io.opensphere.server.display;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.server.services.OGCServiceValidationResponse;
import io.opensphere.server.services.OGCServiceValidator;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.toolbox.ServerValidatorRegistry;

/**
 * OGCServerValidationEnvoy validates OGC services that are registered with the
 * {@link ServerValidatorRegistry}.
 */
public class OGCServerValidationEnvoy extends AbstractEnvoy
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(OGCServerValidationEnvoy.class);

    /**
     * Instantiates a new envoy that validates OGC Services.
     *
     * @param toolbox the Core toolbox
     */
    public OGCServerValidationEnvoy(Toolbox toolbox)
    {
        super(toolbox);
    }

    @Override
    public void open()
    {
    }

    /**
     * Validate an OGC Service.
     *
     * @param validator the validator for the specific OGC service
     * @param params the parameters needed to connect to the server
     * @param timeBudget the time budget for the validation
     * @return the OGC service validation response
     */
    public OGCServiceValidationResponse validate(final OGCServiceValidator validator, final ServerConnectionParams params,
            PausingTimeBudget timeBudget)
    {
        Callable<OGCServiceValidationResponse> validateTask = new Callable<OGCServiceValidationResponse>()
        {
            @Override
            public OGCServiceValidationResponse call()
            {
                return validator.validate(params);
            }
        };

        String error = null;
        OGCServiceValidationResponse response = null;
        String serverId = params.getServerId(validator.getService());
        timeBudget.unpause();
        Future<OGCServiceValidationResponse> result = getExecutor().submit(validateTask);
        while (response == null && !timeBudget.isExpired() && error == null)
        {
            try
            {
                response = result.get(timeBudget.getRemainingNanoseconds(), TimeUnit.NANOSECONDS);
            }
            catch (InterruptedException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e, e);
                }
            }
            catch (TimeoutException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e, e);
                }
            }
            catch (ExecutionException e)
            {
                // Handle exceptions thrown from Callable.call() method
                error = "Exception received while validating server [" + serverId + "]: " + e.getMessage();
                UserMessageEvent.error(super.getToolbox().getEventManager(), error, false, true);
                LOGGER.info(e, e);
            }
        }
        if (response == null && timeBudget.isExpired() && error == null)
        {
            error = "Timed out waiting for response from " + serverId;
        }
        if (StringUtils.isNotEmpty(error))
        {
            response = new OGCServiceValidationResponse(serverId);
            response.setErrorMessage(error);
        }

        return response;
    }
}
