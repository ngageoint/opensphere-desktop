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
import io.opensphere.mantle.iconproject.impl.IconBuilderProjPane;

/** The component class for building icons. */
public class IconProjBuilderNewDialog extends JFXDialog
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(IconProjBuilderNewDialog.class);

    /** The Icon Registry. */
    private final IconRegistry myIconRegistry;

    /**
     * Constructor.
     *
     * @param owner the owner
     * @param iconRegistry the icon registry
     * @param mainPanel the main panel of the icon manager frame.
     */

    public IconProjBuilderNewDialog(Window owner, IconRegistry iconRegistry,IconRecord record2)
    {
        super(owner, "Build an Icon");
        myIconRegistry = iconRegistry;

        IconBuilderProjPane pane = new IconBuilderProjPane(owner, record2);
        setFxNode(pane);
        setMinimumSize(new Dimension(450, 600));

        setLocationRelativeTo(owner);
        setAcceptEar(() -> saveImage(pane.getFinalImage(), pane.getImageName()));
     //   setAcceptEar(() -> saveRotatedIcon(record);
    }

    /**
     * Saves a built image to the Icon Registry.
     *
     * @param image the image to save
     * @param name the image name
     */
    private void saveImage(BufferedImage image,String name)
    {
      
        try
        {
          //  BufferedImage image = ImageIO.read(record.getImageURL());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);

            URL imageURL = myIconRegistry.getIconCache().cacheIcon(outputStream.toByteArray(), name, true);
            IconProvider provider = new DefaultIconProvider(imageURL, IconRecord.USER_ADDED_COLLECTION, null, "User");

            myIconRegistry.addIcon(provider, this);

            // myChooserPanel.refreshFromRegistry(IconRecord.USER_ADDED_COLLECTION);

        }
        catch (IOException e)
        {
            Notify.error(e.getMessage());
            LOGGER.error(e, e);
        }
    }
    
    
    
}
