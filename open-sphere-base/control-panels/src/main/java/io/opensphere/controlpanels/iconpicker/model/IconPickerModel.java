package io.opensphere.controlpanels.iconpicker.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

/**
 * The model used by the icon picker.
 */
public class IconPickerModel
{
    /**
     * The id of the chosen icon.
     */
    private final IntegerProperty myIconId;

    /**
     * The icon image.
     */
    private final ObjectProperty<Image> myImage = new SimpleObjectProperty<>();

    /**
     * Constructs a new model.
     *
     * @param iconIdProperty An {@link IntegerProperty} to be used as the icon
     *            id property.
     */
    public IconPickerModel(IntegerProperty iconIdProperty)
    {
        myIconId = iconIdProperty;
    }

    /**
     * Gets the icon id property.
     *
     * @return The icon id property.
     */
    public IntegerProperty icondIdProperty()
    {
        return myIconId;
    }

    /**
     * Gets the image property.
     *
     * @return The image property.
     */
    public ObjectProperty<Image> imageProperty()
    {
        return myImage;
    }

    /**
     * Gets the id of the selected icon.
     *
     * @return the iconId.
     */
    public int getIconId()
    {
        return myIconId.get();
    }

    /**
     * Sets the id of the selected icon.
     *
     * @param iconId the iconId to set.
     */
    public void setIconId(int iconId)
    {
        myIconId.set(iconId);
    }

    /**
     * Gets the image of the selected icon.
     *
     * @return the image.
     */
    public Image getImage()
    {
        return myImage.get();
    }

    /**
     * Sets the image of the selected icon.
     *
     * @param image the image to set.
     */
    public void setImage(Image image)
    {
        myImage.set(image);
    }
}
