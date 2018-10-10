package io.opensphere.core.util.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXImagePanel;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;

/**
 * Image Manipulation Utilities.
 */
public final class ImageUtil
{
    /** The Constant ourBadURLImage. */
    public static final BufferedImage BAD_URL_IMAGE = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

    /** The Constant ourNoImage. */
    public static final BufferedImage BLANK_IMAGE = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

    /** An image to represent an image that is unreadable or unavailable. */
    public static final BufferedImage BROKEN_IMAGE;

    /** The Constant ourLoadingImage. */
    public static final BufferedImage LOADING_IMAGE = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

    /** The Constant ourNoImage. */
    public static final BufferedImage NO_IMAGE = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImageUtil.class);

    static
    {
        Graphics g2D = NO_IMAGE.getGraphics();
        g2D.setColor(new Color(0, 0, 0, 0));
        g2D.fillRect(0, 0, 100, 100);
        g2D.setColor(Color.white);
        g2D.drawString("No Preview", 20, 40);
        g2D.drawString("Available", 27, 60);

        g2D = BAD_URL_IMAGE.getGraphics();
        g2D.setColor(new Color(0, 0, 0, 0));
        g2D.fillRect(0, 0, 100, 100);
        g2D.setColor(Color.yellow);
        g2D.drawString("Invalid URL", 20, 40);

        g2D = LOADING_IMAGE.getGraphics();
        g2D.setColor(new Color(0, 0, 0, 0));
        g2D.fillRect(0, 0, 100, 100);
        g2D.setColor(Color.white);
        g2D.drawString("Loading...", 20, 40);

        BufferedImage brokenImage;
        try
        {
            URL brokenImageURL = ImageUtil.class.getClassLoader().getResource("images/brokenimage.gif");
            brokenImage = ImageIO.read(brokenImageURL);
        }
        catch (IOException e)
        {
            LOGGER.warn("Unable to locate broken image.", e);
            brokenImage = LOADING_IMAGE;
        }
        BROKEN_IMAGE = brokenImage;
    }

    /**
     * Convert a buffered image of one type to another type.
     *
     * @param bufferedImage the buffered image
     * @param type the type of buffered image to convert to.
     * @return the converted buffered image.
     */
    public static BufferedImage convertImageType(BufferedImage bufferedImage, int type)
    {
        if (type < 0 || type > BufferedImage.TYPE_BYTE_INDEXED)
        {
            throw new IllegalArgumentException("Cannot convert to buffered image type " + type + " is not a valid value.");
        }
        Utilities.checkNull(bufferedImage, "bufferedImage");
        if (bufferedImage.getType() == type)
        {
            return bufferedImage;
        }

        BufferedImage newImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), type);
        Graphics2D g2D = newImage.createGraphics();
        g2D.drawImage(bufferedImage, 0, 0, null);
        return newImage;
    }

    /**
     * Make a copy of the image data and use the same color model.
     *
     * @param image The image to copy.
     * @return The copy of the image.
     */
    public static BufferedImage copyImage(BufferedImage image)
    {
        ColorModel colorModel = image.getColorModel();
        WritableRaster raster = image.getRaster().createCompatibleWritableRaster();
        image.copyData(raster);
        return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
    }

    /**
     * Get the scale factor which most closely approximates the max height and
     * max width without exceeding either and maintains the aspect ratio of the
     * original image.
     *
     * @param image The image which is to be scaled.
     * @param maxHeight The max height.
     * @param maxWidth The max width.
     * @return the scale factor.
     */
    public static float getAspectAdjustedScaleFactor(RenderedImage image, int maxHeight, int maxWidth)
    {
        int currentHeight = image.getHeight();
        int currentWidth = image.getWidth();
        float heightScaleFactor = (float)maxHeight / (float)currentHeight;
        float widthScaleFactor = (float)maxWidth / (float)currentWidth;
        return heightScaleFactor < widthScaleFactor ? heightScaleFactor : widthScaleFactor;
    }

    /**
     * Get the image width and height which most closely approximate the max
     * height and max width without exceeding either and maintains the aspect
     * ratio of the original image.
     *
     * @param image The image which is to be scaled.
     * @param maxHeight The max height.
     * @param maxWidth The max width.
     * @return the scaled width followed by the scaled height.
     */
    public static int[] getAspectAdustedScale(BufferedImage image, int maxHeight, int maxWidth)
    {
        float scaleFactor = getAspectAdjustedScaleFactor(image, maxHeight, maxWidth);
        int scaledWidth = (int)Math.floor(image.getWidth() * scaleFactor);
        int scaledHeight = (int)Math.floor(image.getHeight() * scaleFactor);
        return new int[] { scaledWidth, scaledHeight };
    }

    /**
     * Scale image to a desired height or width whichever is most restrictive
     * while maintaining the aspect ratio and do not allow up scaling.
     *
     * @param image the image to scale
     * @param maxHeight the maximum height
     * @param maxWidth the maximum width
     * @return the buffered image
     */
    public static Image scaleDownImage(BufferedImage image, int maxHeight, int maxWidth)
    {
        int currentHeight = image.getHeight();
        int currentWidth = image.getWidth();

        // Only allow scale down
        if (maxHeight < currentHeight || maxWidth < currentWidth)
        {
            return scaleImageMaintainAspect(image, maxHeight, maxWidth);
        }

        return image;
    }

    /**
     * Scales a provided image to a desired dimension.
     *
     * @param image the image to scale.
     * @param dim The target dimension.
     * @return the scaled buffered image
     */
    public static BufferedImage scaleImage(BufferedImage image, Dimension dim)
    {
        BufferedImage destBuff = new BufferedImage(dim.width, dim.height,
                image.getType() == 0 ? BufferedImage.TYPE_4BYTE_ABGR : image.getType());
        Graphics2D g = destBuff.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(dim.getWidth() / image.getWidth(),
                dim.getHeight() / image.getHeight());
        g.drawRenderedImage(image, at);
        return destBuff;
    }

    /**
     * Scales a provided image by the scale factor.
     *
     * @param image the image to scale.
     * @param scaleFactor the scale factor
     * @return the scaled buffered image
     */
    public static BufferedImage scaleImage(BufferedImage image, double scaleFactor)
    {
        return scaleImage(image, scaleFactor, scaleFactor);
    }

    /**
     * Scales a provided image by the scale factor.
     *
     * @param image the image to scale.
     * @param scaleFactorX The scale factor x.
     * @param scaleFactorY The scale factor y.
     * @return the scaled buffered image
     */
    public static BufferedImage scaleImage(BufferedImage image, double scaleFactorX, double scaleFactorY)
    {
        int scaleWidth = (int)Math.ceil(scaleFactorX * image.getWidth());
        int scaleHeight = (int)Math.ceil(scaleFactorY * image.getHeight());
        BufferedImage destBuff = new BufferedImage(scaleWidth, scaleHeight,
                image.getType() == 0 ? BufferedImage.TYPE_4BYTE_ABGR : image.getType());
        Graphics2D g = destBuff.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(scaleFactorX, scaleFactorY);
        g.drawRenderedImage(image, at);
        return destBuff;
    }

    /**
     * Scales an image.
     *
     * @param image the image
     * @param desiredHeight the desired height
     * @param expand Whether or not to expand the image
     * @return scaled image
     */
    public static BufferedImage scaleImage(BufferedImage image, int desiredHeight, boolean expand)
    {
        BufferedImage destBuff = null;

        int currentHeight = image.getHeight();
        // Scale down
        if (desiredHeight < currentHeight || expand)
        {
            double scaleFactor = (double)desiredHeight / (double)currentHeight;
            int scaledWidth = (int)Math.floor(image.getWidth() * scaleFactor);

            destBuff = new BufferedImage(scaledWidth, desiredHeight,
                    image.getType() == 0 ? BufferedImage.TYPE_4BYTE_ABGR : image.getType());
            Graphics2D g = destBuff.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
            g.drawRenderedImage(image, at);
        }
        // Don't scale up, use original image
        else
        {
            destBuff = image;
        }

        return destBuff;
    }

    /**
     * Scales an ImageIcon.
     *
     * @param icon the ImageIcon
     * @param desiredHeight the desired height
     * @param expand Whether or not to expand the icon
     * @return scaled ImageIcon
     */
    public static ImageIcon scaleImageIcon(ImageIcon icon, int desiredHeight, boolean expand)
    {
        ImageIcon destBuff = null;

        // Scale the icon
        int currentHeight = icon.getIconHeight();
        if (desiredHeight < currentHeight || expand)
        {
            double scaleFactor = (double)desiredHeight / (double)currentHeight;
            int scaledWidth = (int)Math.floor(icon.getIconWidth() * scaleFactor);

            Image scaledImage = icon.getImage().getScaledInstance(scaledWidth, desiredHeight, Image.SCALE_SMOOTH);
            icon.setImage(scaledImage);
        }

        destBuff = icon;

        return destBuff;
    }

    /**
     * Scale image to a desired height or width whichever is most restrictive
     * while maintaining the aspect ratio.
     *
     * @param image the image to scale
     * @param maxHeight the maximum height
     * @param maxWidth the maximum width
     * @return the buffered image
     */
    public static Image scaleImageMaintainAspect(BufferedImage image, int maxHeight, int maxWidth)
    {
        int[] scale = getAspectAdustedScale(image, maxHeight, maxWidth);
        return image.getScaledInstance(scale[0], scale[1], Image.SCALE_SMOOTH);
    }

    /**
     * Brings up an image file chooser dialog with an image preview panel that
     * allows gif, jpeg, and png image types.
     *
     * The caller may optionally provide the absolute path for the last image
     * selected to guide the open dialog to the proper directory. Will position
     * itself near the provided parent.
     *
     * @param title the title to display in the file chooser ( null will display
     *            a default ).
     * @param parent the {@link Component} that owns the chooser.
     * @param prefsRegistry The preference registry.
     * @return the file chosen or null if cancelled by the user.
     */
    public static File showImageFileChooser(String title, Component parent, PreferencesRegistry prefsRegistry)
    {
        File chosenFile = null;

        MnemonicFileChooser chooser = new MnemonicFileChooser(prefsRegistry, "ImageUtil");

        chooser.setDialogTitle(title == null ? "Choose Image File" : title);
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Image Preview:"));
        final JXImagePanel imagePreviewPanel = new JXImagePanel();
        imagePreviewPanel.setMinimumSize(new Dimension(180, 180));
        imagePreviewPanel.setPreferredSize(new Dimension(180, 180));
        imagePanel.add(imagePreviewPanel, BorderLayout.SOUTH);

        chooser.setAccessory(imagePanel);
        chooser.setFileFilter(new FileFilter()
        {
            @Override
            public boolean accept(File f)
            {
                String fName = f.getName().toLowerCase();
                return f.isDirectory() || fName.endsWith(".jpeg") || fName.endsWith(".jpg") || fName.endsWith(".gif")
                        || fName.endsWith(".png") || fName.endsWith(".svg") || fName.endsWith(".bmp");
            }

            @Override
            public String getDescription()
            {
                return "Image Files[*.jpeg,*.jpg,*.gif,*.png,*.svg,*.bmp]";
            }
        });

        chooser.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName()))
                {
                    String filePath = evt.getNewValue() == null ? null : evt.getNewValue().toString();
                    if (filePath != null)
                    {
                        try
                        {
                            BufferedImage buff = ImageIO.read(new File(filePath));
                            Image scale = ImageUtil.scaleDownImage(buff, imagePreviewPanel.getHeight(),
                                    imagePreviewPanel.getWidth());
                            imagePreviewPanel.setImage(scale);
                        }
                        catch (IOException e)
                        {
                            LOGGER.warn("Unable to read image from path '" + filePath + "'", e);
                            imagePreviewPanel.setImage(ImageUtil.NO_IMAGE);
                        }
                    }
                    else
                    {
                        imagePreviewPanel.setImage(ImageUtil.NO_IMAGE);
                    }
                }
            }
        });

        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            chosenFile = chooser.getSelectedFile();
        }
        return chosenFile;
    }

    /**
     * Shrinks the image below the specified number of pixels.
     *
     * @param original The original image.
     * @param maxPixels The number of pixels to shrink the image below.
     * @return The shrunk image.
     */
    public static InputStream shrinkImage(InputStream original, int maxPixels)
    {
        InputStream stream = original;
        try
        {
            BufferedImage image = ImageIO.read(original);
            int totalPixels = image.getHeight() * image.getWidth();
            int divisible = 1;
            while (totalPixels > maxPixels)
            {
                divisible++;
                totalPixels = image.getHeight() / divisible * image.getWidth() / divisible;
            }

            int newWidth = image.getWidth() / divisible;
            int newHeight = image.getHeight() / divisible;
            BufferedImage tThumbImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_4BYTE_ABGR);
            // create a graphics object to paint to
            Graphics2D tGraphics2D = tThumbImage.createGraphics();
            tGraphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            // draw the image scaled
            tGraphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(tThumbImage, "png", output);
            stream = new ByteArrayInputStream(output.toByteArray());
        }
        catch (IOException e1)
        {
            LOGGER.error(e1, e1);
        }

        return stream;
    }

    /** Disallow instantiation. */
    private ImageUtil()
    {
    }
}
