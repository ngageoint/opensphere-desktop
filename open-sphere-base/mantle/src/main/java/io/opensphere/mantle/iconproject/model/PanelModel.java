package io.opensphere.mantle.iconproject.model;

import java.awt.Window;
import java.util.HashMap;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.impl.DefaultIconRecordTreeItemObject;

/** The model for the IconManagerFrame. */
public class PanelModel
{
    /** View set to default of Grid. */
    private final ObjectProperty<ViewStyle> myViewType = new SimpleObjectProperty<>(this, "viewtype", ViewStyle.GRID);

    /** The selected icon to be used for customization dialogs. */
    private ObjectProperty<IconRecord> mySelectedRecord = new SimpleObjectProperty<IconRecord>();

    /** The registry of icons. */
    private IconRegistry myIconRegistry;

    /** The toolbox. */
    private Toolbox myToolbox;

    /** The owner of this window. */
    private Window myOwner;

    /**
     * The value used for the tilewidth. The number inside is the default value
     * on program startup.
     */
    private final IntegerProperty myCurrentTileWidth = new SimpleIntegerProperty(80);

    /** The import property. */
    private ImportProp myImportProps = new ImportProp();

    /** The icon record list. */
    private List<IconRecord> myIconRecordList;

    /** The model for the panels contained in the UI. */
    private ViewModel myViewModel;

    /** The icons currently selected. */
    private HashMap<IconRecord, Button> mySelectedIcons = new HashMap<IconRecord, Button>();

    /** The Tree model. */
    private DefaultIconRecordTreeItemObject myTreeObject;

    /**
     * Used to keep track of which icon and button are selected on the grid for
     * single selection purposes.
     */
    private HashMap<IconRecord, Button> mySingleSelectedIcon = new HashMap<IconRecord, Button>();

    /**
     * Builds the panel to use inside the Icon Manager.
     *
     * @param toolbox the toolbox
     */
    public PanelModel (Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Gets the icon display view type.
     *
     * @return the chosen view.
     */
    public ObjectProperty<ViewStyle> getViewType()
    {
        return myViewType;
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
     * Gets the tile width.
     *
     * @return the width of the tiles
     */
    public IntegerProperty getCurrentTileWidth()
    {
        return myCurrentTileWidth;
    }

    /**
     * Sets the myIconRegistry.
     *
     * @param iconRegistry the icon registry
     */
    public void setIconRegistry(IconRegistry iconRegistry)
    {
        myIconRegistry = iconRegistry;
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
    public ImportProp getImportProps()
    {
        return myImportProps;
    }

    /**
     * Sets the import properties.
     *
     * @param importProps the import properties
     */
    public void setImportProps(ImportProp importProps)
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
    public void setIconRecordList(List<IconRecord> list)
    {
        myIconRecordList = list;
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
     * Gets the value of the {@link #mySelectedIcons} field.
     *
     * @return the value stored in the {@link #mySelectedIcons} field.
     */
    public HashMap<IconRecord, Button> getSelectedIcons()
    {
        return mySelectedIcons;
    }

    /**
     * Sets the value of the {@link #mySelectedIcons} field.
     *
     * @param selectedIcons the value to store in the
     *            {@link #mySelectedIcons} field.
     */
    public void setSelectedIcons(HashMap<IconRecord, Button> selectedIcons)
    {
        mySelectedIcons = selectedIcons;
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
     * Gets the selected icon map.
     *
     * @return a map containing an icon record and it's corresponding
     *         button in the display.
     */
    public HashMap<IconRecord, Button> getSelectedIconMap()
    {
        return mySingleSelectedIcon;
    }
}
