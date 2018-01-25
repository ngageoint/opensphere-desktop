package io.opensphere.featureactions.editor.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javafx.scene.control.Accordion;

import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;

/**
 * Unit test for {@link FeatureActionTitledPaneBinder}.
 */
public class FeatureActionTitledPaneTestDisplay
{
    /** Test group name. */
    private static final String ourGroupName = "other group";

    /**
     * The id of the layer.
     */
    private static final String ourLayerId = "theLayerId";

    /**
     * Tests adding an action.
     */
    @Test
    public void testAddAction()
    {
        PlatformImpl.startup(() ->
        {
        });

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        FeatureActionTitledPane editor = createEditor(actions, group);

        editor.getAddButton().fire();

        assertEquals(1, group.getActions().size());

        editor.close();

        editor.getAddButton().fire();

        assertEquals(1, group.getActions().size());
    }

    /**
     * Tests when all actions are enabled.
     */
    @Test
    public void testAllChecked()
    {
        PlatformImpl.startup(() ->
        {
        });

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();

        FeatureAction action1 = new FeatureAction();
        action1.setEnabled(true);
        SimpleFeatureAction simple1 = new SimpleFeatureAction(action1);

        FeatureAction action2 = new FeatureAction();
        action2.setEnabled(true);
        SimpleFeatureAction simple2 = new SimpleFeatureAction(action2);

        FeatureAction action3 = new FeatureAction();
        action3.setEnabled(true);
        SimpleFeatureAction simple3 = new SimpleFeatureAction(action3);

        group.getActions().addAll(New.list(simple1, simple2, simple3));

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        FeatureActionTitledPane editor = createEditor(actions, group);

        assertTrue(editor.getSelectDeselectAll().selectedProperty().get());

        editor.close();

        editor.getSelectDeselectAll().selectedProperty().set(false);
        assertTrue(action1.isEnabled());
        assertTrue(action2.isEnabled());
        assertTrue(action3.isEnabled());
    }

    /**
     * Tests renaming the group name.
     */
    @Test
    public void testGroupRename()
    {
        PlatformImpl.startup(() ->
        {
        });

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName(ourGroupName);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        FeatureActionTitledPane editor = createEditor(actions, group);

        assertEquals(ourGroupName, editor.getTitle().getText());

        editor.getTitle().setText("New name");

        assertEquals("New name", group.getGroupName());

        editor.close();

        editor.getTitle().setText("New name again");

        assertEquals("New name", group.getGroupName());
    }

    /**
     * Tests when no actions are enabled.
     */
    @Test
    public void testNoneChecked()
    {
        PlatformImpl.startup(() ->
        {
        });

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();

        FeatureAction action1 = new FeatureAction();
        action1.setEnabled(false);
        SimpleFeatureAction simple1 = new SimpleFeatureAction(action1);

        FeatureAction action2 = new FeatureAction();
        action2.setEnabled(false);
        SimpleFeatureAction simple2 = new SimpleFeatureAction(action2);

        FeatureAction action3 = new FeatureAction();
        action3.setEnabled(false);
        SimpleFeatureAction simple3 = new SimpleFeatureAction(action3);

        group.getActions().addAll(New.list(simple1, simple2, simple3));

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        FeatureActionTitledPane editor = createEditor(actions, group);

        assertFalse(editor.getSelectDeselectAll().selectedProperty().get());

        editor.close();

        editor.getSelectDeselectAll().selectedProperty().set(true);
        assertFalse(action1.isEnabled());
        assertFalse(action2.isEnabled());
        assertFalse(action3.isEnabled());
    }

    /**
     * Tests selecting all and deselecting all.
     */
    @Test
    public void testSelectAll()
    {
        PlatformImpl.startup(() ->
        {
        });

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName(ourGroupName);

        FeatureAction action1 = new FeatureAction();
        action1.setEnabled(false);
        SimpleFeatureAction simple1 = new SimpleFeatureAction(action1);

        FeatureAction action2 = new FeatureAction();
        action2.setEnabled(false);
        SimpleFeatureAction simple2 = new SimpleFeatureAction(action2);

        FeatureAction action3 = new FeatureAction();
        action3.setEnabled(false);
        SimpleFeatureAction simple3 = new SimpleFeatureAction(action3);

        group.getActions().addAll(New.list(simple1, simple2, simple3));

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        FeatureActionTitledPane editor = createEditor(actions, group);

        assertFalse(editor.getSelectDeselectAll().selectedProperty().get());
        assertEquals("Click to enable all actions in " + ourGroupName, editor.getSelectDeselectAll().getTooltip().getText());

        editor.getSelectDeselectAll().selectedProperty().set(true);

        assertEquals("Click to disable all actions in " + ourGroupName, editor.getSelectDeselectAll().getTooltip().getText());
        assertTrue(action1.isEnabled());
        assertTrue(action2.isEnabled());
        assertTrue(action3.isEnabled());

        editor.getSelectDeselectAll().selectedProperty().set(false);
        assertEquals("Click to enable all actions in " + ourGroupName, editor.getSelectDeselectAll().getTooltip().getText());
        assertFalse(action1.isEnabled());
        assertFalse(action2.isEnabled());
        assertFalse(action3.isEnabled());

        editor.close();

        editor.getSelectDeselectAll().selectedProperty().set(true);
        assertFalse(action1.isEnabled());
        assertFalse(action2.isEnabled());
        assertFalse(action3.isEnabled());
    }

    /**
     * Tests when some actions are enabled.
     */
    @Test
    public void testSomeChecked()
    {
        PlatformImpl.startup(() ->
        {
        });

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();

        FeatureAction action1 = new FeatureAction();
        action1.setEnabled(true);
        SimpleFeatureAction simple1 = new SimpleFeatureAction(action1);

        FeatureAction action2 = new FeatureAction();
        action2.setEnabled(true);
        SimpleFeatureAction simple2 = new SimpleFeatureAction(action2);

        FeatureAction action3 = new FeatureAction();
        action3.setEnabled(false);
        SimpleFeatureAction simple3 = new SimpleFeatureAction(action3);

        group.getActions().addAll(New.list(simple1, simple2, simple3));

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        FeatureActionTitledPane editor = createEditor(actions, group);

        assertFalse(editor.getSelectDeselectAll().selectedProperty().get());

        editor.close();

        editor.getSelectDeselectAll().selectedProperty().set(false);
        assertTrue(action1.isEnabled());
        assertTrue(action2.isEnabled());
        assertFalse(action3.isEnabled());
    }

    /**
     * Creates an easy mocked {@link SimpleFeatureActionEditor}.
     *
     * @param mainModel The main model for simple feature actions.
     * @param group The group to be editing.
     * @return The mocked {@link SimpleFeatureActionEditor}.
     */
    private FeatureActionTitledPane createEditor(SimpleFeatureActions mainModel, SimpleFeatureActionGroup group)
    {
        Accordion accordion = new Accordion();
        FeatureActionTitledPane editor = new FeatureActionTitledPane(accordion, mainModel, group, null, null);

        return editor;
    }
}
