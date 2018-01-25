/**
 * FileZipper.java Oct 9, 2007
 *
 */
package io.opensphere.core.common.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class is used to create an object that is extends the {@link File}
 * class. The object will be a zip file created from the files in the
 * {@link String}[] is a parameter of the constructor.
 *
 * The created zip file will be a compressed, rather than stored zip file, using
 * the maximum compression available. Each entry in the zip file will have
 * relative path as define by input {@link String}[].
 */
public class FileZipper extends File
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private ZipOutputStream zipOS;

    /**
     *
     *
     * @param zipPathAndFile the absolute path and file name for the created zip
     *            file
     * @param dir the directory to which all input paths and files are relative
     * @param fStrs the input paths and files. These paths are relative to
     *            &quot;dir&quot;
     * @throws FileNotFoundException
     * @throws IOException
     *
     *
     */
    public FileZipper(String zipPathAndFile, File dir, String[] fStrs) throws FileNotFoundException, IOException
    {
        super(zipPathAndFile);
        // zipFile = this;
        zipfiles(dir, fStrs);
    }

    /**
     * @param zipdir {@link File} object representing the directory where the
     *            output zip file will be created
     * @param zipFileName {@link String} object containing the output zip file's
     *            name
     * @param dir the directory to which all input paths and files are relative
     * @param fStrs the input paths and files. These paths are relative to
     *            &quot;dir&quot;
     * @throws FileNotFoundException
     * @throws IOException FileZipper
     *
     */
    public FileZipper(File zipdir, String zipFileName, File dir, String[] fStrs) throws FileNotFoundException, IOException
    {
        super(zipdir, zipFileName);
        // zipFile = this;
        zipfiles(dir, fStrs);
    }

    /**
     * @param zfile {@link File} object representing the output zip file
     * @param dir the directory to which all input paths and files are relative
     * @param fStrs the input paths and files. These paths are relative to
     *            &quot;dir&quot;
     * @throws FileNotFoundException
     * @throws IOException FileZipper
     *
     */
    public FileZipper(File zfile, File dir, String[] fStrs) throws FileNotFoundException, IOException
    {
        super(zfile.toString());
        // zipFile = this;
        if (!zfile.getParentFile().exists())
        {
            zfile.getParentFile().mkdirs();
        }
        zipfiles(dir, fStrs);
    }

    /**
     * @param dir the directory to which all input paths and files are relative
     * @param fileStrs the input paths and files. These paths are relative to
     *            &quot;dir&quot;
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void zipfiles(File dir, String[] fileStrs) throws FileNotFoundException, IOException
    {
        // System.out.println("zipfiles(): this.toString() = "+this.toString());
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        zipOS = new ZipOutputStream(new FileOutputStream(this));
        zipOS.setLevel(9);
        byte[] buf = null;
        String inputFileName = null;
        for (int i = 0; i < fileStrs.length; i++)
        {
            inputFileName = "" + dir.toString() + File.separatorChar + fileStrs[i];
            buf = null;
            buf = new byte[1024];
            ZipEntry entry = new ZipEntry(fileStrs[i]);
            // entry.setMethod(ZipEntry.DEFLATED);
            // FileInputStream FIS = new
            // FileInputStream(dir.toString()+File.separatorChar+files[i].toString());
            FileInputStream FIS = new FileInputStream(inputFileName);
            zipOS.putNextEntry(entry);
            int len;
            while ((len = FIS.read(buf)) > 0)
            {
                zipOS.write(buf, 0, len);
            }
            zipOS.closeEntry();
            FIS.close();
        }
        zipOS.close();

    }

}
