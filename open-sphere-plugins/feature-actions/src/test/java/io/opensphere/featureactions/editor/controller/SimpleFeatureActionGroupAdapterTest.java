package io.opensphere.featureactions.editor.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javafx.scene.paint.Color;

import org.junit.Test;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;

/**
 * Unit test for {@link SimpleFeatureActionGroupAdapter}.
 */
public class SimpleFeatureActionGroupAdapterTest
{
    /**
     * The test group name.
     */
    private static final String ourGroupName = "I am group";

    /**
     * The test value.
     */
    private static final String ourTestValue = "any*";

    /**
     * Tests adding feature actions.
     */
    @Test
    public void testAdd()
    {
        FeatureAction action = new FeatureAction();
        action.getActions().add(new StyleAction());
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName(ourGroupName);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);
        group.getActions().add(simpleAction);

        assertEquals(ourGroupName, action.getGroupName());

        simpleAction.setColor(Color.BLUE);
        simpleAction.getColumn().set("column1");
        simpleAction.getOption().set(CriteriaOptions.VALUE);
        simpleAction.getValue().set(ourTestValue);

        assertEquals(java.awt.Color.BLUE, ((StyleAction)action.getActions().get(0)).getStyleOptions().getColor());
        assertEquals("column1", action.getFilter().getFilterGroup().getCriteria().get(0).getField());
        assertEquals(Conditional.LIKE, action.getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals(ourTestValue, action.getFilter().getFilterGroup().getCriteria().get(0).getValue());

        adapter.close();
    }

    /**
     * Tests removing feature actions.
     */
    @Test
    public void testClose()
    {
        FeatureAction action = new FeatureAction();
        action.setGroupName(ourGroupName);
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName(ourGroupName);
        group.getActions().add(simpleAction);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        adapter.close();

        group.setGroupName("new name");
        assertEquals(ourGroupName, action.getGroupName());

        simpleAction.setColor(Color.BLUE);
        simpleAction.getColumn().set("column1");
        simpleAction.getOption().set(CriteriaOptions.VALUE);
        simpleAction.getValue().set(ourTestValue);

        assertTrue(action.getActions().isEmpty());
        assertNull(action.getFilter());
    }

    /**
     * Tests removing feature actions.
     */
    @Test
    public void testRemove()
    {
        FeatureAction action = new FeatureAction();
        action.getActions().add(new StyleAction());
        action.setGroupName(ourGroupName);
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName(ourGroupName);
        group.getActions().add(simpleAction);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        simpleAction.setColor(Color.BLUE);
        simpleAction.getColumn().set("column1");
        simpleAction.getOption().set(CriteriaOptions.VALUE);
        simpleAction.getValue().set(ourTestValue);

        assertEquals(java.awt.Color.BLUE, ((StyleAction)action.getActions().get(0)).getStyleOptions().getColor());
        assertEquals("column1", action.getFilter().getFilterGroup().getCriteria().get(0).getField());
        assertEquals(Conditional.LIKE, action.getFilter().getFilterGroup().getCriteria().get(0).getComparisonOperator());
        assertEquals(ourTestValue, action.getFilter().getFilterGroup().getCriteria().get(0).getValue());

        group.getActions().remove(simpleAction);

        assertNull(action.getGroupName());

        simpleAction.setColor(Color.RED);
        simpleAction.getValue().set("anyofus");

        assertEquals(java.awt.Color.BLUE, ((StyleAction)action.getActions().get(0)).getStyleOptions().getColor());
        assertEquals(ourTestValue, action.getFilter().getFilterGroup().getCriteria().get(0).getValue());

        adapter.close();
    }

    /**
     * Tests renaming group names.
     */
    @Test
    public void testRename()
    {
        FeatureAction action = new FeatureAction();
        action.setGroupName(ourGroupName);
        SimpleFeatureAction simpleAction = new SimpleFeatureAction(action);
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName(ourGroupName);
        group.getActions().add(simpleAction);

        SimpleFeatureActionGroupAdapter adapter = new SimpleFeatureActionGroupAdapter(group);

        group.setGroupName("new name");
        assertEquals("new name", action.getGroupName());

        adapter.close();
    }
}
