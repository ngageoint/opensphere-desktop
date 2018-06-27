package io.opensphere.icon.manager;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.gui.IconBuilderDialog;
import io.opensphere.mantle.icon.impl.gui.IconChooserDialog;
import io.opensphere.mantle.icon.impl.gui.IconChooserPanel;
import io.opensphere.mantle.util.MantleToolboxUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class IconManagerDialog
{
    /** The AnchorPane myTopMenuBar; Top window of GUI */
    @FXML
    private AnchorPane myTopMenuBar;

    /** The ButtonBar mySizeMenu; Top Right Buttons of GUI */
    @FXML
    private ButtonBar mySizeMenu;

    /** The Text mySizeLabel; The centered text label */
    @FXML
    private Text mySizeLabel;

    /** The Button myShrinkButton; centered minus sign */
    @FXML
    private Button myShrinkButton;

    /** The Button myEnlargeButton; centered plus sign */
    @FXML
    private Button myEnlargeButton;

    /** The Hbox myViewToggle; holds toggle buttons List/Grid */
    @FXML
    private HBox myViewToggle;

    /* The Text myViewLabel; labels view buttons */
    @FXML
    private Text myViewLabel;

    /* The RadioButton myListView; switch for list view */
    @FXML
    private RadioButton myListView;

    /* The RadioButton myGridView; switch for grid view */
    @FXML
    private RadioButton myGridView;

    /* The ToggleGroup ViewStyle; contains the two buttons */
    @FXML
    private ToggleGroup ViewStyle;

    /* The ButtonBar mySearchBar; contains search functions*/
    @FXML
    private ButtonBar mySearchBar;

    /* The ButtonBar mySearchLabel; contains search label*/
    @FXML
    private Text mySearchLabel;

    /* The AnchorPane myTreeView; contains Tree features*/
    @FXML
    private AnchorPane myTreeView;
    
    /* The AnchorPane myTreeView; contains the file tree*/
    @FXML
    private TreeView<?> myTreeList;

    /** The add icons from file button. */
    @FXML
    private Button myAddIconButton;

    /** The customize icon button. */
    @FXML
    private Button myCustIconButton;

    /** The generate new icon button. */
    @FXML
    private Button myGenIconButton;

    /** The bottom bar interface. */
    @FXML
    private AnchorPane myBottomMenuBar;

    /** The close window button in the bottom right. */
    @FXML
    private Button myCloseButton;

    /** The bottom center notification bar. */
    @FXML
    private TextField myDataBar;

    /** The bottom left notification text. */
    @FXML
    private Text myNotifyText;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     
     */
    private boolean myChoice;

    @FXML
    void ADD(MouseEvent event)
    {

    }

    /** The Busy label. */
    @FXML
    void CUSTOMIZE(MouseEvent event)
    {

    }

    @FXML
    void Close(MouseEvent event)
    {
        // get a handle to the stage
        Stage stage = (Stage)myCloseButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void shrink(MouseEvent event)
    {
        myChoice = false;
        setIconSize(myChoice);
    }

    @FXML
    void enlarge(MouseEvent event)
    {
        myChoice = true;
        setIconSize(myChoice);
    }

    @FXML
    void GRIDV(MouseEvent event)
    {

    }

    @FXML
    void IMPORT(MouseEvent event)
    {
        // IconChooserPanel.loadFromFile(IconRecord.USER_ADDED_COLLECTION, null)
        // Button.setOnAction(loadFromFile(IconRecord.USER_ADDED_COLLECTION,
        // null));
    }

    @FXML
    void LISTV(MouseEvent event)
    {

    }

    @FXML
    void SHRINK(MouseEvent event)
    {

    }

    /**
     * Handles the "Add Icon from File" button event
     * @param event
     */
    @FXML
    void GENERATE(MouseEvent event)
    {

    }

    @FXML
    void handleAddIconButtonAction(MouseEvent event)
    {
        // myNotifyText.setText("Icon Added to Favorites");
        // loadFromFile(IconRecord.USER_ADDED_COLLECTION, null);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void setIconSize(boolean myChoice)
    {
        if (myChoice)
        {
            System.out.println("you enlarged Icons");
        }
        else
        {
            System.out.println("you shrank icons");
        }

    }
}
