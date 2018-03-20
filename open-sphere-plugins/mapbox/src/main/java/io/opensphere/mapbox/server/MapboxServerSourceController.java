package io.opensphere.mapbox.server;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.mapbox.util.MapboxUtil;
import io.opensphere.server.control.UrlServerSourceController;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.display.ServiceValidator;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/** Mapbox {@link ServerSourceController}. */
public class MapboxServerSourceController extends UrlServerSourceController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MapboxServerSourceController.class);

    @Override
    public IDataSource createNewSource(String typeName)
    {
        UrlDataSource source = new UrlDataSource();
        source.setName("Mapbox");
        source.setBaseUrl(getExampleUrl());
        return source;
    }

    @Override
    public void open(Toolbox toolbox, Class<?> prefsTopic)
    {
        super.open(toolbox, prefsTopic);
    }

    @Override
    protected String getExampleUrl()
    {
        return "http://mapbox.geointservices.io:2999";
    }

    @Override
    protected ServerCustomization getServerCustomization()
    {
        return new DefaultCustomization("Mapbox Server");
    }

    @Override
    protected ServiceValidator<UrlDataSource> getValidator(ServerProviderRegistry registry)
    {
        return new MapboxServerSourceValidator(registry);
    }

    @Override
    protected boolean handleActivateSource(IDataSource source)
    {
        boolean success = false;

        DataModelCategory category = XYZTileUtils.newLayersCategory(((UrlDataSource)source).getURL(), MapboxUtil.PROVIDER);
        SimpleQuery<XYZTileLayerInfo> query = new SimpleQuery<>(category, XYZTileUtils.LAYERS_DESCRIPTOR);
        QueryTracker tracker = getToolbox().getDataRegistry().performQuery(query);
        if (tracker.getQueryStatus() == QueryTracker.QueryStatus.SUCCESS)
        {
            success = true;
        }
        else if (tracker.getQueryStatus() == QueryTracker.QueryStatus.CANCELLED)
        {
            LOGGER.info("The query for " + source.getName() + " was canceled");
        }
        else
        {
            Throwable e = tracker.getException();
            Notify.error(e.getMessage(), Method.ALERT_HIDDEN);
            LOGGER.error(e, e);
        }

        return success;
    }

    @Override
    protected void handleDeactivateSource(IDataSource source)
    {
        DataModelCategory category = XYZTileUtils.newLayersCategory(((UrlDataSource)source).getURL(), MapboxUtil.PROVIDER);
        getToolbox().getDataRegistry().removeModels(category, false);
    }
}
