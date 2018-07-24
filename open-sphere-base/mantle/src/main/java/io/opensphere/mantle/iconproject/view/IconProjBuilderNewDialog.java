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
import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.panels.IconBuilderProjPane;

/** The component class for building icons. */
public class IconProjBuilderNewDialog extends JFXDialog
{
    /** serial ID. */
    private static final long serialVersionUID = -8284546944940700345L;

    /** The logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(IconProjBuilderNewDialog.class);

    /** The Icon Registry. */
    private final IconRegistry myIconRegistry;

    /**
     * Constructor.
     *
     * @param owner the parent window.
     * @param iconRegistry the icon registry.
     * @param iconRecord the current selected icon.
     */

    public IconProjBuilderNewDialog(Window owner, IconRegistry iconRegistry, IconRecord iconRecord)
    {
        super(owner, "Build an Icon");
        myIconRegistry = iconRegistry;
        IconBuilderProjPane pane = new IconBuilderProjPane(owner, iconRecord);
        setFxNode(pane);
        setMinimumSize(new Dimension(450, 550));
        setLocationRelativeTo(owner);
        setAcceptEar(() -> saveImage(pane.getFinalImage(), pane.getImageName(), pane.getSaveState(), pane.getIconRecord(),
                pane.getXPos(), pane.getYPos()));
    }

    /**
     * Saves a built image to the Icon Registry.
     *
     * @param snapshot the edited icon to save.
     * @param name the image name.
     * @param savestate whether or not to overwrite the existing file.
     * @param Icon the icon record.
     * @param double XPos the X translation coordinate.
     * @param double YPos the Y translation coordinate.
     */
    private void saveImage(WritableImage snapshot, String name, boolean savestate, IconRecord Icon, int XPos, int YPos)
    {
        BufferedImage image = null;
        image = SwingFXUtils.fromFXImage(snapshot, image);

        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);

            if (savestate)
            {
                BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(snapshot, null);
                BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(),BufferedImage.TRANSLUCENT);

                Graphics2D graphics = bufImageRGB.createGraphics();
                graphics.drawImage(bufImageARGB, XPos, YPos, null);
                try
                {
                    String filename = Icon.getImageURL().toString();
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
                URL imageURL = myIconRegistry.getIconCache().cacheIcon(outputStream.toByteArray(), name, true);
                IconProvider provider = new DefaultIconProvider(imageURL, IconRecord.USER_ADDED_COLLECTION, null, "User");
                myIconRegistry.addIcon(provider, this);
            }
        }
        catch (IOException e)
        {
            Notify.error(e.getMessage());
            LOGGER.error(e, e);
        }
    }
}
