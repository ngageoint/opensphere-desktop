package io.opensphere.mantle.iconproject.panels;

import java.awt.Button;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

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

    /**
     * The ToggleGroup contains both the two radio buttons.
     */
    private final ToggleGroup myToggleGroup = new ToggleGroup();

    /**
     * The text field into which the user may enter a new collection name. This
     * field is disabled if the user is selecting an existing collection.
     */
    private TextField myNewTF;

    private ObservableList<String> options;

    private IconRegistry myIconRegistry;

    /**
     * Instantiates a new collection name panel.
     *
     * @param collectionNameSet The set of collection names to display in the
     *            panel.
     */
    public IconProjCollectionNamesPane(IconRegistry iconRegistry)
    {
        myIconRegistry = iconRegistry;
        Set<String> collectionNameSet = myIconRegistry.getCollectionNames();

        collectionNameSet.remove(IconRecord.DEFAULT_COLLECTION);
        collectionNameSet.remove(IconRecord.USER_ADDED_COLLECTION);
        collectionNameSet.remove(IconRecord.FAVORITES_COLLECTION);
        List<String> colNames = New.list(collectionNameSet);
        Collections.sort(colNames);
        colNames.add(0, IconRecord.FAVORITES_COLLECTION);
        colNames.add(0, IconRecord.USER_ADDED_COLLECTION);
        colNames.add(0, IconRecord.DEFAULT_COLLECTION);

        options = FXCollections.observableArrayList(colNames);

        VBox vbox = createCollection();
        setTop(vbox);
        setCenter(createSubCollection());

    }

    private VBox createSubCollection()
    {

        SubCollectPane topPane = new SubCollectPane(myIconRegistry.getSubCategoiresForCollection(getCollectionName()), true);
        return topPane;
    }

    private VBox createCollection()
    {

        VBox topPane = new VBox();
        HBox bottomPane = new HBox();

        Label CollectionText = new Label("Select collection name for icons:");
        CollectionText.setFont(Font.font(CollectionText.getFont().getFamily(), FontPosture.ITALIC, 11));
        CollectionText.setContentDisplay(ContentDisplay.BOTTOM);

        myExistingRB = new RadioButton("Existing");
        myExistingRB.setToggleGroup(myToggleGroup);
        myExistingRB.setSelected(true);
        myExistingRB.setOnMouseClicked(event -> lockfeature(true));

        myExistingComboBox = new ComboBox<>(options);

        myExistingComboBox.setOnAction((event) ->
        {

            System.out.println(myExistingComboBox.getValue());

        });

        myNewRB = new RadioButton("New");
        myNewRB.setToggleGroup(myToggleGroup);
        myNewRB.setOnMouseClicked(event -> lockfeature(false));

        myNewTF = new TextField();
        myNewTF.setDisable(true);

        bottomPane.getChildren().addAll(myExistingRB, myExistingComboBox, myNewRB, myNewTF);
        topPane.getChildren().addAll(CollectionText, bottomPane);
        topPane.setSpacing(5.);

        return topPane;
    }

    private void updateCollectionName()
    {
        // TODO Auto-generated method stub

    }

    /**
     * Controls whether the user can enter data via the textfield or the drop
     * down menu provided by the combobox.
     * 
     * @param b the indication of which state the buttons should be set to.
     */
    private void lockfeature(boolean b)
    {
        if (b)
        {
            myExistingComboBox.setDisable(false);
            myNewTF.setDisable(true);
        }
        else
        {
            myExistingComboBox.setDisable(true);
            myNewTF.setDisable(false);
        }
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
