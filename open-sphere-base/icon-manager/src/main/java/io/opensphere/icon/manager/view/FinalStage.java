package io.opensphere.icon.manager.view;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FinalStage extends Stage
{

    public FinalStage()
    {
        StackPane test = new StackPane();
        test.getChildren().add(new Label("Hello World"));

        Scene theScene = new Scene(test);
        setScene(theScene);
    }

    public FinalStage(String string)
    {
        StackPane test = new StackPane();
        test.getChildren().add(new Label(string));      
        Scene theScene = new Scene(test);
        setScene(theScene);
        setTitle("rawr");
        setSize(500.,500.);
    }

    private void setSize(double d, double e)
    {
        setMinWidth(d);
        setMinHeight(e);
    }
}
