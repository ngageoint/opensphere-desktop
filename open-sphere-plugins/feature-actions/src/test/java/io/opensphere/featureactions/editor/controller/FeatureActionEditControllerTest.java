package io.opensphere.featureactions.editor.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesImpl;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.FeatureActions;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;

/**
 * Unit test for {@link FeatureActionEditController}.
 */
public class FeatureActionEditControllerTest
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
     * The test group name.
     */
    private static final String ourGroupName = "group";

    /**
     * The test layer id.
     */
    private static final String ourLayerId = "I am layer";

    /**
     * Simulates the user canceling.
     */
    @Test
    public void testCancel()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(createAction1());
        actions.getActions().add(createAction2());

        prefs.putJAXBObject(ourLayerId, actions, false, this);

        FeatureActionsRegistry registry = new FeatureActionsRegistry(prefsRegistry);

        FeatureActionEditController controller = new FeatureActionEditController(registry, ourLayerId);

        controller.getModel().getFeatureGroups().get(0).getActions().get(0).setColor(FXUtilities.fromAwtColor(Color.RED));
        controller.close();

        assertEquals(Color.CYAN, ((StyleAction)actions.getActions().get(0).getActions().get(0)).getStyleOptions().getColor());
        assertEquals(Conditional.LIKE,
                actions.getActions().get(0).getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());

        assertEquals(Color.ORANGE, ((StyleAction)actions.getActions().get(1).getActions().get(0)).getStyleOptions().getColor());
        assertEquals(Conditional.GTE,
                actions.getActions().get(1).getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals(Conditional.LT,
                actions.getActions().get(1).getFilter().getFilterGroup().getCriteria().get(1).getComparisonOperator());

        support.verifyAll();
    }

    /**
     * Tests creating a new action.
     */
    @Test
    public void testCreateExisting()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(createAction1());

        prefs.putJAXBObject(ourLayerId, actions, false, this);

        FeatureActionsRegistry registry = new FeatureActionsRegistry(prefsRegistry);

        FeatureActionEditController controller = new FeatureActionEditController(registry, ourLayerId);

        FeatureAction action2 = createAction2();
        controller.getModel().getFeatureGroups().get(0).getActions().add(new SimpleFeatureAction(action2));
        controller.applyChanges();

        assertEquals(2, actions.getActions().size());

        assertEquals(Color.CYAN, ((StyleAction)actions.getActions().get(0).getActions().get(0)).getStyleOptions().getColor());
        assertEquals(Conditional.LIKE,
                actions.getActions().get(0).getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());

        assertEquals(Color.ORANGE, ((StyleAction)actions.getActions().get(1).getActions().get(0)).getStyleOptions().getColor());
        assertEquals(Conditional.GTE,
                actions.getActions().get(1).getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals(Conditional.LT,
                actions.getActions().get(1).getFilter().getFilterGroup().getCriteria().get(1).getComparisonOperator());

        support.verifyAll();
    }

    /**
     * Tests creating a new action.
     */
    @Test
    public void testCreateNew()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry registry = new FeatureActionsRegistry(prefsRegistry);

        FeatureActionEditController controller = new FeatureActionEditController(registry, ourLayerId);

        FeatureAction action = createAction1();
        controller.getModel().getFeatureGroups().add(new SimpleFeatureActionGroup());
        controller.getModel().getFeatureGroups().get(0).getActions().add(new SimpleFeatureAction(action));

        controller.applyChanges();

        FeatureActions actions = prefs.getJAXBObject(FeatureActions.class, ourLayerId, null);

        assertEquals(action, actions.getActions().get(0));

        support.verifyAll();
    }

    /**
     * Tests deleting an existing action.
     */
    @Test
    public void testDelete()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(createAction1());
        actions.getActions().add(createAction2());

        prefs.putJAXBObject(ourLayerId, actions, false, this);

        FeatureActionsRegistry registry = new FeatureActionsRegistry(prefsRegistry);

        FeatureActionEditController controller = new FeatureActionEditController(registry, ourLayerId);

        controller.getModel().getFeatureGroups().get(0).getActions().remove(0);
        controller.applyChanges();

        assertEquals(Color.ORANGE, ((StyleAction)actions.getActions().get(0).getActions().get(0)).getStyleOptions().getColor());
        assertEquals(Conditional.GTE,
                actions.getActions().get(0).getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals(Conditional.LT,
                actions.getActions().get(0).getFilter().getFilterGroup().getCriteria().get(1).getComparisonOperator());

        support.verifyAll();
    }

    /**
     * Tests deleting an existing action.
     */
    @Test
    public void testDeleteAll()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(createAction1());
        actions.getActions().add(createAction2());

        prefs.putJAXBObject(ourLayerId, actions, false, this);

        FeatureActionsRegistry registry = new FeatureActionsRegistry(prefsRegistry);

        FeatureActionEditController controller = new FeatureActionEditController(registry, ourLayerId);

        controller.getModel().getFeatureGroups().get(0).getActions().remove(0);
        controller.getModel().getFeatureGroups().get(0).getActions().remove(0);
        controller.applyChanges();

        assertNull(prefs.getJAXBObject(FeatureActions.class, ourLayerId, null));

        support.verifyAll();
    }

    /**
     * Tests just reading an existing action.
     */
    @Test
    public void testRead()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(createAction1());
        actions.getActions().add(createAction2());

        prefs.putJAXBObject(ourLayerId, actions, false, this);

        FeatureActionsRegistry registry = new FeatureActionsRegistry(prefsRegistry);

        FeatureActionEditController controller = new FeatureActionEditController(registry, ourLayerId);

        assertEquals(ourGroupName, controller.getModel().getFeatureGroups().get(0).getGroupName());

        assertEquals(2, controller.getModel().getFeatureGroups().get(0).getActions().size());

        assertEquals(FXUtilities.fromAwtColor(Color.CYAN),
                controller.getModel().getFeatureGroups().get(0).getActions().get(0).getColor());
        assertEquals(CriteriaOptions.VALUE,
                controller.getModel().getFeatureGroups().get(0).getActions().get(0).getOption().get());

        assertEquals(FXUtilities.fromAwtColor(Color.ORANGE),
                controller.getModel().getFeatureGroups().get(0).getActions().get(1).getColor());
        assertEquals(CriteriaOptions.RANGE,
                controller.getModel().getFeatureGroups().get(0).getActions().get(1).getOption().get());

        support.verifyAll();
    }

    /**
     * Tests just reading an existing action.
     */
    @Test
    public void testReadMultiplegroups()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(createAction1());
        FeatureAction action2 = createAction2();
        action2.setGroupName("another");
        actions.getActions().add(action2);

        prefs.putJAXBObject(ourLayerId, actions, false, this);

        FeatureActionsRegistry registry = new FeatureActionsRegistry(prefsRegistry);

        FeatureActionEditController controller = new FeatureActionEditController(registry, ourLayerId);

        assertEquals(ourGroupName, controller.getModel().getFeatureGroups().get(0).getGroupName());
        assertEquals("another", controller.getModel().getFeatureGroups().get(1).getGroupName());

        assertEquals(2, controller.getModel().getFeatureGroups().size());

        assertEquals(FXUtilities.fromAwtColor(Color.ORANGE),
                controller.getModel().getFeatureGroups().get(1).getActions().get(0).getColor());
        assertEquals(CriteriaOptions.RANGE,
                controller.getModel().getFeatureGroups().get(1).getActions().get(0).getOption().get());

        assertEquals(FXUtilities.fromAwtColor(Color.CYAN),
                controller.getModel().getFeatureGroups().get(0).getActions().get(0).getColor());
        assertEquals(CriteriaOptions.VALUE,
                controller.getModel().getFeatureGroups().get(0).getActions().get(0).getOption().get());

        support.verifyAll();
    }

    /**
     * Tests just reading an existing action.
     */
    @Test
    public void testReadWithInvisible()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(createAction1());
        actions.getActions().add(createAction2());
        FeatureAction invisible = createAction2();
        invisible.setVisible(false);
        actions.getActions().add(invisible);

        prefs.putJAXBObject(ourLayerId, actions, false, this);

        FeatureActionsRegistry registry = new FeatureActionsRegistry(prefsRegistry);

        FeatureActionEditController controller = new FeatureActionEditController(registry, ourLayerId);

        assertEquals(ourGroupName, controller.getModel().getFeatureGroups().get(0).getGroupName());

        assertEquals(2, controller.getModel().getFeatureGroups().get(0).getActions().size());

        assertEquals(FXUtilities.fromAwtColor(Color.CYAN),
                controller.getModel().getFeatureGroups().get(0).getActions().get(0).getColor());
        assertEquals(CriteriaOptions.VALUE,
                controller.getModel().getFeatureGroups().get(0).getActions().get(0).getOption().get());

        assertEquals(FXUtilities.fromAwtColor(Color.ORANGE),
                controller.getModel().getFeatureGroups().get(0).getActions().get(1).getColor());
        assertEquals(CriteriaOptions.RANGE,
                controller.getModel().getFeatureGroups().get(0).getActions().get(1).getOption().get());

        support.verifyAll();
    }

    /**
     * Tests updating an existing action.
     */
    @Test
    public void testUpdate()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActions actions = new FeatureActions();
        actions.getActions().add(createAction1());
        actions.getActions().add(createAction2());

        prefs.putJAXBObject(ourLayerId, actions, false, this);

        FeatureActionsRegistry registry = new FeatureActionsRegistry(prefsRegistry);

        FeatureActionEditController controller = new FeatureActionEditController(registry, ourLayerId);

        controller.getModel().getFeatureGroups().get(0).getActions().get(0).setColor(FXUtilities.fromAwtColor(Color.RED));
        controller.applyChanges();

        assertEquals(Color.RED, ((StyleAction)actions.getActions().get(0).getActions().get(0)).getStyleOptions().getColor());
        assertEquals(Conditional.LIKE,
                actions.getActions().get(0).getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());

        assertEquals(Color.ORANGE, ((StyleAction)actions.getActions().get(1).getActions().get(0)).getStyleOptions().getColor());
        assertEquals(Conditional.GTE,
                actions.getActions().get(1).getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals(Conditional.LT,
                actions.getActions().get(1).getFilter().getFilterGroup().getCriteria().get(1).getComparisonOperator());

        support.verifyAll();
    }

    /**
     * Creates a test feature action.
     *
     * @return The test feature action.
     */
    private FeatureAction createAction1()
    {
        FeatureAction action = new FeatureAction();
        action.setName(ourActionName);
        action.setEnabled(true);
        action.setGroupName(ourGroupName);
        StyleAction styleAction = new StyleAction();
        styleAction.getStyleOptions().setColor(Color.CYAN);
        styleAction.getStyleOptions().setIconId(22);
        action.getActions().add(styleAction);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName(ourGroupName);
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        group.getActions().add(simpleAction);

        Filter filter = new Filter();
        Group filterGroup = new Group();
        filterGroup.getCriteria().add(new Criteria(ourColumns.get(2), Conditional.LIKE, "B*"));
        filter.setFilterGroup(filterGroup);
        action.setFilter(filter);

        return action;
    }

    /**
     * Creates another test feature action.
     *
     * @return The test feature action.
     */
    private FeatureAction createAction2()
    {
        FeatureAction action2 = new FeatureAction();
        action2.setName(ourActionName);
        action2.setEnabled(true);
        action2.setGroupName(ourGroupName);
        StyleAction styleAction2 = new StyleAction();
        styleAction2.getStyleOptions().setColor(Color.ORANGE);
        styleAction2.getStyleOptions().setIconId(10);
        action2.getActions().add(styleAction2);

        SimpleFeatureActionGroup group2 = new SimpleFeatureActionGroup();
        group2.setGroupName(ourGroupName);
        SimpleFeatureAction simpleAction2 = new SimpleFeatureAction(action2);
        group2.getActions().add(simpleAction2);

        Filter filter2 = new Filter();
        Group filterGroup2 = new Group();
        filterGroup2.getCriteria().add(new Criteria(ourColumns.get(2), Conditional.GTE, "B"));
        filterGroup2.getCriteria().add(new Criteria(ourColumns.get(2), Conditional.LT, "G"));
        filter2.setFilterGroup(filterGroup2);
        action2.setFilter(filter2);

        return action2;
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
}
