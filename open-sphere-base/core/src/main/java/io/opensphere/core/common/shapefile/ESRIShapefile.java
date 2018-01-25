/**
 *
 */
package io.opensphere.core.common.shapefile;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import io.opensphere.core.common.shapefile.shapes.PolygonRecord;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;

/**
 * A utility class representing an ESRI Shapefile per the ERSI specification
 * dated July 1998.
 *
 * Currently only supports reading.
 *
 * A shapefile consists of 3 parts, a main (shp), an index (shx) and a dBASE
 * (dbf) This class represents the superset of the three. To construct, pass the
 * path of the three files and the name. Per the ESRI spec, it will be assumed
 * that all three files will have the same name and will exist in the same
 * directory.
 *
 * Supported shape types - point - polyline - polygon - ... - PolyLineM (for
 * Brat)
 *
 * This package has been redesigned to minimize the memory footprint while
 * parsing the file. In our environment it appears that most/all of the
 * shapefiles we work with are huge; making this the correct choice. As a
 * consequence, the per-record retrieve time is a bit inefficient. If you
 * attempt to optimize the access speed, please be kind on the memory footprint.
 *
 * When using the list interfaces, be aware of the memory and performance
 * implications of calling anything that will require an operation to be done on
 * the entire list.
 *
 * @deprecated Deprecated to com.bitsys.common.shapefile.v2.ESRIShapefile
 * @see io.opensphere.core.common.shapefile.v2.ESRIShapefile
 *
 */
@Deprecated
public class ESRIShapefile implements Collection<ShapefileRecord>
{
    // enums
    public enum ShapeFormat
    {
        INTERNAL
        // GML,
        // ORACLE
    }

    public enum MetadataFormat
    {
        STRING, ACTUAL;
    }

    public enum Mode
    {
        READ, WRITE;
    }

    public class ESRIShapefileIterator implements Iterator<ShapefileRecord>
    {
        ESRIShapefile parent = null;

        Iterator<ShapeRecord> shpItr = null;

        Iterator<Object[]> dbfItr = null;

        public ESRIShapefileIterator(ESRIShapefile es)
        {
            parent = es;
            shpItr = parent.shp.iterator();
            dbfItr = parent.dbf.iterator();
        }

        @Override
        public boolean hasNext()
        {
            return shpItr.hasNext() && dbfItr.hasNext();
        }

