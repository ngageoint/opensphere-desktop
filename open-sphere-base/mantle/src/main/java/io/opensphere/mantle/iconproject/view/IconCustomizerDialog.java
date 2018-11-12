package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.IconCustomizerPane;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

/** The window to customize icons. */
public class IconCustomizerDialog extends JFXDialog
{
    /** Serial ID. */
    private static final long serialVersionUID = -8284546944940700345L;

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(IconCustomizerDialog.class);

    /** The Icon Registry. */
    private final IconRegistry myIconRegistry;

    /** the current UI model. */
    private final PanelModel myPanelModel;

    /**
     * Wraps the IconCustomizerPane into a java swing window.
     *
     * @param owner the parent window.
     * @param panelModel the model for the overall UI.
     */
    public IconCustomizerDialog(Window owner, PanelModel panelModel)
    {
        super(owner, "Customize an Icon");
        myPanelModel = panelModel;
        myIconRegistry = panelModel.getIconRegistry();
        IconCustomizerPane pane = new IconCustomizerPane(owner, panelModel);
        setMinimumSize(new Dimension(450, 550));
        setLocationRelativeTo(owner);
        setResizable(false);
        setFxNode(pane);
        setAcceptListener(() -> saveImage(pane.getFinalImage(), pane.getImageName(), pane.getOverwriteIcon(), pane.getIconRecord(),
                pane.getXPosition(), pane.getYPosition()));
    }

    /**
     * Saves a built image to the Icon Registry.
     *
     * @param snapshot the edited icon to save.
     * @param name the image name.
     * @param savestate whether or not to overwrite the existing file.
     * @param icon the icon record.
     * @param xPos the X translation coordinate.
     * @param yPos the Y translation coordinate.
     */
    private void saveImage(WritableImage snapshot, String name, boolean savestate, IconRecord icon, int xPos, int yPos)
    {
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(snapshot, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(),
                BufferedImage.TRANSLUCENT);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, xPos, yPos, null);
        try
        {
            if (savestate)
            {
                try
                {
                    String filename = icon.imageURLProperty().get().toString();
                    filename = filename.replace("file:", "");
                    filename = filename.replace("%20", " ");
                    ImageIO.write(bufImageRGB, FilenameUtils.getExtension(filename), new File(filename));
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to write image.", e);
                }
                graphics.dispose();
            }
            else
            {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(bufImageRGB, "png", outputStream);
                URL imageURL = myIconRegistry.getIconCache().cacheIcon(outputStream.toByteArray(), name, true);
                IconProvider provider = new DefaultIconProvider(imageURL, IconRecord.USER_ADDED_COLLECTION, null, "User");
                myIconRegistry.addIcon(provider, this);
            }
//            myPanelModel.getViewModel().getMainPanel().refresh();
        }
        catch (IOException e)
        {
        	LOGGER.error("Failed to write image.", e);
        }
    }
}
