package io.opensphere.imagery;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.core.image.DDSEncodableImage;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.icon.IconImageProvider;

/**
 * The Class ImageryEnvoyHelper.
 */
public final class ImageryEnvoyHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImageryEnvoyHelper.class);

    /**
     * Gets the image from source.
     *
     * @param typeInfo the type info
     * @param imageKey the image key
     * @return the image from source
     */
    public static DDSEncodableImage getImageFromSource(ImageryDataTypeInfo typeInfo, ImageryImageKey imageKey)
    {
        ImageIOImage result = null;
        long beginTime = System.currentTimeMillis();
        if (typeInfo.initializeGDALTools())
        {
            if (LOGGER.isTraceEnabled())
            {
                long endTime = System.currentTimeMillis();
                LOGGER.trace(StringUtilities.formatTimingMessage("Initilzed Image Data: [" + typeInfo.getFile().getAbsolutePath()
                        + "]" + imageKey.getBoundingBox() + " in ", endTime - beginTime));
                beginTime = System.currentTimeMillis();
            }

            BufferedImage image = typeInfo.getGDALTools().retrieveGeographicalPiece(typeInfo.getFile(), imageKey.getBoundingBox(),
                    ImageryFileSource.DEFAULT_TILE_SIZE, ImageryFileSource.DEFAULT_TILE_SIZE, typeInfo.getHistoInfo());

            result = postProcessResult(typeInfo, image);
            if (result == null)
            {
                result = postProcessResult(typeInfo, createMissingDataTile(typeInfo));
            }

            if (LOGGER.isDebugEnabled())
            {
                long endTime = System.currentTimeMillis() - beginTime;
                LOGGER.debug(StringUtilities.formatTimingMessage(
                        "Read Image: [" + typeInfo.getFile().getAbsolutePath() + "]" + imageKey.getBoundingBox() + " in ",
                        endTime));
            }
        }

        return result;
    }

    /**
     * If switchChannel map the "thisColorToTransparent" color to become fully
     * transparent, do not affect other pixels. Accepts 3 channel or 4 channel
     * and converts to 4 channel as well.
     *
     * @param image the image
     * @param switchChannel the switch channel
     * @param thisColorToTransparent the this color to transparent
     * @return the buffered image
     */
    private static BufferedImage makeFourChannelWithAlpha(BufferedImage image, boolean switchChannel,
            Color thisColorToTransparent)
    {
        int w = image.getWidth();
        int h = image.getHeight();
        int type = BufferedImage.TYPE_4BYTE_ABGR;

        BufferedImage dst = null;

        if (switchChannel)
        {
            dst = new BufferedImage(w, h, type);
            Graphics2D g2 = dst.createGraphics();
            g2.drawImage(image, 0, 0, null);
            g2.dispose();
            int repl = thisColorToTransparent.getRGB();
            int xp = new Color(0, 0, 0, 0).getRGB();

            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++)
                {
                    if (image.getRGB(x, y) == repl)
                    {
                        dst.setRGB(x, y, xp);
                    }
                }
            }
        }
        else
        {
            // adds alpha channel
            dst = new BufferedImage(w, h, type);
            Graphics2D g2 = dst.createGraphics();
            g2.drawImage(image, 0, 0, null);
            g2.dispose();
        }

        return dst;
    }

    /**
     * Creates the missing data tile.
     *
     * @param typeInfo the type info
     * @return the buffered image
     */
    private static BufferedImage createMissingDataTile(ImageryDataTypeInfo typeInfo)
    {
        BufferedImage image;
        try
        {
            BufferedImage buff = ImageIO.read(IconImageProvider.ourBrokenImageURL);
            buff = ImageUtil.convertImageType(buff, BufferedImage.TYPE_4BYTE_ABGR);
            BufferedImage tile = new BufferedImage(ImageryFileSource.DEFAULT_TILE_SIZE, ImageryFileSource.DEFAULT_TILE_SIZE,
                    BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2D = (Graphics2D)tile.getGraphics();
            g2D.setColor(Color.gray);
            g2D.fillRect(0, 0, ImageryFileSource.DEFAULT_TILE_SIZE - 1, ImageryFileSource.DEFAULT_TILE_SIZE - 1);
            int xLoc = ImageryFileSource.DEFAULT_TILE_SIZE / 2 - buff.getWidth() / 2;
            int yLoc = ImageryFileSource.DEFAULT_TILE_SIZE / 2 - buff.getHeight() / 2;
            g2D.drawImage(buff, xLoc, yLoc, buff.getWidth(), buff.getHeight(), null);
            String text = "Could not load data from image.";
            g2D.setFont(g2D.getFont().deriveFont(Font.BOLD, g2D.getFont().getSize() + 2));
            FontMetrics fm = g2D.getFontMetrics();
            Rectangle2D rect = fm.getStringBounds(text, g2D);
            g2D.setColor(Color.black);
            int text1X = tile.getWidth() / 2 - (int)(rect.getWidth() / 2);
            int text1Y = yLoc + buff.getHeight() + (int)rect.getHeight() + 10;
            g2D.drawString(text, text1X, text1Y);
            String text2 = "Group:" + typeInfo.getGroupName();
            rect = fm.getStringBounds(text2, g2D);
            int text2X = tile.getWidth() / 2 - (int)(rect.getWidth() / 2);
            int text2Y = text1Y + (int)rect.getHeight() + 10;
            g2D.drawString(text2, text2X, text2Y);

            String text3 = "Image: " + typeInfo.getDisplayName();
            rect = fm.getStringBounds(text3, g2D);
            int text3X = tile.getWidth() / 2 - (int)(rect.getWidth() / 2);
            int text3Y = text2Y + (int)rect.getHeight() + 10;
            g2D.drawString(text3, text3X, text3Y);
            image = tile;
        }
        catch (IOException e)
        {
            image = null;
        }
        return image;
    }

    /**
     * Post process result.
     *
     * @param typeInfo the type info
     * @param pImage the image
     * @return the image io image
     */
    private static ImageIOImage postProcessResult(ImageryDataTypeInfo typeInfo, BufferedImage pImage)
    {
        BufferedImage image = pImage;
        ImageIOImage result = null;
        if (image != null)
        {
            image = makeFourChannelWithAlpha(image, typeInfo.getImageryFileSource().ignoreZeros(), Color.black);
            result = new ImageIOImage(image);
        }
        return result;
    }

    /**
     * Disallow instantiation.
     */
    private ImageryEnvoyHelper()
    {
    }
}
