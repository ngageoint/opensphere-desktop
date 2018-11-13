package io.opensphere.mantle.iconproject.model;

import java.awt.Window;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.ConcurrentDoubleProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.IconRegistryListener;
import io.opensphere.mantle.iconproject.impl.DefaultIconRecordTreeItemObject;
import io.opensphere.mantle.iconproject.model.IconRegistryChangeListener.Change;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

/** The model for the IconManagerFrame. */
public class PanelModel
{
    /**
     * A pass-through registry listener used to inform functional-interface type
     * listeners of registry additions / removals.
     */
    private final class PassThroughIconRegistryListener implements IconRegistryListener
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

    /** The owner of this window. */
    private Window myOwner;

    /** The model in which search text is maintained. */
    private StringProperty mySearchText = new ConcurrentStringProperty();

    /**
     * The value used for the tilewidth. The number inside is the default value
     * on program startup.
     */
    private final DoubleProperty myCurrentTileWidth = new ConcurrentDoubleProperty(80);

    /** The import property. */
    private ImportProp myImportProps = new ImportProp();

    /** The icon record list. */
    private List<IconRecord> myIconRecordList;

    /** The filtered icon record list. */
    private final List<IconRecord> myFilteredIconRecordList;

    /** The model for the panels contained in the UI. */
    private ViewModel myViewModel;

    /** The icons currently selected. */
    private final HashMap<IconRecord, Node> myAllSelectedIcons = new HashMap<>();

    /** The Tree model. */
    private DefaultIconRecordTreeItemObject myTreeObject;

    /**
     * Used to keep track of which icon and button are selected on the grid for
     * single selection purposes.
     */
    private final HashMap<IconRecord, Node> mySingleSelectedIcon = new HashMap<>();

    /** Whether to use the filtered icon record list or the regular one. */
    private boolean myUseFilteredList;

    /** The set of change listeners called when icons are added or removed. */
    private final Set<IconRegistryChangeListener> myRegistryChangeListeners = New.set();

    /** The registry listener used to react to additions and removals. */
    private final IconRegistryListener myRegistryListener;

    /**
     * Builds the panel to use inside the Icon Manager.
     *
     * @param toolbox the toolbox
     */
    public PanelModel(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myFilteredIconRecordList = New.list();
        myUseFilteredList = false;
        myRegistryListener = new PassThroughIconRegistryListener();
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
        if (myIconRegistry != null)
        {
            myIconRegistry.addListener(myRegistryListener);
        }
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
     * Sets the window's owner.
     *
     * @param owner the window's owner
     */
    public void setOwner(Window owner)
    {
        myOwner = owner;
    }

    /**
     * Gets the window's owner.
     *
     * @return the window's owner
     */
    public Window getOwner()
    {
        return myOwner;
    }

    /**
     * Gets the import properties.
     *
     * @return the import properties
     */
    public ImportProp getImportProperties()
    {
        return myImportProps;
    }

    /**
     * Sets the import properties.
     *
     * @param importProps the import properties
     */
    public void setImportProperties(ImportProp importProps)
    {
        myImportProps = importProps;
    }

    /**
     * Gets the icon record list.
     *
     * @return the icon record list
     */
    public List<IconRecord> getRecordList()
    {
        return myIconRecordList;
    }

    /**
     * Sets the icon record list.
     *
     * @param list the icon record list
     */
    public void setRecordList(List<IconRecord> list)
    {
        myIconRecordList = list;
    }

    /**
     * Gets the filtered icon record list.
     *
     * @return the filtered icon record list
     */
    public List<IconRecord> getFilteredRecordList()
    {
        return myFilteredIconRecordList;
    }

    /**
     * Gets the value of the {@link #myViewModel} field.
     *
     * @return the value stored in the {@link #myViewModel} field.
     */
    public ViewModel getViewModel()
    {
        return myViewModel;
    }

    /**
     * Sets the value of the {@link #myViewModel} field.
     *
     * @param viewModel the value to store in the {@link #myViewModel} field.
     */
    public void setViewModel(ViewModel viewModel)
    {
        myViewModel = viewModel;
    }

    /**
     * Gets the value of the {@link #myAllSelectedIcons} field.
     *
     * @return the value stored in the {@link #myAllSelectedIcons} field.
     */
    public HashMap<IconRecord, Node> getAllSelectedIcons()
    {
        return myAllSelectedIcons;
    }

    /**
     * Gets the value of the {@link #mySelectedRecord} field.
     *
     * @return the value stored in the {@link #mySelectedRecord} field.
     */
    public ObjectProperty<IconRecord> getSelectedRecord()
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
     * Gets the value of the {@link #myTreeObject} field.
     *
     * @return the value stored in the {@link #myTreeObject} field.
     */
    public DefaultIconRecordTreeItemObject getTreeObject()
    {
        return myTreeObject;
    }

    /**
     * Sets the value of the {@link #myTreeObject} field.
     *
     * @param treeObject the value to store in the {@link #myTreeObject} field.
     */
    public void setTreeObject(DefaultIconRecordTreeItemObject treeObject)
    {
        myTreeObject = treeObject;
    }

    /**
     * Gets the single, primary selected button.
     *
     * @return the selected button
     */
    public HashMap<IconRecord, Node> getSingleSelectedIcon()
    {
        return mySingleSelectedIcon;
    }

    /**
     * Gets whether to use the filtered list.
     *
     * @return whether to use the filtered list
     */
    public boolean getUseFilteredList()
    {
        return myUseFilteredList;
    }

    /**
     * Sets whether to use the filtered list.
     *
     * @param useFilteredList true to force the use of the filtered list, false
     *            otherwise.
     */
    public void setUseFilteredList(boolean useFilteredList)
    {
        myUseFilteredList = useFilteredList;
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
