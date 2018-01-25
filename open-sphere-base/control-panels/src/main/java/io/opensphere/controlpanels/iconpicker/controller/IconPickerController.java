package io.opensphere.controlpanels.iconpicker.controller;

import java.io.IOException;
import java.io.InputStream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.iconpicker.model.IconPickerModel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Shows the Icon picker dialog to the user and updates the model when user
 * picks different icons.
 */
public class IconPickerController
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(IconPickerController.class);

    /**
     * Displays the icon chooser and gets the {@link IconRecord} of the selected
     * icon.
     */
    private final IconChooserDisplayer myIconChooser;

    /**
     * The model used by the picker.
     */
    private final IconPickerModel myModel;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new controller.
     *
     * @param toolbox The system toolbox.
     * @param iconChooser Displays the icon chooser and gets the
     *            {@link IconRecord} of the selected icon.
     * @param model The model used by the picker.
     */
    public IconPickerController(Toolbox toolbox, IconChooserDisplayer iconChooser, IconPickerModel model)
    {
        myToolbox = toolbox;
        myModel = model;
        myIconChooser = iconChooser;

        IconRegistry iconRegistry = MantleToolboxUtils.getMantleToolbox(toolbox).getIconRegistry();
        if (myModel.getIconId() <= 0)
        {
            IconRecord iconRecord = iconRegistry.getIconRecord(IconRegistry.DEFAULT_ICON_URL);
            setModel(iconRecord, false);
        }
        else
        {
            IconRecord iconRecord = iconRegistry.getIconRecordByIconId(myModel.getIconId());
            if (iconRecord != null)
            {
                setModel(iconRecord, true);
            }
            else
            {
                iconRecord = iconRegistry.getIconRecord(IconRegistry.DEFAULT_ICON_URL);
                setModel(iconRecord, false);
            }
        }
    }

    /**
     * Shows the icon picking dialog that allows the user to pick icons
     * registered within the system.
     */
    public void showPicker()
    {
        ObjectProperty<IconRecord> selectedIcon = new SimpleObjectProperty<>();
        selectedIcon.addListener((obs, old, newValue) ->
        {
            if (newValue != null)
            {
                setModel(newValue, true);
            }
        });
        myIconChooser.displayIconChooser(myToolbox, selectedIcon);
    }

    /**
     * Sets the model with the {@link IconRecord} values.
     *
     * @param record The icon to set in the model.
     * @param userPicked True if the new record has been picked by the user,
     *            false if this record is just to decorate the icon picker
     *            button.
     */
    private void setModel(IconRecord record, boolean userPicked)
    {
        ThreadUtilities.runBackground(() ->
        {
            try
            {
                InputStream stream = record.getImageURL().openStream();
                Image image = new Image(stream);
                FXUtilities.runOnFXThread(() ->
                {
                    if (userPicked)
                    {
                        myModel.setIconId(record.getId());
                    }
                    myModel.setImage(image);
                });
            }
            catch (IOException e)
            {
                LOGGER.error(e, e);
            }
        });
    }
}
