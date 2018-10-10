package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
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
import io.opensphere.mantle.icon.impl.gui.IconBuilderPane;
import io.opensphere.mantle.iconproject.panels.MainPanel;

/** Creates the Icon Generation Window. */
@SuppressWarnings("serial")
public class IconProjGenDialog extends JFXDialog
{
    /** The logger for the IconProjGenDialog class. */
    private static final Logger LOGGER = Logger.getLogger(IconProjGenDialog.class);

    /** The current IconRegistry. */
    private final IconRegistry myIconRegistry;

    private final MainPanel myMainPanel;

    /**
     * Constructor.
     *
     * Packages icon generation into a swing pane for MIST.
     *
     * @param owner the calling window.
     * @param iconRegistry the current icon registry.
     */
    public IconProjGenDialog(Window owner, IconRegistry iconRegistry, MainPanel mainPanel)
    {
        super(owner, "Generate New Icon");
        myIconRegistry = iconRegistry;
        myMainPanel = mainPanel;

        IconBuilderPane pane = new IconBuilderPane(owner);
        setFxNode(pane);
        setMinimumSize(new Dimension(450, 550));
        setLocationRelativeTo(owner);
        setAcceptListener(() -> saveImage(pane.getFinalImage(), pane.getImageName()));
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
        }
        catch (IOException e)
        {
            Notify.error(e.getMessage());
            LOGGER.error("Failure to save newly generated icon.", e);
        }
        myMainPanel.refresh();
    }
}
