package io.opensphere.mantle.iconproject.model;

import java.awt.Window;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** The model for the IconManagerFrame. */
public class PanelModel
{
    /** View set to default of Grid. */
    private final ObjectProperty<ViewStyle> viewType = new SimpleObjectProperty<>(this, "viewtype", ViewStyle.GRID);

    /** The Collection Name */
    private StringProperty myCollectionName = new SimpleStringProperty("Default");

    /** The SubCollection Name */
    private Set<String> mySubCollectionList = null;
    
    /** The selected icon to be used for the builder. */
    private IconRecord mySelectedIcon;

    private IconRegistry myIconRegistry;

    private Toolbox myToolBox;

    private Window myOwner;

    private StringProperty mySubCollectionName;
    
    private ImportProp myImportProps = new ImportProp();

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
     * Sets the myIconRegistry.
     *
     * @param theIconRegistry the icon registry
     */
    public void setMyIconRegistry(IconRegistry theIconRegistry)
    {
        myIconRegistry = theIconRegistry;
    }

    public void setToolBox(Toolbox tb)
    {
        this.myToolBox = tb;
    }
    
    public Toolbox getToolBox()
    {
        return myToolBox;
    }

    public void setOwner(Window owner)
    {
        this.myOwner = owner;
    }
    
    public Window getOwner()
    {
        return myOwner;
    }

    public ImportProp getImportProps()
    {
        return myImportProps;
    }

    public void setImportProps(ImportProp myImportProps)
    {
        this.myImportProps = myImportProps;
    }

    public IconRecord getIconRecord()
    {
        return mySelectedIcon;
    }

    public void setIconRecord(IconRecord mySelectedIcon)
    {
        this.mySelectedIcon = mySelectedIcon;
    }

}
