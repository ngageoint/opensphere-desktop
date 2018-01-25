package io.opensphere.featureactions.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesImpl;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.FeatureActions;
import io.opensphere.featureactions.model.LabelAction;
import io.opensphere.featureactions.model.StyleAction;

/**
 * Unit test for {@link FeatureActionsRegistry}.
 */
public class FeatureActionRegistryTest
{
    /**
     * The test layer name.
     */
    private static final String ourLayerName = "layer1";

    /**
     * The set label string.
     */
    private static final String ourSetLabel = "Set Label";

    /**
     * The set style string.
     */
    private static final String ourSetStyle = "Set Style";

    /**
     * Tests adding feature actions.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testAdd() throws JAXBException
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry actionRegistry = new FeatureActionsRegistry(prefsRegistry);

        FeatureAction action1 = new FeatureAction();
        action1.getActions().add(new StyleAction());

        FeatureAction action2 = new FeatureAction();
        LabelAction labelAction = new LabelAction();
        ColumnLabel label = new ColumnLabel();
        label.setColumn("column1");
        labelAction.getLabelOptions().getColumnLabels().getColumnsInLabel().add(label);
        labelAction.getLabelOptions().setSize(10);
        action2.getActions().add(labelAction);

        actionRegistry.add(ourLayerName, New.list(action1, action2), this);

        List<FeatureAction> actions = actionRegistry.get(ourLayerName);
        assertEquals(action1, actions.get(0));
        assertEquals(action2, actions.get(1));

        PreferencesImpl saved = XMLUtilities.jaxbClone(prefs, PreferencesImpl.class);

        List<FeatureAction> savedActions = saved.getJAXBObject(FeatureActions.class, ourLayerName, null).getActions();

        assertEquals(ourSetStyle, savedActions.get(0).getActions().get(0).toString());
        assertEquals(ourSetLabel, savedActions.get(1).getActions().get(0).toString());
        assertEquals(10, ((LabelAction)savedActions.get(1).getActions().get(0)).getLabelOptions().getSize());
        assertEquals("column1", ((LabelAction)savedActions.get(1).getActions().get(0)).getLabelOptions().getColumnLabels()
                .getColumnsInLabel().get(0).getColumn());

        FeatureAction action3 = new FeatureAction();
        action3.getActions().add(new StyleAction());
        action3.getActions().add(new LabelAction());

        actionRegistry.add(ourLayerName, New.list(action3), this);

        actions = actionRegistry.get("layer1");
        assertEquals(action1, actions.get(0));
        assertEquals(action2, actions.get(1));
        assertEquals(action3, actions.get(2));

        saved = XMLUtilities.jaxbClone(prefs, PreferencesImpl.class);

        savedActions = saved.getJAXBObject(FeatureActions.class, ourLayerName, null).getActions();

        assertEquals(ourSetStyle, savedActions.get(0).getActions().get(0).toString());
        assertEquals(ourSetLabel, savedActions.get(1).getActions().get(0).toString());
        assertEquals(ourSetStyle, savedActions.get(2).getActions().get(0).toString());
        assertEquals(ourSetLabel, savedActions.get(2).getActions().get(1).toString());

        support.verifyAll();
    }

    /**
     * Tests getting any saved feature actions.
     */
    @Test
    public void testGet()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry actionRegistry = new FeatureActionsRegistry(prefsRegistry);

        FeatureAction action1 = new FeatureAction();
        FeatureActions featureActions = new FeatureActions();
        featureActions.getActions().add(action1);

        prefs.putJAXBObject(ourLayerName, featureActions, false, this);

        List<FeatureAction> actions = actionRegistry.get(ourLayerName);
        assertEquals(action1, actions.get(0));

