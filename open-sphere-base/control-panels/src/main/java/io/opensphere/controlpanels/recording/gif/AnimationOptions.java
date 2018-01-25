package io.opensphere.controlpanels.recording.gif;

import java.io.File;
import java.util.List;

/** Class to hold options selected by user for GIF animation creation. */
public class AnimationOptions
{
    /** The file to write to. */
    private File myFile;

    /** The interval between frames in milliseconds. */
    private int myFrameIntervalMS;

    /** The height. */
    private int myHeight;

    /** The list of components to hide. */
    private List<String> myHiddenComponents;

    /**
     * Indicates if the GIF file should be written in the compressed format.
     */
    private boolean myIsCompressed;

    /** The original height of the image. */
    private int myOriginalHeight;

    /** The original width of the image. */
    private int myOriginalWidth;

    /**
     * Whether to resize the image by compressing the original or by resizing
     * the window so that the original matches the desired size.
     */
    private ResizeOption myResizeOption;

    /** The width. */
    private int myWidth;

    /**
     * Standard getter.
     *
     * @return The file.
     */
    public File getFile()
    {
        return myFile;
    }

    /**
     * Get the frameIntervalMS.
     *
     * @return the frameIntervalMS
     */
    public int getFrameIntervalMS()
    {
        return myFrameIntervalMS;
    }

    /**
     * Standard getter.
     *
     * @return The height.
     */
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Standard getter.
     *
     * @return The list of selected components.
     */
    public List<String> getHiddenComponents()
    {
        return myHiddenComponents;
    }

    /**
     * Get the original height of the image.
     *
     * @return The original height.
     */
    public int getOriginalHeight()
    {
        return myOriginalHeight;
    }

    /**
     * Get the original width of the image.
     *
     * @return The original width.
     */
    public int getOriginalWidth()
    {
        return myOriginalWidth;
    }

    /**
     * Get the resizeOption.
     *
     * @return the resizeOption
     */
    public ResizeOption getResizeOption()
    {
        return myResizeOption;
    }

    /**
     * Standard getter.
     *
     * @return The width.
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Indicates if the GIF file should be written in the compressed format.
     *
     * @return True if compressed, false if uncompressed.
     */
    public boolean isCompressed()
    {
        return myIsCompressed;
    }

    /**
     * Standard setter.
     *
     * @param file The selected file.
     */
    public void setFile(File file)
    {
        myFile = file;
    }

    /**
     * Set the frameIntervalMS.
     *
     * @param frameIntervalMS the frameIntervalMS to set
     */
    public void setFrameIntervalMS(int frameIntervalMS)
    {
        myFrameIntervalMS = frameIntervalMS;
    }

    /**
     * Standard setter.
     *
     * @param height The height.
     */
    public void setHeight(int height)
    {
        myHeight = height;
    }

    /**
     * Standard setter.
     *
     * @param hiddenComponents The list of hidden components.
     */
    public void setHiddenComponents(List<String> hiddenComponents)
    {
        myHiddenComponents = hiddenComponents;
    }

    /**
     * Indicates if the GIF file should be written in the compressed format.
     *
     * @param isCompressed True if compressed, false if uncompressed.
     */
    public void setIsCompressed(boolean isCompressed)
    {
        myIsCompressed = isCompressed;
    }

    /**
     * Set the original height of the image.
     *
     * @param origHeight The original height.
     */
    public void setOriginalHeight(int origHeight)
    {
        myOriginalHeight = origHeight;
    }

    /**
     * Set the original width of the image.
     *
     * @param origWidth The original width.
     */
    public void setOriginalWidth(int origWidth)
    {
        myOriginalWidth = origWidth;
    }

    /**
     * Set the resizeOption.
     *
     * @param resizeOption the resizeOption to set
     */
    public void setResizeOption(ResizeOption resizeOption)
    {
        myResizeOption = resizeOption;
    }

    /**
     * Standard setter.
     *
     * @param width The width.
     */
    public void setWidth(int width)
    {
        myWidth = width;
    }

    /** Options for making the image match the desired size. */
    public enum ResizeOption
    {
        /** Compress the image to the desired size. */
        COMPRESS("Compress Image"),

        /**
         * Resize the window so that the original image matches the desired
         * size.
         */
        RESIZE("Resize Window"),

        ;

        /** The string version of the option. */
        private final String myLabel;

        /**
         * Constructor.
         *
         * @param label The string version of the option.
         */
        ResizeOption(String label)
        {
            myLabel = label;
        }

        @Override
        public String toString()
        {
            return myLabel;
        }
    }
}
