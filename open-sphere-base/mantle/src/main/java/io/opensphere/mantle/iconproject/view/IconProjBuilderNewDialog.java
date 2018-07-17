package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.impl.IconBuilderProjPane;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/** The component class for building icons. */
public class IconProjBuilderNewDialog extends JFXDialog
{
    /** serial ID.   */
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
        //System.out.println(myIconRegistry.getAllAssignedElementIds().toString());
        IconBuilderProjPane pane = new IconBuilderProjPane(owner, iconRecord);
        setFxNode(pane);
        setMinimumSize(new Dimension(450, 550));

        setLocationRelativeTo(owner);
        setAcceptEar(() -> saveImage(pane.getFinalImage(), pane.getImageName()));
    }

    /**
     * Saves a built image to the Icon Registry.
     *
     * @param snapshot the image to save
     * @param name the image name
     */
    private void saveImage(WritableImage snapshot, String name)
    {
        BufferedImage image = null;
        image = javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, image);

        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            URL imageURL = myIconRegistry.getIconCache().cacheIcon(outputStream.toByteArray(), name, true);
            System.out.println(imageURL);
            IconProvider provider = new DefaultIconProvider(imageURL, IconRecord.USER_ADDED_COLLECTION, null, "User");

            myIconRegistry.addIcon(provider, this);
        }
        catch (IOException e)
        {
            Notify.error(e.getMessage());
            LOGGER.error(e, e);
        }
    }
}