        support.verifyAll();
    }

    /**
     * Tests removing feature actions.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testRemove() throws JAXBException
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry actionRegistry = new FeatureActionsRegistry(prefsRegistry);

        FeatureAction action1 = new FeatureAction();
        action1.getActions().add(new StyleAction());

        FeatureAction action2 = new FeatureAction();
        action2.getActions().add(new LabelAction());

        actionRegistry.add(ourLayerName, New.list(action1, action2), this);

        List<FeatureAction> actions = actionRegistry.get(ourLayerName);
        assertEquals(action1, actions.get(0));
        assertEquals(action2, actions.get(1));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(prefs, output);

        PreferencesImpl saved = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), PreferencesImpl.class);

        List<FeatureAction> savedActions = saved.getJAXBObject(FeatureActions.class, ourLayerName, null).getActions();

        assertEquals(ourSetStyle, savedActions.get(0).getActions().get(0).toString());
        assertEquals(ourSetLabel, savedActions.get(1).getActions().get(0).toString());

        actionRegistry.remove(ourLayerName, New.list(action2), this);

        actions = actionRegistry.get("layer1");
        assertEquals(1, actions.size());
        assertEquals(action1, actions.get(0));

        output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(prefs, output);

        saved = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), PreferencesImpl.class);

        savedActions = saved.getJAXBObject(FeatureActions.class, ourLayerName, null).getActions();

        assertEquals(1, savedActions.size());
        assertEquals(ourSetStyle, savedActions.get(0).getActions().get(0).toString());

        actionRegistry.remove(ourLayerName, New.list(action1), this);

        assertNull(prefs.getJAXBObject(FeatureActions.class, ourLayerName, null));

        support.verifyAll();
    }

    /**
     * Tests adding feature actions.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testUpdate() throws JAXBException
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesImpl prefs = new PreferencesImpl(FeatureActionsRegistry.class.getName());
        PreferencesRegistry prefsRegistry = createPrefsRegistry(support, prefs);

        support.replayAll();

        FeatureActionsRegistry actionRegistry = new FeatureActionsRegistry(prefsRegistry);

        FeatureAction action1 = new FeatureAction();
        action1.getActions().add(new StyleAction());

        FeatureAction action2 = new FeatureAction();
        LabelAction labelAction = new LabelAction();
        ColumnLabel label = new ColumnLabel();
        label.setColumn("column1");
        labelAction.getLabelOptions().getColumnLabels().getColumnsInLabel().add(label);
        labelAction.getLabelOptions().setSize(10);
        action2.getActions().add(labelAction);

        actionRegistry.add(ourLayerName, New.list(action1, action2), this);

        List<FeatureAction> actions = actionRegistry.get(ourLayerName);
        assertEquals(action1, actions.get(0));
        assertEquals(action2, actions.get(1));

        action2.setName("new name");

        actionRegistry.update(ourLayerName, New.list(action2), this);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(prefs, output);

        PreferencesImpl saved = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), PreferencesImpl.class);

        List<FeatureAction> savedActions = saved.getJAXBObject(FeatureActions.class, ourLayerName, null).getActions();

        assertEquals("new name", savedActions.get(1).getName());
        assertEquals(ourSetStyle, savedActions.get(0).getActions().get(0).toString());
        assertEquals(ourSetLabel, savedActions.get(1).getActions().get(0).toString());
        assertEquals(10, ((LabelAction)savedActions.get(1).getActions().get(0)).getLabelOptions().getSize());
        assertEquals("column1", ((LabelAction)savedActions.get(1).getActions().get(0)).getLabelOptions().getColumnLabels()
                .getColumnsInLabel().get(0).getColumn());

        FeatureAction action3 = new FeatureAction();
        action3.getActions().add(new StyleAction());
        action3.getActions().add(new LabelAction());

        actionRegistry.add(ourLayerName, New.list(action3), this);

        actions = actionRegistry.get("layer1");
        assertEquals(action1, actions.get(0));
        assertEquals(action2, actions.get(1));
        assertEquals(action3, actions.get(2));

        output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(prefs, output);

        saved = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), PreferencesImpl.class);

        savedActions = saved.getJAXBObject(FeatureActions.class, ourLayerName, null).getActions();

        assertEquals(ourSetStyle, savedActions.get(0).getActions().get(0).toString());
        assertEquals(ourSetLabel, savedActions.get(1).getActions().get(0).toString());
        assertEquals(ourSetStyle, savedActions.get(2).getActions().get(0).toString());
        assertEquals(ourSetLabel, savedActions.get(2).getActions().get(1).toString());

        support.verifyAll();
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
