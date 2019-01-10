package io.opensphere.mantle.icon.impl;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.icon.IconCache;

/**
 * The Class IconCacheImpl.
 */
public class IconCacheImpl implements IconCache
{
    /** The Constant BASE_ICON_EXTENSION. */
    private static final String BASE_ICON_EXTENSION = "png";

    /** The Constant BASE_ICON_FILE_NAME. */
    private static final String BASE_ICON_FILE_NAME = "CachedIcon";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(IconCacheImpl.class);

    /** The Cache location. */
    private final File myCacheLocation;

    /** The File namer lock. */
    private final ReentrantLock myFileNamerLock;

    /**
     * Instantiates a new icon cache impl.
     *
     * @param iconCacheLocation the icon cache location
     */
    public IconCacheImpl(File iconCacheLocation)
    {
        myCacheLocation = iconCacheLocation;
        myFileNamerLock = new ReentrantLock();

        if (!myCacheLocation.exists() && !myCacheLocation.mkdirs())
        {
            LOGGER.error("Could not create icon cache at: " + myCacheLocation.getAbsolutePath());
        }
    }

    @Override
    public URL cacheIcon(byte[] byteArray) throws IOException
    {
        return cacheIcon(byteArray, null, true);
    }

    @Override
    public URL cacheIcon(byte[] byteArray, String destFileName, boolean overwriteExisting) throws IOException
    {
        Utilities.checkNull(byteArray, "byteArray");
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        return cacheIcon(bais, destFileName, overwriteExisting);
    }

    @Override
    public URL cacheIcon(File source) throws IOException
    {
        return cacheIcon(source, null, true);
    }

    @Override
    public URL cacheIcon(File source, String destFileName, boolean overwriteExisting) throws IOException
    {
        URL result = null;
        try (FileInputStream fis = new FileInputStream(source))
        {
            result = cacheIcon(fis, destFileName, overwriteExisting);
        }
        return result;
    }

    @Override
    public URL cacheIcon(InputStream stream) throws IOException
    {
        return cacheIcon(stream, null, true);
    }

    @Override
    public URL cacheIcon(InputStream stream, String destFileName, boolean overwriteExisting) throws IOException
    {
        String fileName = destFileName;
        if (fileName == null || fileName.isEmpty())
        {
            fileName = determineNextUniqueIconFileName();
        }

        String outFileName = myCacheLocation.getAbsolutePath() + File.separator + fileName;
        if (!outFileName.toLowerCase().endsWith("." + BASE_ICON_EXTENSION))
        {
            outFileName = new StringBuilder().append(outFileName).append('.').append(BASE_ICON_EXTENSION).toString();
        }

        File outFile = new File(outFileName);
        if (overwriteExisting || !outFile.exists())
        {
            BufferedImage iconImage = ImageIO.read(stream);
            if (iconImage != null)
            {
                ImageIO.write(iconImage, BASE_ICON_EXTENSION, outFile);
            }
        }

        return outFile.toURI().toURL();
    }

    @Override
    public URL cacheIcon(URL source) throws IOException
    {
        return cacheIcon(source, null, true);
    }

    @Override
    public URL cacheIcon(URL source, String destFileName, boolean overwriteExisting) throws IOException
    {
        BufferedInputStream inputStream = new BufferedInputStream(IconCacheImpl.class.getResourceAsStream(source.toString()));
        return cacheIcon(inputStream, destFileName, overwriteExisting);
    }

    @Override
    public File getIconCacheLocation()
    {
        return myCacheLocation;
    }

    @Override
    public boolean removeIcon(URL cacheURL)
    {
        Utilities.checkNull(cacheURL, "cacheURL");
        boolean success = false;
        try
        {
            File aFile = new File(cacheURL.toURI());
            if (aFile.getAbsolutePath().startsWith(myCacheLocation.getAbsolutePath()) && aFile.exists())
            {
                success = aFile.delete();
            }
        }
        catch (@SuppressWarnings("unused") URISyntaxException e)
        {
            success = false;
        }
        return success;
    }

    /**
     * Determine next unique icon file name.
     *
     * @return the string
     */
    private String determineNextUniqueIconFileName()
    {
        String resultName = null;
        myFileNamerLock.lock();
        try
        {
            int counter = 1;
            String nameToTest = myCacheLocation.getAbsolutePath() + File.separator + BASE_ICON_FILE_NAME
                    + Integer.toString(counter) + "." + BASE_ICON_EXTENSION;

            File aFile = new File(nameToTest);
            while (aFile.exists())
            {
                counter++;
                nameToTest = myCacheLocation.getAbsolutePath() + File.separator + BASE_ICON_FILE_NAME + Integer.toString(counter)
                        + "." + BASE_ICON_EXTENSION;
                aFile = new File(nameToTest);
            }
            resultName = aFile.getName();
        }
        finally
        {
            myFileNamerLock.unlock();
        }
        return resultName;
    }
}
