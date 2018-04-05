package io.opensphere.osh.results.video;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.io.ByteArrayInputStream;

import javax.swing.JDialog;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** A video window. */
public class VideoWindow extends JDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The image view. */
    private ImageView myImageView;

    /** Whether we're handling the first image. */
    private boolean myFirstTime = true;

    /**
     * Constructor.
     *
     * @param owner the owner
     * @param dataType the data type
     */
    public VideoWindow(Window owner, DataTypeInfo dataType)
    {
        super(owner, dataType.getDisplayName(), ModalityType.MODELESS);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setMinimumSize(new Dimension(300, 300));
        setSize(600, 600);
        setLocationRelativeTo(owner);
        JFXPanel fxPanel = new JFXPanel();
        add(fxPanel);
        FXUtilities.runOnFXThreadAndWait(() -> initFx(fxPanel));
    }

    /**
     * Sets the image bytes.
     *
     * @param bytes the image bytes
     */
    public void setImageBytes(byte[] bytes)
    {
        FXUtilities.runOnFXThreadAndWait(() ->
        {
            Image image = new Image(new ByteArrayInputStream(bytes));
            myImageView.setImage(image);
            if (myFirstTime)
            {
                myFirstTime = false;
                int width = (int)image.getWidth();
                int height = (int)image.getHeight();
                EventQueue.invokeLater(() -> setSize(width, height));
            }
        });
    }

    /**
     * Initializes the JavaFX stuff.
     *
     * @param fxPanel the JFXPanel
     */
    private void initFx(JFXPanel fxPanel)
    {
        myImageView = new ImageView();
        Scene scene = new Scene(new Group(myImageView));
        fxPanel.setScene(scene);

        myImageView.fitWidthProperty().bind(scene.widthProperty());
        myImageView.fitHeightProperty().bind(scene.heightProperty());
    }
}
