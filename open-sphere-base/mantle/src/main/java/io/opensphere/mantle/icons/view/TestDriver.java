package io.opensphere.mantle.icons.view;

import io.opensphere.core.util.fx.FXUtilities;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 */
public class TestDriver extends Application
{
    /**
     * {@inheritDoc}
     *
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        SelectIconEditor root = new SelectIconEditor();

        Scene scene = new Scene(root, 800, 600);

        FXUtilities.addDesktopStyle(scene);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }

}
