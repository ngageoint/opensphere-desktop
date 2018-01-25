package io.opensphere.mantle.crust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDeletableDataGroupInfo;
import io.opensphere.mantle.data.impl.DeletableDataGroupInfoAssistant;

/**
 * Unit test for {@link DefaultDataGroupAndTypeFactory}.
 */
public class DefaultDataGroupAndTypeFactoryTest
{
    /**
     * The test folder name.
     */
    private static final String ourFolderName = "New Folder";

    /**
     * The test provider type.
     */
    private static final String ourProviderType = "I am provider";

    /**
     * The test type key.
     */
    private static final String ourTypeKey = "I am key";

    /**
     * Tests creating the group.
     */
    @Test
    public void testCreateGroup()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        DefaultDataGroupAndTypeFactory factory = new DefaultDataGroupAndTypeFactory();
        DefaultDataGroupInfo group = factory.createGroup(toolbox, ourProviderType, ourFolderName, null);

        assertEquals(toolbox, group.getToolbox());
        assertEquals(ourProviderType, group.getProviderType());
        assertEquals(ourFolderName, group.getDisplayName());

        support.verifyAll();
    }

    /**
     * Tests creating the group.
     */
    @Test
    public void testCreateGroupDelete()
    {
        EasyMockSupport support = new EasyMockSupport();

        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        Toolbox toolbox = createToolbox(support, mantle);
        @SuppressWarnings("unchecked")
        Consumer<DataGroupInfo> deleteListener = support.createMock(Consumer.class);

        support.replayAll();

        DefaultDataGroupAndTypeFactory factory = new DefaultDataGroupAndTypeFactory();
        DefaultDataGroupInfo group = factory.createGroup(toolbox, ourProviderType, ourFolderName, deleteListener);

        assertEquals(toolbox, group.getToolbox());
        assertEquals(ourProviderType, group.getProviderType());
        assertEquals(ourFolderName, group.getDisplayName());
        assertTrue(group instanceof DefaultDeletableDataGroupInfo);
        assertTrue(group.getAssistant() instanceof DeletableDataGroupInfoAssistant);

        support.verifyAll();
    }

    /**
     * Tests creating the type.
     */
    @Test
    public void testCreateType()
    {
        EasyMockSupport support = new EasyMockSupport();

        MantleToolbox mantle = createMantle(support);
        Toolbox toolbox = createToolbox(support, mantle);

        support.replayAll();

        DefaultDataGroupAndTypeFactory factory = new DefaultDataGroupAndTypeFactory();
        DataTypeInfo type = factory.createType(toolbox, ourProviderType, ourTypeKey, "my type name", "My layer name");

        assertEquals(ourProviderType, type.getSourcePrefix());
        assertEquals(ourTypeKey, type.getTypeKey());
        assertEquals("My layer name", type.getDisplayName());
        assertEquals("my type name", type.getTypeName());
        assertFalse(type.providerFiltersMetaData());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link MantleToolbox}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link MantleToolbox}.
     */
    private MantleToolbox createMantle(EasyMockSupport support)
    {
        DataTypeInfoPreferenceAssistant assistant = support.createMock(DataTypeInfoPreferenceAssistant.class);
        EasyMock.expect(Boolean.valueOf(assistant.isVisiblePreference(EasyMock.cmpEq(ourTypeKey)))).andReturn(Boolean.TRUE);
        EasyMock.expect(Integer.valueOf(assistant.getColorPreference(EasyMock.cmpEq(ourTypeKey), EasyMock.anyInt())))
                .andReturn(Integer.valueOf(20));
        EasyMock.expect(Integer.valueOf(assistant.getOpacityPreference(EasyMock.cmpEq(ourTypeKey), EasyMock.anyInt())))
                .andReturn(Integer.valueOf(150));

        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantle.getDataTypeInfoPreferenceAssistant()).andReturn(assistant).atLeastOnce();

        return mantle;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param mantle A mocked {@link MantleToolbox}.
     * @return The mocked {@link Toolbox}.
     */
    private Toolbox createToolbox(EasyMockSupport support, MantleToolbox mantle)
    {
        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantle).atLeastOnce();

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry).atLeastOnce();

        return toolbox;
    }
}
