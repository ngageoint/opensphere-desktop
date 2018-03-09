package io.opensphere.arcgis2.server;

import io.opensphere.arcgis2.mantle.ArcGISMantleController;
import io.opensphere.arcgis2.mantle.ArcGISToolbox;
import io.opensphere.core.Toolbox;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.server.control.UrlServerSourceController;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.display.ServiceValidator;
import io.opensphere.server.toolbox.ServerSourceController;

/** ArcGIS {@link ServerSourceController}. */
public class ArcGISServerSourceController extends UrlServerSourceController
{
    /** The mantle controller. */
    private volatile ArcGISMantleController myMantleController;

    /** The core toolbox. */
    private Toolbox myToolbox;

    @Override
    public void open(Toolbox toolbox, Class<?> prefsTopic)
    {
        myMantleController = toolbox.getPluginToolboxRegistry().getPluginToolbox(ArcGISToolbox.class).getMantleController();
        myToolbox = toolbox;
        super.open(toolbox, prefsTopic);
    }

    @Override
    public IDataSource createNewSource(String typeName)
    {
        return new UrlDataSource();
    }

    @Override
    protected ServerCustomization getServerCustomization()
    {
        return new DefaultCustomization("ArcGIS");
    }

    @Override
    protected ServiceValidator<UrlDataSource> getValidator(ServerProviderRegistry registry)
    {
        return new ArcGISServerSourceValidator(registry);
    }

    @Override
    protected String getExampleUrl()
    {
        return "http://yourarcserver.com/ArcGIS/rest/services";
    }

    @Override
    protected boolean handleActivateSource(IDataSource source)
    {
        try (TaskActivity activity = TaskActivity.createActive(source.getName() + " is loading.."))
        {
            myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(activity);
            return myMantleController.addServer(source.getName(), ((UrlDataSource)source).getURL());
        }
    }

    @Override
    protected void handleDeactivateSource(IDataSource source)
    {
        myMantleController.removeServer(source.getName(), ((UrlDataSource)source).getURL());
    }
}
