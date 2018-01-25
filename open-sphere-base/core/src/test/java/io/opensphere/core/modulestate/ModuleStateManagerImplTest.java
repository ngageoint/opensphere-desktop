package io.opensphere.core.modulestate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.modulestate.config.v1.ModuleStateData;
import io.opensphere.core.modulestate.config.v1.ModuleStateManagerState;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.xml.MutableNamespaceContext;

/**
 * Tests for {@link ModuleStateManagerImpl}.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ModuleStateManagerImplTest
{
    /** Description for testing. */
    private static final String DESCRIPTION = "This is a test description.";

    /** Module name for testing. */
    private static final String ONE = "one";

    /** State id for testing. */
    private static final String STATEID = "test";

    /** Tags for testing. */
    private static final Collection<? extends String> TAGS = Arrays.asList("tag1", "tag2");

    /** Module name for testing. */
    private static final String THREE = "three";

    /** Module name for testing. */
    private static final String TWO = "two";

    /**
     * Test for {@link ModuleStateManagerImpl#deactivateAllStates()} .
     *
     * @throws InterruptedException Interrupted
     */
    @Test
    public void testDeactivateAllStates() throws InterruptedException
    {
        ModuleStateManager manager = new ModuleStateManagerImpl(null);
        ModuleStateController one = EasyMock.createNiceMock(ModuleStateController.class);
        ModuleStateController two = EasyMock.createNiceMock(ModuleStateController.class);
        ModuleStateController three1 = EasyMock.createNiceMock(ModuleStateController.class);
        ModuleStateController three2 = EasyMock.createNiceMock(ModuleStateController.class);

        Element node = EasyMock.createMock(Element.class);
        one.activateState(EasyMock.eq(STATEID), EasyMock.eq(DESCRIPTION), EasyMockHelper.eq(TAGS), EasyMock.eq(node));
        three1.activateState(EasyMock.eq(STATEID), EasyMock.eq(DESCRIPTION), EasyMockHelper.eq(TAGS), EasyMock.eq(node));
        three2.activateState(EasyMock.eq(STATEID), EasyMock.eq(DESCRIPTION), EasyMockHelper.eq(TAGS), EasyMock.eq(node));

        EasyMock.replay(one, two, three1, three2, node);

        manager.registerModuleStateController(ONE, one);
        manager.registerModuleStateController(TWO, two);
        manager.registerModuleStateController(THREE, three1);
        manager.registerModuleStateController(THREE, three2);

        manager.registerState(STATEID, DESCRIPTION, TAGS, Arrays.asList(ONE, THREE), node);

        manager.toggleState(STATEID);

        EasyMock.verify(one, two, three1, three2, node);

        Assert.assertTrue(manager.isStateActive(STATEID));
        Assert.assertTrue(manager.getActiveStateIds().size() == 1 && manager.getActiveStateIds().contains(STATEID));

        EasyMock.reset(one, two, three1, three2);

        one.deactivateState(STATEID, node);
        three1.deactivateState(STATEID, node);
        three2.deactivateState(STATEID, node);

        EasyMock.replay(one, two, three1, three2);

        manager.deactivateAllStates();

        EasyMock.verify(one, two, three1, three2, node);

        Assert.assertFalse(manager.isStateActive(STATEID));
        Assert.assertTrue(manager.getActiveStateIds().isEmpty());
    }

    /**
     * Tests for {@link ModuleStateManagerImpl#detectModules(org.w3c.dom.Node)}.
     */
    @Test
    public void testDetectModules()
    {
        Node node = EasyMock.createMock(Node.class);

        ModuleStateController controller1 = EasyMock.createNiceMock(ModuleStateController.class);
        EasyMock.expect(Boolean.valueOf(controller1.canActivateState(node))).andReturn(Boolean.TRUE).atLeastOnce();
        ModuleStateController controller2 = EasyMock.createNiceMock(ModuleStateController.class);
        EasyMock.expect(Boolean.valueOf(controller2.canActivateState(node))).andReturn(Boolean.FALSE).atLeastOnce();
        ModuleStateController controller3a = EasyMock.createNiceMock(ModuleStateController.class);
        EasyMock.expect(Boolean.valueOf(controller3a.canActivateState(node))).andReturn(Boolean.TRUE).atLeastOnce();
        ModuleStateController controller3b = EasyMock.createNiceMock(ModuleStateController.class);
        EasyMock.expect(Boolean.valueOf(controller3b.canActivateState(node))).andReturn(Boolean.FALSE).atLeastOnce();

        EasyMock.replay(node, controller1, controller2, controller3a, controller3b);

        ModuleStateManager manager = new ModuleStateManagerImpl((PreferencesRegistry)null);

        Collection<? extends String> moduleNames;
        moduleNames = manager.detectModules(node);
        Assert.assertTrue(moduleNames.isEmpty());

        manager.registerModuleStateController(TWO, controller2);
        moduleNames = manager.detectModules(node);
        Assert.assertTrue(moduleNames.isEmpty());

        manager.registerModuleStateController(ONE, controller1);
        moduleNames = manager.detectModules(node);
        Assert.assertEquals(1, moduleNames.size());
        Assert.assertTrue(moduleNames.contains(ONE));

        manager.registerModuleStateController(THREE, controller3a);
        manager.registerModuleStateController(THREE, controller3b);
        moduleNames = manager.detectModules(node);
        Assert.assertEquals(2, moduleNames.size());
        Assert.assertTrue(moduleNames.contains(ONE));
        Assert.assertTrue(moduleNames.contains(THREE));

        EasyMock.verify(node, controller1, controller2, controller3a, controller3b);
    }

    /**
     * Test for {@link ModuleStateManagerImpl#getModulesThatCanSaveState()} .
     */
    @Test
    public void testGetModulesThatCanSaveState()
    {
        ModuleStateManager manager = new ModuleStateManagerImpl(null);
        ModuleStateController one = EasyMock.createNiceMock(ModuleStateController.class);
        ModuleStateController two = EasyMock.createNiceMock(ModuleStateController.class);

        EasyMock.expect(Boolean.valueOf(one.canSaveState())).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(two.canSaveState())).andReturn(Boolean.FALSE);

        EasyMock.replay(one, two);

        manager.registerModuleStateController(ONE, one);
        manager.registerModuleStateController(TWO, two);

        Collection<? extends String> names;
        names = manager.getModulesThatCanSaveState();
        Assert.assertTrue(names.size() == 1 && names.contains(ONE));

        EasyMock.verify(one, two);
        EasyMock.reset(one, two);

        EasyMock.expect(Boolean.valueOf(one.canSaveState())).andReturn(Boolean.FALSE);
        EasyMock.expect(Boolean.valueOf(two.canSaveState())).andReturn(Boolean.TRUE);

        EasyMock.replay(one, two);

        names = manager.getModulesThatCanSaveState();
        Assert.assertTrue(names.size() == 1 && names.contains(TWO));

        EasyMock.verify(one, two);
        EasyMock.reset(one, two);

        EasyMock.expect(Boolean.valueOf(one.canSaveState())).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(two.canSaveState())).andReturn(Boolean.TRUE);

        EasyMock.replay(one, two);

        names = manager.getModulesThatCanSaveState();
        Assert.assertTrue(names.size() == 2 && names.containsAll(Arrays.asList(ONE, TWO)));

        EasyMock.verify(one, two);
    }

    /**
     * Test for
     * {@link ModuleStateManagerImpl#getModulesThatSaveStateByDefault()} .
     */
    @Test
    public void testGetModulesThatSaveStateByDefault()
    {
        ModuleStateManager manager = new ModuleStateManagerImpl(null);
        ModuleStateController one = EasyMock.createMock(ModuleStateController.class);
        ModuleStateController two = EasyMock.createMock(ModuleStateController.class);

        EasyMock.expect(Boolean.valueOf(one.isSaveStateByDefault())).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(two.isSaveStateByDefault())).andReturn(Boolean.FALSE);

        EasyMock.replay(one, two);

        manager.registerModuleStateController(ONE, one);
        manager.registerModuleStateController(TWO, two);

        Collection<? extends String> names;
        names = manager.getModulesThatSaveStateByDefault();
        Assert.assertTrue(names.size() == 1 && names.contains(ONE));

        EasyMock.verify(one, two);
        EasyMock.reset(one, two);

        EasyMock.expect(Boolean.valueOf(one.isSaveStateByDefault())).andReturn(Boolean.FALSE);
        EasyMock.expect(Boolean.valueOf(two.isSaveStateByDefault())).andReturn(Boolean.TRUE);

        EasyMock.replay(one, two);

        names = manager.getModulesThatSaveStateByDefault();
        Assert.assertTrue(names.size() == 1 && names.contains(TWO));

        EasyMock.verify(one, two);
        EasyMock.reset(one, two);

        EasyMock.expect(Boolean.valueOf(one.isSaveStateByDefault())).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(two.isSaveStateByDefault())).andReturn(Boolean.TRUE);

        EasyMock.replay(one, two);

        names = manager.getModulesThatSaveStateByDefault();
        Assert.assertTrue(names.size() == 2 && names.containsAll(Arrays.asList(ONE, TWO)));

        EasyMock.verify(one, two);
    }

    /**
     * Tests for
     * {@link ModuleStateManagerImpl#registerModuleStateController(String, ModuleStateController)}
     * ,
     * {@link ModuleStateManagerImpl#unregisterModuleStateController(String, ModuleStateController)}
     * , and {@link ModuleStateManagerImpl#getModuleNames()}.
     */
    @Test
    public void testRegisterUnregisterModuleStateController()
    {
        Element activeElement = EasyMock.createMock(Element.class);
        Element inactiveElement = EasyMock.createMock(Element.class);

        Collection<ModuleStateData> initStates = New.collection();
        initStates.add(new ModuleStateData(STATEID, DESCRIPTION, TAGS, true, Arrays.asList(ONE, THREE), activeElement));
        initStates.add(new ModuleStateData("state2", DESCRIPTION, TAGS, false, Arrays.asList(ONE, THREE), inactiveElement));

        ModuleStateController controller1 = EasyMock.createMock(ModuleStateController.class);
        ModuleStateController controller2 = EasyMock.createMock(ModuleStateController.class);
        ModuleStateController controller3a = EasyMock.createMock(ModuleStateController.class);
        ModuleStateController controller3b = EasyMock.createMock(ModuleStateController.class);

        EasyMock.replay(activeElement, inactiveElement, controller1, controller2, controller3a, controller3b);

        ModuleStateManager manager = new ModuleStateManagerImpl(null);
        manager.registerModuleStateController(ONE, controller1);
        manager.registerModuleStateController(TWO, controller2);
        manager.registerModuleStateController(THREE, controller3a);
        manager.registerModuleStateController(THREE, controller3b);

        Collection<? extends String> moduleNames;
        moduleNames = manager.getModuleNames();
        Assert.assertEquals(3, moduleNames.size());
        Assert.assertTrue(moduleNames.contains(ONE));
        Assert.assertTrue(moduleNames.contains(TWO));
        Assert.assertTrue(moduleNames.contains(THREE));

        manager.unregisterModuleStateController(TWO, controller2);
        moduleNames = manager.getModuleNames();
        Assert.assertEquals(2, moduleNames.size());
        Assert.assertFalse(moduleNames.contains(TWO));

        EasyMock.verify(activeElement, inactiveElement, controller1, controller2, controller3a, controller3b);

        manager.unregisterModuleStateController(THREE, controller3a);
        moduleNames = manager.getModuleNames();
        Assert.assertEquals(2, moduleNames.size());
        Assert.assertTrue(moduleNames.contains(THREE));

        manager.unregisterModuleStateController(THREE, controller3b);
        moduleNames = manager.getModuleNames();
        Assert.assertEquals(1, moduleNames.size());
        Assert.assertFalse(moduleNames.contains(THREE));

        manager.unregisterModuleStateController(ONE, controller1);
        moduleNames = manager.getModuleNames();
        Assert.assertTrue(moduleNames.isEmpty());
    }

    /**
     * Test for
     * {@link ModuleStateManagerImpl#registerState(String, String, Collection, Collection, Element)}
     * and {@link ModuleStateManagerImpl#unregisterState(String)} .
     */
    @Test
    public void testRegisterUnregisterState()
    {
        ModuleStateManager manager = new ModuleStateManagerImpl(null);
        Element element = EasyMock.createMock(Element.class);
        EasyMock.replay(element);

        Assert.assertTrue(manager.getRegisteredStateIds().isEmpty());

        Collection<? extends String> modules = Arrays.asList(ONE, TWO, THREE);
        manager.registerState(STATEID, DESCRIPTION, TAGS, modules, element);

        Assert.assertEquals(1, manager.getRegisteredStateIds().size());
        Assert.assertTrue(manager.getRegisteredStateIds().contains(STATEID));
        Assert.assertEquals(DESCRIPTION, manager.getStateDescription(STATEID));
        Assert.assertEquals(New.list(TAGS), New.list(manager.getStateTags(STATEID)));

        manager.unregisterState(STATEID);

        Assert.assertTrue(manager.getRegisteredStateIds().isEmpty());
    }

    /**
     * Test for
     * {@link ModuleStateManagerImpl#saveState(String, String, Collection, Collection, Node)}
     * .
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws IOException If the test fails.
     * @throws SAXException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testSaveState() throws SAXException, IOException, ParserConfigurationException, XPathExpressionException
    {
        ModuleStateManager manager = new ModuleStateManagerImpl(null);
        ModuleStateController one = EasyMock.createNiceMock(ModuleStateController.class);
        ModuleStateController two = EasyMock.createNiceMock(ModuleStateController.class);

        // Test two controllers with the same module name.
        ModuleStateController three1 = EasyMock.createNiceMock(ModuleStateController.class);
        ModuleStateController three2 = EasyMock.createNiceMock(ModuleStateController.class);

        one.saveState(EasyMock.isA(Node.class));
        three1.saveState(EasyMock.isA(Node.class));
        three2.saveState(EasyMock.isA(Node.class));

        EasyMock.replay(one, two, three1, three2);

        manager.registerModuleStateController(ONE, one);
        manager.registerModuleStateController(TWO, two);
        manager.registerModuleStateController(THREE, three1);
        manager.registerModuleStateController(THREE, three2);

        String id = "stateid";
        String description = "State description";
        Collection<? extends String> tags = Arrays.asList("tag1", "tag2", "tag3");
        Document doc1 = XMLUtilities.newDocument();

        manager.saveState(id, description, tags, Arrays.asList(ONE, THREE), doc1);

        EasyMock.verify(one, two, three1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtilities.format(doc1, baos, null);

        // Read the DOM back in to make sure it parses correctly.
        Document doc2 = XMLUtilities.newDocumentBuilderNS().parse(new ByteArrayInputStream(baos.toByteArray()));

        // Test that the namespace got set correctly for external applications
        // to use.
        MutableNamespaceContext nsContext = new MutableNamespaceContext();
        nsContext.addNamespace("st", ModuleStateController.STATE_NAMESPACE);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsContext);

        Assert.assertEquals(id, xpath.evaluate("/st:state/st:title", doc2));
        Assert.assertEquals(description, xpath.evaluate("/st:state/st:description", doc2));
        Assert.assertEquals("tag1", xpath.evaluate("/st:state/st:tags/st:tag[1]", doc2));
        Assert.assertEquals("tag2", xpath.evaluate("/st:state/st:tags/st:tag[2]", doc2));
        Assert.assertEquals("tag3", xpath.evaluate("/st:state/st:tags/st:tag[3]", doc2));
    }

    /**
     * Test for {@link ModuleStateManagerImpl#toggleState(String)} .
     *
     * @throws InterruptedException the interrupted exception
     */
