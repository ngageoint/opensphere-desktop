package io.opensphere.stkterrain.server;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.stkterrain.envoy.TileSetEnvoy;

/**
 * If the {@link TileSetEnvoy} fails to get a list of tile sets we will
 * deactivate the server and show the user an error message.
 */
public class STKValidatorSupport extends DefaultValidatorSupport
{
    /**
     * The server in question.
     */
    private final IDataSource mySource;

    /**
     * The server controller, used to deactivate the server if need be.
     */
    private final STKServerController mySourceController;

    /**
     * Constructs a new validator support.
     *
     * @param server The server in question.
     * @param controller The server controller, used to deactivate the server if
     *            need be.
     */
    public STKValidatorSupport(IDataSource server, STKServerController controller)
    {
        super(null);
        mySource = server;
        mySourceController = controller;
    }

    @Override
    public void setValidationResult(ValidationStatus successful, String message)
    {
        if (successful == ValidationStatus.ERROR)
        {
            Notify.error(message, Method.TOAST);
        }
        else if (successful == ValidationStatus.WARNING)
        {
            Notify.warn(message, Method.TOAST);
        }

        boolean success = successful != ValidationStatus.ERROR;

        if (!success)
        {
            mySource.setLoadError(true, this);
            mySourceController.deactivateSource(mySource);
        }
    }
}
