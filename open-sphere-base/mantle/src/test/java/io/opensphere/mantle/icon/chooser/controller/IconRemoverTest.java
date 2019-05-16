package io.opensphere.mantle.icon.chooser.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

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
        EasyMock.expect(icon.collectionNameProperty()).andReturn(new SimpleStringProperty("User")).anyTimes();
        EasyMock.expect(icon.favoriteProperty()).andReturn(new SimpleBooleanProperty(false)).anyTimes();
        EasyMock.expect(icon.getTags()).andReturn(FXCollections.observableArrayList()).anyTimes();
        EasyMock.expect(icon.nameProperty()).andReturn(new SimpleStringProperty("testIcon")).anyTimes();
        EasyMock.expect(icon.descriptionProperty()).andReturn(new SimpleStringProperty("A description")).anyTimes();
        IconRegistry iconRegistry = createIconRegistry(support, icon);
        EasyMock.expect(Boolean.valueOf(iconRegistry.removeIcon(EasyMock.eq(icon), EasyMock.isA(IconRemover.class)))).andReturn(Boolean.TRUE);

        Toolbox toolbox = support.createMock(Toolbox.class);

        support.replayAll();

        IconModel model = new IconModel(toolbox);
        model.setIconRegistry(iconRegistry);
        model.selectedRecordProperty().set(icon);

        assertEquals(1, model.getModel().getIconRecords().size());
        assertEquals(icon, model.getModel().getIconRecords().get(0));

        IconRemover remover = new IconRemover(model);
        remover.deleteIcons();

        assertTrue(model.getModel().getIconRecords().isEmpty());

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
     * @param iconRecords The records the registry will have to start with.
     * @return The mocked icon registry.
     */
    private IconRegistry createIconRegistry(EasyMockSupport support, IconRecord ... iconRecords)
    {
        IconRegistry iconRegistry = support.createNiceMock(IconRegistry.class);

        EasyMock.expect(iconRegistry.getIconRecords()).andReturn(New.list(iconRecords));

        return iconRegistry;
    }
}
