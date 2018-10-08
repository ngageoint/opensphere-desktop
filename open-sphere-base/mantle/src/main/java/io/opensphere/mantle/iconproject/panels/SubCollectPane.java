package io.opensphere.mantle.iconproject.panels;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.model.ImportProp;
import io.opensphere.mantle.iconproject.model.PanelModel;

/**
 * A panel in which a user may select a subcategory, create a new subcategory,
 * or select no subcategories.
 */
public class SubCollectPane extends VBox
{
    /**
     * The combo box from which an existing category is selected. This is
     * disabled if the user is entering a new category.
     */
    private ComboBox<String> myComboBox;

    /**
     * The radio button with which the user selects the mode of the category
     * panel to use existing categories. This button is in a group with
     * {@link #myNewCatRB}, {@link #myNoneRB} and optionally
     * {@link #mySubCatsFromDirNamesRB}, and the group is used to put the panel
     * in a mode in which the user may select an existing group, or create a new
     * group.
     */
    private RadioButton myExistingRB;

    /**
     * The radio button with which the user selects the mode of the name panel
     * to create a new category. This button is in a group with
     * {@link #myExistingRB}, {@link #myNoneRB} and optionally
     * {@link #mySubCatsFromDirNamesRB}, and the group is used to put the panel
     * in a mode in which the user may select an existing group, or create a new
     * group.
     */
    private RadioButton myNewCatRB;

    /**
     * The radio button with which the user selects the mode of the name panel
     * to not use a category. This button is in a group with
     * {@link #myExistingRB}, {@link #myNewCatRB} and optionally
     * {@link #mySubCatsFromDirNamesRB}, and the group is used to put the panel
     * in a mode in which the user may select an existing group, or create a new
     * group.
     */
    private RadioButton myNoneRB;

    /** The selection options contained in the combo context menu. */
    private ObservableList<String> myComboBoxItems;

    /**
     * The toggle group containing {@link #myExistingRB}
     * {@link #myNewCatRB} @{@link #myNoneRB}.
     */
    private final ToggleGroup myToggleGroup = new ToggleGroup();

    /** The UI model. */
    private final PanelModel myPanelModel;

    /** The icon registry. */
    private final IconRegistry myIconRegistry;

    /** The names of sub collection names. */
    private final Set<String> myCategorySet;

    /** The importation properties model.  */
    private final ImportProp myImportProps;

    /**
     * Instantiates a new sub-category selection panel.
     *
     * @param thePanelModel the model to be used for the registry items.
     */
    public SubCollectPane(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        myImportProps = myPanelModel.getImportProps();
        myIconRegistry = myPanelModel.getIconRegistry();
        myCategorySet = myIconRegistry.getSubCategoiresForCollection("Default");
        List<String> names = New.list(myCategorySet);
        Collections.sort(names);
        myComboBoxItems = FXCollections.observableArrayList(names);
        myImportProps.getCollectionName().addListener((observable, oldValue, newValue) -> updateComboBox());
        createPanel();
    }

    /** Creates the panel for the Sub Collection controls. */
    private void createPanel()
    {
        HBox hbox = new HBox();
        Label subCollectMessage = new Label("Do you want to add a sub-category to your icon?:");
        subCollectMessage.setFont(Font.font(subCollectMessage.getFont().getFamily(), FontPosture.ITALIC, 11));
        subCollectMessage.setContentDisplay(ContentDisplay.BOTTOM);

        myNoneRB = new RadioButton("No Sub-category");
        myNoneRB.setToggleGroup(myToggleGroup);
        myNoneRB.setSelected(true);
        myNoneRB.setOnAction(event ->
        {
            myComboBox.setEditable(false);
            myComboBox.setDisable(true);
        });

        myExistingRB = new RadioButton("Existing");
        myExistingRB.setToggleGroup(myToggleGroup);
        myExistingRB.setOnAction(event ->
        {
            myComboBox.setEditable(false);
            myComboBox.setDisable(false);
        });

        myComboBox = new ComboBox<>(myComboBoxItems);
        HBox existingPnl = new HBox();
        existingPnl.getChildren().addAll(myExistingRB, myComboBox);

        myNewCatRB = new RadioButton("New");
        myNewCatRB.setOnAction(event ->
        {
            myComboBox.setDisable(false);
            myComboBox.setEditable(true);
        });
        myNewCatRB.setToggleGroup(myToggleGroup);

        hbox.setAlignment(Pos.BASELINE_LEFT);
        hbox.setSpacing(5);
        myComboBox.setOnAction(event ->
        {
            if (!(myToggleGroup.getSelectedToggle() == myNewCatRB))
            {
                myExistingRB.selectedProperty().set(true);
            }
        });

        if (myComboBoxItems.isEmpty())
        {
            hbox.getChildren().addAll(myNoneRB, myNewCatRB, myComboBox);
            myComboBox.setDisable(true);
        }
        else
        {
            hbox.getChildren().addAll(myNoneRB, myExistingRB, myNewCatRB, myComboBox);
        }

        getChildren().addAll(subCollectMessage, hbox);
        setStyle("-fx-padding: 10");
    }

    /**
     * Updates the values contained in the {@link #myComboBox }drop down menu.
     */
    private void updateComboBox()
    {
        List<String> names = New.list(myIconRegistry.getSubCategoiresForCollection(myImportProps.getCollectionName().get()));
        Collections.sort(names);
        myComboBoxItems = FXCollections.observableArrayList(names);
        getChildren().removeAll(getChildren());
        createPanel();
    }

    /**
     * Gets the category name.
     *
     * @return the category name
     */
    public String getSubCategory()
    {
        if (myNewCatRB.isSelected())
        {
            return myComboBox.getSelectionModel().getSelectedItem().toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Checks if is no category.
     *
     * @return true, if is no category
     */
    public boolean isNoCategory()
    {
        return myNoneRB.isSelected();
    }

    /**
     * Updates the value of the sub collection name in the model.
     */    
    public void updateSubCollectName()
    {
        myPanelModel.getImportProps().getSubCollectionName().set(myComboBox.getValue());
    }
}
