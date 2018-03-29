package io.opensphere.mantle.icon.impl.gui;

import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;

/**
 *
 */
public class IconBuilderDialog extends JFXDialog
{

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(IconBuilderDialog.class);

    /** The Icon Registry. */
    private final IconRegistry myIconRegistry;

    /** The panel this dialog spawned from. */
    private final IconChooserPanel myChooserPanel;

    /**
     * Constructor.
     *
     * @param owner the owner
     * @param iconRegistry the icon registry
     * @param chooserPanel the icon chooser panel
     */
    public IconBuilderDialog(Window owner, IconRegistry iconRegistry, IconChooserPanel chooserPanel)
    {
        super(owner, "Build an Icon");
        myIconRegistry = iconRegistry;
        myChooserPanel = chooserPanel;
        IconBuilderPane pane = new IconBuilderPane();
        setFxNode(pane);

        setSize(400, 600);

        setLocationRelativeTo(owner);
        setAcceptEar(() -> saveImage(pane.getFinalImage(), pane.getImageName()));
    }

    /**
     * Saves a built image to the Icon Registry.
     *
     * @param image the image to save
     * @param name the image name
     */
    private void saveImage(BufferedImage image, String name)
    {
        if (image == null || name == null)
        {
            return;
        }

        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);

            URL imageURL = myIconRegistry.getIconCache().cacheIcon(outputStream.toByteArray(), name, true);
            IconProvider provider = new DefaultIconProvider(imageURL, IconRecord.USER_ADDED_COLLECTION, null, "User");

            myIconRegistry.addIcon(provider, this);
            myChooserPanel.refreshFromRegistry(IconRecord.USER_ADDED_COLLECTION);
        }
        catch (IOException e)
        {
            Notify.error(e.getMessage());
            LOGGER.error(e, e);
        }
    }
}
