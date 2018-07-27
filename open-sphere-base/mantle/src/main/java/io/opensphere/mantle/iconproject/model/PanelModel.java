package io.opensphere.mantle.iconproject.model;

import java.awt.Window;
import java.util.Set;

import io.opensphere.core.Toolbox;
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
    private Set<String> mySubCollectionNames = null;

    private IconRegistry myIconRegistry;

    private Toolbox myToolBox;

    private Window myOwner;

    /**
     * gets the icon display view type.
     * 
     * @return viewType the chosen view.
     */
    public ObjectProperty<ViewStyle> getViewType()
    {
        return viewType;
    }

    public StringProperty getMyCollectionName()
    {
        return myCollectionName;
    }

    public void setMyCollectionName(StringProperty myCollectionName)
    {
        this.myCollectionName = myCollectionName;
    }

    public Set<String> getMySubCollectionNames()
    {
        return mySubCollectionNames;
    }

    public void setMySubCollectionNames(Set<String> mySubCollectionName)
    {
        this.mySubCollectionNames = mySubCollectionName;
    }

    public IconRegistry getMyIconRegistry()
    {
        return myIconRegistry;
    }
    
    public void setMyIconRegistry(IconRegistry iconRegistry)
    {
        this.myIconRegistry = iconRegistry;
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
    
}
