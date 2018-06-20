package io.opensphere.core.common.util.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/**
 * The class <b>Zip</b> is a wrapper to classes and methods int the
 * java.util.zip package. The static methods contained in this class are
 * intended to make using the java zip classes and methods more convenient.
 */
public class Zip
{

    /**
     * This method is used to uncompress a GZIP compressed file. The output file
     * name will be the same as the input file name, without the .??? extension.
     *
     * @param zipFile the file to be unzipped
     * @param destDir the directory where you would like to place the unzipped
     *            file.
     * @return output file from the gunzip action
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static File gunzip(File zipFile, File destDir) throws FileNotFoundException, IOException
    {
        final int BUFFER_SIZE = 2048;
        int count;
        FileInputStream fis = new FileInputStream(zipFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        GZIPInputStream zis = new GZIPInputStream(bis);

        FileOutputStream fos;
        BufferedOutputStream bos;

        String outputFileName = zipFile.getName();
        outputFileName = outputFileName.substring(0, outputFileName.lastIndexOf("."));
        File outputDir = destDir;
        File outputFile = new File(outputDir, outputFileName);

        byte[] data;

        data = new byte[BUFFER_SIZE];
        fos = new FileOutputStream(outputFile);
        bos = new BufferedOutputStream(fos, BUFFER_SIZE);
        while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1)
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
     * <code>untgz</code> is used to retrieve directories and files from a
     * TarBall, or a TARred and GZIPped group of files.
     *
     * @param tgzFile the input GZIPped TAR file, generally with a .tgz
     *            extension. The .tgz extension is not a requirement.
     * @param destDir the location where you would like the contents of the TAR
     *            file placed.
     * @return {@link ArrayList}<{@link File}> of files contained in the TAR
     *         file
     * @throws FileNotFoundException
     * @throws IOException
     */
    // TODO this method is not available because we do not have the Tar Package
    // approved yet ....
    // public static ArrayList<File> untgz(File tgzFile, File destDir) throws
    // FileNotFoundException, IOException
    // {
    // File tempDir = new File(System.getProperty("java.io.tmpdir"));
    // File tarFile = gunzip(tgzFile, tempDir);
    // ArrayList<File> files = Tar.untar(tarFile, destDir);
    // tarFile.delete();
    // return files;
    // }

    // ************** ZIP METHODS *******************//

