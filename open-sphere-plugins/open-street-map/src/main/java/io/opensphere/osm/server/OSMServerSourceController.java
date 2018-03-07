package io.opensphere.osm.server;

import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.UrlDataSource;
import io.opensphere.osm.util.OSMUtil;
import io.opensphere.server.control.UrlServerSourceController;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.display.ServiceValidator;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/** Open Street Map's {@link ServerSourceController}. */
public class OSMServerSourceController extends UrlServerSourceController
{
    @Override
    public IDataSource createNewSource(String typeName)
    {
        return new UrlDataSource("Open Street Map", getExampleUrl());
    }

    @Override
    protected String getExampleUrl()
    {
        return "http://osm.geointservices.io/osm_tiles_pc/{z}/{x}/{y}.png";
    }

    @Override
    protected ServerCustomization getServerCustomization()
    {
        return new DefaultCustomization("Open Street Map Server");
    }

    @Override
    protected ServiceValidator<UrlDataSource> getValidator(ServerProviderRegistry registry)
    {
        return new OSMServerSourceValidator(registry);
    }

    @Override
    protected boolean handleActivateSource(IDataSource source)
    {
        DataModelCategory category = XYZTileUtils.newLayersCategory(((UrlDataSource)source).getURLString(), OSMUtil.PROVIDER);
        XYZServerInfo serverInfo = new XYZServerInfo(source.getName(), ((UrlDataSource)source).getURLString());
        XYZTileLayerInfo layerInfo = null;
        if (serverInfo.getServerUrl().contains("osm_tiles_pc"))
        {
            layerInfo = new XYZTileLayerInfo(OSMUtil.PROVIDER, source.getName(), Projection.EPSG_4326, 2, false, 2, serverInfo);
        }
        else
        {
            layerInfo = new XYZTileLayerInfo(OSMUtil.PROVIDER, source.getName(), Projection.EPSG_3857, 1, false, 5, serverInfo);
        }

        getToolbox().getDataRegistry()
                .addModels(new SimpleSessionOnlyCacheDeposit<>(category, XYZTileUtils.LAYERS_DESCRIPTOR, New.list(layerInfo)));
        return true;
    }

    @Override
    protected void handleDeactivateSource(IDataSource source)
    {
        DataModelCategory category = XYZTileUtils.newLayersCategory(((UrlDataSource)source).getURLString(), OSMUtil.PROVIDER);
        getToolbox().getDataRegistry().removeModels(category, false);
    }
}
