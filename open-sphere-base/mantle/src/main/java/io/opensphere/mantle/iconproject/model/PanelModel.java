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
    private final ObjectProperty<ViewStyle> viewType = new SimpleObjectProperty<>(this, "viewtype", ViewStyle.GRID);

    /** The selected icon to be used for customization dialogs. */
    private ObjectProperty<IconRecord> mySelectedRecord = new SimpleObjectProperty<IconRecord>();

    /** The registry of icons. */
    private IconRegistry myIconRegistry;

    /** The toolbox. */
    private Toolbox myToolBox;

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
    private DefaultIconRecordTreeItemObject myTreeObj;

    /**
     * Used to keep track of which icon and button are selected on the grid for
     * single selection purposes.
     */
    private HashMap<IconRecord, Button> mySingleSelectedIcon = new HashMap<IconRecord, Button>();

    /**
     * Gets the icon display view type.
     *
     * @return viewType the chosen view.
     */
    public ObjectProperty<ViewStyle> getViewType()
    {
        return viewType;
    }

    /**
     * The getter for the IconRegistry.
     *
     * @return myIconRegistry the icon registry
     */
    public IconRegistry getIconRegistry()
    {
        return myIconRegistry;
    }

    /**
     * The getter for the tile width.
     *
     * @return myTileWidth the width of the tiles
     */
    public IntegerProperty getCurrentTileWidth()
    {
        return myCurrentTileWidth;
    }

    /**
     * Sets the myIconRegistry.
     *
     * @param theIconRegistry the icon registry
     */
    public void setIconRegistry(IconRegistry theIconRegistry)
    {
        myIconRegistry = theIconRegistry;
    }

    /**
     * Sets the toolbox.
     *
     * @param tb the toolbox
     */
    public void setToolBox(Toolbox tb)
    {
        myToolBox = tb;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolBox()
    {
        return myToolBox;
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
     * @param theImportProps the import properties
     */
    public void setImportProps(ImportProp theImportProps)
    {
        myImportProps = theImportProps;
    }

    /**
     * The getter for the icon record list.
     *
     * @return myIconRecordList the icon record list
     */
    public List<IconRecord> getRecordList()
    {
        return myIconRecordList;
    }

    /**
     * The setter for the icon record list.
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
     * @param theViewModel the value to store in the {@link #myViewModel} field.
     */
    public void setViewModel(ViewModel theViewModel)
    {
        myViewModel = theViewModel;
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
     * @param theSelectedIcons the value to store in the
     *            {@link #mySelectedIcons} field.
     */
    public void setSelectedIcons(HashMap<IconRecord, Button> theSelectedIcons)
    {
        mySelectedIcons = theSelectedIcons;
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
     * Gets the value of the {@link #myTreeObj} field.
     *
     * @return the value stored in the {@link #myTreeObj} field.
     */
    public DefaultIconRecordTreeItemObject getTreeObj()
    {
        return myTreeObj;
    }

    /**
     * Sets the value of the {@link #myTreeObj} field.
     *
     * @param theTreeObj the value to store in the {@link #myTreeObj} field.
     */
    public void setTreeObj(DefaultIconRecordTreeItemObject theTreeObj)
    {
        myTreeObj = theTreeObj;
    }

    /**
     * Gets the selected icon map.
     *
     * @return myHash a map containing an icon record and it's corresponding
     *         button in the display.
     */
    public HashMap<IconRecord, Button> getSelectedIconMap()
    {
        return mySingleSelectedIcon;
    }
}
