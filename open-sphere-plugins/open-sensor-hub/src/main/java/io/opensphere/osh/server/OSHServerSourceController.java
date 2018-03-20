package io.opensphere.osh.server;

import io.opensphere.core.Toolbox;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.osh.mantle.OSHMantleController;
import io.opensphere.server.control.UrlServerSourceController;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.display.ServiceValidator;
import io.opensphere.server.toolbox.ServerSourceController;

/** OpenSensorHub {@link ServerSourceController}. */
public class OSHServerSourceController extends UrlServerSourceController
{
    /** The mantle controller. */
    private volatile OSHMantleController myMantleController;

    @Override
    public void open(Toolbox toolbox, Class<?> prefsTopic)
    {
        myMantleController = new OSHMantleController(toolbox);
        myMantleController.open();
        super.open(toolbox, prefsTopic);
    }

    @Override
    public IDataSource createNewSource(String typeName)
    {
        return new UrlDataSource("OpenSensorHub", getExampleUrl());
    }

    @Override
    protected ServerCustomization getServerCustomization()
    {
        return new DefaultCustomization("OpenSensorHub Server");
    }

    @Override
    protected ServiceValidator<UrlDataSource> getValidator(ServerProviderRegistry registry)
    {
        return new OSHServerSourceValidator(registry);
    }

    @Override
    protected String getExampleUrl()
    {
        return "http://sensiasoft.net:8181/sensorhub/sos";
    }

    @Override
    protected boolean handleActivateSource(IDataSource source)
    {
        return myMantleController.addServer(source.getName(), ((UrlDataSource)source).getURL());
    }

    @Override
    protected void handleDeactivateSource(IDataSource source)
    {
        myMantleController.removeServer(source.getName());
    }
}
