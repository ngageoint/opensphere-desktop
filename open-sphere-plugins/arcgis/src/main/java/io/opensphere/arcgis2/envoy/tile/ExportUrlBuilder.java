package io.opensphere.arcgis2.envoy.tile;

import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.ZYXImageKey;

/**
 * Knows how to build a get url for layers that do not have cached tiles on the
 * server.
 */
public class ExportUrlBuilder implements TileUrlBuilder
{
    @Override
    public String buildUrl(DataModelCategory category, ZYXImageKey key)
    {
        String layerUrl = category.getCategory();
        int lastSlashIndex = layerUrl.lastIndexOf('/');
        String parentUrl = layerUrl.substring(0, lastSlashIndex);
        String id = layerUrl.substring(lastSlashIndex + 1);

        StringBuilder urlBuilder = new StringBuilder(192);
        urlBuilder.append(parentUrl).append("/export?f=image&bbox=");
        urlBuilder.append(key.getBounds().getMinLonD()).append(',').append(key.getBounds().getMinLatD()).append(',');
        urlBuilder.append(key.getBounds().getMaxLonD()).append(',').append(key.getBounds().getMaxLatD());
        urlBuilder.append("&bboxSR=4326&imageSR=4326&transparent=true&layers=show%3A").append(id);
        return urlBuilder.toString();
    }
}
