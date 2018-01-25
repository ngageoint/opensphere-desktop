package io.opensphere.merge.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.h2.command.dml.Set;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.TypeService;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElementProvider;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDeletableDataGroupInfo;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.merge.model.MergeModel;
import io.opensphere.merge.model.MergedDataRow;

/**
 * Tests the {@link MergeController} class.
 */
public class MergeControllerTest
{
    /**
     * The latitude column.
     */
    private static final String ourColumn1 = "Lat";

    /**
     * The longitude column.
     */
    private static final String ourColumn2 = "Lon";

    /**
     * The user column.
     */
    private static final String ourColumn3 = "UserId";

    /**
     * The text column.
     */
    private static final String ourColumn4 = "text";

    /**
     * The message column.
     */
    private static final String ourColumn5 = "message";

    /**
     * The defined column mapping UserId to UserName.
     */
    private static final String ourDefinedColumn = "User";

    /**
     * The expected root group name.
     */
    private static final String ourExpectedRootGroup = "Merged";

    /**
     * A test layer name.
     */
    private static final String ourLayer1 = "twitter";

    /**
     * Layer columns for layer 1.
     */
    private static final List<String> ourLayer1Columns = New.list(ourColumn1, ourColumn2, ourColumn3, ourColumn4);

    /**
     * Another test layer name.
     */
    private static final String ourLayer2 = "embers";

    /**
     * Columns for layer 2.
     */
    private static final List<String> ourLayer2Columns = New.list("UserName", ourColumn5, ourColumn1, ourColumn2);

    /**
     * The merge layer name.
     */
    private static final String ourMergeLayerName = "mergeLayer";

    /**
     * The type key for layer 1.
     */
    private static final String ourTypeKey1 = "server!!" + ourLayer1;

    /**
     * The type key for layer 2.
     */
    private static final String ourTypeKey2 = "server!!" + ourLayer2;

    /**
     * The added merged layer.
     */
    private DataTypeInfo myAddedType;

    /**
     * The merged data that was deposited into the registry.
     */
    private List<MergedDataRow> myDeposits;

    /**
     * The data element provider given to the type controller when adding
     * elements.
     */
    private DataElementProvider myProvider;

    /**
     * The root group for the merge types.
     */
    private DataGroupInfo myRootGroup;

    /**
     * The task activity added for the merge.
     */
    private TaskActivity myTa;

