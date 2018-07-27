package io.opensphere.mantle.iconproject.panels;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
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

public class CollectNamesPane extends VBox
{
    private RadioButton myExistingRB;

    private ComboBox<String> myExistingComboBox;

    private ObservableList<String> options;

    private RadioButton myNewRB;

    private TextField myNewTF;

    private PanelModel myPanelModel;

    private IconRegistry myIconRegistry;

    private ToggleGroup test = new ToggleGroup();

    private ImportProp myIconProps;

    public CollectNamesPane(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        myIconProps = myPanelModel.getImportProps();
        myIconRegistry = myPanelModel.getMyIconRegistry();
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

        HBox bottomPane = new HBox();

        Label CollectionText = new Label("Select collection name for icons:");
        CollectionText.setFont(Font.font(CollectionText.getFont().getFamily(), FontPosture.ITALIC, 11));
        CollectionText.setContentDisplay(ContentDisplay.BOTTOM);

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
            System.out.println("the current value is: " + myExistingComboBox.getValue());
            myIconProps.getCollectionName().set(myExistingComboBox.getValue());
            System.out.println("value is set " + myIconProps.getCollectionName().get());
            if (!options.contains(myExistingComboBox.getValue()))
            {
                options.add(myExistingComboBox.getValue());
            }
        });

        myNewRB = new RadioButton("New");
        myNewRB.setOnMouseClicked(event ->
        {
            lockfeature(true);
        });

        myNewTF = new TextField();
        myNewTF.setDisable(true);
        bottomPane.setSpacing(5.);
        bottomPane.setAlignment(Pos.BASELINE_LEFT);

        myExistingRB.setToggleGroup(test);
        myNewRB.setToggleGroup(test);
        bottomPane.getChildren().addAll(myExistingRB, myNewRB, myExistingComboBox);
        bottomPane.setSpacing(5.);

        getChildren().addAll(CollectionText, bottomPane);
        setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: purple;");
        System.out.println("Parent is: " + getParent());

    }

    private void updateCollectionName()
    {
    }

    /**
     * Controls whether the user can enter data via the textfield or the drop
     * down menu provided by the combobox.
     * 
     * @param b the indication of which state the buttons should be set to.
     */
    private void lockfeature(boolean b)
    {
        myExistingComboBox.setEditable(b);
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
