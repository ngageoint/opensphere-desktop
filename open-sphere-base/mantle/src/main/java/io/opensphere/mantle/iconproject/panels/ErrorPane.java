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

/** @deprecated Used to create a simple error message. */
public class ErrorPane
{
    /**
     * Creates a window containing a self closing popup message.
     *
     * @param timer the time before the dialog closes itself.
     * @param message the text to be displayed inside the window.
     * @param title the text for the top bar.
     * @param panelModel the model used
     * @return the JFX Dialog.
     */
    public JFXDialog createErrorPane(int timer, String message, String title, PanelModel panelModel)
    {
        JFXDialog messageWindow = new JFXDialog(panelModel.getOwner(), title, true);
        if (timer != 0)
        {
            ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
            s.schedule(() ->
            {
                messageWindow.setVisible(false);
                messageWindow.dispose();
            }, timer, TimeUnit.SECONDS);
        }
        messageWindow.setSize(new Dimension(400, 170));
        messageWindow.setLocationRelativeTo(panelModel.getOwner());
        messageWindow.setResizable(false);
        BorderPane thePane = errorMessagePane(message);

        messageWindow.setFxNode(thePane);
        messageWindow.setVisible(true);
        return messageWindow;
    }

    /**
     * Creates the image and message text pane.
     *
     * @param message the text to be displayed.
     * @return a borderpane containing the image and message for the window.
     */
    public BorderPane errorMessagePane(String message)
    {
        BorderPane messagePane = new BorderPane();
        Image image = new Image("images/warns.png");
        ImageView errorIcon = new ImageView(image);
        errorIcon.setFitWidth(75);
        errorIcon.setFitHeight(75);
        BorderPane.setAlignment(errorIcon, Pos.CENTER_LEFT);
        messagePane.setLeft(errorIcon);

        Text displayMessage = new LabelMaker(message);
        displayMessage.setFont(Font.font(displayMessage.getFont().getFamily(), FontPosture.ITALIC, 18));
        final double wrapWidth = 200.;
        displayMessage.setWrappingWidth(wrapWidth);
        BorderPane.setAlignment(displayMessage, Pos.CENTER);
        messagePane.setCenter(displayMessage);
        return messagePane;
    }
}
