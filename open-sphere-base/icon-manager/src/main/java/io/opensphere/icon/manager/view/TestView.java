package io.opensphere.icon.manager.view;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class TestView extends Stage

{

    public void TestView()

    {

        setTitle("Welcome to JavaFX!");

        System.out.println(
                "you called it; yay!ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss!!");

        setMinWidth(400);

        setMinHeight(400);

        AnchorPane root = new AnchorPane();

        Scene scene = new Scene(root, 400, 400);
        show();
        setScene(scene);





    }

}

