package io.opensphere.core.common.shapefile.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.shapefile.prj.Projection;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;

public class ShapefileZipUtil
{

    /**
     * Constructs an <code>ESRIShapefile</code> from a Zip stream. If multiple
     * shapefiles are stored in the Zip, only the first will be returned.
     * Additional files stored in the Zip will not be extracted. <br>
     * TODO:
     * <li>handle multiple shape files</li>
     * <li>handle user specified directory better</li> <br>
     *
     * @param zipStream the <code>ZipInputStream</code> from which an
     *            <code>ESRIShapefile</code> will be created.
     * @param outputDir if <code> null </code> or "" is passed the default is
     *            used <code>System.getProperty("java.io.tmpdir")</code>
     * @return the first <code>ESRIShapefile</code> found in the Zip or
     *         <code>null</code> if one was not found.
     * @throws IOException if an error occurs while processing the Zip.
     */
    public static ESRIShapefile createFromZipFile(ZipInputStream zipStream, String pOutputDir) throws IOException
    {

        // Setup the output directory
        String outputDir = null;
        if (StringUtils.isEmpty(pOutputDir))
        {
            outputDir = System.getProperty("java.io.tmpdir");
        }
        else
        {
            outputDir = pOutputDir;
        }

        ESRIShapefile shapefile = null;
        List<File> tempFiles = new ArrayList<>();
        String selectedEntryName = null;
        String shapeName = null;
        while (tempFiles.size() < 3)
        {
            ZipEntry entry = zipStream.getNextEntry();

            // Break after the last entry.
            if (entry == null)
            {
                break;
            }

            int lastDecimalIndex = entry.getName().lastIndexOf('.');

            // If the entry is a directory or does not contain a dot, skip it.
            if (entry.isDirectory() || (lastDecimalIndex < 0))
            {
                zipStream.closeEntry();
                continue;
            }

            // TODO: Shapefiles stored in directories within the Zip are not
            // properly handled.
            int lastSlashIndex = entry.getName().lastIndexOf('/');
            String entryPath = entry.getName().substring(0, lastSlashIndex + 1);
            String entryName = entry.getName().substring(0, lastDecimalIndex);
            String extension = entry.getName().substring(lastDecimalIndex + 1);

            // If the entry has a standard shapefile extension, determine if the
            // entry should be extracted.
            if (extension.equalsIgnoreCase("shp") || extension.equalsIgnoreCase("shx") || extension.equalsIgnoreCase("dbf"))
            {
                // If a shapefile hasn't been selected from the zip, do so now.
                if (selectedEntryName == null)
                {
                    selectedEntryName = entryName;
                    shapeName = outputDir + File.separator + selectedEntryName;
                }

                // If the paths don't match or current entry name does not match
                // the
                // selected entry name, skip it.
                else if (!selectedEntryName.startsWith(entryPath) || !selectedEntryName.equalsIgnoreCase(entryName))
                {
                    zipStream.closeEntry();
                    continue;
                }
            }

            // Otherwise, skip it.
            else
            {
                zipStream.closeEntry();
                continue;
            }

            // Store the file
            String tmpFileName = shapeName + "." + extension.toLowerCase();
            FileOutputStream tmpFile = null;
            try
            {
                tmpFile = new FileOutputStream(tmpFileName);
                long size = IOUtils.copyLarge(zipStream, tmpFile);

                // Report an error if the sizes don't match.
                if (entry.getSize() != size)
                {
                    System.err.println("ESRIShapefile.createFromZipFile: The size of " + entry.getName() + " was expected to be "
                            + entry.getSize() + " but " + size + " bytes were read");
                }
            }
            finally
            {
                if (tmpFile != null)
                {
                    tmpFile.close();
                }
            }

            // Break out of the loop if all three entries have been processed.
            tempFiles.add(new File(tmpFileName));
        }

        // If all three files that make up a shapefile are found, build it.
        if (tempFiles.size() == 3)
        {
            shapefile = new ESRIShapefile(ESRIShapefile.Mode.READ, shapeName);
        }
        else if (selectedEntryName != null)
        {
            System.err.println("Only found " + tempFiles.size() + " of 3 parts of the shapefile");

            // Delete the unzipped files from the file system.
            for (File file : tempFiles)
            {
                if (!file.delete())
                {
                    file.deleteOnExit();
                }
            }
        }

        return shapefile;
    }