//    @Test
    public void testToggleStates() throws InterruptedException
    {
        Preferences prefs = EasyMock.createMock(Preferences.class);
        EasyMock.expect(prefs.getJAXBObject(ModuleStateManagerState.class, ModuleStateManagerImpl.PREFS_KEY, null))
                .andReturn(null);
        PreferencesRegistry prefsRegistry = EasyMock.createMock(PreferencesRegistry.class);
        EasyMock.expect(prefsRegistry.getPreferences(ModuleStateManagerImpl.class)).andReturn(prefs);
        EasyMock.replay(prefs, prefsRegistry);

        ModuleStateManager manager = new ModuleStateManagerImpl(prefsRegistry);
        ModuleStateController one = EasyMock.createNiceMock(ModuleStateController.class);
        ModuleStateController two = EasyMock.createNiceMock(ModuleStateController.class);
        ModuleStateController three1 = EasyMock.createNiceMock(ModuleStateController.class);
        ModuleStateController three2 = EasyMock.createNiceMock(ModuleStateController.class);

        Element node = EasyMock.createMock(Element.class);
        one.activateState(EasyMock.eq(STATEID), EasyMock.eq(DESCRIPTION), EasyMockHelper.eq(TAGS), EasyMock.eq(node));
        three1.activateState(EasyMock.eq(STATEID), EasyMock.eq(DESCRIPTION), EasyMockHelper.eq(TAGS), EasyMock.eq(node));
        three2.activateState(EasyMock.eq(STATEID), EasyMock.eq(DESCRIPTION), EasyMockHelper.eq(TAGS), EasyMock.eq(node));

        EasyMock.reset(prefs);

        ModuleStateManagerState expectedData1 = new ModuleStateManagerState(
                Collections.singleton(new ModuleStateData(STATEID, DESCRIPTION, TAGS, false, Arrays.asList(ONE, THREE), node)));

        EasyMock.expect(prefs.putJAXBObject(ModuleStateManagerImpl.PREFS_KEY, expectedData1, false, null, manager))
                .andReturn(null);

        ModuleStateManagerState expectedData2 = new ModuleStateManagerState(
                Collections.singleton(new ModuleStateData(STATEID, DESCRIPTION, TAGS, true, Arrays.asList(ONE, THREE), node)));

        EasyMock.expect(prefs.putJAXBObject(ModuleStateManagerImpl.PREFS_KEY, expectedData2, false, null, manager))
                .andReturn(null);
        EasyMock.expect(prefs.putJAXBObject(ModuleStateManagerImpl.PREFS_KEY, expectedData1, false, null, manager))
                .andReturn(null);

        EasyMock.replay(one, two, three1, three2, node, prefs);

        manager.registerModuleStateController(ONE, one);
        manager.registerModuleStateController(TWO, two);
        manager.registerModuleStateController(THREE, three1);
        manager.registerModuleStateController(THREE, three2);

        manager.registerState(STATEID, DESCRIPTION, TAGS, Arrays.asList(ONE, THREE), node);

        manager.toggleState(STATEID);

        EasyMock.verify(one, two, three1, three2, node);

        Assert.assertTrue(manager.isStateActive(STATEID));
        Assert.assertTrue(manager.getActiveStateIds().size() == 1 && manager.getActiveStateIds().contains(STATEID));

        EasyMock.reset(one, two, three1, three2);

        one.deactivateState(STATEID, node);
        three1.deactivateState(STATEID, node);
        three2.deactivateState(STATEID, node);

        EasyMock.replay(one, two, three1, three2);

        manager.toggleState(STATEID);

        EasyMock.verify(one, two, three1, three2, node, prefs, prefsRegistry);

        Assert.assertFalse(manager.isStateActive(STATEID));
        Assert.assertTrue(manager.getActiveStateIds().isEmpty());
    }
}
