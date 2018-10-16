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
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.impl.DefaultIconRecordTreeItemObject;

/** The model for the IconManagerFrame. */
public class PanelModel
{
    /** View set to default of Grid. */
    private final ObjectProperty<ViewStyle> myViewStyle = new SimpleObjectProperty<>(this, "viewtype", ViewStyle.GRID);

    /** The selected icon to be used for customization dialogs. */
    private final ObjectProperty<IconRecord> mySelectedRecord = new SimpleObjectProperty<>();

    /** The registry of icons. */
    private IconRegistry myIconRegistry;

    /** The toolbox. */
    private final Toolbox myToolbox;

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

    /** The filtered icon record list. */
    private final List<IconRecord> myFilteredIconRecordList;

    /** The model for the panels contained in the UI. */
    private ViewModel myViewModel;

    /** The icons currently selected. */
    private final HashMap<IconRecord, Button> myAllSelectedIcons = new HashMap<>();

    /** The Tree model. */
    private DefaultIconRecordTreeItemObject myTreeObject;

    /**
     * Used to keep track of which icon and button are selected on the grid for
     * single selection purposes.
     */
    private final HashMap<IconRecord, Button> mySingleSelectedIcon = new HashMap<>();

    /** Whether to use the filtered icon record list or the regular one. */ 
    private boolean myUseFilteredList;

    /**
     * Builds the panel to use inside the Icon Manager.
     *
     * @param toolbox the toolbox
     */
    public PanelModel (Toolbox toolbox)
    {
        myToolbox = toolbox;
        myFilteredIconRecordList = New.list();
        myUseFilteredList = false;
    }

    /**
     * Gets the icon display view style.
     *
     * @return the chosen view.
     */
    public ObjectProperty<ViewStyle> getViewStyle()
    {
        return myViewStyle;
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
    public HashMap<IconRecord, Button> getAllSelectedIcons()
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
    public HashMap<IconRecord, Button> getSingleSelectedIcon()
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
     * @param whether to use the filtered list
     */
    public void setUseFilteredList(boolean useFilteredList)
    {
        myUseFilteredList = useFilteredList;
    }
}
