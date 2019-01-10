package io.opensphere.featureactions.editor.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
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

/**
 * Unit test for {@link SimpleFeatureActionRowBinder}.
 */
public class SimpleFeatureActionRowTestDisplay
{
    /** The test action name. */
    private static final String ourActionName = "Action 1";

    /** The available columns for test. */
    private static final List<String> ourColumns = New.list("MESSAGE", "TIME", "USER NAME");

    /** The test layer id. */
    private static final String ourLayerId = "layerId";

    /** The test layer name. */
    private static final String ourLayerName = "We are layer";

    /** The test layer itself. */
    private static final DefaultDataTypeInfo ourLayer = new DefaultDataTypeInfo(null, "bla", ourLayerId, ourLayerName,
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

    /** Tests initial setup. */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

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

        SimpleFeatureActionRow view = createView(support, actions, group, simpleAction);

        support.replayAll();

        view.updateItem(simpleAction, false);
        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        assertEquals(ourActionName, view.getName().getText());
        assertTrue(view.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), view.getColorPicker().valueProperty().get());
        assertEquals(ourColumns.get(0), view.getField().valueProperty().get());
        assertEquals(ourColumns, view.getField().getItems());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().getItems().get(0));
        assertEquals(CriteriaOptions.RANGE, view.getOptions().getItems().get(1));
        assertEquals("B*", view.getValue().getText());
        assertTrue(view.getValue().isVisible());
        assertFalse(view.getMinimumValue().isVisible());
        assertFalse(view.getMaximumValue().isVisible());

        adapter.close();

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        support.verifyAll();
    }

    /**
     * Tests initial setup.
     */
    @Test
    public void testNullUpdateItem()
    {
        EasyMockSupport support = new EasyMockSupport();

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

        Toolbox toolbox = support.createMock(Toolbox.class);

        SimpleFeatureActionRow view = new SimpleFeatureActionRow(toolbox, actions, group, ourLayer, null);

        support.replayAll();

        view.updateItem(null, false);

        assertNull(view.getGraphic());

        support.verifyAll();
    }

    /**
     * Tests closing.
     */
    @Test
    public void testClose()
    {
        EasyMockSupport support = new EasyMockSupport();

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

        SimpleFeatureActionRow view = createView(support, actions, group, simpleAction);

        support.replayAll();

        view.updateItem(simpleAction, false);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        assertEquals(ourActionName, view.getName().getText());
        assertTrue(view.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), view.getColorPicker().valueProperty().get());
        assertEquals(ourColumns.get(0), view.getField().valueProperty().get());
        assertEquals(ourColumns, view.getField().getItems());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().getItems().get(0));
        assertEquals(CriteriaOptions.RANGE, view.getOptions().getItems().get(1));
        assertEquals("B*", view.getValue().getText());
        assertTrue(view.getValue().isVisible());
        assertFalse(view.getMinimumValue().isVisible());
        assertFalse(view.getMaximumValue().isVisible());
        assertNull(view.getMinimumValue().getText());
        assertNull(view.getMaximumValue().getText());

        view.updateItem(new SimpleFeatureAction(new FeatureAction()), false);
        adapter.close();

        simpleAction.getFeatureAction().setName("Action 2");
        simpleAction.getFeatureAction().setEnabled(false);
        simpleAction.getColumn().set("MESSAGE");
        simpleAction.getOption().set(CriteriaOptions.RANGE);
        simpleAction.getValue().set("new value");
        simpleAction.getMinimumValue().set("A");
        simpleAction.getMaximumValue().set("B");
        simpleAction.setColor(javafx.scene.paint.Color.RED);

        support.verifyAll();
    }

    /**
     * Tests when user clicks copy.
     */
    @Test
    public void testCopy()
    {
        EasyMockSupport support = new EasyMockSupport();

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

        SimpleFeatureActionRow view = createView(support, actions, group, simpleAction);

        support.replayAll();

        view.updateItem(simpleAction, false);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        view.getCopyButton().fire();

        assertEquals(2, actions.getFeatureGroups().get(0).getActions().size());

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests initial setup.
     */
    @Test
    public void testEditNew()
    {
        EasyMockSupport support = new EasyMockSupport();

        FeatureAction action = new FeatureAction();
        action.getActions().add(new StyleAction());
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName("the group name");
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        group.getActions().add(simpleAction);
        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        SimpleFeatureActionRow view = createView(support, actions, group, simpleAction);

        support.replayAll();

        view.updateItem(simpleAction, false);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        assertTrue(view.getValue().isVisible());
        assertFalse(view.getMinimumValue().isVisible());
        assertFalse(view.getMaximumValue().isVisible());

        view.getName().textProperty().set(ourActionName);
        view.getField().valueProperty().set(ourColumns.get(2));
        view.getValue().setText("B*");
        view.getColorPicker().valueProperty().set(FXUtilities.fromAwtColor(Color.CYAN));

        assertEquals(ourActionName, action.getName());
        assertEquals("the group name", action.getGroupName());
        assertTrue(action.enabledProperty().get());
        StyleAction styleAction = (StyleAction)action.getActions().get(0);
        assertEquals(Color.CYAN, styleAction.getStyleOptions().getColor());
        assertEquals(0, styleAction.getStyleOptions().getIconId());
        assertEquals(ourColumns.get(2), action.getFilter().getFilterGroup().getCriteria().get(0).getField());
        assertEquals(Conditional.LIKE, action.getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals("B*", action.getFilter().getFilterGroup().getCriteria().get(0).getValue());

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests initial setup.
     */
    @Test
    public void testRange()
    {
        EasyMockSupport support = new EasyMockSupport();

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
        filterGroup.getCriteria().add(new Criteria(ourColumns.get(0), Conditional.GTE, "B"));
        filterGroup.getCriteria().add(new Criteria(ourColumns.get(0), Conditional.LT, "M"));
        filter.setFilterGroup(filterGroup);
        action.setFilter(filter);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        SimpleFeatureActionRow view = createView(support, actions, group, simpleAction);

        support.replayAll();

        view.updateItem(simpleAction, false);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        assertEquals(ourActionName, view.getName().getText());
        assertTrue(view.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), view.getColorPicker().valueProperty().get());
        assertEquals(ourColumns.get(0), view.getField().valueProperty().get());
        assertEquals(CriteriaOptions.RANGE, view.getOptions().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().getItems().get(0));
        assertEquals(CriteriaOptions.RANGE, view.getOptions().getItems().get(1));
        assertEquals("B", view.getMinimumValue().getText());
        assertEquals("M", view.getMaximumValue().getText());
        assertFalse(view.getValue().isVisible());
        assertTrue(view.getMinimumValue().isVisible());
        assertTrue(view.getMaximumValue().isVisible());

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when user clicks remove.
     */
    @Test
    public void testRemove()
    {
        EasyMockSupport support = new EasyMockSupport();

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

        SimpleFeatureActionRow view = createView(support, actions, group, simpleAction);

        support.replayAll();

        view.updateItem(simpleAction, false);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        view.getRemoveButton().fire();

        assertFalse(actions.getFeatureGroups().isEmpty());

        adapter.close();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link DataTypeController}.
     *
     * @param support Used to create the mock.
     * @return The mocked controller.
     */
    private DataTypeController createTypeController(EasyMockSupport support)
    {
        MetaDataInfo metadataInfo = support.createMock(MetaDataInfo.class);
        List<String> columnNames = New.list(ourColumns);
        Collections.reverse(columnNames);
        EasyMock.expect(metadataInfo.getKeyNames()).andReturn(columnNames).atLeastOnce();

        DataTypeInfo layer = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer.getMetaDataInfo()).andReturn(metadataInfo).atLeastOnce();

        DataTypeController controller = support.createMock(DataTypeController.class);
        EasyMock.expect(controller.getDataTypeInfoForType(ourLayerId)).andReturn(layer).atLeastOnce();

        return controller;
    }

    /**
     * Creates an easy mocked {@link SimpleFeatureActionRowView}.
     *
     * @param support Used to create the mock.
     * @param actions The actions.
     * @param group The group.
     * @param action The action.
     * @return The mocked view.
     */
    @SuppressWarnings("unchecked")
    private SimpleFeatureActionRow createView(EasyMockSupport support, SimpleFeatureActions actions,
            SimpleFeatureActionGroup group, SimpleFeatureAction action)
    {
        IconRecord record = support.createMock(IconRecord.class);
        EasyMock.expect(Long.valueOf(record.idProperty().get())).andReturn(Long.valueOf(7)).anyTimes();
        EasyMock.expect(record.imageURLProperty().get()).andReturn(IconRegistry.DEFAULT_ICON_URL).atLeastOnce();

        IconRegistry iconRegistry = support.createMock(IconRegistry.class);
        EasyMock.expect(iconRegistry.getIconRecord(IconRegistry.DEFAULT_ICON_URL)).andReturn(record).atLeastOnce();

        DataTypeController typeController = createTypeController(support);

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantleToolbox.getDataTypeController()).andReturn(typeController).atLeastOnce();
        EasyMock.expect(mantleToolbox.getIconRegistry()).andReturn(iconRegistry).atLeastOnce();

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantleToolbox).atLeastOnce();

        @SuppressWarnings("rawtypes")
        Supplier supplier = support.createMock(Supplier.class);
        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getMainFrameProvider()).andReturn(supplier).atLeastOnce();

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry).atLeastOnce();

        SimpleFeatureActionRow view = new SimpleFeatureActionRow(toolbox, actions, group, ourLayer, null);

        return view;
    }
}
