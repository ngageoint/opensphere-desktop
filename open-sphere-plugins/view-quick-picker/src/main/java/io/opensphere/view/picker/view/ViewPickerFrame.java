package io.opensphere.view.picker.view;

import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.view.picker.controller.ViewPickerController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

/** The frame in which the view picker is rendered. */
public class ViewPickerFrame extends AbstractInternalFrame
{
    /** The unique identifier used for serialization. */
    private static final long serialVersionUID = -7311805714568237979L;

    /** The controller managing interactions with the view picker. */
    private final ViewPickerController myController;

    /** The Swing panel on which JavaFX components are rendered. */
    private JFXPanel myPanel;

    /**
     * Creates a new frame with the supplied parameters.
     * 
     * @param controller the controller managing interactions with the view
     *            picker.
     */
    public ViewPickerFrame(ViewPickerController controller)
    {
        myController = controller;

        FXUtilities.runOnFXThread(this::createView);

        setSize(75, 450);
        setPreferredSize(getSize());
        setMinimumSize(getSize());

        setTitle("Views");
        setOpaque(false);
        setIconifiable(false);
        setClosable(false);
        setPopable(false);
        setResizable(false);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    /** Creates the view and adds it to the frame. */
    private void createView()
    {
        assert Platform.isFxApplicationThread();

        myPanel = new JFXPanel();

        myPanel.setScene(FXUtilities.addDesktopStyle(new Scene(myController.getPanel())));
        setContentPane(myPanel);
    }
}
