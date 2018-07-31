package io.opensphere.mantle.iconproject.model;

import java.awt.Window;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ScrollPane;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;

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
    private IntegerProperty myTileWidth = new SimpleIntegerProperty(100);

    /** The import property. */
    private ImportProp myImportProps = new ImportProp();

    private List<IconRecord> myIconRecordList;

    private ViewModel myViewModel;
    
    

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
     * Gets the icon record.
     *
     * @return the icon record
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

    public List<IconRecord> getRecordList()
    {
        return myIconRecordList;
    }

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
