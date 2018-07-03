package io.opensphere.icon.manager;

import java.awt.Component;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import io.opensphere.core.Toolbox;
import io.opensphere.icon.manager.IconManagerBuilder;

/**
 * This is the main control for the Icon Manager GUI. All button actions and
 * styling are contained in class.
 */
public class IconManagerFrame extends IconManagerBuilder
{

    private Scene myScene;
    private Stage myIconManagerInterFace;

    public static void main(String[] args)
    {
        launch(args);

    }
    public IconManagerFrame(Component owner, boolean modal, String message, Toolbox tb) throws FileNotFoundException
    {
        super();
        AnchorPane myMainAnchorPane = createIconManagerPane();

        myScene = new Scene(myMainAnchorPane, 720, 400);
        myScene.getStylesheets().add(getClass().getResource("iconmanager.css").toExternalForm());
        myIconManagerInterFace.setScene(myScene);
        myIconManagerInterFace.setMinHeight(400);
        myIconManagerInterFace.setMinWidth(720);
        Image myWindowIcon = new Image(new FileInputStream("src/main/resources/Images/caci.jpg"));
        myIconManagerInterFace.getIcons().add(myWindowIcon);
        myIconManagerInterFace.setTitle("Icon Manager");
        // myIconManagerInterFace.initStyle(StageStyle.UNDECORATED);
        myIconManagerInterFace.show();
        ResizeHelper.addResizeListener(myIconManagerInterFace);
    }
    
    /*
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
        // myIconManagerInterFace.initStyle(StageStyle.UNDECORATED);
        myIconManagerInterFace.show();
        ResizeHelper.addResizeListener(myIconManagerInterFace);
    }
*/
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
    @Override
    public void start(Stage arg0) throws Exception
    {
        // TODO Auto-generated method stub
        
    }
}
