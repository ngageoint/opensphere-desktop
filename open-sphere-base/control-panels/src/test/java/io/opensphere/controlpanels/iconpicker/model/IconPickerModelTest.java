package io.opensphere.controlpanels.iconpicker.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import io.opensphere.core.util.image.ImageUtil;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Unit test for the {@link IconPickerModel}.
 */
public class IconPickerModelTest
{
    /**
     * Tests the model.
     */
    @Test
    public void test()
    {
        LongProperty iconId = new SimpleLongProperty();
        IconPickerModel model = new IconPickerModel(iconId);

        assertSame(iconId, model.iconIdProperty());

        Image image = SwingFXUtils.toFXImage(ImageUtil.BROKEN_IMAGE, null);
        model.setIconId(2);
        model.setImage(image);

        assertEquals(2, model.getIconId());
        assertEquals(model.getIconId(), model.iconIdProperty().get());

        assertEquals(image, model.getImage());
        assertEquals(model.getImage(), model.imageProperty().get());
    }
}
