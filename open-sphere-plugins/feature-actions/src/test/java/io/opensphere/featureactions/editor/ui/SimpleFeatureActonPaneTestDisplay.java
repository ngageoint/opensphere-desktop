package io.opensphere.featureactions.editor.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.featureactions.editor.controller.SimpleFeatureActionGroupAdapter;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;

/**
 * Unit test for {@link SimpleFeatureActionPane}.
 */
public class SimpleFeatureActonPaneTestDisplay
{
    /** The test action name. */
    private static final String ourActionName = "Action 1";

    /** The available columns for test. */
    private static final List<String> ourColumns = New.list("MESSAGE", "TIME", "USER NAME");

    /** The id of the layer. */
    private static final String ourLayerId = "twitterid";

    /** The test layer name. */
    private static final String ourLayerName = "We are layer";

    /** The test layer itself. */
    private static final DefaultDataTypeInfo ourTestLayer = new DefaultDataTypeInfo(null, "bla", ourLayerId, ourLayerName,
            ourLayerName, true);

    /** Initializes the JavaFX platform. */
    @Before
    public void initialize()
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
    }

    /**
     * Tests when the user creates a new feature action.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testCreate() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(1);
        Toolbox toolbox = createToolbox(support, 7, latch);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName("the group name");
        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);

        support.replayAll();

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        FeatureAction newAction = new FeatureAction();
        newAction.getActions().add(new StyleAction());
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(newAction);
        FXUtilities.runOnFXThreadAndWait(() ->
        {
            group.getActions().add(simpleAction);
        });

        SimpleFeatureActionPane editor = new SimpleFeatureActionPane(toolbox, actions, group, ourTestLayer, null,
                new DragDropHandler(actions));

        ListView<SimpleFeatureAction> listView = editor.getListView();

        JFXPanel panel = new JFXPanel();
        panel.setScene(FXUtilities.addDesktopStyle(new Scene(editor)));

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        @SuppressWarnings("unchecked")
        VirtualFlow<SimpleFeatureActionRow> flow = (VirtualFlow<SimpleFeatureActionRow>)listView.getChildrenUnmodifiable().get(0);

        assertEquals(1, flow.getCellCount());

        SimpleFeatureActionRow row = flow.getCell(0);

        row.getName().textProperty().set(ourActionName);
        row.getField().valueProperty().set(ourColumns.get(2));
        row.getValue().setText("B*");
        row.getColorPicker().valueProperty().set(FXUtilities.fromAwtColor(Color.CYAN));

        assertEquals(ourActionName, newAction.getName());
        assertEquals("the group name", newAction.getGroupName());
        assertTrue(newAction.enabledProperty().get());
        StyleAction styleAction = (StyleAction)newAction.getActions().get(0);
        assertEquals(Color.CYAN, styleAction.getStyleOptions().getColor());
        assertEquals(0, styleAction.getStyleOptions().getIconId());
        assertEquals(ourColumns.get(2), newAction.getFilter().getFilterGroup().getCriteria().get(0).getField());
        assertEquals(Conditional.LIKE, newAction.getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals("B*", newAction.getFilter().getFilterGroup().getCriteria().get(0).getValue());

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when the user deletes a bin criteria element.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testDelete() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(1);
        Toolbox toolbox = createToolbox(support, 22, latch);
        FeatureAction action = new FeatureAction();
        action.setName(ourActionName);
        action.setEnabled(true);
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.CYAN);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        Filter filter = new Filter();
        Group filterGroup = new Group();
        filterGroup.getCriteria().add(new Criteria(ourColumns.get(0), Conditional.LIKE, "B*"));
        filter.setFilterGroup(filterGroup);
        action.setFilter(filter);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        group.getActions().add(simpleAction);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        support.replayAll();

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionPane editor = new SimpleFeatureActionPane(toolbox, actions, group, ourTestLayer, null,
                new DragDropHandler(actions));

        ListView<SimpleFeatureAction> listView = editor.getListView();

        JFXPanel panel = new JFXPanel();
        Scene scene = new Scene(editor);
        panel.setScene(FXUtilities.addDesktopStyle(scene));
        @SuppressWarnings("unchecked")
        VirtualFlow<SimpleFeatureActionRow> flow = (VirtualFlow<SimpleFeatureActionRow>)listView.getChildrenUnmodifiable().get(0);

        assertEquals(1, flow.getCellCount());

        SimpleFeatureActionRow view = flow.getCell(0);
        assertEquals(ourActionName, view.getName().getText());
        assertTrue(view.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), view.getColorPicker().valueProperty().get());
        assertEquals(ourColumns.get(0), view.getField().valueProperty().get());
        assertEquals(ourColumns, view.getField().getItems());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().getItems().get(0));
        assertEquals(CriteriaOptions.RANGE, view.getOptions().getItems().get(1));
        assertEquals("B*", view.getValue().getText());

        FXUtilities.runOnFXThreadAndWait(() ->
        {
            view.getRemoveButton().fire();
        });

        FXUtilities.addDesktopStyle(scene);

        assertEquals(0, group.getActions().size());

        adapter.close();

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        support.verifyAll();
    }

    /**
     * Tests when the user just wants to read the bin criteria.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testRead() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(1);

        Toolbox toolbox = createToolbox(support, 22, latch);
        FeatureAction action = new FeatureAction();
        action.setName(ourActionName);
        action.setEnabled(true);
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.CYAN);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        group.getActions().add(simpleAction);

        Filter filter = new Filter();
        Group filterGroup = new Group();
        filterGroup.getCriteria().add(new Criteria(ourColumns.get(0), Conditional.LIKE, "B*"));
        filter.setFilterGroup(filterGroup);
        action.setFilter(filter);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        support.replayAll();

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionPane editor = new SimpleFeatureActionPane(toolbox, actions, group, ourTestLayer, null,
                new DragDropHandler(actions));

        ListView<SimpleFeatureAction> listView = editor.getListView();

        JFXPanel panel = new JFXPanel();
        Scene scene = new Scene(editor);
        panel.setScene(FXUtilities.addDesktopStyle(scene));
        @SuppressWarnings("unchecked")
        VirtualFlow<SimpleFeatureActionRow> flow = (VirtualFlow<SimpleFeatureActionRow>)listView.getChildrenUnmodifiable().get(0);

        assertEquals(1, flow.getCellCount());

        SimpleFeatureActionRow view = flow.getCell(0);
        assertEquals(ourActionName, view.getName().getText());
        assertTrue(view.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), view.getColorPicker().valueProperty().get());
        assertEquals(ourColumns.get(0), view.getField().valueProperty().get());
        assertEquals(ourColumns, view.getField().getItems());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().getItems().get(0));
        assertEquals(CriteriaOptions.RANGE, view.getOptions().getItems().get(1));
        assertEquals("B*", view.getValue().getText());

        adapter.close();

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        support.verifyAll();
    }

    /**
     * Tests when the user updates a bin criteria.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testUpdate() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(1);
        Toolbox toolbox = createToolbox(support, 22, latch);
        FeatureAction action = new FeatureAction();
        action.setName(ourActionName);
        action.setEnabled(true);
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.CYAN);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        group.getActions().add(simpleAction);

        Filter filter = new Filter();
        Group filterGroup = new Group();
        filterGroup.getCriteria().add(new Criteria(ourColumns.get(0), Conditional.LIKE, "B*"));
        filter.setFilterGroup(filterGroup);
        action.setFilter(filter);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        support.replayAll();

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionPane editor = new SimpleFeatureActionPane(toolbox, actions, group, ourTestLayer, null,
                new DragDropHandler(actions));

        ListView<SimpleFeatureAction> listView = editor.getListView();

        JFXPanel panel = new JFXPanel();
        Scene scene = new Scene(editor);
        panel.setScene(FXUtilities.addDesktopStyle(scene));
        @SuppressWarnings("unchecked")
        VirtualFlow<SimpleFeatureActionRow> flow = (VirtualFlow<SimpleFeatureActionRow>)listView.getChildrenUnmodifiable().get(0);

        assertEquals(1, flow.getCellCount());

        SimpleFeatureActionRow view = flow.getCell(0);
        assertEquals(ourActionName, view.getName().getText());
        assertTrue(view.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), view.getColorPicker().valueProperty().get());
        assertEquals(ourColumns.get(0), view.getField().valueProperty().get());
        assertEquals(ourColumns, view.getField().getItems());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().getItems().get(0));
        assertEquals(CriteriaOptions.RANGE, view.getOptions().getItems().get(1));
        assertEquals("B*", view.getValue().getText());

        view.getValue().setText("C*");

        assertEquals("C*", action.getFilter().getFilterGroup().getCriteria().get(0).getValue());

        adapter.close();

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the toolbox.
     * @param iconId The icon id to expect.
     * @param latch Used to synchronize the test.
     * @return The system toolbox.
     */
    @SuppressWarnings("unchecked")
    private Toolbox createToolbox(EasyMockSupport support, int iconId, CountDownLatch latch)
    {
        MetaDataInfo metadataInfo = support.createMock(MetaDataInfo.class);
        List<String> columnNames = New.list(ourColumns);
        Collections.reverse(columnNames);
        EasyMock.expect(metadataInfo.getKeyNames()).andReturn(columnNames).atLeastOnce();

        DataTypeInfo layer = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer.getMetaDataInfo()).andReturn(metadataInfo).atLeastOnce();

        DataTypeController controller = support.createMock(DataTypeController.class);
        EasyMock.expect(controller.getDataTypeInfoForType(ourLayerId)).andReturn(layer).atLeastOnce();

        IconRecord record = support.createMock(IconRecord.class);
        EasyMock.expect(Long.valueOf(record.idProperty().get())).andAnswer(() ->
        {
            latch.countDown();
            return Long.valueOf(iconId);
        }).anyTimes();
        EasyMock.expect(record.imageURLProperty().get()).andReturn(IconRegistry.DEFAULT_ICON_URL).atLeastOnce();

        IconRegistry iconRegistry = support.createMock(IconRegistry.class);
        if (iconId == 7)
        {
            EasyMock.expect(iconRegistry.getIconRecord(IconRegistry.DEFAULT_ICON_URL)).andReturn(record).atLeastOnce();
        }
        else
        {
            EasyMock.expect(iconRegistry.getIconRecordByIconId(iconId)).andReturn(record).atLeastOnce();
        }

        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantle.getDataTypeController()).andReturn(controller).atLeastOnce();
        EasyMock.expect(mantle.getIconRegistry()).andReturn(iconRegistry).atLeastOnce();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantle).atLeastOnce();

        @SuppressWarnings("rawtypes")
        Supplier supplier = support.createMock(Supplier.class);
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getMainFrameProvider()).andReturn(supplier).atLeastOnce();

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry).atLeastOnce();

        return toolbox;
    }
}