    /**
     * The <code>zip</code> method is used to zip one or more files into a
     * single zip file.
     *
     * @param zipPathAndFile the output zip files path and name
     * @param relDir the directory to which all files are relative. (ex:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/myfiles/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quote;c:/myfiles%quote; in this case.
     * @param fStrs array of relative file strings. These strings contain the
     *            paths and file names relative to the <code>relDir</code>.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(String zipPathAndFile, File relDir, String[] fStrs) throws FileNotFoundException, IOException
    {
        File zipFile = new File(zipPathAndFile);
        zipfiles(zipFile, relDir, fStrs);
    }

    /**
     * The <code>zip</code> method is used to zip one or more files into a
     * single zip file.
     *
     * @param zipdir the directory where the output zip file will be placed
     * @param zipFileName the name of the output zip file
     * @param relDir the directory to which all files are relative. (ex:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/myfiles/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quote;c:/myfiles%quote; in this case.
     * @param fStrs array of relative file strings. These strings contain the
     *            paths and file names relative to the <code>relDir</code>.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(File zipdir, String zipFileName, File relDir, String[] fStrs) throws FileNotFoundException, IOException
    {
        File zipFile = new File(zipdir, zipFileName);
        zipfiles(zipFile, relDir, fStrs);
    }

    /**
     * The <code>zip</code> method is used to zip one or more files into a
     * single zip file.
     *
     * @param zfile the output zip file
     * @param relDir the directory to which all files are relative. (ex:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/myfiles/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quote;c:/myfiles%quote; in this case.
     * @param fStrs array of relative file strings. These strings contain the
     *            paths and file names relative to the <code>relDir</code>.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(File zfile, File relDir, String[] fStrs) throws FileNotFoundException, IOException
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
     * @param zipPathAndFile the output zip files path and name
     * @param relDir the directory to which all files are relative. (ex:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/myfiles/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quote;c:/myfiles%quote; in this case.
     * @param inputDir the input directory whose contents you want to zip.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(String zipPathAndFile, File relDir, File inputDir) throws FileNotFoundException, IOException
    {
        String[] fStrs = null;
        if (inputDir.isDirectory())
        {
            fStrs = DirectoryListRecursion.getAbsoluteFiles(inputDir);
        }
        else
        {
            fStrs = new String[1];
            fStrs[0] = inputDir.getAbsolutePath();
        }
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
     * @param zipdir the directory where the output zip file will be placed
     * @param zipFileName the name of the output zip file
     * @param relDir the directory to which all files are relative. (ex:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/myfiles/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quote;c:/myfiles%quote; in this case.
     * @param inputDir the input directory whose contents you want to zip.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(File zipdir, String zipFileName, File relDir, File inputDir) throws FileNotFoundException, IOException
    {
        String[] fStrs = null;
        if (inputDir.isDirectory())
        {
            fStrs = DirectoryListRecursion.getAbsoluteFiles(inputDir);
        }
        else
        {
            fStrs = new String[1];
            fStrs[0] = inputDir.getPath();
        }
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
     * @param zfile the output zip file
     * @param relDir the directory to which all files are relative. (ex:
     *            data/mydata.csv is the relative path of an absolute path like
     *            c:/myfiles/data/mydata.csv. The parameter <code>relDir</code>
     *            would be &quote;c:/myfiles%quote; in this case.
     * @param inputDir the input directory whose contents you want to zip.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(File zfile, File relDir, File inputDir) throws FileNotFoundException, IOException
    {
        String[] fStrs = null;
        if (inputDir.isDirectory())
        {
            fStrs = DirectoryListRecursion.getRelativeFiles(inputDir);
        }
        else
        {
            fStrs = new String[1];
            fStrs[0] = inputDir.getAbsolutePath();
        }
        zipfiles(zfile, relDir, fStrs);
    }

    /**
     * This is the method that actually does the work of zipping the files.
     *
     * @param zipFile
     * @param relDir
     * @param fileStrs
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void zipfiles(File zipFile, File relDir, String[] fileStrs) throws FileNotFoundException, IOException
    {
        ZipOutputStream zipOS = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOS.setLevel(9);
        byte[] buf = null;
        String inputFileName = null;
        for (int i = 0; i < fileStrs.length; i++)
        {
            inputFileName = "" + relDir.toString() + File.separatorChar + fileStrs[i];
            buf = null;
            buf = new byte[1024];
            ZipEntry entry = new ZipEntry(fileStrs[i]);
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

    /**
     * Creates a Zip file given a list of inputs for the zip file. The inputs
     * can be anything that provide a name, location, input stream, and method
     *
     * @param zipFile - the output zip file
     * @param zipInputs - the list of {@link ZipInputAdapter} inputs
     * @param pm the progress monitor to hook into
     * @param progressByFiles TODO
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zipfiles(File zipFile, List<ZipInputAdapter> zipInputs, final ProgressMonitor pm,
            final boolean progressByFiles)
        throws FileNotFoundException, IOException
    {
        ZipOutputStream zipOS = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOS.setLevel(9);
        byte[] buf = new byte[1024 * 1024 * 10];
        long totalWritten = 0;
        CRC32 crc = new CRC32();
        boolean canceled = false;
        int totalEntries = zipInputs.size();
        int entryCount = 0;
        for (ZipInputAdapter entry : zipInputs)
        {
            entryCount++;
            String inputFileName = "";
            if (entry.getLocation() != null)
            {
                inputFileName += entry.getLocation() + File.separatorChar;
            }
            inputFileName += entry.getName();

            if (pm != null)
            {
                final String note = inputFileName;
                final int count = entryCount;
                final int totEntries = totalEntries;
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        pm.setNote("Adding Entry " + count + " of " + totEntries + " : " + note);
                    }
                });
            }

            ZipEntry zEntry = new ZipEntry(inputFileName);
            zEntry.setMethod(entry.getMethod());
            if (entry.getMethod() == ZipEntry.STORED)
            {
                crc.reset();
                InputStream eIS = entry.getInputStream();
                int len;
                while ((len = eIS.read(buf)) > 0)
                {
                    crc.update(buf, 0, len);
                }
                entry.closeInputStream();

                zEntry.setSize(entry.getSize());
                zEntry.setCompressedSize(entry.getSize());
                zEntry.setCrc(crc.getValue());
            }
            if (pm != null && pm.isCanceled())
            {
                canceled = true;
                break;
            }

            InputStream FIS = entry.getInputStream();
            zipOS.putNextEntry(zEntry);
            int len;
            while ((len = FIS.read(buf)) > 0)
            {
                zipOS.write(buf, 0, len);
                totalWritten += len;

                if (pm != null)
                {
                    final int progress = (int)totalWritten;
                    final int fCount = entryCount;
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (progressByFiles)
                            {
                                pm.setProgress(fCount);
                            }
                            else
                            {
                                pm.setProgress(progress);
                            }
                        }
                    });
                }
            }
            zipOS.closeEntry();
            entry.closeInputStream();
            if (pm != null && pm.isCanceled())
            {
                canceled = true;
                break;
            }
        }
        zipOS.close();
        if (canceled)
        {
            zipFile.delete();
        }
    }

    /**
     * The <code>unzip</code> method is used to unzip files and directories from
     * the input zip file.
     *
     * @param zipFile the input zip file.
     * @param destDir the location where files and directories are to be
     *            unzipped
     * @return {@link ArrayList}<{@link File}> of files and directories found in
     *         the zip file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static ArrayList<File> unzip(File zipFile, File destDir) throws FileNotFoundException, IOException
    {
        final int BUFFER_SIZE = 2048;
        int count;
        ArrayList<File> files = new ArrayList<>();
        ZipEntry entry;
        FileInputStream fis = new FileInputStream(zipFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipInputStream zis = new ZipInputStream(bis);

        FileOutputStream fos;
        BufferedOutputStream bos;

        File outputFile;
        File outputDir;

        byte[] data;

        while ((entry = zis.getNextEntry()) != null)
        {
            if (entry.isDirectory())
            {
                outputDir = new File(entry.getName());
                outputDir.mkdirs();
            }
            else
            {
                outputFile = new File(destDir, entry.getName());
                outputDir = outputFile.getParentFile();
                if (!outputDir.exists())
                {
                    outputDir.mkdirs();
                }
                files.add(outputFile);
                data = new byte[BUFFER_SIZE];
                fos = new FileOutputStream(outputFile);
                bos = new BufferedOutputStream(fos, BUFFER_SIZE);
                while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1)
                {
                    bos.write(data, 0, count);
                }
                bos.flush();
                bos.close();
            }
        }
        zis.close();

        return files;
    }

    /**
     * Creates a recursive listing of adapters from a directory
     *
     * @param location
     * @param directory
     * @param appendList
     * @return list of input adapters
     */
    public static List<ZipInputAdapter> createAdaptersForDirectory(String location, File directory,
            List<ZipInputAdapter> appendList)
    {
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
                        appendList.add(new ZipFileInputAdapter(newLocation, child, ZipEntry.DEFLATED));
                    }
                }
            }
        }

        return appendList;
    }

    /** A ZipInputAdapter. */
    public static abstract class ZipInputAdapter
    {
        /** Zip method. Defaults to DEFLATED. */
        int myMethod = ZipEntry.DEFLATED;

        /**
         * Constructor.
         *
         * @param method {@link ZipEntry} method
         */
        public ZipInputAdapter(int method)
        {
            myMethod = method;
        }

        /**
         * Gets the method.
         *
         * @return the method
         */
        public int getMethod()
        {
            return myMethod;
        }

        /**
         * Gets the location.
         *
         * @return the location
         */
        public abstract String getLocation();

        /**
         * Gets the name.
         *
         * @return the name
         */
        public abstract String getName();

        /**
         * Gets the size.
         *
         * @return the size
         */
        public long getSize()
        {
            return 0;
        }

        /**
         * Gets the input stream.
         *
         * @return the stream
         * @throws IOException
         */
        public abstract InputStream getInputStream() throws IOException;

        /**
         * Closes the input stream.
         *
         * @throws IOException
         */
        public void closeInputStream() throws IOException
        {
        };
    }

    /** Instance of ZipInputAdapter that reads a file. */
    public static class ZipFileInputAdapter extends ZipInputAdapter
    {
        /** The file. */
        File myFile;

        /** The location of the file. */
        String myLocation;

        /** The FileInputStream. */
        FileInputStream myFIS;

        /**
         * Constructor.
         *
         * @param location the location
         * @param aFile the file object
         * @param method the Zip method
         */
        public ZipFileInputAdapter(String location, File aFile, int method)
        {
            super(method);
            myFile = aFile;
            myLocation = location;
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
            if (myFIS == null)
            {
                myFIS = new FileInputStream(myFile);
            }
            return myFIS;
        }

        @Override
        public String getLocation()
        {
            return myLocation;
        }

        @Override
        public long getSize()
        {
            return myFile.length();
        }

        @Override
        public String getName()
        {
            return myFile.getName();
        }

        @Override
        public void closeInputStream() throws IOException
        {
            if (myFIS != null)
            {
                myFIS.close();
                myFIS = null;
            }
        }
    }

    /** Instance of ZipInputAdapter that reads a byte array. */
    public static class ZipByteArrayInputAdapter extends ZipInputAdapter
    {
        /** The location. */
        String myLocation;

        /** The name. */
        String myName;

        /** The ByteArrayInputStream. */
        ByteArrayInputStream myBAIS;

        /** The byte array. */
        byte[] myByteArray;

        /**
         * Constructor.
         *
         * @param name the name
         * @param location the location
         * @param byteArray the byte array
         * @param method the zip method
         */
        public ZipByteArrayInputAdapter(String name, String location, byte[] byteArray, int method)
        {
            super(method);
            myName = name;
            myLocation = location;
            myByteArray = byteArray;
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
            if (myBAIS == null)
            {
                myBAIS = new ByteArrayInputStream(myByteArray);
            }
            return myBAIS;
        }

        @Override
        public String getLocation()
        {
            return myLocation;
        }

        @Override
        public String getName()
        {
            return myName;
        }

        @Override
        public void closeInputStream() throws IOException
        {
            if (myBAIS != null)
            {
                myBAIS.close();
                myBAIS = null;
            }
        }

    }

    /**
     * Zips files in a directory into a buffered byte array.
     *
     * @param dir the directory to read
     * @param fileStrs the filenames to zip
     * @return an output stream for the byte array
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static ByteArrayOutputStream zipToBufferedOutput(File dir, String[] fileStrs) throws FileNotFoundException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOS = new ZipOutputStream(baos);
        zipOS.setLevel(9);
        byte[] buf = null;
        String inputFileName = null;
        for (int i = 0; i < fileStrs.length; i++)
        {
            inputFileName = "" + dir.toString() + File.separatorChar + fileStrs[i];
            buf = null;
            buf = new byte[1024];
            ZipEntry entry = new ZipEntry(fileStrs[i]);
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
        baos.close();
        return baos;
    }

}