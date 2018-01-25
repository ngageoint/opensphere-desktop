package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.MapManager;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.geopackage.model.GeoPackageFeatureLayer;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/**
 * Tests the {@link FeatureDataTypeBuilder} class.
 */
public class FeatureDataTypeBuilderTest
{
    /**
     * The id of the layer.
     */
    private static final String ourLayerId = "layerId";

    /**
     * The test layer name.
     */
    private static final String ourLayerName = "myNameIsLayer";

    /**
     * The test file.
     */
    private static final String ourPackageFile = "c:\\somefile.gpkg";

    /**
     * The actual elements added to the {@link DataTypeController}.
     */
    private List<DataElement> myActualElements;

    /**
     * The actual type passed in to the {@link DataTypeController}.
     */
    private DefaultDataTypeInfo myActualType;

    /**
     * Creates the test geometry data.
     */
    private final GeometryFactory myFactory = new GeometryFactory();

    /**
     * Tests building a {@link DataTypeInfo}.
     */
    @Test
    public void testBuildDataType()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        DataTypeController controller = support.createMock(DataTypeController.class);
        List<Map<String, Serializable>> testData = createTestData(true, true);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, true);
        MapManager mapManager = support.createMock(MapManager.class);

        support.replayAll();

        FeatureDataTypeBuilder builder = new FeatureDataTypeBuilder(controller, orderRegistry, mapManager);
        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer("package", ourPackageFile, ourLayerName, testData.size());
        GeoPackageDataTypeInfo expectedDataType = new GeoPackageDataTypeInfo(toolbox, layer, ourLayerId);
        layer.getData().addAll(testData);

        builder.buildDataType(layer, expectedDataType, ourLayerId);

        assertEquals(1000, expectedDataType.getMapVisualizationInfo().getZOrder());
        assertEquals(Color.RED.getRed(), expectedDataType.getBasicVisualizationInfo().getTypeColor().getRed());
        assertEquals(0, expectedDataType.getBasicVisualizationInfo().getTypeColor().getGreen());
        assertEquals(0, expectedDataType.getBasicVisualizationInfo().getTypeColor().getBlue());
        assertEquals(50, expectedDataType.getBasicVisualizationInfo().getTypeColor().getAlpha());
        assertTrue(expectedDataType.getMapVisualizationInfo().usesVisualizationStyles());
        assertTrue(expectedDataType.getBasicVisualizationInfo().usesDataElements());
        assertEquals(MapVisualizationType.MIXED_ELEMENTS, expectedDataType.getMapVisualizationInfo().getVisualizationType());

        support.verifyAll();
    }

    /**
     * Tests building a {@link DataTypeInfo}.
     */
    @Test
    public void testBuildDataTypeAndActivate()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        EventManager eventManager = support.createNiceMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);
        DataTypeController controller = createController(support, true);
        List<Map<String, Serializable>> testData = createTestData(true, true);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, true);
        MapManager mapManager = createMapManager(support);

        support.replayAll();

        FeatureDataTypeBuilder builder = new FeatureDataTypeBuilder(controller, orderRegistry, mapManager);
        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer("package", ourPackageFile, ourLayerName, testData.size());
        GeoPackageDataTypeInfo expectedDataType = new GeoPackageDataTypeInfo(toolbox, layer, ourLayerId);
        layer.getData().addAll(testData);

        builder.buildDataType(layer, expectedDataType, ourLayerId);
        builder.layerActivated(expectedDataType);

        assertEquals(expectedDataType, myActualType);
        assertEquals(1000, expectedDataType.getMapVisualizationInfo().getZOrder());
        assertTrue(expectedDataType.getMapVisualizationInfo().usesVisualizationStyles());
        assertEquals(Color.RED.getRed(), expectedDataType.getBasicVisualizationInfo().getTypeColor().getRed());
        assertEquals(0, expectedDataType.getBasicVisualizationInfo().getTypeColor().getGreen());
        assertEquals(0, expectedDataType.getBasicVisualizationInfo().getTypeColor().getBlue());
        assertEquals(50, expectedDataType.getBasicVisualizationInfo().getTypeColor().getAlpha());
        assertEquals(new GeographicBoundingBox(LatLonAlt.createFromDegrees(1, 0), LatLonAlt.createFromDegrees(3, 2)),
                expectedDataType.getBoundingBox());
        assertEquals(MapVisualizationType.MIXED_ELEMENTS, expectedDataType.getMapVisualizationInfo().getVisualizationType());
        assertEquals(TimeSpan.get(0, testData.size() - 1), expectedDataType.getTimeExtents().getExtent());

        assertEquals(3, myActualElements.size());

        Set<Long> elementIds = New.set();
        for (int i = 0; i < 3; i++)
        {
            DataElement element = myActualElements.get(i);

            elementIds.add(element.getId());
            assertEquals(TimeSpan.get(i), element.getTimeSpan());
            assertEquals(Color.RED.getRed(), element.getVisualizationState().getColor().getRed());
            assertEquals(0, element.getVisualizationState().getColor().getBlue());
            assertEquals(0, element.getVisualizationState().getColor().getGreen());
            assertEquals(50, element.getVisualizationState().getColor().getAlpha());
            MetaDataProvider metadataProvider = element.getMetaData();

            for (int j = 0; j < 3; j++)
            {
                assertEquals(i * j, metadataProvider.getValue("Column" + j));
            }

            assertEquals(myFactory.createPoint(new Coordinate(i, i + 1, i + 2)),
                    metadataProvider.getValue(GeoPackageColumns.GEOMETRY_COLUMN));
        }

        support.verifyAll();
    }

    /**
     * Tests building a {@link DataTypeInfo}.
     */
    @Test
    public void testBuildDataTypeAndActivateNoGeom()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        DataTypeController controller = createController(support, false);
        List<Map<String, Serializable>> testData = createTestData(false, false);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, true);
        MapManager manager = createMapManager(support);

        support.replayAll();

        FeatureDataTypeBuilder builder = new FeatureDataTypeBuilder(controller, orderRegistry, manager);
        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer("package", ourPackageFile, ourLayerName, testData.size());
        GeoPackageDataTypeInfo expectedDataType = new GeoPackageDataTypeInfo(toolbox, layer, ourLayerId);
        layer.getData().addAll(testData);

        builder.buildDataType(layer, expectedDataType, ourLayerId);
        builder.layerActivated(expectedDataType);

        assertEquals(expectedDataType, myActualType);
        assertEquals(1000, expectedDataType.getMapVisualizationInfo().getZOrder());
        assertEquals(Color.RED.getRed(), expectedDataType.getBasicVisualizationInfo().getTypeColor().getRed());
        assertEquals(0, expectedDataType.getBasicVisualizationInfo().getTypeColor().getGreen());
        assertEquals(0, expectedDataType.getBasicVisualizationInfo().getTypeColor().getBlue());
        assertEquals(50, expectedDataType.getBasicVisualizationInfo().getTypeColor().getAlpha());
        assertNull(expectedDataType.getTimeExtents());

        assertEquals(3, myActualElements.size());

        Set<Long> elementIds = New.set();
        for (int i = 0; i < 3; i++)
        {
            DataElement element = myActualElements.get(i);

            elementIds.add(element.getId());
            assertEquals(TimeSpan.TIMELESS, element.getTimeSpan());
            assertEquals(Color.RED.getRed(), element.getVisualizationState().getColor().getRed());
            assertEquals(0, element.getVisualizationState().getColor().getBlue());
            assertEquals(0, element.getVisualizationState().getColor().getGreen());
            assertEquals(50, element.getVisualizationState().getColor().getAlpha());
            MetaDataProvider metadataProvider = element.getMetaData();

            for (int j = 0; j < 3; j++)
            {
                assertEquals(i * j, metadataProvider.getValue("Column" + j));
            }
        }

        support.verifyAll();
    }

    /**
     * Tests deactivating a geopackage type.
     */
    @Test
    public void testTypeDeactivated()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeController controller = support.createMock(DataTypeController.class);
        controller.removeDataType(EasyMock.isA(GeoPackageDataTypeInfo.class), EasyMock.isA(FeatureDataTypeBuilder.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myActualType = (DefaultDataTypeInfo)EasyMock.getCurrentArguments()[0];
            return true;
        });

        Toolbox toolbox = createToolbox(support);
        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer("package", ourPackageFile, ourLayerName, 10);
        OrderManagerRegistry orderRegistry = createOrderRegistry(support, false);
        MapManager manager = support.createMock(MapManager.class);

        support.replayAll();

        GeoPackageDataTypeInfo expectedDataType = new GeoPackageDataTypeInfo(toolbox, layer, ourLayerId);
        FeatureDataTypeBuilder builder = new FeatureDataTypeBuilder(controller, orderRegistry, manager);
        builder.layerDeactivated(expectedDataType);

        assertEquals(expectedDataType, myActualType);

        support.verifyAll();
    }

    /**
     * Verifies the data type is correct and sets myActualElements to the
     * elements passed in to addDataElements.
     *
     * @return The array of ids.
     */
    @SuppressWarnings("unchecked")
    private long[] answerAddDataElements()
    {
        DefaultDataTypeInfo dataType = (DefaultDataTypeInfo)EasyMock.getCurrentArguments()[0];
        assertEquals(myActualType, dataType);
        int index = 2;
        if (EasyMock.getCurrentArguments()[3] instanceof List)
        {
            index = 3;
        }
        myActualElements = (List<DataElement>)EasyMock.getCurrentArguments()[index];
        return new long[] { 0, 1, 2 };
    }

    /**
     * Sets myActualType to the data type passed in to addDatatype.
     *
     * @return Null.
     */
    private Void assertAddedDataType()
    {
        myActualType = (DefaultDataTypeInfo)EasyMock.getCurrentArguments()[2];
        return null;
    }

    /**
     * Creates a mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @param includeGeometry Indicates if the geometry will be in the test
     *            data.
     * @return The mocked {@link DataTypeController}.
     */
    @SuppressWarnings("unchecked")
    private DataTypeController createController(EasyMockSupport support, boolean includeGeometry)
    {
        DataTypeController controller = support.createMock(DataTypeController.class);

        controller.addDataType(EasyMock.cmpEq(ourPackageFile), EasyMock.cmpEq(ourLayerName),
                EasyMock.isA(GeoPackageDataTypeInfo.class), EasyMock.isA(FeatureDataTypeBuilder.class));
        EasyMock.expectLastCall().andAnswer(this::assertAddedDataType);
        if (includeGeometry)
        {
            EasyMock.expect(controller.addMapDataElements(EasyMock.isA(DefaultDataTypeInfo.class), EasyMock.isNull(),
                    EasyMock.isNull(), EasyMock.isA(List.class), EasyMock.isA(FeatureDataTypeBuilder.class)))
                    .andAnswer(this::answerAddDataElements);
        }
        else
        {
            EasyMock.expect(controller.addDataElements(EasyMock.isA(DefaultDataTypeInfo.class), EasyMock.isNull(),
                    EasyMock.isA(List.class), EasyMock.isA(FeatureDataTypeBuilder.class))).andAnswer(this::answerAddDataElements);
        }

        return controller;
    }

    /**
     * Creates an easy mocked {@link MapManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link MapManager}.
     */
    private MapManager createMapManager(EasyMockSupport support)
    {
        Projection projection = support.createMock(Projection.class);
        EasyMock.expect(projection.getSnapshot()).andReturn(projection);

        MapManager mapManager = support.createMock(MapManager.class);
        EasyMock.expect(mapManager.getProjection()).andReturn(projection);

        return mapManager;
    }

    /**
     * Creates a mocked order registry.
     *
     * @param support Used to create the mock.
     * @param expectActivate True if zorder activation is to be expected.
     * @return The mocked {@link OrderManagerRegistry}.
     */
    private OrderManagerRegistry createOrderRegistry(EasyMockSupport support, boolean expectActivate)
    {
        OrderManager orderManager = support.createMock(OrderManager.class);
        if (expectActivate)
        {
            EasyMock.expect(orderManager.activateParticipant(
                    EasyMock.eq(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                            DefaultOrderCategory.FEATURE_CATEGORY, ourLayerId))))
                    .andReturn(1000);
        }
        OrderManagerRegistry registry = support.createMock(OrderManagerRegistry.class);
        EasyMock.expect(registry.getOrderManager(EasyMock.cmpEq(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY),
                EasyMock.eq(DefaultOrderCategory.FEATURE_CATEGORY))).andReturn(orderManager);

        return registry;
    }

    /**
     * Creates the test data.
     *
     * @param includeGeometry Indicates if the geometry will be in the test
     *            data.
     * @param includeTime Indicates if time data should be included or not.
     * @return The test data.
     */
    private List<Map<String, Serializable>> createTestData(boolean includeGeometry, boolean includeTime)
    {
        List<Map<String, Serializable>> testData = New.list();

        long time = 0;
        for (int i = 0; i < 3; i++, time++)
        {
            Map<String, Serializable> row = New.map();

            for (int j = 0; j < 3; j++)
            {
                row.put("Column" + j, j * i);
            }

            if (includeTime)
            {
                row.put("TIME", DateTimeUtilities.generateISO8601DateString(new Date(time)));
            }

            if (includeGeometry)
            {
                row.put(GeoPackageColumns.GEOMETRY_COLUMN, myFactory.createPoint(new Coordinate(i, i + 1, i + 2)));
            }

            testData.add(row);
        }

        return testData;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        DataTypeInfoPreferenceAssistant assistant = support.createMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(assistant.isVisiblePreference(EasyMock.isA(String.class))).andReturn(true).anyTimes();
        EasyMock.expect(assistant.getColorPreference(EasyMock.isA(String.class), EasyMock.anyInt())).andReturn(Color.RED.getRGB())
                .anyTimes();
        EasyMock.expect(assistant.getOpacityPreference(EasyMock.isA(String.class), EasyMock.anyInt())).andReturn(50).anyTimes();

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataTypeInfoPreferenceAssistant()).andReturn(assistant).anyTimes();

        PluginToolboxRegistry pluginToolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(pluginToolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantleToolbox)
                .anyTimes();

        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(pluginToolboxRegistry).anyTimes();

        return toolbox;
    }
}
