package io.opensphere.mantle.crust;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;

/**
 * Unit test for {@link DataTypeChecker}.
 */
public class DataTypeCheckerTest
{
    /**
     * Tests the isFeatureDataType call.
     */
    @Test
    public void testIsFeatureDataType()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<DataTypeInfo> dataTypes = createDataTypes(support);

        support.replayAll();

        int index = 1;
        for (DataTypeInfo dataType : dataTypes)
        {
            if (index % 3 == 0)
            {
                assertTrue(DataTypeChecker.isFeatureType(dataType));
            }
            else
            {
                assertFalse(DataTypeChecker.isFeatureType(dataType));
            }
            index++;
        }

        support.verifyAll();
    }

    /**
     * Creates a list of mocked test data types where the feature data types are
     * at index 2 and 5.
     *
     * @param support Used to create the mock.
     * @return The list of test data types.
     */
    private List<DataTypeInfo> createDataTypes(EasyMockSupport support)
    {
        List<DataTypeInfo> dataTypes = New.list();

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(null);
        EasyMock.expect(dataType.getMapVisualizationInfo()).andReturn(null);
        dataTypes.add(dataType);

        BasicVisualizationInfo visInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(Boolean.valueOf(visInfo.usesDataElements())).andReturn(Boolean.FALSE);
        dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(visInfo).times(2);
        EasyMock.expect(dataType.getMapVisualizationInfo()).andReturn(null);
        dataTypes.add(dataType);

        visInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(Boolean.valueOf(visInfo.usesDataElements())).andReturn(Boolean.TRUE);
        dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(visInfo).times(2);
        dataTypes.add(dataType);

        dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(null);
        EasyMock.expect(dataType.getMapVisualizationInfo()).andReturn(null);
        dataTypes.add(dataType);

        MapVisualizationInfo mapVisInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(Boolean.valueOf(mapVisInfo.usesMapDataElements())).andReturn(Boolean.FALSE);
        dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(null);
        EasyMock.expect(dataType.getMapVisualizationInfo()).andReturn(mapVisInfo).times(2);
        dataTypes.add(dataType);

        mapVisInfo = support.createMock(MapVisualizationInfo.class);
        EasyMock.expect(Boolean.valueOf(mapVisInfo.usesMapDataElements())).andReturn(Boolean.TRUE);
        dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(null);
        EasyMock.expect(dataType.getMapVisualizationInfo()).andReturn(mapVisInfo).times(2);
        dataTypes.add(dataType);

        return dataTypes;
    }
}
