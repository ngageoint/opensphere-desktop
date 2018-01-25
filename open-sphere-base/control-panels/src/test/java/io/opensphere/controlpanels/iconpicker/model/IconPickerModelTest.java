package io.opensphere.controlpanels.iconpicker.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import org.junit.Test;

import io.opensphere.core.util.image.ImageUtil;

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
        IntegerProperty iconId = new SimpleIntegerProperty();
        IconPickerModel model = new IconPickerModel(iconId);

        assertSame(iconId, model.icondIdProperty());

        Image image = SwingFXUtils.toFXImage(ImageUtil.BROKEN_IMAGE, null);
        model.setIconId(2);
        model.setImage(image);

        assertEquals(2, model.getIconId());
        assertEquals(model.getIconId(), model.icondIdProperty().get());

        assertEquals(image, model.getImage());
        assertEquals(model.getImage(), model.imageProperty().get());
    }
}
