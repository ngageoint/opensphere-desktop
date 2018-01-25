package io.opensphere.controlpanels.iconpicker.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.ImageView;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.controlpanels.iconpicker.controller.IconChooserDisplayer;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;

/**
 * Unit test for {@link IconPickerButton}.
 */
public class IconPickerButtonTestDisplay
{
    /**
     * The test image id.
     */
    private static final int ourImageId = 22;

    /**
     * The test image url.
     */
    private static final String ourImageUrl = IconPickerButtonTestDisplay.class.getClassLoader()
            .getResource("images/brokenimage.gif").toString();

    /**
     * Tests having the user pick an icon.
     *
     * @throws MalformedURLException Bad url.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void test() throws MalformedURLException, InterruptedException
    {
        PlatformImpl.startup(() ->
        {
        });

        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        IconRecord record = createRecord(support);
        IconChooserDisplayer displayer = createDisplayer(support, toolbox, record);

        support.replayAll();

        IntegerProperty iconIdProperty = new SimpleIntegerProperty();
        IconPickerButton button = new IconPickerButton(toolbox, iconIdProperty, displayer);
        ImageView imageView = (ImageView)button.getGraphic();

        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        Thread.sleep(500);
        assertNotNull(imageView.getImage());

        button.fire();

        CountDownLatch latch = new CountDownLatch(1);
        iconIdProperty.addListener((obs, old, newVal) ->
        {
            latch.countDown();
        });
        assertTrue(latch.await(1000, TimeUnit.SECONDS));
        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        assertEquals(ourImageId, iconIdProperty.get());
        assertNotNull(imageView.getImage());

        support.verifyAll();
    }

    /**
     * Creates a mocked {@link IconChooserDisplayer}.
     *
     * @param support Used to create the mock.
     * @param toolbox A mocked toolbox.
     * @param record A mocked record to simulate what user picked, or null if
     *            use didn't pick an icon.
     * @return The mock.
     */
    @SuppressWarnings("unchecked")
    private IconChooserDisplayer createDisplayer(EasyMockSupport support, Toolbox toolbox, IconRecord record)
    {
        IconChooserDisplayer displayer = support.createMock(IconChooserDisplayer.class);

        displayer.displayIconChooser(EasyMock.eq(toolbox), EasyMock.isA(ObjectProperty.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            ObjectProperty<IconRecord> iconProperty = (ObjectProperty<IconRecord>)EasyMock.getCurrentArguments()[1];
            iconProperty.set(record);
            return null;
        });

        return displayer;
    }

    /**
     * Creates a mocked {@link IconRecord}.
     *
     * @param support Used to create the mock.
     * @return The mock.
     * @throws MalformedURLException Bad url.
     */
    private IconRecord createRecord(EasyMockSupport support) throws MalformedURLException
    {
        IconRecord record = support.createMock(IconRecord.class);

        EasyMock.expect(Integer.valueOf(record.getId())).andReturn(Integer.valueOf(ourImageId));
        EasyMock.expect(record.getImageURL()).andReturn(new URL(ourImageUrl));

        return record;
    }

    /**
     * Creates a mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @return The mock.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        IconRecord record = support.createMock(IconRecord.class);
        EasyMock.expect(record.getImageURL()).andReturn(IconRegistry.DEFAULT_ICON_URL);

        IconRegistry iconRegistry = support.createMock(IconRegistry.class);
        EasyMock.expect(iconRegistry.getIconRecord(IconRegistry.DEFAULT_ICON_URL)).andReturn(record);

        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantle.getIconRegistry()).andReturn(iconRegistry);

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantle);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry);

        return toolbox;
    }
}
