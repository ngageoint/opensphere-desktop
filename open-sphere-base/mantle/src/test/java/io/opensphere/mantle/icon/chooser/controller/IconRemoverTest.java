package io.opensphere.mantle.icon.chooser.controller;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.chooser.model.IconModel;

/**
 * Tests the {@link IconRemover} class.
 */
public class IconRemoverTest
{
    /**
     * Tests removing an icon.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        IconRecord icon = support.createMock(IconRecord.class);
        IconRegistry iconRegistry = createIconRegistry(support);
        EasyMock.expect(Boolean.valueOf(iconRegistry.removeIcon(EasyMock.eq(icon), EasyMock.isA(IconRemover.class)))).andReturn(Boolean.TRUE);

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        IconModel model = new IconModel(toolbox);
        model.setIconRegistry(iconRegistry);
        model.selectedRecordProperty().set(icon);

        IconRemover remover = new IconRemover(model);
        remover.deleteIcons();

        support.verifyAll();
    }

    /**
     * Tests removing an icon.
     */
    @Test
    public void testRemoveNothingSelected()
    {
        EasyMockSupport support = new EasyMockSupport();

        IconRegistry iconRegistry = createIconRegistry(support);

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        IconModel model = new IconModel(toolbox);
        model.setIconRegistry(iconRegistry);

        IconRemover remover = new IconRemover(model);
        remover.deleteIcons();

        support.verifyAll();
    }

    /**
     * Creates a mocked icon registry.
     * @param support Used to create the mock.
     * @return The mocked icon registry.
     */
    private IconRegistry createIconRegistry(EasyMockSupport support)
    {
        IconRegistry iconRegistry = support.createNiceMock(IconRegistry.class);

        EasyMock.expect(iconRegistry.getIconRecords()).andReturn(New.list());

        return iconRegistry;
    }
}