    /**
     * Tests the check associations logic.
     */
    @Test
    public void testCheckAssociations()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<DataTypeInfo> layers = createDataTypes(support);
        EasyMock.expect(layers.get(0).getDisplayName()).andReturn(ourLayer1);
        EasyMock.expect(layers.get(1).getDisplayName()).andReturn(ourLayer2);

        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);
        DataGroupController groupController = createGroupController(support);
        EasyMock.expect(groupController.getDataGroupInfo(EasyMock.cmpEq(ourLayer1 + " " + ourLayer2))).andReturn(null);
        DataTypeController typeController = support.createMock(DataTypeController.class);
        MantleToolbox mantle = createMantle(support, lookup, groupController, typeController);

        ColumnMappingController mapper = createMapperNoAssociations(support);
        Toolbox toolbox = createToolbox(support, mantle, mapper);

        MergeModel model = new MergeModel(layers);

        support.replayAll();

        MergeController controller = new MergeController(toolbox);
        controller.setModel(model);
        assertEquals(ourLayer1 + " " + ourLayer2, model.getNewLayerName().get());
        assertEquals("", controller.getModel().getUserMessage().get());
        controller.close();

        model.getUserMessage().set(null);
        assertEquals("These layers have no associated columns.", controller.getModel().getUserMessage().get());

        support.verifyAll();
    }

    /**
     * Tests that the class handles default name collisions and adds a 1 up
     * counter to the name.
     */
    @Test
    public void testDefaultNameCollisions()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<DataTypeInfo> layers = createDataTypes(support);
        EasyMock.expect(layers.get(0).getDisplayName()).andReturn(ourLayer1);
        EasyMock.expect(layers.get(1).getDisplayName()).andReturn(ourLayer2);

        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);

        String existingLayer = ourLayer1 + " " + ourLayer2;
        DataGroupInfo existingGroup = support.createMock(DataGroupInfo.class);
        DataGroupController groupController = createGroupController(support);
        EasyMock.expect(groupController.getDataGroupInfo(EasyMock.cmpEq(existingLayer))).andReturn(existingGroup);
        EasyMock.expect(groupController.getDataGroupInfo(EasyMock.cmpEq(existingLayer + " 1"))).andReturn(null);

        DataTypeController typeController = support.createMock(DataTypeController.class);
        MantleToolbox mantle = createMantle(support, lookup, groupController, typeController);

        ColumnMappingController mapper = createMapperNoAssociations(support);
        Toolbox toolbox = createToolbox(support, mantle, mapper);

        MergeModel model = new MergeModel(layers);

        support.replayAll();

        MergeController controller = new MergeController(toolbox);
        controller.setModel(model);
        assertEquals(existingLayer + " 1", model.getNewLayerName().get());
        assertEquals("", controller.getModel().getUserMessage().get());
        controller.close();

        model.getUserMessage().set(null);
        assertEquals("These layers have no associated columns.", controller.getModel().getUserMessage().get());

        support.verifyAll();
    }

    /**
     * Tests performing a merge.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testPerformMerge() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        myRootGroup = null;
        CountDownLatch latch = new CountDownLatch(1);

        List<DataTypeInfo> layers = createDataTypes(support);

        DataElementLookupUtils lookup = createLookupUtils(support, layers);
        DataGroupController groupController = createGroupController(support);
        DataTypeController typeController = createTypeController(support, latch);
        MantleToolbox mantle = createMantleMerge(support, lookup, groupController, typeController);

        ColumnMappingController mapper = createMapperMerge(support);
        Toolbox toolbox = createToolboxMerge(support, mantle, mapper);

        MergeModel model = new MergeModel(layers);
        model.getNewLayerName().set(ourMergeLayerName);

        support.replayAll();

        MergeController controller = new MergeController(toolbox);
        controller.setModel(model);
        controller.performMerge();
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertEquals(ourExpectedRootGroup, myRootGroup.getDisplayName());
        assertTrue(myRootGroup.isRootNode());
        assertEquals(ourExpectedRootGroup, myRootGroup.getId());
        assertEquals(ourExpectedRootGroup, myRootGroup.getProviderType());

        assertEquals(ourMergeLayerName, myAddedType.getDisplayName());
        assertEquals(ourExpectedRootGroup, myAddedType.getSourcePrefix());
        assertEquals(ourMergeLayerName, myAddedType.getTypeKey());
        assertEquals(ourMergeLayerName, myAddedType.getTypeName());
        assertFalse(myAddedType.providerFiltersMetaData());
        assertFalse(myAddedType.isFilterable());
        assertTrue(myAddedType.isVisible());
        List<String> expectedKeyNames = New.list(ourColumn1, ourColumn2, ourDefinedColumn, ourColumn4, ourColumn5);
        assertEquals(expectedKeyNames.size(), myAddedType.getMetaDataInfo().getKeyNames().size());
        assertTrue(myAddedType.getMetaDataInfo().getKeyNames().containsAll(expectedKeyNames));
        assertTrue(myAddedType.getParent().activationProperty().isActive());
        assertTrue(myAddedType.getParent() instanceof DefaultDeletableDataGroupInfo);

        MapDataElement twitter = (MapDataElement)myProvider.next();
        MapDataElement embers = (MapDataElement)myProvider.next();

        assertFalse(myProvider.hasNext());

        assertEquals(TimeSpan.get(1000), twitter.getTimeSpan());
        assertEquals(new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10, 11)), twitter.getMapGeometrySupport());
        assertEquals(10d, ((Double)twitter.getMetaData().getValue(ourColumn1)).doubleValue(), 0d);
        assertEquals(11d, ((Double)twitter.getMetaData().getValue(ourColumn2)).doubleValue(), 0d);
        assertEquals("testuser", twitter.getMetaData().getValue(ourDefinedColumn).toString());
        assertEquals("your mom", twitter.getMetaData().getValue(ourColumn4).toString());
        assertNull(twitter.getMetaData().getValue(ourColumn5));

        assertEquals(TimeSpan.get(2000), embers.getTimeSpan());
        assertEquals(new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(12, 13)), embers.getMapGeometrySupport());
        assertEquals(12d, ((Double)embers.getMetaData().getValue(ourColumn1)).doubleValue(), 0d);
        assertEquals(13d, ((Double)embers.getMetaData().getValue(ourColumn2)).doubleValue(), 0d);
        assertEquals("anotheruser", embers.getMetaData().getValue(ourDefinedColumn).toString());
        assertNull(embers.getMetaData().getValue(ourColumn4));
        assertEquals("hello there", embers.getMetaData().getValue(ourColumn5));

        Thread.sleep(100);
        assertTrue(myTa.isComplete());
        assertEquals("Merging selected layers to mergeLayer", myTa.getLabelValue());

        controller.close();

        assertNull(myRootGroup);
        assertNull(myAddedType);

        support.verifyAll();
    }

    /**
     * The answer to the mocked.
     *
     * @return The model ids.
     */
    @SuppressWarnings("unchecked")
    private long[] addModelsAnswer()
    {
        SimpleSessionOnlyCacheDeposit<MergedDataRow> deposit = (SimpleSessionOnlyCacheDeposit<MergedDataRow>)EasyMock
                .getCurrentArguments()[0];

        assertEquals(DataRegistryUtils.getInstance().getMergeDataCategory(ourMergeLayerName), deposit.getCategory());
        assertEquals(DataRegistryUtils.MERGED_PROP_DESCRIPTOR, deposit.getAccessors().iterator().next().getPropertyDescriptor());

        myDeposits = New.list();
        for (MergedDataRow row : deposit.getInput())
        {
            myDeposits.add(row);
        }

        return new long[] { 0, 1 };
    }

    /**
     * Creates a mocked data type.
     *
     * @param support Used to create the mock.
     * @param layer The layer name.
     * @param columns The layer columns.
     * @return The mocked data type.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private DataTypeInfo createDataType(EasyMockSupport support, String layer, List<String> columns)
    {
        MetaDataInfo metadataInfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metadataInfo.getKeyNames()).andReturn(columns).atLeastOnce();
        EasyMock.expect(Boolean.valueOf(metadataInfo.hasKey(EasyMock.isA(String.class)))).andAnswer(() ->
        {
            return Boolean.valueOf(columns.contains(EasyMock.getCurrentArguments()[0]));
        }).anyTimes();
        EasyMock.expect(metadataInfo.getKeyClassType(EasyMock.isA(String.class))).andAnswer(() ->
        {
            Class type = String.class;
            String column = EasyMock.getCurrentArguments()[0].toString();
            if (ourColumn1.equals(column) || ourColumn2.equals(column))
            {
                type = Double.class;
            }
            return type;
        }).anyTimes();
        EasyMock.expect(metadataInfo.getSpecialKeyToTypeMap()).andReturn(New.map()).anyTimes();

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getTypeKey()).andReturn("server!!" + layer).atLeastOnce();
        EasyMock.expect(dataType.getMetaDataInfo()).andReturn(metadataInfo).atLeastOnce();

        return dataType;
    }

    /**
     * Creates the data types.
     *
     * @param support Used to create the mock.
     * @return The mocked data types.
     */
    private List<DataTypeInfo> createDataTypes(EasyMockSupport support)
    {
        return New.list(createDataType(support, ourLayer1, ourLayer1Columns),
                createDataType(support, ourLayer2, ourLayer2Columns));
    }

    /**
     * Creates an easy mocked {@link DataGroupController}.
     *
     * @param support Used to create the mock.
     * @return The mocked group controller.
     */
    private DataGroupController createGroupController(EasyMockSupport support)
    {
        DataGroupController groupController = support.createMock(DataGroupController.class);

        EasyMock.expect(Boolean.valueOf(
                groupController.addRootDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(MergeController.class))))
                .andAnswer(() ->
                {
                    myRootGroup = (DataGroupInfo)EasyMock.getCurrentArguments()[0];
                    return Boolean.TRUE;
                });
        EasyMock.expect(Boolean.valueOf(
                groupController.removeDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(MergeController.class))))
                .andAnswer(() ->
                {
                    if (myRootGroup.equals(EasyMock.getCurrentArguments()[0]))
                    {
                        myRootGroup = null;
                    }
                    return Boolean.TRUE;
                });

        return groupController;
    }

    /**
     * Creates the easy mocked {@link DataElementLookupUtils}.
     *
     * @param support Used to create the mock.
     * @param dataTypes The mocked data types.
     * @return The mocked lookup utils.
     */
    private DataElementLookupUtils createLookupUtils(EasyMockSupport support, List<DataTypeInfo> dataTypes)
    {
        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);

        Map<String, Serializable> twitterData = New.map();
        twitterData.put(ourColumn1, Double.valueOf(10));
        twitterData.put(ourColumn2, Double.valueOf(11));
        twitterData.put(ourColumn3, "testuser");
        twitterData.put(ourColumn4, "your mom");

        DefaultMapDataElement twitterRow = new DefaultMapDataElement(1, TimeSpan.get(1000), dataTypes.get(0),
                new SimpleMetaDataProvider(twitterData), new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10, 11)));
        EasyMock.expect(lookup.getDataElements(EasyMock.eq(dataTypes.get(0)))).andReturn(New.list(twitterRow));

        Map<String, Serializable> embersData = New.map();
        embersData.put("UserName", "anotheruser");
        embersData.put(ourColumn5, "hello there");
        embersData.put(ourColumn1, Double.valueOf(12));
        embersData.put(ourColumn2, Double.valueOf(13));

        DefaultMapDataElement embersRow = new DefaultMapDataElement(2, TimeSpan.get(2000), dataTypes.get(1),
                new SimpleMetaDataProvider(embersData), new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(12, 13)));
        EasyMock.expect(lookup.getDataElements(EasyMock.eq(dataTypes.get(1)))).andReturn(New.list(embersRow));

        return lookup;
    }

    /**
     * Creates the mantle toolbox.
     *
     * @param support Used to create the mock.
     * @param lookup Mocked lookup utils.
     * @param groupController Mocked group controller.
     * @param typeController Mocked type controller.
     * @return The mocked mantle toolbox.
     */
    private MantleToolbox createMantle(EasyMockSupport support, DataElementLookupUtils lookup,
            DataGroupController groupController, DataTypeController typeController)
    {
        MantleToolbox mantle = support.createMock(MantleToolbox.class);

        EasyMock.expect(mantle.getDataElementLookupUtils()).andReturn(lookup);
        EasyMock.expect(mantle.getDataGroupController()).andReturn(groupController).atLeastOnce();
        EasyMock.expect(mantle.getDataTypeController()).andReturn(typeController).atLeastOnce();

        return mantle;
    }

    /**
     * Creates the mantle toolbox.
     *
     * @param support Used to create the mock.
     * @param lookup Mocked lookup utils.
     * @param groupController Mocked group controller.
     * @param typeController Mocked type controller.
     * @return The mocked mantle toolbox.
     */
    private MantleToolbox createMantleMerge(EasyMockSupport support, DataElementLookupUtils lookup,
            DataGroupController groupController, DataTypeController typeController)
    {
        MantleToolbox mantle = createMantle(support, lookup, groupController, typeController);

        DataTypeInfoPreferenceAssistant prefAssist = support.createMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(Boolean.valueOf(prefAssist.isVisiblePreference(EasyMock.cmpEq(ourMergeLayerName))))
                .andReturn(Boolean.TRUE).atLeastOnce();
        EasyMock.expect(Integer.valueOf(prefAssist.getColorPreference(EasyMock.cmpEq(ourMergeLayerName), EasyMock.anyInt())))
                .andReturn(Integer.valueOf(Color.RED.getRGB())).atLeastOnce();
        EasyMock.expect(Integer.valueOf(prefAssist.getOpacityPreference(EasyMock.cmpEq(ourMergeLayerName), EasyMock.anyInt())))
                .andReturn(Integer.valueOf(255)).atLeastOnce();
        EasyMock.expect(mantle.getDataTypeInfoPreferenceAssistant()).andReturn(prefAssist).atLeastOnce();

        return mantle;
    }

    /**
     * Creates an easy mocked {@link ColumnMappingController} expected check
     * associations to be called with an association.
     *
     * @param support Used to create the mock.
     * @return The mocked mapping controller.
     */
    private ColumnMappingController createMapper(EasyMockSupport support)
    {
        ColumnMappingController mapper = support.createMock(ColumnMappingController.class);

        List<Pair<String, List<String>>> expected = New.list();
        expected.add(new Pair<String, List<String>>(ourTypeKey1, ourLayer1Columns));
        expected.add(new Pair<String, List<String>>(ourTypeKey2, ourLayer2Columns));

        Map<String, Map<String, String>> mappings = New.map();
        Map<String, String> mapping1 = New.map();
        mapping1.put(ourColumn3, ourDefinedColumn);

        Map<String, String> mapping2 = New.map();
        mapping2.put("UserName", ourDefinedColumn);

        mappings.put(ourTypeKey1, mapping1);
        mappings.put(ourTypeKey2, mapping2);

        EasyMock.expect(mapper.getDefinedColumns(EasyMock.eq(expected))).andReturn(mappings);

        return mapper;
    }

    /**
     * Creates an easy mocked {@link ColumnMappingController} used for merging.
     *
     * @param support Used to create the mock.
     * @return The mocked mapping controller.
     */
    private ColumnMappingController createMapperMerge(EasyMockSupport support)
    {
        ColumnMappingController mapper = createMapper(support);

        EasyMock.expectLastCall().times(2);
        EasyMock.expect(mapper.getDefinedColumn(EasyMock.cmpEq(ourTypeKey1), EasyMock.cmpEq(ourColumn3)))
                .andReturn(ourDefinedColumn).atLeastOnce();
        EasyMock.expect(mapper.getDefinedColumn(EasyMock.cmpEq(ourTypeKey2), EasyMock.cmpEq("UserName")))
                .andReturn(ourDefinedColumn).atLeastOnce();
        EasyMock.expect(mapper.getDefinedColumn(EasyMock.isA(String.class), EasyMock.isA(String.class))).andReturn(null)
                .atLeastOnce();

        return mapper;
    }

    /**
     * Creates an easy mocked {@link ColumnMappingController} expected check
     * associations to be called twice.
     *
     * @param support Used to create the mock.
     * @return The mocked mapping controller.
     */
    private ColumnMappingController createMapperNoAssociations(EasyMockSupport support)
    {
        ColumnMappingController mapper = createMapper(support);

        List<Pair<String, List<String>>> expected = New.list();
        expected.add(new Pair<String, List<String>>(ourTypeKey1, ourLayer1Columns));
        expected.add(new Pair<String, List<String>>(ourTypeKey2, ourLayer2Columns));

        EasyMock.expect(mapper.getDefinedColumns(EasyMock.eq(expected))).andReturn(New.map());

        return mapper;
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @param mantle Mocked mantle to return.
     * @param mapper Mocked column mapper.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, MantleToolbox mantle, ColumnMappingController mapper)
    {
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        Toolbox toolbox = createToolbox(support, mantle, mapper, dataRegistry);

        return toolbox;
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @param mantle Mocked mantle to return.
     * @param mapper Mocked column mapper.
     * @param registry Mocked data registry.
     * @return The mocked toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support, MantleToolbox mantle, ColumnMappingController mapper,
            DataRegistry registry)
    {
        Toolbox toolbox = support.createMock(Toolbox.class);

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantle).atLeastOnce();
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();

        DataFilterRegistry filterRegistry = support.createMock(DataFilterRegistry.class);
        EasyMock.expect(filterRegistry.getColumnMappingController()).andReturn(mapper);
        EasyMock.expect(toolbox.getDataFilterRegistry()).andReturn(filterRegistry);

        EventManager eventManager = support.createNiceMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).anyTimes();

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(registry);

        return toolbox;
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @param mantle Mocked mantle to return.
     * @param mapper Mocked column mapper.
     * @return The mocked toolbox.
     */
    @SuppressWarnings("unchecked")
    private Toolbox createToolboxMerge(EasyMockSupport support, MantleToolbox mantle, ColumnMappingController mapper)
    {
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);

        EasyMock.expect(dataRegistry.addModels(EasyMock.isA(SimpleSessionOnlyCacheDeposit.class)))
                .andAnswer(this::addModelsAnswer);
        EasyMock.expect(dataRegistry.performLocalQuery(EasyMock.isA(SimpleQuery.class))).andAnswer(this::queryAnswer);

        Toolbox toolbox = createToolbox(support, mantle, mapper, dataRegistry);

        OrderManager orderManager = support.createNiceMock(OrderManager.class);
        OrderManagerRegistry orderRegistry = support.createNiceMock(OrderManagerRegistry.class);
        EasyMock.expect(orderRegistry.getOrderManager(EasyMock.isA(OrderParticipantKey.class))).andReturn(orderManager)
                .anyTimes();
        EasyMock.expect(toolbox.getOrderManagerRegistry()).andReturn(orderRegistry).anyTimes();

        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(prefs.getStringSet(EasyMock.isA(String.class), (java.util.Set<String>)EasyMock.isA(Set.class)))
                .andReturn(New.set()).anyTimes();
        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(EasyMock.eq(DefaultDataTypeInfo.class))).andReturn(prefs).anyTimes();
        EasyMock.expect(toolbox.getPreferencesRegistry()).andReturn(prefsRegistry).anyTimes();

        MenuBarRegistry bar = support.createMock(MenuBarRegistry.class);
        bar.addTaskActivity(EasyMock.isA(CancellableTaskActivity.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myTa = (TaskActivity)EasyMock.getCurrentArguments()[0];
            assertTrue(myTa.isActive());
            return null;
        });
        UIRegistry ui = support.createMock(UIRegistry.class);
        EasyMock.expect(ui.getMenuBarRegistry()).andReturn(bar);
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(ui);

        return toolbox;
    }

    /**
     * Creates an easy mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @param latch Used to synchronize the test.
     * @return The mocked type controller.
     */
    private DataTypeController createTypeController(EasyMockSupport support, CountDownLatch latch)
    {
        DataTypeController typeController = support.createMock(DataTypeController.class);

        typeController.addDataType(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(DataTypeInfo.class),
                EasyMock.isA(TypeService.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myAddedType = (DataTypeInfo)EasyMock.getCurrentArguments()[2];
            myAddedType.setVisible(false, this);
            myAddedType.getParent().activationProperty().setActive(true);
            return null;
        });
        EasyMock.expect(typeController.addDataElements(EasyMock.isA(DataElementProvider.class), EasyMock.isNull(),
                EasyMock.isNull(), EasyMock.isA(MergeGroupActivationListener.class))).andAnswer(() ->
                {
                    myProvider = (DataElementProvider)EasyMock.getCurrentArguments()[0];
                    latch.countDown();
                    return New.list(Long.valueOf(1), Long.valueOf(2));
                });
        EasyMock.expect(
                Boolean.valueOf(typeController.removeDataType(EasyMock.isA(DataTypeInfo.class), EasyMock.isA(TypeService.class))))
                .andAnswer(() ->
                {
                    if (myAddedType.equals(EasyMock.getCurrentArguments()[0]))
                    {
                        myAddedType = null;
                    }
                    return Boolean.TRUE;
                });

        return typeController;
    }

    /**
     * The answer to the mocked query call.
     *
     * @return The ids.
     */
    @SuppressWarnings("unchecked")
    private long[] queryAnswer()
    {
        SimpleQuery<MergedDataRow> query = (SimpleQuery<MergedDataRow>)EasyMock.getCurrentArguments()[0];
        assertEquals(DataRegistryUtils.getInstance().getMergeDataCategory(ourMergeLayerName), query.getDataModelCategory());
        PropertyValueReceiver<MergedDataRow> receiver = (PropertyValueReceiver<MergedDataRow>)query.getPropertyValueReceivers()
                .iterator().next();
        receiver.receive(myDeposits);

        return new long[] { 0, 1 };
    }
}
