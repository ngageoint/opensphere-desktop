package io.opensphere.mantle.util.columnanalyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

/**
 * An XmlAdapter used to marshal and unmarshal a
 * columnKey-to-list-of-ColumnAnalyzerData map.
 */
public class ColumnAnalyzerDataXmlAdapter extends XmlAdapter<ColumnAnalyzerXmlDataMap, Map<String, ColumnAnalyzerData>>
{
    @Override
    public ColumnAnalyzerXmlDataMap marshal(Map<String, ColumnAnalyzerData> map)
    {
        ColumnAnalyzerXmlDataMap layerMap = new ColumnAnalyzerXmlDataMap();
        for (Entry<String, ColumnAnalyzerData> entry : map.entrySet())
        {
            if (StringUtils.isNotEmpty(entry.getKey()) && entry.getValue() != null)
            {
                ColumnAnalyzerXmlDataMapEntry mapEntry = new ColumnAnalyzerXmlDataMapEntry();
                mapEntry.setColumnKey(entry.getKey());
                mapEntry.setData(entry.getValue());
                layerMap.addLayer(mapEntry);
            }
        }
        return layerMap;
    }

    @Override
    public Map<String, ColumnAnalyzerData> unmarshal(ColumnAnalyzerXmlDataMap value)
    {
        Map<String, ColumnAnalyzerData> map = new HashMap<>();
        for (ColumnAnalyzerXmlDataMapEntry entry : value.getDataList())
        {
            map.put(entry.getColumnKey(), entry.getData());
        }
        return map;
    }
}
