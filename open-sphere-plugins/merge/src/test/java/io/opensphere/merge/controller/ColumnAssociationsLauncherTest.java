package io.opensphere.merge.controller;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.mantle.data.columns.gui.ColumnMappingOptionsProvider;

/**
 * Unit test for {@link ColumnAssociationsLauncher}.
 */
public class ColumnAssociationsLauncherTest
{
    /**
     * Tests launching the column associations.
     */
    @Test
    public void testLaunchColumnAssociations()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        ColumnAssociationsLauncher launcher = new ColumnAssociationsLauncher(toolbox);
        launcher.launchColumnAssociations();

        support.verifyAll();
    }

    /**
     * Creates the easy mocked toolbox.
     *
     * @param support Used to create the mock.
     * @return The toolbox.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        OptionsRegistry optionsRegistry = support.createMock(OptionsRegistry.class);
        optionsRegistry.requestShowTopic(EasyMock.cmpEq(ColumnMappingOptionsProvider.TOPIC));

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);
        EasyMock.expect(uiRegistry.getOptionsRegistry()).andReturn(optionsRegistry);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry);

        return toolbox;
    }
}
