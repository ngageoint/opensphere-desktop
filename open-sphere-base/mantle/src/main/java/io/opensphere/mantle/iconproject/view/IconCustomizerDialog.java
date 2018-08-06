package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.IconCustomizerPane;

/** The window to customize icons. */
public class IconCustomizerDialog extends JFXDialog
{
    /** serial ID. */
    private static final long serialVersionUID = -8284546944940700345L;

    /** The logger for this class. */
    //private static final Logger LOGGER = Logger.getLogger(IconCustomizerDialog.class);

    /** The Icon Registry. */
    private final IconRegistry myIconRegistry;
    /** the current UI model. */
    private PanelModel myPanelModel;

    /**
     * Wraps the IconCustomizerPane into a java swing window.
     *
     * @param owner the parent window.
     * @param thePanelModel the model for the overall UI.
     */
    public IconCustomizerDialog(Window owner, PanelModel thePanelModel)
    {
        super(owner, "Customize an Icon");
        myPanelModel = thePanelModel;
        myIconRegistry = thePanelModel.getIconRegistry();
        IconCustomizerPane pane = new IconCustomizerPane(owner, thePanelModel);
        setMinimumSize(new Dimension(450, 550));
        setLocationRelativeTo(owner);
        setResizable(false);
        setFxNode(pane);
        setAcceptEar(() -> saveImage(pane.getFinalImage(), pane.getImageName(), pane.getSaveState(), pane.getIconRecord(),
                pane.getXPos(), pane.getYPos()));
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
                    String filename = icon.getImageURL().toString();
                    filename = filename.replace("file:", "");
                    filename = filename.replace("%20", " ");
                    ImageIO.write(bufImageRGB, FilenameUtils.getExtension(filename), new File(filename));
                }
                catch (IOException e)
                {
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
        }
        catch (IOException e)
        {
        }
        myPanelModel.getViewModel().getMainPanel().refresh();
    }
}
