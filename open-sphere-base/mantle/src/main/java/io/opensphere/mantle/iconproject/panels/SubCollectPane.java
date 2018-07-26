package io.opensphere.mantle.iconproject.panels;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

/**
 * A panel in which a user may select a subcategory, create a new subcategory,
 * or select no subcategories.
 */
public class SubCollectPane extends VBox
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The combo box from which an existing category is selected. This is
     * disabled if the user is entering a new category.
     */
    private final ComboBox<String> myExistingCatComboBox;

    /**
     * The radio button with which the user selects the mode of the category
     * panel to use existing categories. This button is in a group with
     * {@link #myNewCatRB}, {@link #myNoneRB} and optionally
     * {@link #mySubCatsFromDirNamesRB}, and the group is used to put the panel
     * in a mode in which the user may select an existing group, or create a new
     * group.
     */
    private final RadioButton myExistingCatRB;

    /**
     * The radio button with which the user selects the mode of the name panel
     * to create a new category. This button is in a group with
     * {@link #myExistingCatRB}, {@link #myNoneRB} and optionally
     * {@link #mySubCatsFromDirNamesRB}, and the group is used to put the panel
     * in a mode in which the user may select an existing group, or create a new
     * group.
     */
    private final RadioButton myNewCatRB;

    /**
     * The radio button with which the user selects the mode of the name panel
     * to not use a category. This button is in a group with
     * {@link #myExistingCatRB}, {@link #myNewCatRB} and optionally
     * {@link #mySubCatsFromDirNamesRB}, and the group is used to put the panel
     * in a mode in which the user may select an existing group, or create a new
     * group.
     */
    private final RadioButton myNoneRB;

    /**
     * The radio button with which the user selects the mode of the name panel
     * to create subcategories from directory names. This radio button may not
     * be instantiated, and is available if the constructor is called with a
     * value of <code>true</code> for its second argument. This button is in a
     * group with {@link #myExistingCatRB}, {@link #myNewCatRB}, and
     * {@link #myNoneRB}, and the group is used to put the panel in a mode in
     * which the user may select an existing group, or create a new group.
     */
    private RadioButton mySubCatsFromDirNamesRB;

    /**
     * The text field into which the user may enter a new category name. This
     * field is disabled if the user is selecting an existing category.
     */
    private final TextField myNewCatTF;

    private ObservableList<String> options;

    private ToggleGroup buttonGroup = new ToggleGroup();

    /**
     * Instantiates a new sub-category selection panel.
     *
     * @param categorySet the set of categories with which to populate the
     *            selection panel.
     * @param subCatsFromDirNames true if the user should be allowed to create
     *            subcategories from directory names, false otherwise (if
     *            <code>true</code>, this causes the instantiation of
     *            {@link #mySubCatsFromDirNamesRB}).
     */
    public SubCollectPane(Set<String> categorySet, boolean subCatsFromDirNames)
    {
        HBox hbox = new HBox();
        int numRows = 3 + (categorySet.isEmpty() ? 0 : 1) + (subCatsFromDirNames ? 1 : 0);
        System.out.print(numRows);
        // setLayout(new GridLayout(numRows, 1, 0, 6));

        List<String> names = New.list(categorySet);
        Collections.sort(names);
        options = FXCollections.observableArrayList(names);

        Label CollectionText = new Label("Do you want to add a sub-category to your icon?:");
        CollectionText.setFont(Font.font(CollectionText.getFont().getFamily(), FontPosture.ITALIC, 11));
        CollectionText.setContentDisplay(ContentDisplay.BOTTOM);

        myNoneRB = new RadioButton("No Sub-category");
        myNoneRB.setOnMouseClicked(e -> updateSelectedCategory(false, false));
        myNoneRB.setToggleGroup(buttonGroup);

        myExistingCatRB = new RadioButton("Existing");
        myExistingCatRB.setOnMouseClicked(event -> updateSelectedCategory(true, false));
        myExistingCatRB.setToggleGroup(buttonGroup);

        myExistingCatComboBox = new ComboBox<>(options);
        HBox existingPnl = new HBox();
        existingPnl.getChildren().addAll(myExistingCatRB, myExistingCatComboBox);
        if (!categorySet.isEmpty())
        {
            // add(existingPnl);
        }

        myNewCatRB = new RadioButton("New");
        myNewCatRB.setOnMouseClicked(e -> updateSelectedCategory(false, true));
        myNewCatRB.setToggleGroup(buttonGroup);

        myNewCatTF = new TextField();
        myNewCatTF.setDisable(true);

        HBox newPnl = new HBox();
        newPnl.getChildren().addAll(myNewCatRB, myNewCatTF);

        if (subCatsFromDirNames)
        {
            mySubCatsFromDirNamesRB = new RadioButton("Create sub-categories from folder names.");
            mySubCatsFromDirNamesRB.setTooltip(new Tooltip("Search for all sub-folders of existing folders"
                    + " and add found icons with the folder name as the sub-category."));
            mySubCatsFromDirNamesRB.setOnMouseClicked(e -> updateSelectedCategory(false, false));
            mySubCatsFromDirNamesRB.setToggleGroup(buttonGroup);
            // add(mySubCatsFromDirNamesRB);
        }
        hbox.getChildren().addAll(myNoneRB, myNewCatRB, myNewCatTF);
        getChildren().addAll(CollectionText, hbox);
    }

    /**
     * Updates the enabled state of the "existing" combo box, and the "new" text
     * field using the supplied parameters.
     *
     * @param pEnableComboBox true if the "existing" combo box should be
     *            enabled, false otherwise.
     * @param pEnableTextField true if the "new" text field should be enabled,
     *            false otherwise.
     */
    protected void updateSelectedCategory(boolean pEnableComboBox, boolean pEnableTextField)
    {
        myExistingCatComboBox.setDisable(!pEnableComboBox);
        myNewCatTF.setDisable(!pEnableTextField);
    }

    /**
     * Gets the category name.
     *
     * @return the category name
     */
    public String getCategory()
    {
        if (myNewCatRB.isSelected())
        {
            return myNewCatTF.getText();
        }
        else if (myExistingCatRB.isSelected())
        {
            return myExistingCatComboBox.getSelectionModel().getSelectedItem();
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
     * Checks if is sub cats from dir names.
     *
     * @return true, if is sub cats from dir names
     */
    public boolean isSubCatsFromDirNames()
    {
        return mySubCatsFromDirNamesRB != null && mySubCatsFromDirNamesRB.isSelected();
    }
}
