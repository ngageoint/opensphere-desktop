package io.opensphere.featureactions.editor.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javafx.scene.paint.Color;

import org.junit.Test;

import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;

/**
 * Unit test for {@link FeatureActionsDefaulter}.
 */
public class FeatureActionsDefaulterTest
{
    /**
     * The test column.
     */
    private static final String ourColumn = "Column 1";

    /**
     * The test layer id.
     */
    private static final String ourLayerId = "id of layer";

    /**
     * Tests creating a new feature group and action.
     */
    @Test
    public void testNew()
    {
        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);

        FeatureActionsDefaulter defaulter = new FeatureActionsDefaulter(actions);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        actions.getFeatureGroups().add(group);

        SimpleFeatureAction simple = new SimpleFeatureAction(new FeatureAction());
        group.getActions().add(simple);

        assertEquals("Action 1", simple.getFeatureAction().getName());
        assertTrue(simple.getFeatureAction().isEnabled());
        assertEquals("", simple.getColumn().get());
        assertEquals(CriteriaOptions.VALUE, simple.getOption().get());
        assertNull(simple.getValue().get());

        defaulter.close();
    }

    /**
     * Tests creating a new feature group and action.
     */
    @Test
    public void testNewRange()
    {
        FeatureAction act = new FeatureAction();
        act.getActions().add(new StyleAction());
        SimpleFeatureAction existing = new SimpleFeatureAction(act);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(existing);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        SimpleFeatureActionsAdapter adapter = new SimpleFeatureActionsAdapter(actions);

        existing.getFeatureAction().setName("Action 1");
        existing.getColumn().set(ourColumn);
        existing.getOption().set(CriteriaOptions.RANGE);
        existing.getMinimumValue().set("0.0");
        existing.getMaximumValue().set("5.5");
        existing.setColor(Color.BLUE);

        FeatureActionsDefaulter defaulter = new FeatureActionsDefaulter(actions);

        SimpleFeatureAction simple = new SimpleFeatureAction(new FeatureAction());
        group.getActions().add(simple);

        assertEquals("Action 2", simple.getFeatureAction().getName());
        assertTrue(simple.getFeatureAction().isEnabled());
        assertEquals(ourColumn, simple.getColumn().get());
        assertEquals(CriteriaOptions.RANGE, simple.getOption().get());
        assertEquals("5.5", simple.getMinimumValue().get());
        assertEquals("11.0", simple.getMaximumValue().get());
        assertEquals(Color.BLUE.deriveColor(20, 1, 1, 1).brighter().brighter(), simple.getColor());

        adapter.close();
        defaulter.close();
    }

    /**
     * Tests creating a new feature group and action.
     */
    @Test
    public void testNewRangeAlpha()
    {
        FeatureAction act = new FeatureAction();
        act.getActions().add(new StyleAction());
        SimpleFeatureAction existing = new SimpleFeatureAction(act);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(existing);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        SimpleFeatureActionsAdapter adapter = new SimpleFeatureActionsAdapter(actions);

        existing.getFeatureAction().setName("Action 1");
        existing.getColumn().set(ourColumn);
        existing.getOption().set(CriteriaOptions.RANGE);
        existing.getMinimumValue().set("A");
        existing.getMaximumValue().set("C");
        existing.setColor(Color.BLUE);

        FeatureActionsDefaulter defaulter = new FeatureActionsDefaulter(actions);

        SimpleFeatureAction simple = new SimpleFeatureAction(new FeatureAction());
        group.getActions().add(simple);

        assertEquals("Action 2", simple.getFeatureAction().getName());
        assertTrue(simple.getFeatureAction().isEnabled());
        assertEquals(ourColumn, simple.getColumn().get());
        assertEquals(CriteriaOptions.RANGE, simple.getOption().get());
        assertNull(simple.getMinimumValue().get());
        assertNull(simple.getMaximumValue().get());
        assertEquals(Color.BLUE.deriveColor(20, 1, 1, 1).brighter().brighter(), simple.getColor());

        adapter.close();
        defaulter.close();
    }
    /**
     * Tests creating a new feature group and action.
     */
    @Test
    public void testNewRangeNull()
    {
        FeatureAction act = new FeatureAction();
        act.getActions().add(new StyleAction());
        SimpleFeatureAction existing = new SimpleFeatureAction(act);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(existing);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        SimpleFeatureActionsAdapter adapter = new SimpleFeatureActionsAdapter(actions);

        existing.getFeatureAction().setName("Action 1");
        existing.getColumn().set(ourColumn);
        existing.getOption().set(CriteriaOptions.RANGE);
        existing.getMinimumValue().set(null);
        existing.getMaximumValue().set(null);
        existing.setColor(Color.BLUE);

        FeatureActionsDefaulter defaulter = new FeatureActionsDefaulter(actions);

        SimpleFeatureAction simple = new SimpleFeatureAction(new FeatureAction());
        group.getActions().add(simple);

        assertEquals("Action 2", simple.getFeatureAction().getName());
        assertTrue(simple.getFeatureAction().isEnabled());
        assertEquals(ourColumn, simple.getColumn().get());
        assertEquals(CriteriaOptions.RANGE, simple.getOption().get());
        assertNull(simple.getMinimumValue().get());
        assertNull(simple.getMaximumValue().get());
        assertEquals(Color.BLUE.deriveColor(20, 1, 1, 1).brighter().brighter(), simple.getColor());

        adapter.close();
        defaulter.close();
    }

    /**
     * Tests creating a new feature group and action.
     */
    @Test
    public void testNewValue()
    {
        FeatureAction act = new FeatureAction();
        act.getActions().add(new StyleAction());
        SimpleFeatureAction existing = new SimpleFeatureAction(act);

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.getActions().add(existing);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        SimpleFeatureActionsAdapter adapter = new SimpleFeatureActionsAdapter(actions);

        existing.getFeatureAction().setName("Action 1");
        existing.getColumn().set(ourColumn);
        existing.getOption().set(CriteriaOptions.VALUE);
        existing.getValue().set("A*");
        existing.setColor(Color.RED);

        FeatureActionsDefaulter defaulter = new FeatureActionsDefaulter(actions);

        SimpleFeatureAction simple = new SimpleFeatureAction(new FeatureAction());
        group.getActions().add(simple);

        assertEquals("Action 2", simple.getFeatureAction().getName());
        assertTrue(simple.getFeatureAction().isEnabled());
        assertEquals(ourColumn, simple.getColumn().get());
        assertEquals(CriteriaOptions.VALUE, simple.getOption().get());
        assertNull(simple.getValue().get());
        assertEquals(Color.RED.deriveColor(20, 1, 1, 1), simple.getColor());

        adapter.close();
        defaulter.close();
    }
}
