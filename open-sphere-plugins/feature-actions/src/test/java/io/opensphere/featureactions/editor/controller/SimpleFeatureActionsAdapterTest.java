package io.opensphere.featureactions.editor.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;

/**
 * Unit test for {@link SimpleFeatureActionsAdapter}.
 */
public class SimpleFeatureActionsAdapterTest
{
    /**
     * The id of the layer.
     */
    private static final String ourLayerId = "layerId";

    /**
     * The test new name.
     */
    private static final String ourNewGroupName = "new name";

    /**
     * Tests adding new groups.
     */
    @Test
    public void testAdd()
    {
        FeatureAction action = new FeatureAction();
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(simpleAction);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);

        SimpleFeatureActionsAdapter adapter = new SimpleFeatureActionsAdapter(actions);

        actions.getFeatureGroups().add(group);

        group.setGroupName(ourNewGroupName);
        assertEquals(ourNewGroupName, action.getGroupName());

        adapter.close();
    }

    /**
     * Tests closing.
     */
    @Test
    public void testClose()
    {
        FeatureAction action = new FeatureAction();
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(simpleAction);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);

        SimpleFeatureActionsAdapter adapter = new SimpleFeatureActionsAdapter(actions);

        actions.getFeatureGroups().add(group);

        group.setGroupName(ourNewGroupName);
        assertEquals(ourNewGroupName, action.getGroupName());

        adapter.close();

        group.setGroupName("another");
        assertEquals(ourNewGroupName, action.getGroupName());

        actions.getFeatureGroups().remove(group);
        actions.getFeatureGroups().add(group);

        group.setGroupName("yet another");
        assertEquals(ourNewGroupName, action.getGroupName());
    }

    /**
     * Tests everything gets set up for existing group.
     */
    @Test
    public void testExisting()
    {
        FeatureAction action = new FeatureAction();
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(simpleAction);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        SimpleFeatureActionsAdapter adapter = new SimpleFeatureActionsAdapter(actions);

        group.setGroupName(ourNewGroupName);
        assertEquals(ourNewGroupName, action.getGroupName());

        adapter.close();
    }

    /**
     * Tests removing groups.
     */
    @Test
    public void testRemove()
    {
        FeatureAction action = new FeatureAction();
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(simpleAction);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);

        SimpleFeatureActionsAdapter adapter = new SimpleFeatureActionsAdapter(actions);

        actions.getFeatureGroups().add(group);

        group.setGroupName(ourNewGroupName);
        assertEquals(ourNewGroupName, action.getGroupName());

        actions.getFeatureGroups().remove(group);

        group.setGroupName("another");
        assertEquals(ourNewGroupName, action.getGroupName());

        adapter.close();
    }
}
