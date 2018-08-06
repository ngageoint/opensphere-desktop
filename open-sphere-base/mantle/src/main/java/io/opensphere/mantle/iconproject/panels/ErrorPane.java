package io.opensphere.mantle.iconproject.panels;

import java.awt.Dimension;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

/** Used to create a simple error message. */
public class ErrorPane
{
    public JFXDialog createErrorPane(int timer, String theMessage, String theTitle, PanelModel thePanelModel)
    {
        JFXDialog test = new JFXDialog(thePanelModel.getOwner(), theTitle, true);

        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.schedule(new Runnable()
        {
            public void run()
            {
                test.setVisible(false); // should be invoked on the EDT
                test.dispose();
            }
        }, timer, TimeUnit.SECONDS);

        test.setMinimumSize(new Dimension(250, 250));
        test.setLocationRelativeTo(thePanelModel.getOwner());

        BorderPane test2 = ErrorMessagePane(theMessage);

        test.setFxNode(test2);
        test.setVisible(true);
        return test;
    }

    public BorderPane ErrorMessagePane(String theMessage)
    {
//        BorderPane messagePane = new BorderPane();
//        ImageView WarnImg = new ImageView(null);
//        messagePane.setLeft(WarnImg);
//        messagePane.setRight(value);
//        
        return null;
    }

}
