package io.opensphere.mantle.iconproject.panels;

import java.awt.Button;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;

/**
 * A panel in which a user may select an existing collection or create a new
 * one.
 */
public class IconProjCollectionNamesPane extends BorderPane
{
    /**
     * serialVersionUID.
     */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    /**
     * The combo box from which an existing collection name is selected. This is
     * disabled if the user is entering a new collection name.
     */
    private ComboBox<String> myExistingComboBox;

    /**
     * The radio button with which the user selects the mode of the name panel.
     * This button is in a group with {@link #myNewRB}, and the group is used to
     * put the panel in a mode in which the user may select an existing group,
     * or create a new group.
     */
    private RadioButton myExistingRB;

    /**
     * The radio button with which the user selects the mode of the name panel.
     * This button is in a group with {@link #myExistingRB}, and the group is
     * used to put the panel in a mode in which the user may select an existing
     * group, or create a new group.
     */
    private RadioButton myNewRB;
    
    /** The ToggleGroup contains both the two radio buttons.
     * */
    private final ToggleGroup myToggleGroup = new ToggleGroup();
    /**
     * The text field into which the user may enter a new collection name. This
     * field is disabled if the user is selecting an existing collection.
     */
    private TextField myNewTF;
    
    private BooleanProperty myDisable = new SimpleBooleanProperty(false);

    private ObservableList<String> options;

    /**
     * Instantiates a new collection name panel.
     *
     * @param collectionNameSet The set of collection names to display in the
     *            panel.
     */
    public IconProjCollectionNamesPane(Set<String> collectionNameSet)
    {
        collectionNameSet.remove(IconRecord.DEFAULT_COLLECTION);
        collectionNameSet.remove(IconRecord.USER_ADDED_COLLECTION);
        collectionNameSet.remove(IconRecord.FAVORITES_COLLECTION);
        List<String> colNames = New.list(collectionNameSet);
        Collections.sort(colNames);
        colNames.add(0, IconRecord.FAVORITES_COLLECTION);
        colNames.add(0, IconRecord.USER_ADDED_COLLECTION);
        colNames.add(0, IconRecord.DEFAULT_COLLECTION);
        
        options = FXCollections.observableArrayList(colNames);

        setTop(createCollection());
        setCenter(createSubCollection());

    }

    private TilePane createSubCollection()
    {
        TilePane tilepane = new TilePane();
        for (int i = 0; i < 20; i++)
        {
            Label label = new Label("test" + i);
            Button button = new Button("the button");
            button.setBackground(Color.BLUE);
            TilePane.setAlignment(label, Pos.BOTTOM_RIGHT);
            tilepane.getChildren().addAll(label);

        }
        return tilepane;
    }

    private Node createCollection()
    {
        Label CollectionText = new Label("Select collection name for icons:");

        myExistingRB = new RadioButton("Existing");
        myExistingRB.setToggleGroup(myToggleGroup);
        myExistingRB.setSelected(true);
        myExistingComboBox = new ComboBox<>(options);
        
        myNewRB = new RadioButton("New");
        myNewRB.setToggleGroup(myToggleGroup);
        myNewTF = new TextField();
        myNewTF.setEditable(myNewRB.isPressed());
        myNewTF.disableProperty().bind(myNewRB.pressedProperty());
        return null;
    }

    /**
     * Gets the collection name.
     *
     * @return the collection name
     */
    public String getCollectionName()
    {
        if (myNewRB.isSelected())
        {
            return myNewTF.getText();
        }
        return myExistingComboBox.getSelectionModel().getSelectedItem();
    }
}
