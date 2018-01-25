package io.opensphere.core.util;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * The Class ClipBoardTool.
 */
public final class ClipBoardTool
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(ClipBoardTool.class);

    /**
     * Screen capture.
     *
     * @param upperLeftX the upper left x
     * @param upperLeftY the upper left y
     * @param pWidth the width
     * @param pHeight the height
     * @return the buffered image
     */
    public static BufferedImage screenCapture(int upperLeftX, int upperLeftY, int pWidth, int pHeight)
    {
        BufferedImage buf = null;
        try
        {
            Robot r = new Robot();
            buf = r.createScreenCapture(new Rectangle(upperLeftX, upperLeftY, pWidth, pHeight));
        }
        catch (AWTException e)
        {
            LOGGER.error(e);
        }
        return buf;
    }

    /**
     * Write to clipboard.
     *
     * @param image the image
     */
    public static void writeToClipboard(Image image)
    {
        ImageSelection imageSelection = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageSelection, null);
    }

    /**
     * Write to clipboard.
     *
     * @param writeMe the write me
     */
    public static void writeToClipboard(String writeMe)
    {
        // get the system clipboard
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // set the textual content on the clipboard to our
        // transferable object
        // we use the
        Transferable transferableText = new StringSelection(writeMe);
        systemClipboard.setContents(transferableText, null);
    }

    /**
     * Instantiates a new clip board tool.
     */
    private ClipBoardTool()
    {
        // Not implemnted.
    }

    /**
     * Inner class is used to hold an image while on the clipboard.
     */
    public static class ImageSelection implements Transferable
    {
        /** the Image object which will be housed by the ImageSelection. */
        private final Image myImage;

        /**
         * Instantiates a new image selection.
         *
         * @param image the image
         */
        public ImageSelection(Image image)
        {
            myImage = image;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
        {
            if (!DataFlavor.imageFlavor.equals(flavor))
            {
                throw new UnsupportedFlavorException(flavor);
            }
            // else return the payload
            return myImage;
        }

        // Returns true if flavor is supported

        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        // Returns Image object housed by Transferable object

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return DataFlavor.imageFlavor.equals(flavor);
        }
    }
}
