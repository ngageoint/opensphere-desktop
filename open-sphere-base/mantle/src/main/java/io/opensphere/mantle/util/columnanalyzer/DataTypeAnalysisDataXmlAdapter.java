package io.opensphere.mantle.util.columnanalyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

/**
 * An XmlAdapter used to marshal and unmarshal a
 * typeKey-to-list-of-DataTypeColumnAnalyzerDataSet map.
 */
public class DataTypeAnalysisDataXmlAdapter
        extends XmlAdapter<DataTypeAnalysisXmlDataMap, Map<String, DataTypeColumnAnalyzerDataSet>>
{
    @Override
    public DataTypeAnalysisXmlDataMap marshal(Map<String, DataTypeColumnAnalyzerDataSet> map)
    {
        DataTypeAnalysisXmlDataMap result = new DataTypeAnalysisXmlDataMap();
        for (Entry<String, DataTypeColumnAnalyzerDataSet> entry : map.entrySet())
        {
            if (StringUtils.isNotEmpty(entry.getKey()) && entry.getValue() != null)
            {
                DataTypeAnalaysMapXmMapEntry mapEntry = new DataTypeAnalaysMapXmMapEntry();
                mapEntry.setTypeKey(entry.getKey());
                mapEntry.setData(entry.getValue());
                result.addLayer(mapEntry);
            }
        }
        return result;
    }

    @Override
    public Map<String, DataTypeColumnAnalyzerDataSet> unmarshal(DataTypeAnalysisXmlDataMap value)
    {
        Map<String, DataTypeColumnAnalyzerDataSet> map = new HashMap<>();
        for (DataTypeAnalaysMapXmMapEntry entry : value.getDataList())
        {
            map.put(entry.getTypeKey(), entry.getData());
        }
        return map;
    }
}