    /**
     * Writes a <code>ESRIShapefile</code> to a <code>ZipOutputStream</code>
     *
     * @param source
     * @param zipStream
     * @throws IOException
     */
    public static void writeToZipStream(ESRIShapefile source, ZipOutputStream zipStream) throws IOException
    {
        if (source.getMode() == ESRIShapefile.Mode.WRITE && source.isClosed())
        {
            source.flipMode();
        }
        // else, error of some sort?

        String fileName = source.getFilePath().substring(source.getFilePath().lastIndexOf(File.separator) + 1);

        writeShpToZip(source, zipStream, fileName);
        writeShxToZip(source, zipStream, fileName.replace(ESRIShapefile.POSTFIX_SHP, ESRIShapefile.POSTFIX_SHX));
        writeDbfToZip(source, zipStream, fileName.replace(ESRIShapefile.POSTFIX_SHP, ESRIShapefile.POSTFIX_DBF));

        zipStream.finish();
    }

    /**
     * Writes a <code>ESRIShapefile</code> to a <code>ZipOutputStream</code>
     *
     * @param source
     * @param zipStream
     * @throws IOException
     */
    public static void writeToZipStream(ESRIShapefile source, ZipOutputStream zipStream, Projection prj) throws IOException
    {
        if (source.getMode() == ESRIShapefile.Mode.WRITE && source.isClosed())
        {
            source.flipMode();
        }
        // else, error of some sort?

        String fileName = source.getFilePath().substring(source.getFilePath().lastIndexOf(File.separator) + 1);

        writeShpToZip(source, zipStream, fileName);
        writeShxToZip(source, zipStream, fileName.replace(ESRIShapefile.POSTFIX_SHP, ESRIShapefile.POSTFIX_SHX));
        writeDbfToZip(source, zipStream, fileName.replace(ESRIShapefile.POSTFIX_SHP, ESRIShapefile.POSTFIX_DBF));
        writePrjToZip(source, zipStream, fileName.replace(ESRIShapefile.POSTFIX_SHP, ESRIShapefile.POSTFIX_PRJ), prj);
        zipStream.flush();
        zipStream.finish();
    }

    private static void writeShxToZip(ESRIShapefile source, ZipOutputStream zipStream, String fileName) throws IOException
    {
        // Write the shx member to the stream
        ZipEntry entry = new ZipEntry(fileName);
        zipStream.putNextEntry(entry);
        InputStream is = null;
        try
        {
            is = source.getShp().getIndexAsInputStream();
            long size = IOUtils.copyLarge(is, zipStream);
            entry.setSize(size);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    private static String writeShpToZip(ESRIShapefile source, ZipOutputStream zipStream, String fileName) throws IOException
    {

        ZipEntry entry = new ZipEntry(fileName);
        zipStream.putNextEntry(entry);

        InputStream is = null;

        // Write the the shp member to the stream
        try
        {
            is = source.getShp().getAsInputStream();
            long size = IOUtils.copyLarge(is, zipStream);
            entry.setSize(size);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
        return fileName;
    }

    private static void writeDbfToZip(ESRIShapefile source, ZipOutputStream zipStream, String fileName) throws IOException
    {
        // Write the dbf to the stream
        ZipEntry entry = new ZipEntry(fileName);
        entry.setSize(source.getDbf().size());
        zipStream.putNextEntry(entry);
        InputStream is = null;
        try
        {
            is = source.getDbf().getAsInputStream();
            long size = IOUtils.copyLarge(is, zipStream);
            entry.setSize(size);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    private static void writePrjToZip(ESRIShapefile source, ZipOutputStream zipStream, String fileName, Projection prj)
        throws IOException
    {

        // Write the prj to the stream
        ZipEntry entry = new ZipEntry(fileName);

        ByteArrayInputStream bis = new ByteArrayInputStream(prj.getWellKnownText().getBytes());
        entry.setSize(prj.getWellKnownText().getBytes().length);
        zipStream.putNextEntry(entry);
        try
        {

            long size = IOUtils.copyLarge(bis, zipStream);
            entry.setSize(size);
        }
        finally
        {
            if (bis != null)
            {
                bis.close();
            }
        }
    }

}
