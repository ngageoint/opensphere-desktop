package io.opensphere.mantle.iconproject.panels;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.ImportProp;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

/**
 * Creates the elements for the collection name selection in the Add icon
 * Dialog.
 */
public class CollectNamesPane extends VBox
{
    /**
     * The Radio Button to indicate only existing collection names will appear
     * in the combobox.
     */
    private RadioButton myExistingRB;

    /**
     * The ComboBox to show the current collections names and take in user input
     * for new collection names.
     */
    private ComboBox<String> myExistingComboBox;

    /** The existing collection name choices. */
    private ObservableList<String> options;

    /** The Radio Button to enable the user to enter a new collection name. */
    private RadioButton myNewRB;

    /** The text for the new Radio Button. */
    private TextField myNewTF;

    /** The model for the UI. */
    private PanelModel myPanelModel;

    /** The toggle group for the New and Existing radio buttons. */
    private ToggleGroup myToggleGroup = new ToggleGroup();

    /** The model for the importation properties. */
    private ImportProp myIconProps;

    /**
     * Creates the Collection Name selection controls and packages into a VBox.
     *
     * @param thePanelModel the current UI model.
     */
    public CollectNamesPane(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        myIconProps = myPanelModel.getImportProps();
        Set<String> collectionNameSet = myPanelModel.getIconRegistry().getCollectionNames();

        collectionNameSet.remove(IconRecord.DEFAULT_COLLECTION);
        collectionNameSet.remove(IconRecord.USER_ADDED_COLLECTION);
        collectionNameSet.remove(IconRecord.FAVORITES_COLLECTION);
        List<String> colNames = New.list(collectionNameSet);
        Collections.sort(colNames);
        colNames.add(0, IconRecord.FAVORITES_COLLECTION);
        colNames.add(0, IconRecord.USER_ADDED_COLLECTION);
        colNames.add(0, IconRecord.DEFAULT_COLLECTION);

        options = FXCollections.observableArrayList(colNames);

        HBox bottomPane = new HBox();

        Label collectionMessage = new Label("Select collection name for icons:");
        collectionMessage.setFont(Font.font(collectionMessage.getFont().getFamily(), FontPosture.ITALIC, 11));
        collectionMessage.setContentDisplay(ContentDisplay.BOTTOM);

        myExistingRB = new RadioButton("Existing");
        myExistingRB.setSelected(true);
        myExistingRB.setOnMouseClicked(event ->
        {
            lockfeature(false);
        });

        myExistingComboBox = new ComboBox<>(options);
        myExistingComboBox.getSelectionModel().selectFirst();
        myExistingComboBox.setOnAction((event) ->
        {
            myIconProps.getCollectionName().set(myExistingComboBox.getValue());
            if (!options.contains(myExistingComboBox.getValue()))
            {
                options.add(myExistingComboBox.getValue());
                System.out.println("added to registry" + myPanelModel.getIconRegistry().getCollectionNames());
            }
        });

        myNewRB = new RadioButton("New");
        myNewRB.setOnMouseClicked(event ->
        {
            lockfeature(true);
        });

        myNewTF = new TextField();
        myNewTF.setDisable(true);
        bottomPane.setSpacing(5);
        bottomPane.setAlignment(Pos.BASELINE_LEFT);

        myExistingRB.setToggleGroup(myToggleGroup);
        myNewRB.setToggleGroup(myToggleGroup);
        bottomPane.getChildren().addAll(myExistingRB, myNewRB, myExistingComboBox);
        bottomPane.setSpacing(5);

        getChildren().addAll(collectionMessage, bottomPane);
//        setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
//                + "-fx-border-radius: 5;" + "-fx-border-color: purple;");
        setStyle("-fx-padding: 10");
    }

    /**
     * Controls whether the user can enter data via the textfield or the drop
     * down menu provided by the combobox.
     *
     * @param isEditable the indication of which state the buttons should be set
     *            to.
     */
    private void lockfeature(boolean isEditable)
    {
        myExistingComboBox.setEditable(isEditable);
    }

    /**
     * Gets the collection name.
     *
     * @return the collection name
     */
    public String getCollectionName()
    {
        return myExistingComboBox.getSelectionModel().getSelectedItem();
    }
}
