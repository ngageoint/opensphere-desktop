package io.opensphere.controlpanels.iconpicker.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.Image;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.controlpanels.iconpicker.model.IconPickerModel;
import io.opensphere.core.PluginToolboxRegistry;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;

/**
 * Unit test for {@link IconPickerController}.
 */
public class IconPickerControllerTestDisplay
{
    /**
     * The test image id.
     */
    private static final int ourImageId = 22;

    /**
     * The test image url.
     */
    private static final String ourImageUrl = IconPickerControllerTestDisplay.class.getClassLoader()
            .getResource("images/brokenimage.gif").toString();

    /**
     * Another test image url.
     */
    private static final String ourImageUrl2 = IconPickerControllerTestDisplay.class.getClassLoader()
            .getResource("images/bubble.png").toString();

    /**
     * Tests showing the picker and user selects an icon.
     *
     * @throws MalformedURLException Bad Url.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testShowPicker() throws MalformedURLException, InterruptedException
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
        IconPickerModel model = new IconPickerModel(iconIdProperty);
        IconPickerController controller = new IconPickerController(toolbox, displayer, model);

        controller.showPicker();

        CountDownLatch latch = new CountDownLatch(1);
        iconIdProperty.addListener((obs, old, newVal) ->
        {
            latch.countDown();
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        assertEquals(ourImageId, iconIdProperty.get());
        assertTrue(0 < model.getImage().getWidth());

        support.verifyAll();
    }

    /**
     * Tests showing the picker and user does not selects an icon.
     *
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testShowPickerNoIcon() throws InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        IconChooserDisplayer displayer = createDisplayer(support, toolbox, null);

        support.replayAll();

        IntegerProperty iconIdProperty = new SimpleIntegerProperty();
        IconPickerModel model = new IconPickerModel(iconIdProperty);
        CountDownLatch latch = new CountDownLatch(1);
        model.imageProperty().addListener((obs, old, newVal) ->
        {
            latch.countDown();
        });
        IconPickerController controller = new IconPickerController(toolbox, displayer, model);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        assertEquals(0, iconIdProperty.get());
        assertNotNull(model.getImage());
        Image image = model.getImage();

        controller.showPicker();

        Thread.sleep(500);
        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        assertEquals(0, iconIdProperty.get());
        assertEquals(image, model.getImage());

        support.verifyAll();
    }

    /**
     * Tests showing the picker and user selects an icon.
     *
     * @throws MalformedURLException Bad Url.
     * @throws InterruptedException Don't interrupt.
     */
    @Test
    public void testShowPickerNotDefault() throws MalformedURLException, InterruptedException
    {
        PlatformImpl.startup(() ->
        {
        });
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support, 23, new URL(ourImageUrl2));
        IconRecord record = createRecord(support);
        IconChooserDisplayer displayer = createDisplayer(support, toolbox, record);

        support.replayAll();

        IntegerProperty iconIdProperty = new SimpleIntegerProperty(23);
        IconPickerModel model = new IconPickerModel(iconIdProperty);
        CountDownLatch latch = new CountDownLatch(1);
        model.imageProperty().addListener((obs, old, newVal) ->
        {
            latch.countDown();
        });
        IconPickerController controller = new IconPickerController(toolbox, displayer, model);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        Image before = model.getImage();
        assertTrue(0 < model.getImage().getWidth());

        controller.showPicker();

        CountDownLatch latch2 = new CountDownLatch(1);
        iconIdProperty.addListener((obs, old, newVal) ->
        {
            latch2.countDown();
        });
        assertTrue(latch2.await(1, TimeUnit.SECONDS));
        FXUtilities.runOnFXThreadAndWait(() ->
        {
        });

        assertEquals(ourImageId, iconIdProperty.get());
        assertTrue(0 < model.getImage().getWidth());
        assertFalse(before.equals(model.getImage()));

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
        return createRecord(support, ourImageId, ourImageUrl);
    }

    /**
     * Creates a mocked {@link IconRecord}.
     *
     * @param support Used to create the mock.
     * @param imageId The test image id.
     * @param imageUrl The test image url.
     * @return The mock.
     * @throws MalformedURLException Bad url.
     */
    private IconRecord createRecord(EasyMockSupport support, int imageId, String imageUrl) throws MalformedURLException
    {
        IconRecord record = support.createMock(IconRecord.class);

        EasyMock.expect(Integer.valueOf(record.getId())).andReturn(Integer.valueOf(imageId));
        EasyMock.expect(record.getImageURL()).andReturn(new URL(imageUrl));

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
        return createToolbox(support, 7, IconRegistry.DEFAULT_ICON_URL);
    }

    /**
     * Creates a mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param imageId The expected image id.
     * @param imageUrl The expected image url.
     * @return The mock.
     */
    private Toolbox createToolbox(EasyMockSupport support, int imageId, URL imageUrl)
    {
        IconRecord record = support.createMock(IconRecord.class);
        EasyMock.expect(record.getImageURL()).andReturn(imageUrl);

        IconRegistry iconRegistry = support.createMock(IconRegistry.class);
        if (imageUrl.toString().equals(IconRegistry.DEFAULT_ICON_URL.toString()))
        {
            EasyMock.expect(iconRegistry.getIconRecord(imageUrl)).andReturn(record);
        }
        else
        {
            EasyMock.expect(Integer.valueOf(record.getId())).andReturn(Integer.valueOf(imageId));
            EasyMock.expect(iconRegistry.getIconRecordByIconId(imageId)).andReturn(record);
        }

        MantleToolbox mantle = support.createMock(MantleToolbox.class);
        EasyMock.expect(mantle.getIconRegistry()).andReturn(iconRegistry);

        PluginToolboxRegistry toolboxRegistry = support.createMock(PluginToolboxRegistry.class);
        EasyMock.expect(toolboxRegistry.getPluginToolbox(MantleToolbox.class)).andReturn(mantle);

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getPluginToolboxRegistry()).andReturn(toolboxRegistry);

        return toolbox;
    }
}
