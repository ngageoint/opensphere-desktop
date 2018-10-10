package io.opensphere.core.util.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import io.opensphere.core.util.io.StreamReader;

/**
 * <p>
 * This is a collection of static methods for assisting with file actions.
 * </p>
 * <p>
 * NOTE: This class is used for the JNLP deployment, which must run under Java
 * 1.6. Dependencies should be added with care.
 * </p>
 */
@SuppressWarnings("PMD.GodClass")
public final class FileUtilities
{
    /**
     * Private copy of default character set to avoid depending on
     * StringUtilities.
     */
    private static final Charset DEFAULT_CHARSET = Charset.forName(System.getProperty("opensphere.charset", "UTF-8"));

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FileUtilities.class);

    /** Block size to use when reading a file. */
    private static final int READ_BLOCK_SIZE = 16384;

    /**
     * Archives the file.
     *
     * @param file the file
     * @return whether the move was successful
     */
    public static boolean archive(Path file)
    {
        boolean success = false;
        Path archiveDir = Paths.get(file.getParent().toString() + "-archive");
        Path archiveFile = Paths.get(archiveDir.toString(), file.getFileName().toString());
        try
        {
            if (!Files.exists(archiveDir))
            {
                Files.createDirectory(archiveDir);
            }
            Files.move(file, archiveFile);
            success = true;
        }
        catch (IOException e)
        {
            LOGGER.warn("Archive failed: " + e);
        }
        return success;
    }

    /**
     * Copies a directory from one place to another, optionally recursive with
     * sub-directories.
     *
     * @param source The source directory to copy.
     * @param destination The destination to copy to (created if it does not
     *            exist).
     * @param copySubDirectories If true recursively copies sub-directories.
     * @return True if successful, false otherwise.
     */
    public static boolean copyDirectory(File source, File destination, boolean copySubDirectories)
    {
        if (source == null || destination == null)
        {
            throw new IllegalArgumentException("The source and destination directories can not be null.");
        }

        // If the source file is not a directory
        // just do a normal file copy operation.
        if (source.isFile())
        {
            try
            {
                return FileUtilities.copyfile(source, destination);
            }
            catch (final IOException e)
            {
                LOGGER.error("Unable to copy files: " + e.getMessage(), e);
            }
        }

        // Check our destination directory.
        if (!createDirectories(destination))
        {
            LOGGER.error("Directory copy failed, could not create destination directory: " + destination.getAbsolutePath());
            return false;
        }

        final File[] fileList = source.listFiles();

        // If there aren't any files in the source directory then just quit.
        if (fileList == null)
        {
            return true;
        }

        boolean result = true;
        for (final File child : fileList)
        {
            if (child != null && child.isDirectory())
            {
                if (copySubDirectories)
                {
                    final File destDir = new File(destination, child.getName());
                    copyDirectory(child, destDir, copySubDirectories);
                }
            }
            else if (child != null)
            {
                try
                {
                    final File destFile = new File(destination, child.getName());
                    result &= FileUtilities.copyfile(child, destFile);
                }
                catch (final IOException e)
                {
                    LOGGER.error("Unable to copy files: " + e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Copies a source file into a destination file.
     *
     * @param srFile The file to copy.
     * @param dtFile The file to create/overwrite with the copy of srFile.
     * @return True if successful, false otherwise.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static boolean copyfile(File srFile, File dtFile) throws IOException
    {
        try
        {
            final FileInputStream in = new FileInputStream(srFile);
            try
            {
                final FileOutputStream out = new FileOutputStream(dtFile);
                try
                {
                    final byte[] buf = new byte[1024 * 10];
                    int len;
                    while ((len = in.read(buf)) != -1)
                    {
                        out.write(buf, 0, len);
                    }
                }
                finally
                {
                    out.close();
                }
            }
            finally
            {
                in.close();
            }
        }
        catch (final FileNotFoundException fnfe)
        {
            LOGGER.error("Unable to copy file: " + fnfe.getMessage(), fnfe);
            return false;
        }
        return true;
    }

    /**
     * Create the directory and any parent directories for the given path if it
     * does not already exist.
     *
     * @param dir The directory to be create.
     * @return True if the directory exists or was created, false otherwise.
     */
    public static boolean createDirectories(File dir)
    {
        if (dir == null)
        {
            throw new IllegalArgumentException("The directory can not be null.");
        }

        if (!dir.exists() && !dir.mkdirs())
        {
            LOGGER.error("Could not create directory: " + dir.getAbsolutePath());
            return false;
        }
        return true;
    }

    /**
     * Create the file and any parent directories for the given path if
     * necessary.
     *
     * @param file The file to be created.
     * @return True if the file exists or was created, false otherwise.
     */
    public static boolean createDirectoriesAndFile(File file)
    {
        if (file == null)
        {
            throw new IllegalArgumentException("The file can not be null.");
        }

        try
        {
            if (createDirectories(file.getParentFile()) && !file.exists() && !file.createNewFile())
            {
                LOGGER.error("Could not create file " + file.getAbsolutePath());
                return false;
            }
        }
        catch (final IOException e)
        {
            LOGGER.error("Could not create file " + e, e);
        }

        return true;
    }

    /**
     * When passed a directory name, this method deletes all the files and
     * folders underneath that directory, and the directory itself. When passed
     * a file, the file is deleted.
     *
     * @param fileOrDir - the file or directory to delete.
     * @return <code>true</code> iff the directory was deleted
     */
    public static boolean deleteDirRecursive(File fileOrDir)
    {
        if (fileOrDir.isDirectory())
        {
            final File[] files = fileOrDir.listFiles();
            if (files != null)
            {
                for (int i = 0; i < files.length; i++)
                {
                    if (!deleteDirRecursive(files[i]))
                    {
                        return false;
                    }
                }
            }
        }
        return fileOrDir.delete();
    }

    /**
     * Returns a file object that has the given suffix, adding it if necessary.
     *
     * @param file the file
     * @param suffix the desired suffix (without a preceding period)
     * @return the file with the suffix
     */
    public static File ensureSuffix(File file, String suffix)
    {
        if (suffix.equalsIgnoreCase(getSuffix(file)))
        {
            return file;
        }
        return new File(new StringBuilder(file.getPath()).append('.').append(suffix).toString());
    }

    /**
     * Expand the zip file into the given output directory. If an entry in the
     * zip file (or another file with the same name) exists in the the output
     * location, the file will not be written.
     *
     * @param inStream A stream which provides access to the zipped data. This
     *            need not be a ZipInputStream.
     * @param outputLocation When not {@code null}, this must be a directory.
     *            When {@code null} is given the system's temp directory will be
     *            used.
     * @param seed When this value is not "0", a directory in the outputLocation
     *            will created using the the pseudo-random number generator. The
     *            generator is guaranteed to generate the same results when
     *            created with the same seed, so providing the same inputs to
     *            this method will result in the same absolute locations.
     * @return The absolute paths to the files which exist after extraction
     *         including files which were skipped because they already exist. On
     *         failure, this will return an empty list.
     */
    public static List<String> explodeZip(InputStream inStream, File outputLocation, long seed)
    {
        final List<String> extractedPaths = new ArrayList<>();
        final String outputDir = getDirectory(outputLocation, seed);
        if (outputDir == null)
        {
            return extractedPaths;
        }

        ZipInputStream zipStream = null;
        try
        {
            if (inStream instanceof ZipInputStream)
            {
                zipStream = (ZipInputStream)inStream;
            }
            else
            {
                zipStream = new ZipInputStream(inStream);
            }
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null)
            {
                final File outFile = new File(outputDir, entry.getName());
                extractedPaths.add(outFile.getAbsolutePath());

                if (entry.isDirectory())
                {
                    if (!outFile.exists() && !outFile.mkdirs())
                    {
                        LOGGER.error("Failed to create directory: " + outFile);
                    }
                }
                else if ((outFile.getParentFile().exists() || outFile.getParentFile().mkdirs()) && outFile.createNewFile())
                {
                    final FileOutputStream outStream = new FileOutputStream(outFile);
                    try
                    {
                        final StreamReader reader = new StreamReader(zipStream);
                        reader.readStreamToOutputStream(outStream);
                    }
                    finally
                    {
                        outStream.close();
                    }
                }
                else
                {
                    LOGGER.error("Failed to create file: " + outFile.getAbsolutePath());
                }

                // Do not quit reading the stream if the entry cannot be closed.
                try
                {
                    zipStream.closeEntry();
                }
                catch (final IOException e)
                {
                    LOGGER.error("Could not close zip entry." + entry.getName(), e);
                }
            }
        }
        catch (final IOException e)
        {
            LOGGER.error("An error occurred while exploding the zip stream." + e, e);
            return null;
        }
        finally
        {
            if (zipStream != null)
            {
                try
                {
                    zipStream.close();
                }
                catch (final IOException e)
                {
                    LOGGER.warn("An error occurred while closing the zip stream." + e, e);
                }
            }
        }

        return extractedPaths;
    }

    /**
     * Get the basename of a file path (the portion before the last period, but
     * not including the path).
     *
     * @param file The file path.
     * @return The basename.
     */
    public static String getBasename(File file)
    {
        final String name = file.getName();
        final int ix = name.lastIndexOf('.');
        return ix >= 0 ? name.substring(0, ix) : name;
    }

    /**
     * Determine a usable directory if possible given a base directory and a
     * seed for the pseudo-random number generator.
     *
     * @param baseDirectory When not {@code null}, this must be a directory.
     *            When {@code null} is given the system's temp directory will be
     *            used.
     * @param seed When this value is not "0", a directory in the outputLocation
     *            will created using the the pseudo-random number generator. The
     *            generator is guaranteed to generate the same results when
     *            created with the same seed, so providing the same inputs to
     *            this method will result in the same absolute locations.
     * @return The generated directory or {@code null} if the directory was not
     *         created.
     */
    public static String getDirectory(File baseDirectory, long seed)
    {
        File outputDir = baseDirectory;
        if (outputDir == null)
        {
            outputDir = new File(System.getProperty("java.io.tmpdir"));
        }

        if (!outputDir.isDirectory())
        {
            LOGGER.error("Cannot write to location : " + outputDir);
        }

        if (seed != 0)
        {
            // Generate a unique directory to help avoid name conflicts
            final StringBuilder builder = new StringBuilder(outputDir.getAbsolutePath());
            builder.append(File.separator);

            final Random rand = new Random(seed);
            for (int i = 0; i < 15; ++i)
            {
                final int next = rand.nextInt(26);
                builder.append((char)('a' + next));
            }

            outputDir = new File(builder.toString());
            if (!outputDir.exists() && !outputDir.mkdir())
            {
                return null;
            }
        }

        return outputDir.getAbsolutePath();
    }

    /**
     * Returns the size of a directory.
     *
     * @param file The directory.
     * @return The number of bytes comprised by the directory.
     */
    public static long getDirSize(File file)
    {
        long size = 0;
        final File[] children = file.listFiles();
        if (children != null)
        {
            for (final File child : children)
            {
                size += child.length();
                size += getDirSize(child);
            }
        }
        return size;
    }

    /**
     * Return a list of files from the given directory that have the given file
     * extension.
     *
     * @param directory The directory where the files will be searched for.
     * @param extension The extension (".log" for example) of the files to
     *            search for.
     * @return The list of files in the directory that end with the given
     *         extension or null if there is a problem.
     */
    public static List<File> getFilesFromDirectory(File directory, final String extension)
    {
        if (!directory.isDirectory() || extension == null || extension.isEmpty())
        {
            LOGGER.warn("Unable to get files with extension " + extension + " from directory.");
            return null;
        }

        final File[] files = directory.listFiles((FilenameFilter)(dir, name) -> name.endsWith(extension));
        if (files != null)
        {
            return Arrays.asList(files);
        }
        return Collections.emptyList();
    }

    /**
     * Get the suffix of a file path (the portion after the last period) or null
     * if there is no suffix.
     *
     * @param file The file path.
     * @return The suffix or {@code null}.
     */
    public static String getSuffix(File file)
    {
        return getSuffix(file.getName());
    }

    /**
     * Get the suffix of a file path (the portion after the last period) or null
     * if there is no suffix.
     *
     * @param file The file path.
     * @return The suffix or {@code null}.
     */
    public static String getSuffix(String file)
    {
        final int ix = file.lastIndexOf('.');
        return ix > 0 ? file.substring(ix + 1) : null;
    }

    /**
     * Uses new i/o to read blocks of a file into a ByteBuffer.
     *
     * @param in - the File to be read
     * @return The file contents.
     */
    public static ByteBuffer readFileToBuffer(File in)
    {
        ByteBuffer buf;
        final List<ByteBuffer> list = new LinkedList<>();

        FileInputStream fis;
        try
        {
            fis = new FileInputStream(in);
            final FileChannel inChannel = fis.getChannel();
            int bytesRead = 0;
            int totalBytesRead = 0;

            while (true)
            {
                buf = ByteBuffer.allocate(READ_BLOCK_SIZE);
                bytesRead = inChannel.read(buf);
                buf.flip();
                totalBytesRead += buf.limit();
                list.add(buf);

                if (bytesRead == -1)
                {
                    break;
                }
            }

            buf = ByteBuffer.allocate(totalBytesRead);

            for (final ByteBuffer iter : list)
            {
                buf.put(iter);
            }
            buf.flip();

            fis.close();
        }
        catch (final IOException e)
        {
            LOGGER.error(e, e);
            buf = null;
        }

        return buf;
    }

    /**
     * Reads the lines of a file to a Collection of Strings.
     *
     * @param file The file
     * @return The List of Strings for each line
     */
    public static List<String> readLines(File file)
    {
        final List<String> lines = new ArrayList<>();
        try
        {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_CHARSET));
            try
            {
                for (String line; (line = bufferedReader.readLine()) != null;)
                {
                    lines.add(line);
                }
            }
            catch (final IOException e)
            {
                LOGGER.error("IOException reading error file: " + e, e);
            }
            finally
            {
                try
                {
                    bufferedReader.close();
                }
                catch (final IOException e)
                {
                    LOGGER.error("Failed to close input stream: " + e, e);
                }
            }
        }
        catch (final FileNotFoundException e)
        {
            LOGGER.error("Failed to read error file: " + e, e);
        }
        return lines;
    }

    /** Disallow instantiation. */
    private FileUtilities()
    {
    }
}
