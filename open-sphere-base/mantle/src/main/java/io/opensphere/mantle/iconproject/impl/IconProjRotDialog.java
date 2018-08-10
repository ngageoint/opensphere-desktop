package io.opensphere.mantle.iconproject.impl;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.image.processor.RotateImageProcessor;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.IconRotPane;

/** Icon rotation Swing dialog (with JavaFX content). */
public class IconProjRotDialog extends JFXDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(IconProjRotDialog.class);

    /** The current icon registry. */
    private IconRegistry myIconRegistry;

    /** The icon being rotated. */
    private IconRecord myIconRecord;

    /** The current UI model. */
    private PanelModel myPanelModel;

    /**
     * Creates a small window to quickly rotate an icon.
     *
     * @param owner the owner
     * @param thePanelModel the current UI model.
     */
    public IconProjRotDialog(Window owner, PanelModel thePanelModel)
    {
        super(owner, "Rotate Icon");
        myPanelModel = thePanelModel;
        myIconRegistry = myPanelModel.getIconRegistry();
        myIconRecord = myPanelModel.getSelectedRecord().get();
        setMinimumSize(new Dimension(250, 350));
        setSize(new Dimension(450, 550));
        IconRotPane pane = new IconRotPane(myIconRecord);
        setFxNode(pane);
        setAcceptEar(() -> saveRotatedIcon(myIconRecord, pane.getRotation(), pane.getSaveState()));
        try
        {
            BufferedImage image = ImageIO.read(myIconRecord.getImageURL());
            if (image.getWidth() >= 300)
            {
                setSize(450, 550);
            }
            else
            {
                setSize(image.getWidth() + 50, image.getHeight() + 110);
            }
        }
        catch (IOException e)
        {
            setSize(310, 150);
        }
        setLocationRelativeTo(owner);
    }

    /**
     * Saves the modified icon to the icon registry.
     *
     * @param record the icon being rotated.
     * @param rotation the numerical amount the icon has been rotated.
     * @param saveChoice the users indication of whether to replace the existing
     *            icon.
     */
    private void saveRotatedIcon(IconRecord record, int rotation, boolean saveChoice)
    {
        try
        {
            BufferedImage image = ImageIO.read(record.getImageURL());
            RotateImageProcessor processor = new RotateImageProcessor(rotation, false, null);
            image = processor.process(image);

            if (saveChoice)
            {
                String filename = record.getImageURL().toString();
                filename = filename.replace("file:", "");
                filename = filename.replace("%20", " ");
                ImageIO.write(image, FilenameUtils.getExtension(filename), new File(filename));
            }
            else
            {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", outputStream);
                String name = record.getName() + "_" + rotation;
                URL imageURL = myIconRegistry.getIconCache().cacheIcon(outputStream.toByteArray(), name, true);
                IconProvider provider = new DefaultIconProvider(imageURL, IconRecord.USER_ADDED_COLLECTION, null, "User");
                myIconRegistry.addIcon(provider, this);
            }
            myPanelModel.getViewModel().getMainPanel().refresh();
        }
        catch (IOException e)
        {
            Notify.error(e.getMessage());
            LOGGER.error(e, e);
        }
    }
}
