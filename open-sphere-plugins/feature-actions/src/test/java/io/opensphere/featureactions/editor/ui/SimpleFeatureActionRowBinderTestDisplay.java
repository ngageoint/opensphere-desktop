package io.opensphere.featureactions.editor.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import javafx.application.Platform;

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
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * Unit test for {@link SimpleFeatureActionRowBinder}.
 */
public class SimpleFeatureActionRowBinderTestDisplay
{
    /**
     * The test action name.
     */
    private static final String ourActionName = "Action 1";

    /**
     * The available columns for test.
     */
    private static final List<String> ourColumns = New.list("MESSAGE", "TIME", "USER NAME");

    /**
     * The test layer id.
     */
    private static final String ourLayerId = "I am layer";

    /**
     * Tests initial setup.
     */
    @Test
    public void test()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        SimpleFeatureActionRowView view = createView(support);
        DataTypeController typeController = createTypeController(support);

        support.replayAll();

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

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionRowBinder binder = new SimpleFeatureActionRowBinder(view, typeController, actions, group,
                simpleAction);

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });
        assertEquals(ourActionName, view.getName().getText());
        assertTrue(view.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), view.getColorPicker().valueProperty().get());
        assertEquals(ourColumns.get(0), view.getField().valueProperty().get());
        assertEquals(ourColumns, view.getField().getItems());
        assertNotSame(simpleAction.getAvailableColumns(), view.getField().getItems());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, view.getOptions().getItems().get(0));
        assertEquals(CriteriaOptions.RANGE, view.getOptions().getItems().get(1));
        assertEquals("B*", view.getValue().getText());
        assertTrue(view.getValue().isVisible());
        assertFalse(view.getMinimumValue().isVisible());
        assertFalse(view.getMaximumValue().isVisible());

        binder.close();
        adapter.close();

        view.getField().valueProperty().set(ourColumns.get(1));
        view.getOptions().valueProperty().set(CriteriaOptions.RANGE);

        assertEquals(ourColumns.get(0), simpleAction.getColumn().get());
        assertEquals(CriteriaOptions.VALUE, simpleAction.getOption().get());

        support.verifyAll();
    }

    /**
     * Tests closing.
     */
    @Test
    public void testClose()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        SimpleFeatureActionRowView view = createView(support);
        DataTypeController typeController = createTypeController(support);

        support.replayAll();

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

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionRowBinder binder = new SimpleFeatureActionRowBinder(view, typeController, actions, group,
                simpleAction);

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });
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

        binder.close();
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
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        SimpleFeatureActionRowView view = createView(support);
        DataTypeController typeController = createTypeController(support);

        support.replayAll();

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

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionRowBinder binder = new SimpleFeatureActionRowBinder(view, typeController, actions, group,
                simpleAction);

        view.getCopyButton().fire();

        assertEquals(2, actions.getFeatureGroups().get(0).getActions().size());

        binder.close();
        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when user changes criteria options.
     */
    @Test
    public void testCriteriaOptionChanges()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        SimpleFeatureActionRowView view = createView(support);
        DataTypeController typeController = createTypeController(support);

        support.replayAll();

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

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionRowBinder binder = new SimpleFeatureActionRowBinder(view, typeController, actions, group,
                simpleAction);

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });
        assertTrue(view.getValue().isVisible());
        assertFalse(view.getMinimumValue().isVisible());
        assertFalse(view.getMaximumValue().isVisible());

        view.getOptions().valueProperty().set(CriteriaOptions.RANGE);

        assertFalse(view.getValue().isVisible());
        assertTrue(view.getMinimumValue().isVisible());
        assertTrue(view.getMaximumValue().isVisible());

        view.getOptions().valueProperty().set(CriteriaOptions.VALUE);

        assertTrue(view.getValue().isVisible());
        assertFalse(view.getMinimumValue().isVisible());
        assertFalse(view.getMaximumValue().isVisible());

        binder.close();
        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests initial setup.
     */
    @Test
    public void testRange()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        SimpleFeatureActionRowView view = createView(support);
        DataTypeController typeController = createTypeController(support);

        support.replayAll();

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

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionRowBinder binder = new SimpleFeatureActionRowBinder(view, typeController, actions, group,
                simpleAction);

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

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

        binder.close();
        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when user clicks remove.
     */
    @Test
    public void testRemove()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        SimpleFeatureActionRowView view = createView(support);
        DataTypeController typeController = createTypeController(support);

        support.replayAll();

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

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        SimpleFeatureActionRowBinder binder = new SimpleFeatureActionRowBinder(view, typeController, actions, group,
                simpleAction);

        view.getRemoveButton().fire();

        assertFalse(actions.getFeatureGroups().isEmpty());

        binder.close();
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
        EasyMock.expect(metadataInfo.getKeyNames()).andReturn(columnNames);

        DataTypeInfo layer = support.createMock(DataTypeInfo.class);
        EasyMock.expect(layer.getMetaDataInfo()).andReturn(metadataInfo);

        DataTypeController controller = support.createMock(DataTypeController.class);
        EasyMock.expect(controller.getDataTypeInfoForType(ourLayerId)).andReturn(layer);

        return controller;
    }

    /**
     * Creates an easy mocked {@link SimpleFeatureActionRowView}.
     *
     * @param support Used to create the mock.
     * @return The mocked view.
     */
    private SimpleFeatureActionRowView createView(EasyMockSupport support)
    {
        SimpleFeatureActionRowView view = support.createMock(SimpleFeatureActionRowView.class);

        EasyMock.expect(view.getColorPicker()).andReturn(new ColorPicker()).anyTimes();
        EasyMock.expect(view.getCopyButton()).andReturn(new Button()).anyTimes();
        EasyMock.expect(view.getEnabled()).andReturn(new CheckBox()).anyTimes();
        EasyMock.expect(view.getField()).andReturn(new ComboBox<String>()).anyTimes();
        EasyMock.expect(view.getMaximumValue()).andReturn(new TextField()).anyTimes();
        EasyMock.expect(view.getMinimumValue()).andReturn(new TextField()).anyTimes();
        EasyMock.expect(view.getName()).andReturn(new TextField()).anyTimes();
        EasyMock.expect(view.getOptions()).andReturn(new ComboBox<CriteriaOptions>()).anyTimes();
        EasyMock.expect(view.getRemoveButton()).andReturn(new Button()).anyTimes();
        EasyMock.expect(view.getValue()).andReturn(new TextField()).anyTimes();
        EasyMock.expect(view.getComplexFilterMask()).andReturn(new Label()).anyTimes();
        EasyMock.expect(view.getStyleAbsentMask()).andReturn(new Label()).anyTimes();
        EasyMock.expect(view.getIconPicker()).andReturn(null).anyTimes();
        view.setEditListener(EasyMock.notNull());

        return view;
    }
}
