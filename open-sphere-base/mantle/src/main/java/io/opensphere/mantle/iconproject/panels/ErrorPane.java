package io.opensphere.mantle.iconproject.panels;

import java.awt.Dimension;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.iconproject.impl.LabelMaker;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;

/** Used to create a simple error message. */
public class ErrorPane
{
    /**
     * Creates a window containing a self closing popup message.
     *
     * @param timer the time before the dialog closes itself.
     * @param theMessage the text to be displayed inside the window.
     * @param theTitle the text for the top bar.
     * @param thePanelModel the model used
     * @return messageWindow the JFX Dialog.
     */
    public JFXDialog createErrorPane(int timer, String theMessage, String theTitle, PanelModel thePanelModel)
    {
        JFXDialog messageWindow = new JFXDialog(thePanelModel.getOwner(), theTitle, true);
        if (timer != 0)
        {
            ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
            s.schedule(new Runnable()
            {
                public void run()
                {
                    messageWindow.setVisible(false);
                    messageWindow.dispose();
                }
            }, timer, TimeUnit.SECONDS);
        }
        messageWindow.setSize(new Dimension(400, 170));
        messageWindow.setLocationRelativeTo(thePanelModel.getOwner());
        messageWindow.setResizable(false);
        BorderPane thePane = errorMessagePane(theMessage);

        messageWindow.setFxNode(thePane);
        messageWindow.setVisible(true);
        return messageWindow;
    }

    /**
     * Creates the image and message text pane.
     *
     * @param theMessage the text to be displayed.
     * @return messagePane a borderpane containing the image and message for the
     *         window.
     */
    public BorderPane errorMessagePane(String theMessage)
    {
        System.out.println("code updates!!!!!!!!!!!!!!!!!!!");
        BorderPane messagePane = new BorderPane();
        Image image = new Image("images/warns.png");
        ImageView errorIcon = new ImageView(image);
        errorIcon.setFitWidth(75);
        errorIcon.setFitHeight(75);
        BorderPane.setAlignment(errorIcon, Pos.CENTER_LEFT);
        messagePane.setLeft(errorIcon);

        Text displayMessage = new LabelMaker(theMessage);
        displayMessage.setFont(Font.font(displayMessage.getFont().getFamily(), FontPosture.ITALIC, 18));
        final double wrapWidth = 200.;
        displayMessage.setWrappingWidth(wrapWidth);
        BorderPane.setAlignment(displayMessage, Pos.CENTER);
        messagePane.setCenter(displayMessage);
        return messagePane;
    }
}
