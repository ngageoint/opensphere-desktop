package io.opensphere.icon.manager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import de.micromata.opengis.kml.v_2_2_0.Data;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import io.opensphere.icon.manager.IconManagerBuilder;

/**
 * This is the main control for the Icon Manager GUI. All button actions and
 * styling are contained in class.
 */
public class IconManagerFrame extends IconManagerBuilder
{

    /* private TreeItem<String> myTreeRoot;
     * 
     * private TreeItem<String> myFavorites;
     * 
     * private TreeItem<String> myUserAdded;
     * 
     * private TreeItem<String> myGoogleEarth;
     * 
     * private TreeItem<String> myMilStd;
     * 
     * private TreeView<String> myTreeListTemp;
     * 
     * TreeView<String> myTree; */

    private Scene myScene;

    private double initialY;

    private double initialX;

    public static void main(String[] args)
    {
        launch(args);

    }

    public void start(Stage myIconManagerInterFace) throws FileNotFoundException
    {

        AnchorPane myMainAnchorPane = createIconManagerPane();

        myScene = new Scene(myMainAnchorPane, 720, 400);
        myScene.getStylesheets().add(getClass().getResource("iconmanager.css").toExternalForm());
        myIconManagerInterFace.setScene(myScene);
        myIconManagerInterFace.setMinHeight(400);
        myIconManagerInterFace.setMinWidth(720);
        Image myWindowIcon = new Image(new FileInputStream("src/main/resources/Images/caci.jpg"));
        myIconManagerInterFace.getIcons().add(myWindowIcon);
        myIconManagerInterFace.setTitle("Icon Manager");
     //   myIconManagerInterFace.initStyle(StageStyle.UNDECORATED);
        myIconManagerInterFace.show();
        ResizeHelper.addResizeListener(myIconManagerInterFace);
    }

    public TreeItem<String> createBranch(String branchName, TreeItem<String> rootName)
    {
        TreeItem<String> localBranch = new TreeItem<>(branchName);
        localBranch.setExpanded(true);

        rootName.getChildren().add(localBranch);
        return (localBranch);
    }

    @Override
    public void shrink(MouseEvent mouseEvent)
    {

    }

    @Override
    public void enlarge(MouseEvent mouseEvent)
    {

    }

    @Override
    public void LISTV(MouseEvent mouseEvent)
    {

    }

    @Override
    public void GRIDV(MouseEvent mouseEvent)
    {

    }

    @Override
    public void ADD(MouseEvent mouseEvent)
    {

    }

    @Override
    public void handleAddIconButtonAction(MouseEvent mouseEvent)
    {

    }

    @Override
    public void CUSTOMIZE(MouseEvent mouseEvent)
    {

    }

    @Override
    public void GENERATE(MouseEvent mouseEvent)
    {

    }

    @Override
    public void Close(MouseEvent mouseEvent)
    {
        Stage myCurrentStage = (Stage)myCloseButton.getScene().getWindow();
        myCurrentStage.close();

    }
}
