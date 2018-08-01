package io.opensphere.mantle.iconproject.model;

import java.awt.Window;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ContextMenu;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.view.IconPopupMenu;

/** The model for the IconManagerFrame. */
public class PanelModel
{
    /** View set to default of Grid. */
    private final ObjectProperty<ViewStyle> viewType = new SimpleObjectProperty<>(this, "viewtype", ViewStyle.GRID);

    /** The selected icon to be used for the builder. */
    private IconRecord mySelectedIcon;

    /** The registry of icons. */
    private IconRegistry myIconRegistry;

    /** The toolbox. */
    private Toolbox myToolBox;

    /** The owner of this window. */
    private Window myOwner;

    /** The value used for the tilewidth. */
    private final IntegerProperty myTileWidth = new SimpleIntegerProperty(100);

    /** The import property. */
    private ImportProp myImportProps = new ImportProp();

    /** The icon record list. */
    private List<IconRecord> myIconRecordList;

    private ViewModel myViewModel;
    
    

    /**
     * Shows the iconpopupmenu.
     *
     * @return the built context menu
     */
    public ContextMenu showPopupMenu()
    {
        return new IconPopupMenu(getIconRecord());
    }

    /**
     * gets the icon display view type.
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
    public IconRegistry getMyIconRegistry()
    {
        return myIconRegistry;
    }

    /**
     * The getter for the tile width.
     *
     * @return myTileWidth the width of the tiles
     */
    public IntegerProperty getTileWidth()
    {
        return myTileWidth;
    }

    /**
     * Sets the myIconRegistry.
     *
     * @param theIconRegistry the icon registry
     */
    public void setMyIconRegistry(IconRegistry theIconRegistry)
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
     * Gets the selected icon record.
     *
     * @return the selected icon record
     */
    public IconRecord getIconRecord()
    {
        return mySelectedIcon;
    }

    /**
     * Sets the selected icon.
     *
     * @param theSelectedIcon the selected icon
     */
    public void setIconRecord(IconRecord theSelectedIcon)
    {
        mySelectedIcon = theSelectedIcon;
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

    public ViewModel getViewModel()
    {
        return myViewModel;
    }

    public void setViewModel(ViewModel theViewModel)
    {
        myViewModel = theViewModel;
    }

}
