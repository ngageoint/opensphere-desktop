package io.opensphere.core.util.fx;

import java.awt.Window;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;

import javax.swing.JDialog;

/** Swing dialog that contains a JavaFX ButtonPane. */
public abstract class ButtonDialog extends JDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The FX panel. */
    private final JFXPanel myFxPanel;

    /** The button click response. */
    private volatile ButtonData myResponse;

    /**
     * Constructor.
     *
     * @param owner The owner
     * @param title The title
     */
    public ButtonDialog(Window owner, String title)
    {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        myFxPanel = new JFXPanel();
        add(myFxPanel);
    }

    /**
     * Initializes the dialog.
     */
    public void initialize()
    {
        Platform.runLater(() -> myFxPanel.setScene(createScene()));
    }

    /**
     * Gets the response.
     *
     * @return the response
     */
    public ButtonData getResponse()
    {
        return myResponse;
    }

    /**
     * Creates the button pane.
     *
     * @return the button pane
     */
    protected abstract ButtonPane newButtonPane();

    /**
     * Handles a button click.
     *
     * @param button the button clicked
     */
    protected void handleButtonClick(ButtonData button)
    {
        assert Platform.isFxApplicationThread();

        myResponse = button;

        // OK to call off the swing thread
        dispose();
    }

    /**
     * Creates the scene.
     *
     * @return the scene
     */
    private Scene createScene()
    {
        assert Platform.isFxApplicationThread();

        ButtonPane pane = newButtonPane();
        pane.addButtonClickListener(this::handleButtonClick);

        return FXUtilities.addDesktopStyle(new Scene(pane));
    }
}
