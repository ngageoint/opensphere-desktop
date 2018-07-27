package io.opensphere.mantle.iconproject.panels;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * A panel in which a user may select an existing collection or create a new
 * one.
 */
public class AddIconPane extends BorderPane
{
    /**
     * serialVersionUID.
     */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    private PanelModel myPanelModel = new PanelModel();

    /**
     * Instantiates a new collection name panel.
     *
     * @param collectionNameSet The set of collection names to display in the
     *            panel.
     */
    public AddIconPane(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;

        setTop(createCollection());
        setCenter(createSubCollection());
        System.out.println("AddIconPane  gg Parent is: " + getScene());
    }

    private VBox createCollection()
    {
        CollectNamesPane CollectionNamePane = new CollectNamesPane(myPanelModel);
        return CollectionNamePane;
    }

    private VBox createSubCollection()
    {

        SubCollectPane bottomPane = new SubCollectPane(myPanelModel);
        return bottomPane;
    }

//    private static void initAndShowGUI() {
//        // This method is invoked on the EDT thread
//        JFrame frame = new JFrame("Swing and JavaFX");
//        final JFXPanel fxPanel = new JFXPanel();
//        frame.add(fxPanel);
//        frame.setSize(300, 200);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                initFX(fxPanel);
//            }
//       });
//    }
//    private static void initFX(JFXPanel fxPanel) {
//        // This method is invoked on the JavaFX thread
//        Scene scene = createScene();
//        
//        FileChooser test = new FileChooser();
//        test.showOpenDialog(scene.getWindow());
//        
//        fxPanel.setScene(scene);
//    }
//    private static Scene createScene() {
//        Group  root  =  new  Group();
//        Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
//        Text  text  =  new  Text();
//        
//        text.setX(40);
//        text.setY(100);
//        text.setFont(new Font(25));
//        text.setText("Welcome JavaFX!");
//
//        root.getChildren().add(text);
//
//        return (scene);
//    }
    
    

}
