package io.opensphere.featureactions.editor.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import javafx.application.Platform;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;

/**
 * Unit test for {@link FeatureActionTitledPaneBinder}.
 */
public class FeatureActionTitledPaneBinderTestDisplay
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
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        FeatureActionTitledPane editor = createEditor(support);
        Accordion accordion = new Accordion(editor);

        support.replayAll();

        FeatureActionTitledPaneBinder binder = new FeatureActionTitledPaneBinder(accordion, editor, actions, group, null);

        FXUtilities.runOnFXThreadAndWait(() ->
        {
            editor.getAddButton().fire();
        });

        assertEquals(1, group.getActions().size());
        assertEquals(editor, accordion.getExpandedPane());

        binder.close();

        editor.getAddButton().fire();

        assertEquals(1, group.getActions().size());

        support.verifyAll();
    }

    /**
     * Tests removing the group.
     */
    @Test
    public void testRemoveGroup()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        FeatureActionTitledPane editor = createEditor(support);
        Accordion accordion = new Accordion(editor);

        support.replayAll();

        FeatureActionTitledPaneBinder binder = new FeatureActionTitledPaneBinder(accordion, editor, actions, group, null);

        editor.getRemoveButton().fire();

        assertEquals(0, actions.getFeatureGroups().size());

        binder.close();

        support.verifyAll();
    }

    /**
     * Tests when all actions are enabled.
     */
    @Test
    public void testAllChecked()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

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

        FeatureActionTitledPane editor = createEditor(support);
        Accordion accordion = new Accordion(editor);

        support.replayAll();

        FeatureActionTitledPaneBinder binder = new FeatureActionTitledPaneBinder(accordion, editor, actions, group, null);

        assertTrue(editor.getSelectDeselectAll().selectedProperty().get());

        binder.close();

        editor.getSelectDeselectAll().selectedProperty().set(false);
        assertTrue(action1.isEnabled());
        assertTrue(action2.isEnabled());
        assertTrue(action3.isEnabled());

        support.verifyAll();
    }

    /**
     * Tests renaming the group name.
     */
    @Test
    public void testGroupRename()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();
        group.setGroupName(ourGroupName);

        SimpleFeatureActions actions = new SimpleFeatureActions(ourLayerId);
        actions.getFeatureGroups().add(group);

        FeatureActionTitledPane editor = createEditor(support);
        Accordion accordion = new Accordion(editor);

        support.replayAll();

        FeatureActionTitledPaneBinder binder = new FeatureActionTitledPaneBinder(accordion, editor, actions, group, null);

        assertEquals(ourGroupName, editor.getTitle().getText());

        editor.getTitle().setText("New name");

        assertEquals("New name", group.getGroupName());

        binder.close();

        editor.getTitle().setText("New name again");

        assertEquals("New name", group.getGroupName());

        support.verifyAll();
    }

    /**
     * Tests when no actions are enabled.
     */
    @Test
    public void testNoneChecked()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

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

        FeatureActionTitledPane editor = createEditor(support);
        Accordion accordion = new Accordion(editor);

        support.replayAll();

        FeatureActionTitledPaneBinder binder = new FeatureActionTitledPaneBinder(accordion, editor, actions, group, null);

        assertFalse(editor.getSelectDeselectAll().selectedProperty().get());

        binder.close();

        editor.getSelectDeselectAll().selectedProperty().set(true);
        assertFalse(action1.isEnabled());
        assertFalse(action2.isEnabled());
        assertFalse(action3.isEnabled());

        support.verifyAll();
    }

    /**
     * Tests selecting all and deselecting all.
     */
    @Test
    public void testSelectAll()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

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

        FeatureActionTitledPane editor = createEditor(support);
        Accordion accordion = new Accordion(editor);

        support.replayAll();

        FeatureActionTitledPaneBinder binder = new FeatureActionTitledPaneBinder(accordion, editor, actions, group, null);

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

        binder.close();

        editor.getSelectDeselectAll().selectedProperty().set(true);
        assertFalse(action1.isEnabled());
        assertFalse(action2.isEnabled());
        assertFalse(action3.isEnabled());

        support.verifyAll();
    }

    /**
     * Tests when some actions are enabled.
     */
    @Test
    public void testSomeChecked()
    {
        Platform.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

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

        FeatureActionTitledPane editor = createEditor(support);
        Accordion accordion = new Accordion(editor);

        support.replayAll();

        FeatureActionTitledPaneBinder binder = new FeatureActionTitledPaneBinder(accordion, editor, actions, group, null);

        assertFalse(editor.getSelectDeselectAll().selectedProperty().get());

        binder.close();

        editor.getSelectDeselectAll().selectedProperty().set(false);
        assertTrue(action1.isEnabled());
        assertTrue(action2.isEnabled());
        assertFalse(action3.isEnabled());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link SimpleFeatureActionEditor}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link SimpleFeatureActionEditor}.
     */
    private FeatureActionTitledPane createEditor(EasyMockSupport support)
    {
        FeatureActionTitledPane editor = support.createMock(FeatureActionTitledPane.class);

        EasyMock.expect(editor.getSelectDeselectAll()).andReturn(new CheckBox()).atLeastOnce();
        EasyMock.expect(editor.getAddButton()).andReturn(new Button()).atLeastOnce();
        EasyMock.expect(editor.getTitle()).andReturn(new TextField()).atLeastOnce();
        EasyMock.expect(editor.getRemoveButton()).andReturn(new Button()).atLeastOnce();

        return editor;
    }
}
