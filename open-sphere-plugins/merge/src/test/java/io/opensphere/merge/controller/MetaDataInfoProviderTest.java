package io.opensphere.merge.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.merge.model.MergedDataRow;

/**
 * Unit test for {@link MetaDataInfoProvider}.
 */
public class MetaDataInfoProviderTest
{
    /**
     * The time column name.
     */
    private static final String ourColumnTime = "time";

    /**
     * The defined mapped column.
     */
    private static final String ourDefinedColumn = "LAT";

    /**
     * The first layer.
     */
    private static final String ourLayer1 = "twitter";

    /**
     * The second layer.
     */
    private static final String ourLayer2 = "embers";

    /**
     * Layer 1 columns.
     */
    private final List<String> ourLayer1Columns = New.list("lat", ourColumnTime);

    /**
     * Layer 2 columns.
     */
    private final List<String> ourLayer2Columns = New.list("latitude", ourColumnTime);

    /**
     * Tests creating the metadata info.
     */
    @Test
    public void testCreateMetaDataInfo()
    {
        EasyMockSupport support = new EasyMockSupport();

        ColumnMappingController mapper = createMapper(support);
        List<DataTypeInfo> layers = createDataTypes(support);

        Map<String, Serializable> data1 = New.map();
        data1.put(ourDefinedColumn, Double.valueOf(10));
        data1.put(ourColumnTime, TimeSpan.get());

        Map<String, Serializable> data2 = New.map();
        data1.put(ourDefinedColumn, Double.valueOf(11));
        data1.put(ourColumnTime, TimeSpan.get());

        List<MergedDataRow> mergedRows = New.list(new MergedDataRow(data1, null, null), new MergedDataRow(data2, null, null));

        support.replayAll();

        MetaDataInfoProvider provider = new MetaDataInfoProvider(mapper);
        MetaDataInfo metaInfo = provider.createMetaDataInfo(layers, mergedRows);

        assertEquals(Double.class, metaInfo.getKeyClassType(ourDefinedColumn));
        assertTrue(TimeSpan.class.isAssignableFrom(metaInfo.getKeyClassType(ourColumnTime)));

        assertEquals(ourDefinedColumn, metaInfo.getLatitudeKey());
        assertEquals(ourColumnTime, metaInfo.getTimeKey());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked layer.
     *
     * @param support Used to create the mock.
     * @param layerId The id of the layer.
     * @param columns The columns of the layer.
     * @return The mocked layer.
     */
    private DataTypeInfo createDataType(EasyMockSupport support, String layerId, List<String> columns)
    {
        MetaDataInfo metaInfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metaInfo.getKeyNames()).andReturn(columns);

        Map<String, SpecialKey> specialKeys = New.map();
        specialKeys.put(columns.get(0), LatitudeKey.DEFAULT);
        specialKeys.put(columns.get(1), TimeKey.DEFAULT);
        EasyMock.expect(metaInfo.getSpecialKeyToTypeMap()).andReturn(specialKeys);

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        EasyMock.expect(dataType.getTypeKey()).andReturn(layerId).atLeastOnce();
        EasyMock.expect(dataType.getMetaDataInfo()).andReturn(metaInfo).atLeastOnce();

        return dataType;
    }

    /**
     * Creates mocked layers.
     *
     * @param support Used to create the mock.
     * @return The mocked layers.
     */
    private List<DataTypeInfo> createDataTypes(EasyMockSupport support)
    {
        DataTypeInfo type1 = createDataType(support, ourLayer1, ourLayer1Columns);
        DataTypeInfo type2 = createDataType(support, ourLayer2, ourLayer2Columns);

        return New.list(type1, type2);
    }

    /**
     * Creates an easy mocked {@link ColumnMappingController}.
     *
     * @param support Used to create the mock.
     * @return The mocked mappping controller.
     */
    private ColumnMappingController createMapper(EasyMockSupport support)
    {
        ColumnMappingController mapper = support.createMock(ColumnMappingController.class);

        List<Pair<String, List<String>>> layers = New.list();
        layers.add(new Pair<String, List<String>>(ourLayer1, ourLayer1Columns));
        layers.add(new Pair<String, List<String>>(ourLayer2, ourLayer2Columns));

        Map<String, Map<String, String>> map = New.map();
        Map<String, String> mapping1 = New.map();
        mapping1.put(ourLayer1Columns.get(0), ourDefinedColumn);
        map.put(ourLayer1, mapping1);

        Map<String, String> mapping2 = New.map();
        mapping2.put(ourLayer2Columns.get(0), ourDefinedColumn);
        map.put(ourLayer2, mapping2);

        EasyMock.expect(mapper.getDefinedColumns(EasyMock.eq(layers))).andReturn(map);

        return mapper;
    }
}
