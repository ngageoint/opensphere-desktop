package io.opensphere.core.util.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The class <b>Zip</b> is a wrapper to classes and methods int the
 * java.util.zip package. The static methods contained in this class are
 * intended to make using the java zip classes and methods more convenient.
 */
@SuppressWarnings("PMD.GodClass")
public final class Zip
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(Zip.class);

    /** The Constant DEFAULT_BUFFER_SIZE. */
    private static final int DEFAULT_BUFFER_SIZE = 2048;

    /**
     * Instantiates a new zip.
     */
    private Zip()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Creates a recursive listing of adapters from a directory.
     *
     * @param location the location
     * @param directory the directory
     * @param pAppendList the append list
     * @return the list
     */
    public static List<ZipInputAdapter> createAdaptersForDirectory(String location, File directory,
            List<ZipInputAdapter> pAppendList)
    {
        return createAdaptersForDirectory(location, directory, pAppendList, ZipEntry.DEFLATED);
    }

    /**
     * Creates a recursive listing of adapters from a directory.
     *
     * @param location the location
     * @param directory the directory
     * @param pAppendList the append list
     * @param compression the compression type, either ZipEntry.DEFLATED or
     *            ZipEntry.STORED
     * @return the list
     */
    public static List<ZipInputAdapter> createAdaptersForDirectory(String location, File directory,
            List<ZipInputAdapter> pAppendList, int compression)
    {
        List<ZipInputAdapter> appendList = pAppendList;
        if (appendList == null)
        {
            appendList = new ArrayList<>();
        }

        if (directory.isDirectory())
        {
            File[] children = directory.listFiles();
            if (children != null && children.length > 0)
            {
                String newLocation = location + File.separator + directory.getName();
                for (File child : children)
                {
                    if (child.isDirectory())
                    {
                        createAdaptersForDirectory(newLocation, child, appendList);
                    }
                    else
                    {
                        appendList.add(new ZipFileInputAdapter(newLocation, child, compression));
                    }
                }
            }
        }

        return appendList;
    }

    /**
     * This method is used to uncompress a GZIP compressed file. The output file
     * name will be the same as the input file name, without the .??? extension.
     *
     * @param zipFile the file to be unzipped
     * @param destDir the directory where you would like to place the unzipped
     *            file.
     * @return output file from the gunzip action
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static File gunzip(File zipFile, File destDir) throws IOException
    {
        int count;
        FileInputStream fis = new FileInputStream(zipFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        GZIPInputStream zis = new GZIPInputStream(bis);

        FileOutputStream fos;
        BufferedOutputStream bos;

        String outputFileName = zipFile.getName();
        outputFileName = outputFileName.substring(0, outputFileName.lastIndexOf('.'));
        File outputDir = destDir;
        File outputFile = new File(outputDir, outputFileName);

        byte[] data;

        data = new byte[DEFAULT_BUFFER_SIZE];
        fos = new FileOutputStream(outputFile);
        bos = new BufferedOutputStream(fos, DEFAULT_BUFFER_SIZE);
        while ((count = zis.read(data, 0, DEFAULT_BUFFER_SIZE)) != -1)
        {
            bos.write(data, 0, count);
        }
        bos.flush();
        bos.close();
        zis.close();
        bis.close();
        fis.close();
        return outputFile;
    }

    /**
     * The <code>unzip</code> method is used to unzip files and directories from
     * the input zip file.
     *
     * @param zipFile the input zip file.
     * @param destDir the location where files and directories are to be
     *            unzipped
     * @return {@link List}&lt;{@link File}&gt; of files and directories found
     *         in the zip file
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException If the thread is interrupted.
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    public static List<File> unzip(File zipFile, File destDir) throws IOException, InterruptedException
    {
        int count;
        List<File> files = new ArrayList<>();
        ZipEntry entry;
        try (FileInputStream fis = new FileInputStream(zipFile))
        {
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);

            FileOutputStream fos;
            BufferedOutputStream bos;

            File outputFile;
            File outputDir;

            byte[] data;

            while ((entry = zis.getNextEntry()) != null)
            {
                ThreadControl.check();
                if (entry.isDirectory())
                {
                    outputDir = new File(entry.getName());
                    if (!outputDir.mkdirs())
                    {
                        LOGGER.warn("Failed to create directory: " + outputDir.getAbsolutePath());
                    }
                }
                else
                {
                    outputFile = new File(destDir, entry.getName());
                    outputDir = outputFile.getParentFile();
                    if (!outputDir.exists() && !outputDir.mkdirs())
                    {
                        LOGGER.warn("Failed to create directory: " + outputDir.getAbsolutePath());
                    }
                    files.add(outputFile);
                    data = new byte[DEFAULT_BUFFER_SIZE];
                    fos = new FileOutputStream(outputFile);
                    bos = new BufferedOutputStream(fos, DEFAULT_BUFFER_SIZE);
                    while ((count = zis.read(data, 0, DEFAULT_BUFFER_SIZE)) != -1)
                    {
                        bos.write(data, 0, count);
                    }
                    bos.flush();
                    bos.close();
                }
            }
            zis.close();
        }

        return files;
    }

    /**
     * Zip the contents of a directory.
     *
     * @param zipfile The target file.
     * @param dir The directory.
     * @throws FileNotFoundException If the output file cannot be found.
     * @throws IOException Signals If there's an error reading or writing the
     *             files.
     */
    public static void zip(File zipfile, File dir) throws IOException
    {
        String[] files = dir.list();
        if (files != null)
        {
            zipfiles(zipfile, dir, Arrays.asList(files));
        }
        else
        {
            throw new IllegalArgumentException("Argument must be a directory.");
        }
    }

    /**
     * The <code>zip</code> method is used to zip one or more files into a
     * single zip file.
     *
     * @param zfile the output zip file
     * @param relDir the directory to which all files are relative. (example:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/files/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quot;c:/files&quot; in this case.
     * @param fStrs collection of relative file strings. These strings contain
     *            the paths and file names relative to the <code>relDir</code>.
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void zip(File zfile, File relDir, Collection<String> fStrs) throws IOException
    {
        zipfiles(zfile, relDir, fStrs);
    }

    /**
     * The <code>zip</code> method is used to zip one or more files into a
     * single zip file. This version of the zip method is intended to start with
     * a directory and zip its entire contents into the specified file. It will,
     * however, work just fine if the <code>inputDir</code> is a file, not a
     * directory.
     *
     * @param zfile the output zip file
     * @param relDir the directory to which all files are relative. (ex:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/files/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quot;c:/files&quot; in this case.
     * @param inputDir the input directory whose contents you want to zip.
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void zip(File zfile, File relDir, File inputDir) throws IOException
    {
        Collection<String> fStrs;
        if (inputDir.isDirectory())
        {
            fStrs = DirectoryListRecursion.getRelativeFiles(inputDir);
        }
        else
        {
            fStrs = Collections.singleton(inputDir.getAbsolutePath());
        }
        zipfiles(zfile, relDir, fStrs);
    }

    /**
     * The <code>zip</code> method is used to zip one or more files into a
     * single zip file.
     *
     * @param zipdir the directory where the output zip file will be placed
     * @param zipFileName the name of the output zip file
     * @param relDir the directory to which all files are relative. (ex:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/files/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quot;c:/files&quot; in this case.
     * @param fStrs collection of relative file strings. These strings contain
     *            the paths and file names relative to the <code>relDir</code>.
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void zip(File zipdir, String zipFileName, File relDir, Collection<String> fStrs) throws IOException
    {
        File zipFile = new File(zipdir, zipFileName);
        zipfiles(zipFile, relDir, fStrs);
    }

    /**
     * The <code>zip</code> method is used to zip one or more files into a
     * single zip file. This version of the zip method is intended to start with
     * a directory and zip its entire contents into the specified file. It will,
     * however, work just fine if the <code>inputDir</code> is a file, not a
     * directory.
     *
     * @param zipdir the directory where the output zip file will be placed
     * @param zipFileName the name of the output zip file
     * @param relDir the directory to which all files are relative. (ex:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/files/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quot;c:/files&quot; in this case.
     * @param inputDir the input directory whose contents you want to zip.
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void zip(File zipdir, String zipFileName, File relDir, File inputDir) throws IOException
    {
        Collection<String> fStrs;
        if (inputDir.isDirectory())
        {
            fStrs = DirectoryListRecursion.getAbsoluteFiles(inputDir);
        }
        else
        {
            fStrs = Collections.singleton(inputDir.getPath());
        }
        File zipFile = new File(zipdir, zipFileName);
        zipfiles(zipFile, relDir, fStrs);
    }

    /**
     * The <code>zip</code> method is used to zip one or more files into a
     * single zip file.
     *
     * @param zipPathAndFile the output zip files path and name
     * @param relDir the directory to which all files are relative. (example:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/files/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quot;c:/files&quot; in this case.
     * @param fStrs collection of relative file strings. These strings contain
     *            the paths and file names relative to the <code>relDir</code>.
     * @throws FileNotFoundException if the file is not found.
     * @throws IOException if an IOException is encountered.
     */
    public static void zip(String zipPathAndFile, File relDir, Collection<String> fStrs) throws IOException
    {
        File zipFile = new File(zipPathAndFile);
        zipfiles(zipFile, relDir, fStrs);
    }

    /**
     * The <code>zip</code> method is used to zip one or more files into a
     * single zip file. This version of the zip method is intended to start with
     * a directory and zip its entire contents into the specified file. It will,
     * however, work just fine if the <code>inputDir</code> is a file, not a
     * directory.
     *
     * @param zipPathAndFile the output zip files path and name
     * @param relDir the directory to which all files are relative. (example:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/files/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quot;c:/files&quot; in this case.
     * @param inputDir the input directory whose contents you want to zip.
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void zip(String zipPathAndFile, File relDir, File inputDir) throws IOException
    {
        Collection<String> fStrs;
        if (inputDir.isDirectory())
        {
            fStrs = DirectoryListRecursion.getAbsoluteFiles(inputDir);
        }
        else
        {
            fStrs = Collections.singleton(inputDir.getAbsolutePath());
        }
        File zipFile = new File(zipPathAndFile);
        zipfiles(zipFile, relDir, fStrs);
    }

    /**
     * Creates a Zip file given a list of inputs for the zip file. The inputs
     * can be anything that provide a name, location, input stream, and method
     *
     * @param zipFile - the output zip file
     * @param zipInputs - the list of {@link ZipInputAdapter} inputs
     * @param pm the {@link ProgressMonitor} for the zip.
     * @param progressByFiles if true updates progress for each file added to
     *            archive.
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    public static void zipfiles(File zipFile, List<ZipInputAdapter> zipInputs, final ProgressMonitor pm,
            final boolean progressByFiles)
        throws IOException
    {
        ZipOutputStream zipOS = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOS.setLevel(9);
        byte[] buf = new byte[1024 * 1024 * 10];
        long totalWritten = 0;
        CRC32 crc = new CRC32();
        boolean cancelled = false;
        int totalEntries = zipInputs.size();
        int entryCount = 0;
        for (ZipInputAdapter entry : zipInputs)
        {
            entryCount++;
            StringBuilder inputFileNameBuidler = new StringBuilder();
            if (entry.getLocation() != null)
            {
                inputFileNameBuidler.append(entry.getLocation()).append(File.separatorChar);
            }
            inputFileNameBuidler.append(entry.getName());

            String inputFileName = inputFileNameBuidler.toString();

            updateProgressMonitorWithCount(pm, totalEntries, entryCount, inputFileName);

            ZipEntry zEntry = new ZipEntry(inputFileName);
            zEntry.setMethod(entry.getMethod());
            if (entry.getMethod() == ZipEntry.STORED)
            {
                crc.reset();
                try (InputStream eIS = entry.getInputStream())
                {
                    int len;
                    while ((len = eIS.read(buf)) > 0)
                    {
                        crc.update(buf, 0, len);
                    }
                }
                entry.closeInputStream();

                zEntry.setSize(entry.getSize());
                zEntry.setCompressedSize(entry.getSize());
                zEntry.setCrc(crc.getValue());
            }
            if (pm != null && pm.isCanceled())
            {
                cancelled = true;
                break;
            }

            try (InputStream fis = entry.getInputStream();)
            {
                zipOS.putNextEntry(zEntry);
                int len;
                while ((len = fis.read(buf)) > 0)
                {
                    zipOS.write(buf, 0, len);
                    totalWritten += len;

                    updateProgressMonitorProgress(pm, progressByFiles, totalWritten, entryCount);
                }
            }
            zipOS.closeEntry();
            entry.closeInputStream();
            if (pm != null && pm.isCanceled())
            {
                cancelled = true;
                break;
            }
        }
        zipOS.close();
        if (cancelled && !zipFile.delete())
        {
            LOGGER.warn("Failed to delete zip file: " + zipFile.getAbsolutePath());
        }
    }

    /**
     * Update progress monitor progress.
     *
     * @param pm the {@link ProgressMonitor} to update.
     * @param progressByFiles the progress by files
     * @param totalWritten the total written
     * @param entryCount the entry count
     */
    private static void updateProgressMonitorProgress(final ProgressMonitor pm, final boolean progressByFiles,
            final long totalWritten, final int entryCount)
    {
        if (pm != null)
        {
            EventQueueUtilities.runOnEDT(() ->
            {
                if (progressByFiles)
                {
                    pm.setProgress(entryCount);
                }
                else
                {
                    pm.setProgress((int)totalWritten);
                }
            });
        }
    }

    /**
     * Update progress monitor with count.
     *
     * @param pm the progress monitor to which notification updates are sent.
     * @param totalEntries the total entries
     * @param entryCount the entry count
     * @param inputFileName the input file name
     */
    private static void updateProgressMonitorWithCount(final ProgressMonitor pm, final int totalEntries, final int entryCount,
            final String inputFileName)
    {
        if (pm != null)
        {
            EventQueueUtilities
                    .runOnEDT(() -> pm.setNote("Adding Entry " + entryCount + " of " + totalEntries + " : " + inputFileName));
        }
    }

    /**
     * This is the method that actually does the work of zipping the files.
     *
     * @param zipFile the zip file
     * @param relDir the relative directory
     * @param fileNames the file strings.
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void zipfiles(File zipFile, File relDir, Collection<String> fileNames) throws IOException
    {
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOutputStream.setLevel(9);
        byte[] buf = null;
        String inputFileName = null;
        for (String fileName : fileNames)
        {
            inputFileName = relDir.toString() + File.separatorChar + fileName;
            buf = null;
            buf = new byte[1024];
            ZipEntry entry = new ZipEntry(fileName);
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(inputFileName);
                zipOutputStream.putNextEntry(entry);
                int len;
                while ((len = fis.read(buf)) > 0)
                {
                    zipOutputStream.write(buf, 0, len);
                }
                zipOutputStream.closeEntry();
            }
            finally
            {
                if (fis != null)
                {
                    fis.close();
                }
            }
        }
        zipOutputStream.close();
    }
}
