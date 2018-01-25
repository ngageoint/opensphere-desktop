package io.opensphere.core.capture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;

/** Helper class to create screen capture file. */
public class ScreenCaptureWriter
{
    /** Text for gif. */
    private static final String GIF = "gif";

    /** Text for gif extension. */
    private static final String GIF_EXTENSION = "." + GIF;

    /** Text for jpeg. */
    private static final String JPEG = "jpeg";

    /** Text for jpeg extension. */
    private static final String JPEG_EXTENSION = "." + JPEG;

    /** Text for jpg. */
    private static final String JPG = "jpg";

    /** Text for jpg extension. */
    private static final String JPG_EXTENSION = "." + JPG;

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ScreenCaptureWriter.class);

    /** Text for png. */
    private static final String PNG = "png";

    /** Text for png extension. */
    private static final String PNG_EXTENSION = "." + PNG;

    /** The FileChooser used to select a file. */
    private final MnemonicFileChooser myFileChooser;

    /** The Image area. */
    private final ImageArea myImageArea;

    /** The Constant string for the last saved directory preference. */
    public static final String LAST_SAVED_DIRECTORY_PREFERENCE = "LastSavedDirectoryPath";

    /**
     * Default constructor.
     *
     * @param image The Image area to use.
     * @param prefRegistry The preferences registry to use (default save
     *            location).
     */
    public ScreenCaptureWriter(ImageArea image, PreferencesRegistry prefRegistry)
    {
        myImageArea = image;

        // Construct a save file chooser. Initialize the starting directory to
        // the current directory, do not allow the user to select the "all
        // files" filter, and restrict the files that can be selected to those
        // ending
        // with .png, .gif, .jpg or .jpeg extensions.
        FileNameExtensionFilter filter1 = new FileNameExtensionFilter(PNG_EXTENSION, PNG);
        FileNameExtensionFilter filter2 = new FileNameExtensionFilter(GIF_EXTENSION, GIF);
        FileNameExtensionFilter filter3 = new FileNameExtensionFilter(JPEG_EXTENSION, JPG, JPEG);

        myFileChooser = new MnemonicFileChooser(prefRegistry, "ScreenCaptureWriter");
        // Setting the file type filters
        // filter3 is set last so jpeg is default file type when opening 'Save
        // as' window
        myFileChooser.setAcceptAllFileFilterUsed(false);
        myFileChooser.addChoosableFileFilter(filter2);
        myFileChooser.addChoosableFileFilter(filter3);
        myFileChooser.addChoosableFileFilter(filter1);

        myFileChooser.setFileFilter(filter3);
    }

    /**
     * Write image data to the specified file.
     *
     * @return True if successful, false otherwise.
     */
    public boolean writeToFile()
    {
        File file = findFile();
        if (file == null)
        {
            return false;
        }

        String path = file.getAbsolutePath().toLowerCase();

        // Now save image to file.
        ImageWriter writer = null;
        ImageOutputStream ios = null;

        try
        {
            if (path.endsWith(JPEG_EXTENSION) || path.endsWith(JPG_EXTENSION))
            {
                // Obtain a writer based on the jpeg format.
                Iterator<ImageWriter> iter;
                iter = ImageIO.getImageWritersByFormatName(JPEG);

                // Validate existence of writer.
                if (!iter.hasNext())
                {
                    LOGGER.error("Unable to save image to jpeg file type.");
                    JOptionPane.showMessageDialog(null, "Unable to save image to jpeg file type.", "Error Saving",
                            JOptionPane.OK_OPTION);
                    return false;
                }

                // Extract writer.
                writer = iter.next();

                // Configure writer output destination.
                ios = ImageIO.createImageOutputStream(file);
                writer.setOutput(ios);

                // Set JPEG compression quality to 100%.
                ImageWriteParam iwp = writer.getDefaultWriteParam();
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwp.setCompressionQuality(1.00f);

                // Write the image.
                writer.write(null, new IIOImage((BufferedImage)myImageArea.getImage(), null, null), iwp);
            }
            else if (path.endsWith(PNG_EXTENSION) || path.endsWith(GIF_EXTENSION))
            {
                boolean isPNG = path.endsWith(PNG_EXTENSION);
                // Obtain a writer.
                Iterator<ImageWriter> iter;
                String type = isPNG ? PNG : GIF;

                iter = ImageIO.getImageWritersByFormatName(type);

                // Validate existence of writer.
                if (!iter.hasNext())
                {
                    LOGGER.error("Unable to save image to " + type + " file type.");
                    JOptionPane.showMessageDialog(null, "Unable to save image to " + type + " file type.", "Error Saving",
                            JOptionPane.OK_OPTION);
                    return false;
                }

                // Extract writer.
                writer = iter.next();

                // Configure writer output destination.
                ios = ImageIO.createImageOutputStream(file);
                writer.setOutput(ios);

                // Write the image.
                writer.write(null, new IIOImage((BufferedImage)myImageArea.getImage(), null, null), null);
            }
        }
        catch (IOException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                // Cleanup.
                if (ios != null)
                {
                    ios.flush();
                    ios.close();
                }

                if (writer != null)
                {
                    writer.dispose();
                }
            }
            catch (IOException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return true;
    }

    /**
     * Helper method to prompt for and find file that image will be saved as.
     *
     * @return The file
     */
    private File findFile()
    {
        // If the user cancels this file chooser, exit this method.
        if (myFileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }

        // Obtain the selected file. Validate its extension, which must be
        // .png, .gif, .jpg or .jpeg. If extension not present, append
        // .jpg extension.
        File file = myFileChooser.getSelectedFile();
        String path = file.getAbsolutePath().toLowerCase();

        if (!path.endsWith(PNG_EXTENSION) && !path.endsWith(JPG_EXTENSION) && !path.endsWith(JPEG_EXTENSION)
                && !path.endsWith(GIF_EXTENSION))
        {
            file = new File(file.getAbsolutePath() + myFileChooser.getFileFilter().getDescription());
        }

        // If the file exists, inform the user, who might not want
        // to accidentally overwrite an existing file. Exit method
        // if the user specifies that it is not okay to overwrite
        // the file.
        if (file.exists())
        {
            int choice = JOptionPane.showConfirmDialog(null, "Overwrite file?", "Capture", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.NO_OPTION)
            {
                return null;
            }
        }

        return file;
    }
}
