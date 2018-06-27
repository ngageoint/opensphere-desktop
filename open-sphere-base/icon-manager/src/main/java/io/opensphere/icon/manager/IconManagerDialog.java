package io.opensphere.icon.manager;

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

    @FXML
    private AnchorPane myTopMenuBar;

    @FXML
    private ButtonBar mySizeMenu;

    @FXML
    private Text mySizeLabel;

    @FXML
    private Button myShrinkButton;

    @FXML
    private Button myEnlargeButton;

    @FXML
    private HBox myViewToggle;

    @FXML
    private Text myViewLabel;

    @FXML
    private RadioButton myListView;

    @FXML
    private ToggleGroup ViewStyle;

    @FXML
    private RadioButton myGridView;

    @FXML
    private ButtonBar mySearchBar;

    @FXML
    private Text mySearchLabel;

    @FXML
    private AnchorPane myTreeView;

    @FXML
    private TreeView<?> myTreeList;

    @FXML
    private Button myAddIconButton;

    @FXML
    private Button myCustIconButton;

    @FXML
    private Button myGenIconButton;

    @FXML
    private AnchorPane myBottomMenuBar;

    @FXML
    private Button myCloseButton;

    @FXML
    private TextField myDataBar;

    @FXML
    private Text myNotifyText;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @FXML
    void ADD(MouseEvent event)
    {

    }

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
    void ENLARGE(MouseEvent event)
    {

    }

    @FXML
    void GENERATE(MouseEvent event)
    {

    }

    @FXML
    void GRIDV(MouseEvent event)
    {

    }

    @FXML
    void IMPORT(MouseEvent event)
    {
        // Button.setOnAction(loadFromFile(IconRecord.USER_ADDED_COLLECTION,
        // null));
    };

    @FXML
    void LISTV(MouseEvent event)
    {

    }

    @FXML
    void SHRINK(MouseEvent event)
    {

    }

    @FXML
    void handleFavoriteButtonAction(MouseEvent event)
    {
        myNotifyText.setText("Icon Added to Favorites");
    }

}
