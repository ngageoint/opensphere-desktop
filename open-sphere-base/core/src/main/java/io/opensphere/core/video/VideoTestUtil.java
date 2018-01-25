package io.opensphere.core.video;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.io.FilenameUtils;

import io.opensphere.core.util.io.CancellableInputStream;

/**
 * A utility class for the tests within the video package.
 */
public final class VideoTestUtil
{
    /**
     * The file path to a opensphere exported klv file.
     */
    public static final String KLV_FILE_1 = "";

    /**
     * The file path to a opensphere exported klv file.
     */
    public static final String KLV_FILE_2 = "";

    /**
     * The file path to a opensphere exported klv file.
     */
    public static final String KLV_FILE_3 = "";

    /**
     * The file path to a test file.
     */
    public static final String VIDEO_FILE_1 = "";

    /**
     * The file path to a test file.
     */
    public static final String VIDEO_FILE_2 = "";

    /**
     * The file path to a test file.
     */
    public static final String VIDEO_FILE_3 = "";

    /**
     * The file path to a test file.
     */
    public static final String VIDEO_FILE_4 = "";

    /**
     * The file path to a test file.
     */
    public static final String VIDEO_FILE_5 = "";

    /**
     * Gets the video file.
     *
     * @param filePath The file path.
     * @return The file.
     */
    public static File getFile(String filePath)
    {
        return new File(FilenameUtils.normalize(filePath));
    }

    /**
     * Gets the video stream.
     *
     * @param filePath The file path to the video.
     * @return The stream to the video file.
     * @throws FileNotFoundException No file.
     */
    public static CancellableInputStream getFileStream(String filePath) throws FileNotFoundException
    {
        final File file = getFile(filePath);
        return new CancellableInputStream(new FileInputStream(file), null);
    }

    /**
     * Not constructible.
     */
    private VideoTestUtil()
    {
    }
}
