package io.opensphere.icon.manager;

import java.io.FileInputStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class IconManagerFrame extends Application
{
    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource("IconManagerFrameTest.fxml"));
            Scene scene = new Scene(root, 720, 400);
            scene.getStylesheets().add(getClass().getResource("IconManagerFrameStyle.css").toExternalForm());
            primaryStage.setScene(scene);
            Image myWindowIcon = new Image(new FileInputStream("src/main/resources/Images/caci.jpg"));
            primaryStage.getIcons().add(myWindowIcon);
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
