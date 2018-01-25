package io.opensphere.city.transformer;

import java.util.List;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.collections.New;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Transformer used for cyber city 3d.
 */
public class CityTransformer extends DefaultTransformer
{
    /**
     * Constructor.
     *
     * @param dataRegistry the data registry.
     */
    public CityTransformer(DataRegistry dataRegistry)
    {
        super(dataRegistry);
        XYZServerInfo serverInfo = new XYZServerInfo("CyberCity3d", "http://http://api.cc3dnow.com/");
        XYZTileLayerInfo layer = new XYZTileLayerInfo("cybercity3dbuildings", "CyberCity3d", Projection.EPSG_4326, 1, false, 0,
                serverInfo);
        List<XYZTileLayerInfo> xyzLayers = New.list(layer);

        DataModelCategory layerCategory = XYZTileUtils.newLayersCategory(serverInfo.getServerUrl(), "CyberCity3d");
        SimpleSessionOnlyCacheDeposit<XYZTileLayerInfo> deposit = new SimpleSessionOnlyCacheDeposit<>(layerCategory,
                XYZTileUtils.LAYERS_DESCRIPTOR, xyzLayers);
        dataRegistry.addModels(deposit);
    }
}