        @Override
        public ShapefileRecord next()
        {
            return new ShapefileRecord(shpItr.next(), dbfItr.next());
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    public static void writeToZipStream(ESRIShapefile source, ZipOutputStream zipStream) throws IOException
    {
        if (source.mode == Mode.WRITE && source.isClosed)
        {
            source.flipMode();
            // else, error of some sort?
        }

        String fileName = source.filePath.substring(source.filePath.lastIndexOf(File.separator) + 1);
        ZipEntry entry = new ZipEntry(fileName);
        zipStream.putNextEntry(entry);
        InputStream is = null;
        try
        {
            is = source.shp.getAsInputStream();
            IOUtils.copyLarge(is, zipStream);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }

        entry = new ZipEntry(fileName.replace(".shp", ".shx"));
        zipStream.putNextEntry(entry);
        is = null;
        try
        {
            is = source.shp.getIndexAsInputStream();
            IOUtils.copyLarge(is, zipStream);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }

        entry = new ZipEntry(fileName.replace(".shp", ".dbf"));
        zipStream.putNextEntry(entry);
        is = null;
        try
        {
            is = source.dbf.getAsInputStream();
            IOUtils.copyLarge(is, zipStream);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }

        zipStream.finish();
    }

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
     * @param inDeleteFiles <code>true</code> indicates that the physical files
     *            are owned by this instance and will be deleted when
     *            {@link #close()} is called. <code>false</code> means that the
     *            files will remain.
     * @return the first <code>ESRIShapefile</code> found in the Zip or
     *         <code>null</code> if one was not found.
     * @throws IOException if an error occurs while processing the Zip.
     */
    public static ESRIShapefile createFromZipFile(ZipInputStream zipStream, String outputDir, boolean inDeleteFiles)
        throws IOException
    {
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
            if (entry.isDirectory() || lastDecimalIndex < 0)
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
                    if (outputDir == null)
                    {
                        shapeName = System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis() + "_"
                                + selectedEntryName;
                    }
                    else
                    {
                        shapeName = outputDir + File.separator + selectedEntryName;
                    }

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
            shapefile = new ESRIShapefile(Mode.READ, shapeName, inDeleteFiles);
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

    // objects
    private Mode mode;

    private MainPortion shp = null;

    private DBASEPortion dbf = null;

    private ProjectionPortion prj = null;

    private String filePath = null;

    private boolean deleteFiles = false;

    private boolean isClosed = false;

    /**
     * Default CTOR
     */
    protected ESRIShapefile()
    {
    }

    /**
     * A constructor that takes a file name. The physical files will <i>not</i>
     * be deleted when the shapefile is closed.
     *
     * @param inMode Read, Write
     * @param inFilePath Path to the file
     * @throws FileNotFoundException
     */
    public ESRIShapefile(Mode inMode, String inFilePath) throws FileNotFoundException
    {
        this(inMode, inFilePath, false);
    }

    /**
     * A constructor that takes a file name.
     *
     * @param inMode Read, Write
     * @param inFilePath Path to the file
     * @param inDeleteFiles <code>true</code> indicates that the physical files
     *            are owned by this instance and will be deleted when
     *            {@link #close()} is called. <code>false</code> means that the
     *            files will remain.
     * @throws FileNotFoundException
     */
    public ESRIShapefile(Mode inMode, String inFilePath, boolean inDeleteFiles) throws FileNotFoundException
    {
        // Construct subportions.
        // No file access should happen in the following constructors.
        // Intentionally not catching the FNF exception for the shape portion
        // W/O the shape portion, you are royally screwed. You
        // can somewhat live without the other portions.

        mode = inMode;
        filePath = inFilePath;
        if (!filePath.toLowerCase().endsWith(".shp"))
        {
            filePath = filePath.concat(".shp");
        }
        deleteFiles = inDeleteFiles;
        shp = new MainPortion(mode, filePath);

        try
        {
            // prep. Should probably throw an exception if this fails.
            shp.doPrep();

            String idxPath = null;
            String[] extPos = { ".SHX", ".shx", ".Shx" };

            // Not fatal if the following don't exist.

            // Search for the matching SHX file, the extension could be
            // upper, or lower case or mixed case so try to find it.
            idxPath = filePath.substring(0, filePath.length() - 4);
            for (String ext : extPos)
            {
                File aFile = new File(idxPath + ext);
                if (aFile.exists())
                {
                    idxPath = aFile.getAbsolutePath();
                    break;
                }
            }
            // If we didn't find it just append lower case .shx.
            if (!idxPath.toLowerCase().endsWith(".shx"))
            {
                idxPath = idxPath.concat(".shx");
            }

            IndexPortion shx = new IndexPortion(mode, idxPath);

            // prep the index file
            if (shx != null)
            {
                shx.doPrep();
                shp.addIndex(shx);
            }

            // Not fatal if the following don't exist.
            // Search for the matching DBF file, the extension could be
            // upper, or lower case or mixed case so try to find it.
            String dbfPath = null;
            String[] dbfExtPos = { ".DBF", ".dbf", ".Dbf" };

            // Not fatal if the following don't exist.
            dbfPath = filePath.substring(0, filePath.length() - 4);
            for (String ext : dbfExtPos)
            {
                File aFile = new File(dbfPath + ext);
                if (aFile.exists())
                {
                    dbfPath = aFile.getAbsolutePath();
                    break;
                }
            }
            if (!dbfPath.toLowerCase().endsWith(".dbf"))
            {
                dbfPath = dbfPath.concat(".dbf");
            }

            dbf = new DBASEPortion(mode, dbfPath);
            dbf.doPrep();
            dbf.setFormat(MetadataFormat.STRING);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the prj portion of the shapefile
     *
     * @return prj, may be null
     */
    public ProjectionPortion getPrj()
    {
        return prj;
    }

    public boolean flipMode()
    {
        boolean returnValue = true;
        if (mode == Mode.WRITE)
        {
            mode = Mode.READ;
            try
            {
                shp = new MainPortion(mode, filePath);
                // prep. Should probably throw an exception if this fails.
                shp.doPrep();

                // Not fatal if the following don't exist.
                IndexPortion shx = new IndexPortion(mode, filePath.replace(".shp", ".shx"));

                // prep the index file
                if (shx != null)
                {
                    shx.doPrep();
                    shp.addIndex(shx);
                }

                // Not fatal if the following don't exist.
                dbf = new DBASEPortion(mode, filePath.replace(".shp", ".dbf"));
                dbf.doPrep();
                dbf.setFormat(MetadataFormat.STRING);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            // Not yet implemented.
            returnValue = false;
        }

        return returnValue;
    }

    public int getShapeType()
    {
        return shp.getHeader().shapeType;
    }
    /* public List<byte[]> getAsByteArrays() throws IOException { List<byte[]>
     * byteArrayList = new ArrayList<byte[]>(); ByteBuffer[] bufferArray =
     * shp.getAsByteBuffer(); //Write out the shp and shx files for (int i = 0;
     * i < bufferArray.length; i ++) {
     * byteArrayList.add(bufferArray[i].array()); } //Write out the dbf file
     * ByteArrayOutputStream outStream = new ByteArrayOutputStream(); if
     * (this.dbf != null) { dbf.writeDBASEPortion(outStream);
     * byteArrayList.add(outStream.toByteArray()); } return byteArrayList; } */

    /* public boolean writeToFile( String path, String name ) throws
     * FileNotFoundException, IOException { boolean returnValue = true;
     *
     * shp.writeToFiles(path, name); if ( this.dbf != null ) { File dbfFile =
     * new File( path + File.separator + name + ".dbf"); FileOutputStream fos =
     * new FileOutputStream(dbfFile); this.dbf.writeDBASEPortion(fos); }
     *
     * return returnValue; } */

    /* public void setShapes( List<ShapeRecord> shapes ) { if ( shp == null )
     * shp = new MainPortion(shapes); else shp.setShapes(shapes); }
     *
     * public void setMetadata( List<String> columnNames, List<List<String>>
     * valueTable ) { this.dbf = new DBASEPortion(columnNames,valueTable); }
     *
     * public void setMetadata( JTable table ) { this.dbf = new
     * DBASEPortion(table); }
     *
     * public MainPortionRecord[] getShapes(ShapeFormat format) {
     * MainPortionRecord[] returnArray = null;
     *
     * try { returnArray = shp.getShapes(); } catch (IOException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } catch
     * (InstantiationException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } catch (IllegalAccessException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); }
     *
     * return returnArray; } */

    /**
     * @param format Defines how the object[] is built. Presumably a display
     *            client would only be interested in the Strings, whereas a
     *            processing client might actually want the best fit conversion
     *            to string, integer/float, boolean, etc.
     *
     * @return Returns an array of arrays.
     *
     * @throws ParseException
     */
    /* public List<? extends Object[]> getMetadata(MetadataFormat format) throws
     * ParseException { List<? extends Object[]> returnArray = null;
     *
     * try { if (format == MetadataFormat.STRING ) { returnArray =
     * dbf.getStringMetadata(); } else // if ( format == MetadataFormat.ACTUAL )
     * { returnArray = dbf.getActualMetadata(); } } catch (IOException e) { //
     * TODO Auto-generated catch block e.printStackTrace(); }
     *
     * return returnArray; } */

    /**
     *
     */
    public boolean setMetadataMode(MetadataFormat format)
    {
        return dbf.setFormat(format);
    }

    /**
     * Returns the Metadata header information.
     *
     * @return the list of <code>TableField</code>s describing the metadata
     *         header.
     */
    public List<DBFColumnInfo> getMetadataHeader()
    {
        return dbf.getHeader().getFields();
    }

    public boolean setMetadataHeader(List<DBFColumnInfo> headerInfo)
    {
        dbf.getHeader().setFields(headerInfo);

        return true;
    }

    public boolean doFinalWrite() throws IOException
    {
        // Write headers...
        boolean returnValue = shp.writeHeader();
        returnValue &= dbf.writeHeader();

        // Flush entire index file
        returnValue &= shp.writeIndex();

        return returnValue;
    }

    /**
     * Indicates if the physical files will be deleted when the shapefile is
     * closed.
     *
     * @return <code>true</code> if the physical files will be deleted when the
     *         shapefile is closed.
     */
    public boolean isDeleteFiles()
    {
        return deleteFiles;
    }

    /**
     * Closes then deletes the shapefile.
     *
     * @throws IOException if an error occurs while closing or deleting the
     *             shapefile.
     */
    public void delete() throws IOException
    {
        try
        {
            close();
        }
        finally
        {
            try
            {
                shp.delete();
            }
            finally
            {
                dbf.delete();
            }
        }
    }

    /**
     * Closes the shapefile.
     *
     * @throws IOException
     */
    public void close() throws IOException
    {
        isClosed = true;
        try
        {
            if (isDeleteFiles())
            {
                shp.delete();
            }
            else
            {
                shp.close();
            }
        }
        finally
        {
            if (isDeleteFiles())
            {
                dbf.delete();
            }
            else
            {
                dbf.close();
            }
        }
    }

    /* /** Will return shapes + metadata. Will return null if metadata doesn't
     * exist or didn't parse. If metadata is optional, follow up a null response
     * to a call to getShapes() which should return just the shapes w/o metadata
     * or null if the shapes didn't parse either.
     *
     * @return
     *
     * @throws ParseException */
    /* public List<Object[]> getFullRecords(ShapeFormat shapeFormat,
     * MetadataFormat metadataFormat) throws ParseException { Object[] shapes =
     * getShapes(shapeFormat); List<? extends Object[]> metadata =
     * getMetadata(metadataFormat);
     *
     * // TODO Lonny needs to do something cooler than this here...
     * List<Object[]> arrayList = new ArrayList<Object[]>(shapes.length);
     *
     * for ( int i = 0; i < shapes.length; i++ ) { int shapeLength =
     * ((Object[])shapes[i]).length; int rowLength = shapeLength +
     * ((Object[])metadata.get(i)).length;
     *
     * Object[] row = new Object[rowLength]; for (int j = 0; j < rowLength; j++)
     * { if ( j < shapeLength ) { row[j] = ((Object[])shapes[i])[j]; } else {
     * row[j] = ((Object[])metadata.get(i))[j - shapeLength]; } }
     *
     * arrayList.add(row); } return arrayList; } */
    /* public String getName() { return name; }
     *
     * public void setName(String name) { this.name = name; } */
    @Override
    public boolean add(ShapefileRecord e)
    {
        return shp.add(e.shape) && dbf.add(e.metadata);
    }

    @Override
    public boolean addAll(Collection<? extends ShapefileRecord> c)
    {
        boolean returnValue = true;
        for (ShapefileRecord row : c)
        {
            returnValue &= add(row);
        }
        return false;
    }

    @Override
    public void clear()
    {
        shp.clear();
        dbf.clear();
    }

    @Override
    public boolean contains(Object o)
    {
        // SLOW!!
        return shp.contains(((ShapefileRecord)o).shape) && dbf.contains(((ShapefileRecord)o).metadata);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        // EVEN SLOWER!
        boolean returnValue = true;
        for (Object o : c)
        {
            returnValue &= contains(o);
        }
        return false;
    }

    @Override
    public boolean equals(Object o)
    {
        // TODO If you got here, YOU can implement this crazy function.
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        // Only shp is important
        return shp.isEmpty();
    }

    @Override
    public Iterator<ShapefileRecord> iterator()
    {
        return new ESRIShapefileIterator(this);
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size()
    {
        return shp.size();
    }

    @Override
    public Object[] toArray()
    {
        // TODO If you got here, YOU can implement this crazy function.
        return null;
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        // TODO If you got here, YOU can implement this crazy function.
        return null;
    }

    public static void main(String[] args)
    {
        // arguments: <date> <JPIPUrl> ulLon ulLat urLon urLat lrLon lrLat llLon
        // llLat
        // sample args: 2010-08-01 http://sddn-culebra/jp2/xxx/N30-E042.jp2 42
        // 35 48 35 48 30 42 30
        // 2010-08-02 http://sddn-culebra/jp2/xxx/N35-E120.jp2 120 40 126 40 126
        // 35 120 35
        String date = args[0];
        String jpipUrl = args[1];
        Double urLat = Double.parseDouble(args[5]);
        Double urLon = Double.parseDouble(args[4]);
        Double llLat = Double.parseDouble(args[9]);
        Double llLon = Double.parseDouble(args[8]);

        List<Point2D.Double> points = new ArrayList<>();

        for (int i = 2; i < args.length - 1; i += 2)
        {
            points.add(new Point2D.Double(Double.parseDouble(args[i]), Double.parseDouble(args[i + 1])));
        }

        // Polygon usage example
        try
        {
            File baseDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "dropbox");
            if (!baseDir.exists())
            {
                baseDir.mkdirs();
            }

            String outputFile = baseDir.getAbsolutePath() + File.separator + "Shapefile_"
                    + new SimpleDateFormat("HHmmssSSS").format(new Date());
            ESRIShapefile shapefile = new ESRIShapefile(ESRIShapefile.Mode.WRITE, outputFile);

            List<DBFColumnInfo> metadataHeader = new LinkedList<>();
            metadataHeader.add(new DBFColumnInfo("Date", 'C', (short)32));
            metadataHeader.add(new DBFColumnInfo("JPIPUrl", 'C', (short)255));
            metadataHeader.add(new DBFColumnInfo("LLLat", 'N', (short)255));
            metadataHeader.add(new DBFColumnInfo("LLLon", 'N', (short)255));
            metadataHeader.add(new DBFColumnInfo("URLat", 'N', (short)255));
            metadataHeader.add(new DBFColumnInfo("URLon", 'N', (short)255));

            shapefile.setMetadataHeader(metadataHeader);

            ShapeRecord record = new PolygonRecord(points);
            List<Object> metaRow = new ArrayList<>();

            metaRow.add(date);
            metaRow.add(jpipUrl);
            metaRow.add(llLat);
            metaRow.add(llLon);
            metaRow.add(urLat);
            metaRow.add(urLon);

            shapefile.add(new ShapefileRecord(record, metaRow.toArray()));
            shapefile.doFinalWrite();
            shapefile.close();
            System.out.println("Wrote new shapefile to " + outputFile);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
