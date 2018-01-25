package io.opensphere.osh.aerialimagery.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.osh.aerialimagery.model.LinkedLayer;
import io.opensphere.osh.aerialimagery.model.LinkedLayers;

/**
 * Unit test for {@link LayerLinker} class.
 */
public class LayerLinkerTest
{
    /**
     * The linked layer id.
     */
    private static final String ourLinkedLayerId = "linkedTypeKey2";

    /**
     * The other linked layer id.
     */
    private static final String ourOtherLinkedLayerId = "otherLinkedTypeKey2";

    /**
     * Tests getting the linked layer id.
     */
    @Test
    public void testGetLinkedLayer()
    {
        EasyMockSupport support = new EasyMockSupport();

        LinkedLayers links = createLinks();
        PreferencesRegistry prefsReg = createPrefsRegistry(support, links);

        support.replayAll();

        LayerLinker linker = new LayerLinker(prefsReg);

        assertEquals(ourOtherLinkedLayerId, linker.getLinkedLayerId(ourLinkedLayerId));

        support.verifyAll();
    }

    /**
     * Tests getting the linker layer id when the layer id is the other one.
     */
    @Test
    public void testGetLinkedLayerReverse()
    {
        EasyMockSupport support = new EasyMockSupport();

        LinkedLayers links = createLinks();
        PreferencesRegistry prefsReg = createPrefsRegistry(support, links);

        support.replayAll();

        LayerLinker linker = new LayerLinker(prefsReg);

        assertEquals(ourLinkedLayerId, linker.getLinkedLayerId(ourOtherLinkedLayerId));

        support.verifyAll();
    }

    /**
     * Tests getting the linked layer of an unlinked layer.
     */
    @Test
    public void testGetLinkedNotLinked()
    {
        EasyMockSupport support = new EasyMockSupport();

        LinkedLayers links = createLinks();
        PreferencesRegistry prefsReg = createPrefsRegistry(support, links);

        support.replayAll();

        LayerLinker linker = new LayerLinker(prefsReg);

        assertNull(linker.getLinkedLayerId("iamunlinked"));

        support.verifyAll();
    }

    /**
     * Tests getting the linked layer of an unlinked layer.
     */
    @Test
    public void testGetLinkedNull()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry prefsReg = createPrefsRegistry(support, null);

        support.replayAll();

        LayerLinker linker = new LayerLinker(prefsReg);

        assertNull(linker.getLinkedLayerId(ourLinkedLayerId));

        support.verifyAll();
    }

    /**
     * Creates the test linked layers.
     *
     * @return The linked layers.
     */
    private LinkedLayers createLinks()
    {
        LinkedLayers linkedLayers = new LinkedLayers();

        LinkedLayer linkedLayer = new LinkedLayer();
        linkedLayer.setLinkedLayersTypeKey("linkedTypeKey1");
        linkedLayer.setOtherLinkedLayersTypeKey("otherLinkedTypeKey1");
        linkedLayers.getLinkedLayers().add(linkedLayer);

        linkedLayer = new LinkedLayer();
        linkedLayer.setLinkedLayersTypeKey(ourLinkedLayerId);
        linkedLayer.setOtherLinkedLayersTypeKey(ourOtherLinkedLayerId);
        linkedLayers.getLinkedLayers().add(linkedLayer);

        return linkedLayers;
    }

    /**
     * Creates the easy mocked preferences registry.
     *
     * @param support Used to create the mock.
     * @param linkedLayers The linked layers to return.
     * @return The preferences registry.
     */
    private PreferencesRegistry createPrefsRegistry(EasyMockSupport support, LinkedLayers linkedLayers)
    {
        Preferences prefs = support.createMock(Preferences.class);
        EasyMock.expect(prefs.getJAXBObject(EasyMock.eq(LinkedLayers.class), EasyMock.cmpEq("linkedlayers"), EasyMock.isNull()))
                .andReturn(linkedLayers);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        EasyMock.expect(registry.getPreferences(EasyMock.eq(LayerLinker.class))).andReturn(prefs);

        return registry;
    }
}
