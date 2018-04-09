package io.opensphere.featureactions.editor.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ListView;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import javafx.application.Platform;
import javafx.scene.control.skin.VirtualFlow;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesImpl;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.FeatureActions;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
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

/**
 * Unit test for {@link SimpleFeatureActionPane}.
 */
public class SimpleFeatureActonEditorUITestDisplay
{
    /** The test action name. */
    private static final String ourActionName = "Action 1";

    /** The available columns for test. */
    private static final List<String> ourColumns = New.list("MESSAGE", "TIME", "USER NAME");

    /** Test group name. */
    private static final String ourGroupName = "other group";

    /** The id of the layer. */
    private static final String ourLayerId = "twitterid";

    /** The test layer name. */
    private static final String ourLayerName = "We are layer";

    /** The test layer itself. */
    private static final DefaultDataTypeInfo ourLayer = new DefaultDataTypeInfo(null, "bla", ourLayerId, ourLayerName,
            ourLayerName, true);

    /**
     * The current row to test.
     */
    private SimpleFeatureActionRow myRow;

    /**
     * Tests when the user creates a new feature action.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testCreate() throws InterruptedException
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        CountDownLatch latch = new CountDownLatch(1);
        Toolbox toolbox = createToolbox(support, 7, latch);
        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry actionRegistry = new FeatureActionsRegistry(prefsRegistry);
        SimpleFeatureActionEditorUI editor = createEditor(support, toolbox, actionRegistry);

        assertEquals("New Group", editor.getAddButton().getText());
        editor.getAddButton().fire();

        assertNotNull(editor.getAccordion().getExpandedPane());
        assertEquals(2, editor.getAccordion().getPanes().size());
        ListView<SimpleFeatureAction> listView = ((SimpleFeatureActionPane)editor.getAccordion().getPanes().get(1).getContent())
                .getListView();

        JFXPanel panel = new JFXPanel();
        panel.setScene(FXUtilities.addDesktopStyle(new Scene(editor)));

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        @SuppressWarnings("unchecked")
        VirtualFlow<SimpleFeatureActionRow> flow = (VirtualFlow<SimpleFeatureActionRow>)listView.getChildrenUnmodifiable().get(0);

        assertEquals(1, flow.getCellCount());

        FXUtilities.runOnFXThreadAndWait(() ->
        {
            myRow = flow.getCell(0);
        });

        myRow.getName().textProperty().set(ourActionName);
        myRow.getField().valueProperty().set(ourColumns.get(2));
        myRow.getValue().setText("B*");
        myRow.getColorPicker().valueProperty().set(FXUtilities.fromAwtColor(Color.CYAN));

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        Thread.sleep(100);
        editor.accept();

        FeatureAction newAction = prefs.getJAXBObject(FeatureActions.class, ourLayerId, null).getActions().get(0);

        assertEquals(ourActionName, newAction.getName());
        assertEquals("Feature Actions 1", newAction.getGroupName());
        assertTrue(newAction.enabledProperty().get());
        StyleAction styleAction = (StyleAction)newAction.getActions().get(0);
        assertEquals(Color.CYAN, styleAction.getStyleOptions().getColor());
        assertEquals(0, styleAction.getStyleOptions().getIconId());
        assertEquals(ourColumns.get(2), newAction.getFilter().getFilterGroup().getCriteria().get(0).getField());
        assertEquals(Conditional.LIKE, newAction.getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals("B*", newAction.getFilter().getFilterGroup().getCriteria().get(0).getValue());

        support.verifyAll();
    }

    /**
     * Tests when the user deletes a bin criteria element.
     */
    @Test
    public void testDelete()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, 7);
        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        FeatureAction otherAction = new FeatureAction();
        otherAction.setGroupName(ourGroupName);
        FeatureActions actions = new FeatureActions();
        actions.getActions().add(otherAction);
        prefs.putJAXBObject(ourLayerId, actions, false, this);

        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry actionRegistry = new FeatureActionsRegistry(prefsRegistry);
        SimpleFeatureActionEditorUI editor = createEditor(support, toolbox, actionRegistry);

        ListView<SimpleFeatureAction> listView = ((SimpleFeatureActionPane)editor.getAccordion().getPanes().get(0).getContent())
                .getListView();

        JFXPanel panel = new JFXPanel();
        panel.setScene(FXUtilities.addDesktopStyle(new Scene(editor)));

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        @SuppressWarnings("unchecked")
        VirtualFlow<SimpleFeatureActionRow> flow = (VirtualFlow<SimpleFeatureActionRow>)listView.getChildrenUnmodifiable().get(0);

        assertEquals(1, flow.getCellCount());

        FXUtilities.runOnFXThreadAndWait(() ->
        {
            myRow = flow.getCell(0);
        });

        FXUtilities.runOnFXThreadAndWait(() ->
        {
            myRow.getRemoveButton().fire();
        });

        editor.accept();

        assertNull(prefs.getJAXBObject(FeatureActions.class, ourLayerId, null));

        support.verifyAll();
    }

    /**
     * Tests when the user just wants to read the bin criteria. Note: this test
     * never finishes, successfully or otherwise.
     */
    @Test
    public void testRead()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, 7);
        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        FeatureAction action = new FeatureAction();
        action.setName(ourActionName);
        action.setEnabled(true);
        action.setGroupName(ourGroupName);
        StyleAction style = new StyleAction();
        style.getStyleOptions().setColor(Color.CYAN);
        action.getActions().add(style);

        Filter filter = new Filter();
        Group filterGroup = new Group();
        filterGroup.getCriteria().add(new Criteria(ourColumns.get(0), Conditional.LIKE, "B*"));
        filter.setFilterGroup(filterGroup);
        action.setFilter(filter);

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(action);
        prefs.putJAXBObject(ourLayerId, actions, false, this);

        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry actionRegistry = new FeatureActionsRegistry(prefsRegistry);
        SimpleFeatureActionEditorUI editor = createEditor(support, toolbox, actionRegistry);

        assertNotNull(editor.getAccordion().getExpandedPane());

        ListView<SimpleFeatureAction> listView = ((SimpleFeatureActionPane)editor.getAccordion().getPanes().get(0).getContent())
                .getListView();

        JFXPanel panel = new JFXPanel();
        panel.setScene(FXUtilities.addDesktopStyle(new Scene(editor)));

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        @SuppressWarnings("unchecked")
        VirtualFlow<SimpleFeatureActionRow> flow = (VirtualFlow<SimpleFeatureActionRow>)listView.getChildrenUnmodifiable().get(0);

        assertEquals(1, flow.getCellCount());

        FXUtilities.runOnFXThreadAndWait(() ->
        {
            myRow = flow.getCell(0);
        });

        assertEquals(ourActionName, myRow.getName().getText());
        assertEquals(ourGroupName, ((FeatureActionTitledPane)editor.getAccordion().getPanes().get(0)).getTitle().getText());
        assertTrue(myRow.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), myRow.getColorPicker().getValue());
        assertEquals(ourColumns.get(0), myRow.getField().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, myRow.getOptions().valueProperty().get());
        assertEquals("B*", myRow.getValue().getText());

        support.verifyAll();
    }

    /**
     * Tests when the user updates a bin criteria. Note: this test never
     * finishes, successfully or otherwise.
     */
    @Test
    public void testUpdate()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, 7);
        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        FeatureAction action = new FeatureAction();
        action.setName(ourActionName);
        action.setEnabled(true);
        action.setGroupName(ourGroupName);
        StyleAction style = new StyleAction();
        style.getStyleOptions().setColor(Color.CYAN);
        action.getActions().add(style);

        Filter filter = new Filter();
        Group filterGroup = new Group();
        filterGroup.getCriteria().add(new Criteria(ourColumns.get(0), Conditional.LIKE, "B*"));
        filter.setFilterGroup(filterGroup);
        action.setFilter(filter);

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(action);
        prefs.putJAXBObject(ourLayerId, actions, false, this);

        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry actionRegistry = new FeatureActionsRegistry(prefsRegistry);
        SimpleFeatureActionEditorUI editor = createEditor(support, toolbox, actionRegistry);

        ListView<SimpleFeatureAction> listView = ((SimpleFeatureActionPane)editor.getAccordion().getPanes().get(0).getContent())
                .getListView();

        JFXPanel panel = new JFXPanel();
        panel.setScene(FXUtilities.addDesktopStyle(new Scene(editor)));

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        @SuppressWarnings("unchecked")
        VirtualFlow<SimpleFeatureActionRow> flow = (VirtualFlow<SimpleFeatureActionRow>)listView.getChildrenUnmodifiable().get(0);

        assertEquals(1, flow.getCellCount());

        FXUtilities.runOnFXThreadAndWait(() ->
        {
            myRow = flow.getCell(0);
        });

        assertEquals(ourActionName, myRow.getName().getText());
        assertEquals(ourGroupName, ((FeatureActionTitledPane)editor.getAccordion().getPanes().get(0)).getTitle().getText());
        assertTrue(myRow.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), myRow.getColorPicker().getValue());
        assertEquals(ourColumns.get(0), myRow.getField().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, myRow.getOptions().valueProperty().get());
        assertEquals("B*", myRow.getValue().getText());

        myRow.getValue().setText("C*");

        editor.accept();

        FeatureAction newAction = prefs.getJAXBObject(FeatureActions.class, ourLayerId, null).getActions().get(0);

        assertEquals(ourActionName, newAction.getName());
        assertEquals(ourGroupName, newAction.getGroupName());
        assertTrue(newAction.enabledProperty().get());
        StyleAction styleAction = (StyleAction)newAction.getActions().get(0);
        assertEquals(Color.CYAN, styleAction.getStyleOptions().getColor());
        assertEquals(0, styleAction.getStyleOptions().getIconId());
        assertEquals(ourColumns.get(0), newAction.getFilter().getFilterGroup().getCriteria().get(0).getField());
        assertEquals(Conditional.LIKE, newAction.getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals("C*", newAction.getFilter().getFilterGroup().getCriteria().get(0).getValue());

        support.verifyAll();
    }

    /**
     * Tests when the user updates a bin criteria. Note: this test never
     * finishes, successfully or otherwise.
     */
    @Test
    public void testUpdateGroupName()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, 7);
        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        FeatureAction action = new FeatureAction();
        action.setName(ourActionName);
        action.setEnabled(true);
        action.setGroupName(ourGroupName);
        StyleAction style = new StyleAction();
        style.getStyleOptions().setColor(Color.CYAN);
        action.getActions().add(style);

        Filter filter = new Filter();
        Group filterGroup = new Group();
        filterGroup.getCriteria().add(new Criteria(ourColumns.get(0), Conditional.LIKE, "B*"));
        filter.setFilterGroup(filterGroup);
        action.setFilter(filter);

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(action);
        prefs.putJAXBObject(ourLayerId, actions, false, this);

        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry actionRegistry = new FeatureActionsRegistry(prefsRegistry);
        SimpleFeatureActionEditorUI editor = createEditor(support, toolbox, actionRegistry);

        ListView<SimpleFeatureAction> listView = ((SimpleFeatureActionPane)editor.getAccordion().getPanes().get(0).getContent())
                .getListView();

        JFXPanel panel = new JFXPanel();
        panel.setScene(FXUtilities.addDesktopStyle(new Scene(editor)));

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        @SuppressWarnings("unchecked")
        VirtualFlow<SimpleFeatureActionRow> flow = (VirtualFlow<SimpleFeatureActionRow>)listView.getChildrenUnmodifiable().get(0);

        assertEquals(1, flow.getCellCount());

        FXUtilities.runOnFXThreadAndWait(() ->
        {
            myRow = flow.getCell(0);
        });

        assertEquals(ourActionName, myRow.getName().getText());
        assertEquals(ourGroupName, ((FeatureActionTitledPane)editor.getAccordion().getPanes().get(0)).getTitle().getText());
        assertTrue(myRow.getEnabled().selectedProperty().get());
        assertEquals(FXUtilities.fromAwtColor(Color.CYAN), myRow.getColorPicker().getValue());
        assertEquals(ourColumns.get(0), myRow.getField().valueProperty().get());
        assertEquals(CriteriaOptions.VALUE, myRow.getOptions().valueProperty().get());
        assertEquals("B*", myRow.getValue().getText());

        ((FeatureActionTitledPane)editor.getAccordion().getPanes().get(0)).getTitle().setText("New Group Name");

        editor.accept();

        FeatureAction newAction = prefs.getJAXBObject(FeatureActions.class, ourLayerId, null).getActions().get(0);

        assertEquals(ourActionName, newAction.getName());
        assertEquals("New Group Name", newAction.getGroupName());
        assertTrue(newAction.enabledProperty().get());
        StyleAction styleAction = (StyleAction)newAction.getActions().get(0);
        assertEquals(Color.CYAN, styleAction.getStyleOptions().getColor());
        assertEquals(0, styleAction.getStyleOptions().getIconId());
        assertEquals(ourColumns.get(0), newAction.getFilter().getFilterGroup().getCriteria().get(0).getField());
        assertEquals(Conditional.LIKE, newAction.getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals("B*", newAction.getFilter().getFilterGroup().getCriteria().get(0).getValue());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link SimpleFeatureActionEditor}.
     *
     * @param support Used to create the mock.
     * @param toolbox The mocked toolbox.
     * @param actionRegistry The {@link FeatureActionsRegistry}.
     * @return The mocked {@link SimpleFeatureActionEditor}.
     */
    private SimpleFeatureActionEditorUI createEditor(EasyMockSupport support, Toolbox toolbox,
            FeatureActionsRegistry actionRegistry)
    {
        return new SimpleFeatureActionEditorUI(toolbox, actionRegistry, null, ourLayer);
    }

    /**
     * Creates an easy mocked {@link PreferencesRegistry}.
     *
     * @param support Used to create the mock.
     * @param prefs The preferences to return.
     * @return The mock {@link PreferencesRegistry}.
     */
    private PreferencesRegistry createPrefsRegistry(EasyMockSupport support, Preferences prefs)
    {
        PreferencesRegistry prefsRegistry = support.createMock(PreferencesRegistry.class);

        EasyMock.expect(prefsRegistry.getPreferences(FeatureActionsRegistry.class)).andReturn(prefs);

        return prefsRegistry;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the toolbox.
     * @param iconId The icon id to expect.
     * @param latches Used to synchronize the tests.
     * @return The system toolbox.
     */
    @SuppressWarnings("unchecked")
    private Toolbox createToolbox(EasyMockSupport support, int iconId, CountDownLatch... latches)
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
        EasyMock.expect(Integer.valueOf(record.getId())).andReturn(Integer.valueOf(iconId)).anyTimes();
        EasyMock.expect(record.getImageURL()).andReturn(IconRegistry.DEFAULT_ICON_URL).atLeastOnce();

        IconRegistry iconRegistry = support.createMock(IconRegistry.class);
        if (iconId == 7)
        {
            EasyMock.expect(iconRegistry.getIconRecord(IconRegistry.DEFAULT_ICON_URL)).andAnswer(() ->
            {
                if (latches.length > 0)
                {
                    latches[0].countDown();
                }
                return record;
            }).atLeastOnce();
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
