package io.opensphere.wms.sld.config.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.collections.CollectionUtilities;
import net.opengis.sld._100.StyledLayerDescriptor;

/**
 * An XmlAdapter used to marshal and unmarshal a layerKey-to-list-of-SLDs map.
 */
public class SldXmlAdapter extends XmlAdapter<LayerMap, Map<String, List<StyledLayerDescriptor>>>
{
    @Override
    public LayerMap marshal(Map<String, List<StyledLayerDescriptor>> map)
    {
        LayerMap layerMap = new LayerMap();
        for (Entry<String, List<StyledLayerDescriptor>> entry : map.entrySet())
        {
            if (StringUtils.isNotEmpty(entry.getKey()) && !entry.getValue().isEmpty())
            {
                LayerMapEntry mapEntry = new LayerMapEntry();
                mapEntry.setLayerKey(entry.getKey());
                mapEntry.setSlds(entry.getValue());
                layerMap.addLayer(mapEntry);
            }
        }
        return layerMap;
    }

    @Override
    public Map<String, List<StyledLayerDescriptor>> unmarshal(LayerMap value)
    {
        Map<String, List<StyledLayerDescriptor>> map = new HashMap<>();
        for (LayerMapEntry layer : value.getLayers())
        {
            CollectionUtilities.multiMapAddAll(map, layer.getLayerKey(), layer.getSlds(), false);
        }
        return map;
    }
}
