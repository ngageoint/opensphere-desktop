package io.opensphere.merge.controller;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.merge.model.MergedDataRow;

/**
 * Provides {@link MetaDataInfo} for merged data.
 */
public class MetaDataInfoProvider
{
    /**
     * The column mapping controller used to get mapped columns.
     */
    private final ColumnMappingController myMapper;

    /**
     * Creates a merge data metadata provider.
     *
     * @param controller The column mapping controller used to get mapped
     *            columns.
     */
    public MetaDataInfoProvider(ColumnMappingController controller)
    {
        myMapper = controller;
    }

    /**
     * Create the meta data info, based on the data.
     *
     * @param mergedLayers The layers that have been merged.
     * @param data The data to create the metadata info for.
     * @return The metadata info.
     */
    public MetaDataInfo createMetaDataInfo(List<DataTypeInfo> mergedLayers, List<MergedDataRow> data)
    {
        DefaultMetaDataInfo metadataInfo = new DefaultMetaDataInfo();

        Map<String, Class<?>> columnsToTypes = New.map();
        for (MergedDataRow row : data)
        {
            for (Entry<String, Serializable> entry : row.getData().entrySet())
            {
                Class<?> type = String.class;
                if (entry.getValue() != null)
                {
                    type = entry.getValue().getClass();
                    if (!type.equals(columnsToTypes.get(entry.getKey())))
                    {
                        columnsToTypes.put(entry.getKey(), type);
                    }
                }
                else
                {
                    columnsToTypes.put(entry.getKey(), type);
                }
            }
        }

        for (Entry<String, Class<?>> column : columnsToTypes.entrySet())
        {
            metadataInfo.addKey(column.getKey(), column.getValue(), this);
        }

        addSpecialKeys(metadataInfo, mergedLayers);

        return metadataInfo;
    }

    /**
     * Adds the special keys to the {@link MetaDataInfo}.
     *
     * @param metadataInfo The metadata info to add special keys to.
     * @param mergedLayers The layers that were merged.
     */
    private void addSpecialKeys(DefaultMetaDataInfo metadataInfo, List<DataTypeInfo> mergedLayers)
    {
        Map<String, SpecialKey> specialKeys = New.map();

        List<Pair<String, List<String>>> layers = New.list();
        for (DataTypeInfo layer : mergedLayers)
        {
            layers.add(new Pair<>(layer.getTypeKey(), layer.getMetaDataInfo().getKeyNames()));
        }

        Map<String, Map<String, String>> definedColumns = myMapper.getDefinedColumns(layers);

        for (DataTypeInfo dataType : mergedLayers)
        {
            Map<String, String> definedLayerColumns = definedColumns.get(dataType.getTypeKey());
            for (Entry<String, SpecialKey> specialKey : dataType.getMetaDataInfo().getSpecialKeyToTypeMap().entrySet())
            {
                String key = specialKey.getKey();
                if (definedLayerColumns != null && definedLayerColumns.containsKey(key))
                {
                    key = definedLayerColumns.get(key);
                }

                specialKeys.put(key, specialKey.getValue());
            }
        }

        for (Entry<String, SpecialKey> entry : specialKeys.entrySet())
        {
            metadataInfo.setSpecialKey(entry.getKey(), entry.getValue(), this);
        }
    }
}
