package io.opensphere.mantle.icon.chooser.model;

import java.util.List;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.ConcurrentDoubleProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.IconRegistryListener;
import io.opensphere.mantle.icon.chooser.model.IconRegistryChangeListener.Change;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

/** The model for the IconManagerFrame. */
public class IconModel
{
    /**
     * A pass-through registry listener used to inform functional-interface type
     * listeners of registry additions / removals. Not private to prevent
     * generation of synthetic accessors.
     */
    final class PassThroughIconRegistryListener implements IconRegistryListener
    {
        @Override
        public void iconsUnassigned(List<Long> deIds, Object source)
        {
            /* intentionally blank */
        }

        @Override
        public void iconsRemoved(List<IconRecord> removed, Object source)
        {
            Change change = new Change(null, removed);
            myRegistryChangeListeners.forEach(c -> c.onChanged(change));
        }

        @Override
        public void iconsAdded(List<IconRecord> added, Object source)
        {
            Change change = new Change(added, null);
            myRegistryChangeListeners.forEach(c -> c.onChanged(change));
        }

        @Override
        public void iconAssigned(long iconId, List<Long> deIds, Object source)
        {
            /* intentionally blank */
        }
    }

    /** The selected icon to be used for customization dialogs. */
    private final ObjectProperty<IconRecord> mySelectedRecord = new SimpleObjectProperty<>();

    /** The selected icon to be used for customization dialogs. */
    private final ObjectProperty<IconRecord> myPreviewRecordProperty = new SimpleObjectProperty<>();

    /** The registry of icons. */
    private IconRegistry myIconRegistry;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The model in which search text is maintained. */
    private StringProperty mySearchText = new ConcurrentStringProperty();

    /**
     * The value used for the tile width. The number inside is the default value
     * on program startup.
     */
    private final DoubleProperty myCurrentTileWidth = new ConcurrentDoubleProperty(80);

    /**
     * The set of change listeners called when icons are added or removed. Not
     * private to prevent generation of synthetic accessors.
     */
    final Set<IconRegistryChangeListener> myRegistryChangeListeners = New.set();

    /** The registry listener used to react to additions and removals. */
    private final IconRegistryListener myRegistryListener;

    /** The model in which customization state is maintained. */
    private final CustomizationModel myCustomizationModel;

    private final IconChooserModel myModel;

    /**
     * Builds the panel to use inside the Icon Manager.
     *
     * @param toolbox the toolbox
     */
    public IconModel(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myModel = new IconChooserModel();
        myRegistryListener = new PassThroughIconRegistryListener();
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
    public void setIconRegistry(IconRegistry iconRegistry)
    {
        if (iconRegistry == null && myIconRegistry != null)
        {
            myIconRegistry.removeListener(myRegistryListener);
        }
        myIconRegistry = iconRegistry;
        myModel.setIconRegistry(iconRegistry);
        if (myIconRegistry != null)
        {
            myIconRegistry.addListener(myRegistryListener);
        }
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

    /**
     * Adds a new listener to be notified of registry adds and removes.
     *
     * @param listener the listener to add.
     */
    public void addRegistryChangeListener(IconRegistryChangeListener listener)
    {
        myRegistryChangeListeners.add(listener);
    }

    /**
     * Removes the listener from notification of registry adds and removes.
     *
     * @param listener the listener to remove.
     */
    public void removeRegistryChangeListener(IconRegistryChangeListener listener)
    {
        myRegistryChangeListeners.remove(listener);
    }
}
