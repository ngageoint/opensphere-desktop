package io.opensphere.icon.manager.view;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class myStage
{

    /*
    public static class MyStackPane extends StackPane{
        public MyStackPane() {
            getChildren().add(new Label("Hello World"));
        }
    }

    public static class MyScene extends Scene {
        public MyScene() {
            super(new MyStackPane(), 250, 75);
        }
    }
*/
    public static class FinalStage extends Stage {
        public FinalStage() {
            StackPane test = new StackPane();
            test.getChildren().add(new Label("Hello World"));

            Scene theScene = new Scene(test);
            setScene(theScene);
        }
    }
}
