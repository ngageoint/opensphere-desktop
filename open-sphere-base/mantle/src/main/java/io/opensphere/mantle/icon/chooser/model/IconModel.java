package io.opensphere.mantle.icon.chooser.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.javafx.ConcurrentDoubleProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;

/** The model for the IconManagerFrame. */
public class IconModel
{
    /** The selected icon to be used for customization dialogs. */
    private final ObjectProperty<IconRecord> mySelectedRecord = new SimpleObjectProperty<>();

    /** The selected icon to be used for customization dialogs. */
    private final ObjectProperty<IconRecord> myPreviewRecordProperty = new SimpleObjectProperty<>();

    /** The registry of icons. */
    private IconRegistry myIconRegistry;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The model in which search text is maintained. */
    private final StringProperty mySearchText = new ConcurrentStringProperty();

    /**
     * The value used for the tile width. The number inside is the default value
     * on program startup.
     */
    private final DoubleProperty myCurrentTileWidth = new ConcurrentDoubleProperty(80);

    /** The model in which customization state is maintained. */
    private final CustomizationModel myCustomizationModel;

    /** The chooser model in which the dialog's information is maintained. */
    private final IconChooserModel myModel;

    /**
     * Builds the panel to use inside the Icon Manager.
     *
     * @param toolbox the toolbox
     */
    public IconModel(final Toolbox toolbox)
    {
        myToolbox = toolbox;
        myModel = new IconChooserModel();
        myCustomizationModel = new CustomizationModel();
    }

    /**
     * Gets the value of the {@link #myCustomizationModel} field.
     *
     * @return the value of the myCustomizationModel field.
     */
    public CustomizationModel getCustomizationModel()
    {
        return myCustomizationModel;
    }

    /**
     * Gets the IconRegistry.
     *
     * @return the icon registry
     */
    public IconRegistry getIconRegistry()
    {
        return myIconRegistry;
    }

    /**
     * Sets the myIconRegistry.
     *
     * @param iconRegistry the icon registry
     */
    public void setIconRegistry(final IconRegistry iconRegistry)
    {
        myIconRegistry = iconRegistry;
        myModel.setIconRegistry(iconRegistry);
    }

    /**
     * Gets the value of the {@link #myModel} field.
     *
     * @return the value of the myModel field.
     */
    public IconChooserModel getModel()
    {
        return myModel;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Gets the tile width.
     *
     * @return the width of the tiles
     */
    public DoubleProperty tileWidthProperty()
    {
        return myCurrentTileWidth;
    }

    /**
     * Gets the value of the {@link #mySearchText} field.
     *
     * @return the value stored in the {@link #mySearchText} field.
     */
    public StringProperty searchTextProperty()
    {
        return mySearchText;
    }

    /**
     * Gets the value of the {@link #mySelectedRecord} field.
     *
     * @return the value stored in the {@link #mySelectedRecord} field.
     */
    public ObjectProperty<IconRecord> selectedRecordProperty()
    {
        return mySelectedRecord;
    }

    /**
     * Gets the value of the {@link #myPreviewRecordProperty} field.
     *
     * @return the value stored in the {@link #myPreviewRecordProperty} field.
     */
    public ObjectProperty<IconRecord> previewRecordProperty()
    {
        return myPreviewRecordProperty;
    }
}
