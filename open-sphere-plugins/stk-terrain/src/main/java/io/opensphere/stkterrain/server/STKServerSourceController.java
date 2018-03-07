package io.opensphere.stkterrain.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.api.Envoy;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.server.control.UrlServerSourceController;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.display.ServiceValidator;
import io.opensphere.stkterrain.envoy.QuantizedMeshEnvoy;
import io.opensphere.stkterrain.envoy.TileSetEnvoy;
import io.opensphere.stkterrain.envoy.TileSetMetadataEnvoy;
import io.opensphere.stkterrain.mantle.STKDataGroupController;

/**
 * The controller used to handle STK Terrain servers added/removed by the user.
 * This class is also responsible for adding the STK Terrain Server editor to
 * the Server Manager dialog.
 */
public class STKServerSourceController extends UrlServerSourceController implements STKServerController
{
    /**
     * The group controllers that are managing the layers for active STK terrain
     * servers.
     */
    private final Map<String, STKDataGroupController> myGroupControllers = Collections.synchronizedMap(New.map());

    @Override
    public IDataSource createNewSource(String typeName)
    {
        return new UrlDataSource("STK Terrain", getExampleUrl());
    }

    @Override
    protected ServerCustomization getServerCustomization()
    {
        return new DefaultCustomization("STK Terrain Server");
    }

    @Override
    protected ServiceValidator<UrlDataSource> getValidator(ServerProviderRegistry registry)
    {
        return new STKServerSourceValidator(registry);
    }

    @Override
    protected String getExampleUrl()
    {
        return "http://assets.agi.com/stk-terrain";
    }

    @Override
    protected boolean handleActivateSource(IDataSource source)
    {
        UrlDataSource urlSource = (UrlDataSource)source;

        ServiceValidator<UrlDataSource> validator = getValidator(getToolbox().getServerProviderRegistry());
        validator.setSource(urlSource);
        ValidationStatus status = validator.getValidationStatus();

        if (status != ValidationStatus.VALID)
        {
            handleDeactivateSource(source);
            UserMessageEvent.message(getToolbox().getEventManager(), Type.ERROR, validator.getValidationMessage(), false, this,
                    null, true);
        }
        else
        {
            String serverUrl = urlSource.getURLString();
            STKDataGroupController groupController = new STKDataGroupController(getToolbox(), urlSource.getName(), serverUrl);
            myGroupControllers.put(serverUrl, groupController);
            List<Envoy> envoys = New.list(new TileSetEnvoy(getToolbox(), new STKValidatorSupport(urlSource, this), serverUrl),
                    new TileSetMetadataEnvoy(getToolbox(), serverUrl), new QuantizedMeshEnvoy(getToolbox(), serverUrl));
            getToolbox().getEnvoyRegistry().addObjectsForSource(this, envoys);
        }

        return status == ValidationStatus.VALID;
    }

    @Override
    protected void handleDeactivateSource(IDataSource source)
    {
        UrlDataSource urlSource = (UrlDataSource)source;

        String serverUrl = urlSource.getURLString();
        STKDataGroupController groupController = myGroupControllers.remove(serverUrl);
        if (groupController != null)
        {
            groupController.close();
        }
        getToolbox().getDataRegistry().removeModels(new DataModelCategory(serverUrl, null, null), false);
    }
}
