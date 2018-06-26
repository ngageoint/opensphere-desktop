package application;

import java.io.FileInputStream;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource("IconManagerFrameTest.fxml"));
            Scene scene = new Scene(root, 720, 400);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            Image image1 = new Image(new FileInputStream("source/Images/windows.png"));
            primaryStage.getIcons().add(image1);
            primaryStage.setTitle("Icon Manager");
            primaryStage.setMinHeight(400);
            primaryStage.setMinWidth(720);
            primaryStage.show();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
