package io.opensphere.mantle.icon.impl.gui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.image.processor.RotateImageProcessor;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;

/** Icon rotation Swing dialog (with JavaFX content). */
public class IconRotationDialog extends JFXDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(IconRotationDialog.class);

    /** The icon registry. */
    private final IconRegistry myIconRegistry;

    /** The icon chooser panel. */
    private final IconChooserPanel myChooserPanel;

    /**
     * Constructor.
     *
     * @param owner the owner
     * @param record the icon record
     * @param iconRegistry the icon registry
     * @param chooserPanel the icon chooser panel
     */
    public IconRotationDialog(Window owner, IconRecord record, IconRegistry iconRegistry, IconChooserPanel chooserPanel)
    {
        super(owner, "Rotate Icon");
        myIconRegistry = iconRegistry;
        myChooserPanel = chooserPanel;
        setMinimumSize(new Dimension(310, 200));
        IconRotationPane pane = new IconRotationPane(record);
        setFxNode(pane);
        setAcceptListener(() -> saveRotatedIcon(record, pane.getRotation()));
        try
        {
            BufferedImage image = ImageIO.read(record.getImageURL());
            setSize(image.getWidth() + 50, image.getHeight() + 170);
        }
        catch (IOException e)
        {
            setSize(310, 200);
        }
        setLocationRelativeTo(owner);
        
    }

    /**
     * Rotates and saves the icon to the icon registry.
     *
     * @param record the icon record
     * @param rotation the rotation
     */
    private void saveRotatedIcon(IconRecord record, int rotation)
    {
        try
        {
            BufferedImage image = ImageIO.read(record.getImageURL());
            RotateImageProcessor processor = new RotateImageProcessor(rotation, false, null);
            image = processor.process(image);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            String name = record.getName() + "_" + rotation;
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
