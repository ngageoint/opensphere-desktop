package io.opensphere.merge.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;

import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.merge.controller.MergeController;
import io.opensphere.merge.model.MergeModel;

/**
 * Unit test for the {@link MergeUI}.
 */
@SuppressWarnings("restriction")
public class MergeUITestDisplay
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
     * Tests when the user hits the ok button.
     *
     * @throws InterruptedException if the test fails.
     */
    @Test
    public void testAccept() throws InterruptedException
    {
        try
        {
            Platform.startup(() ->
            {
            });
        }
        catch (IllegalStateException e)
        {
            // Platform already started; ignore
        }

        EasyMockSupport support = new EasyMockSupport();

        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);
        DataGroupController groupController = createGroupController(support);
        DataTypeController typeController = support.createMock(DataTypeController.class);
        MantleToolbox mantle = createMantle(support, lookup, groupController, typeController);

        ColumnMappingController mapper = createMapper(support);
        Toolbox toolbox = createToolbox(support, mantle, mapper);

        CountDownLatch latch = new CountDownLatch(1);

        MenuBarRegistry bar = support.createMock(MenuBarRegistry.class);
        bar.addTaskActivity(EasyMock.isA(CancellableTaskActivity.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            latch.countDown();
            return null;
        });
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getMenuBarRegistry()).andReturn(bar);
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry);

        support.replayAll();

        MergeModel model = new MergeModel(New.list());

        MergeController controller = new MergeController(toolbox, null, null);
        controller.setModel(model);
        MergeUI ui = new MergeUI(toolbox, controller, model);
        ui.accept();

        latch.await();

        support.verifyAll();
    }

    /**
     * Tests the binding to certain fields.
     */
    @Test
    public void testMergeUI()
    {
        try
        {
            Platform.startup(() ->
            {
            });
        }
        catch (IllegalStateException e)
        {
            // Platform already started; ignore
        }

        EasyMockSupport support = new EasyMockSupport();

        List<DataTypeInfo> layers = createDataTypes(support);
        EasyMock.expect(layers.get(0).getDisplayName()).andReturn(ourLayer1);
        EasyMock.expect(layers.get(1).getDisplayName()).andReturn(ourLayer2);

        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);
        DataGroupController groupController = createGroupController(support);
        DataTypeController typeController = support.createMock(DataTypeController.class);
        MantleToolbox mantle = createMantle(support, lookup, groupController, typeController);

        ColumnMappingController mapper = createMapper(support);
        Toolbox toolbox = createToolbox(support, mantle, mapper);

        support.replayAll();

        MergeModel model = new MergeModel(layers);

        MergeController controller = new MergeController(toolbox, null, null);
        controller.setModel(model);
        MergeUI ui = new MergeUI(toolbox, controller, model);

        assertFalse(StringUtils.isEmpty(ui.getUserMessage().getText()));
        assertEquals(model.getUserMessage().get(), ui.getUserMessage().getText());

        ui.getNewName().setText("Merged Layer");
        assertEquals(model.getNewLayerName().get(), ui.getNewName().getText());

        assertNotNull(ui.getValidatorSupport());

        support.verifyAll();
    }

    /**
     * Creates a mocked data type.
     *
     * @param support Used to create the mock.
     * @param layer The layer name.
     * @param columns The layer columns.
     * @return The mocked data type.
     */
    private DataTypeInfo createDataType(EasyMockSupport support, String layer, List<String> columns)
    {
        MetaDataInfo metadataInfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metadataInfo.getKeyNames()).andReturn(columns).atLeastOnce();
        EasyMock.expect(Boolean.valueOf(metadataInfo.hasKey(EasyMock.isA(String.class)))).andAnswer(() ->
        {
            return Boolean.valueOf(columns.contains(EasyMock.getCurrentArguments()[0]));
        }).anyTimes();
        metadataInfo.getKeyClassType(EasyMock.isA(String.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            Class<?> type = String.class;
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
        DataGroupController groupController = support.createNiceMock(DataGroupController.class);

        EasyMock.expect(Boolean.valueOf(
                groupController.addRootDataGroupInfo(EasyMock.isA(DataGroupInfo.class), EasyMock.isA(MergeController.class))))
                .andReturn(Boolean.TRUE);

        return groupController;
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
     * Creates an easy mocked {@link ColumnMappingController} expected check
     * associations to be called with an association.
     *
     * @param support Used to create the mock.
     * @return The mocked mapping controller.
     */
    @SuppressWarnings("unchecked")
    private ColumnMappingController createMapper(EasyMockSupport support)
    {
        ColumnMappingController mapper = support.createMock(ColumnMappingController.class);

        EasyMock.expect(mapper.getDefinedColumns(EasyMock.isA(List.class))).andReturn(New.map());

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
        Toolbox toolbox = support.createMock(Toolbox.class);

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(EasyMock.eq(MantleToolbox.class))).andReturn(mantle).atLeastOnce();
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();

        DataFilterRegistry filterRegistry = support.createMock(DataFilterRegistry.class);
        EasyMock.expect(filterRegistry.getColumnMappingController()).andReturn(mapper);
        EasyMock.expect(toolbox.getDataFilterRegistry()).andReturn(filterRegistry);

        EventManager eventManager = support.createNiceMock(EventManager.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager).anyTimes();

        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        EasyMock.expect(toolbox.getDataRegistry()).andReturn(dataRegistry);

        return toolbox;
    }
}
