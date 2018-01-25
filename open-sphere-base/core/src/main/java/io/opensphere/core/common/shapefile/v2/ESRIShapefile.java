package io.opensphere.core.common.shapefile.v2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;
import io.opensphere.core.common.shapefile.v2.dbase.DbasePortion;
import io.opensphere.core.common.shapefile.v2.index.IndexPortion;
import io.opensphere.core.common.shapefile.v2.main.MainPortion;
import io.opensphere.core.common.shapefile.v2.projection.ProjectionPortion;

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
 */
public class ESRIShapefile implements Collection<ShapefileRecord>
{

    /** logger */
    private static Log LOGGER = LogFactory.getLog(ESRIShapefile.class);

    /** DBASE file extension */
    public static final String POSTFIX_DBF = ".dbf";

    /** Index file extension */
    public static final String POSTFIX_SHX = ".shx";

    /** Shape file extension */
    public static final String POSTFIX_SHP = ".shp";

    /** Projection file extension */
    public static final String POSTFIX_PRJ = ".prj";

    /** The current <code>Mode</code> */
    protected Mode mode;

    /** The absolute path of the file that backs this object */
    protected String mFilePath = null;

    /** Determines whether to delete the backign file on close */
    protected boolean deleteFiles = false;

    /** State of the backing file */
    protected boolean closed = false;

    protected MainPortion shp = null;

    protected DbasePortion dbf = null;

    protected ProjectionPortion prj = null;

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

    /**
     * Default Constructor
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
        mFilePath = inFilePath;
        if (!mFilePath.toLowerCase().endsWith(POSTFIX_SHP))
        {
            mFilePath = mFilePath.concat(POSTFIX_SHP);
        }
        deleteFiles = inDeleteFiles;
        shp = new MainPortion(mode, mFilePath);

        try
        {
            // prep. Should probably throw an exception if this fails.
            shp.doPrep();

            String idxPath = null;
            String[] extPos = { ".SHX", POSTFIX_SHX, ".Shx" };

            // Not fatal if the following don't exist.

            // Search for the matching SHX file, the extension could be
            // upper, or lower case or mixed case so try to find it.
            idxPath = mFilePath.substring(0, mFilePath.length() - 4);
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
            if (!idxPath.toLowerCase().endsWith(POSTFIX_SHX))
            {
                idxPath = idxPath.concat(POSTFIX_SHX);
            }

            // Not fatal if the following projection file doesn't exist.
            // Search for the matching PRJ file, the extension could be
            // upper, or lower case or mixed case so try to find it.
            String prjPath = mFilePath.substring(0, mFilePath.length() - 4);
            String[] prjExtPos = { ".PRJ", POSTFIX_PRJ, ".Prj" };
            for (String ext : prjExtPos)
            {
                File aFile = new File(prjPath + ext);
                if (aFile.exists())
                {
                    prjPath = aFile.getAbsolutePath();
                    break;
                }
            }
            if (!prjPath.toLowerCase().endsWith(POSTFIX_PRJ))
            {
                prjPath = prjPath.concat(POSTFIX_PRJ);
            }

            try
            {
                prj = new ProjectionPortion(mode, prjPath);
            }
            catch (FileNotFoundException fnf)
            {
                prj = null;
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
            String[] dbfExtPos = { ".DBF", POSTFIX_DBF, ".Dbf" };

            // Not fatal if the following don't exist.
            dbfPath = mFilePath.substring(0, mFilePath.length() - 4);
            for (String ext : dbfExtPos)
            {
                File aFile = new File(dbfPath + ext);
                if (aFile.exists())
                {
                    dbfPath = aFile.getAbsolutePath();
                    break;
                }
            }
            if (!dbfPath.toLowerCase().endsWith(POSTFIX_DBF))
            {
                dbfPath = dbfPath.concat(POSTFIX_DBF);
            }

            dbf = new DbasePortion(mode, dbfPath);
            dbf.doPrep();
            dbf.setFormat(MetadataFormat.STRING);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Switches the state of the shapefile from that of WRITE to READ.
     *
     * @return
     */
    public boolean flipMode()
    {
        boolean returnValue = true;
        if (mode == Mode.WRITE)
        {
            mode = Mode.READ;
            try
            {
                shp = new MainPortion(mode, mFilePath);
                // prep. Should probably throw an exception if this fails.
                shp.doPrep();

                // Not fatal if the following don't exist.
                IndexPortion shx = new IndexPortion(mode, mFilePath.replace(POSTFIX_SHP, POSTFIX_SHX));

                // prep the index file
                if (shx != null)
                {
                    shx.doPrep();
                    shp.addIndex(shx);
                }

                // Not fatal if the following don't exist.
                dbf = new DbasePortion(mode, mFilePath.replace(POSTFIX_SHP, POSTFIX_DBF));
                dbf.doPrep();
                dbf.setFormat(MetadataFormat.STRING);
            }
            catch (Exception e)
            {
                LOGGER.error("Error flipping mode.", e);
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

    /**
     * Writes the data to the backing file
     *
     * @return
     * @throws IOException
     */
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
     * Opens the shapefile. This implementation relies heavily upon streams. In
     * the case that this shapefile was transient and it's contents deleted from
     * the system it will return false.
     *
     * @return true if the streams were able to be opened.
     */
    public boolean open()
    {
        if (deleteFiles)
        {
            // The files were deleted at closing.
            LOGGER.warn("This shapefile was set to delete its contents from the system on close. It cannot be opened.");
            return false;
        }
        try
        {
            shp.open();
        }
        catch (FileNotFoundException e)
        {
            LOGGER.error("The shape file was not found", e);
            return false;
        }

        try
        {
            dbf.open();
        }
        catch (FileNotFoundException e)
        {
            LOGGER.error("The DbasePortion file was not found", e);
            return false;
        }

        return true;
    }

    /**
     * Closes the shapefile.
     *
     * @throws IOException
     */
    public void close() throws IOException
    {
        closed = true;
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

    public MainPortion getShp()
    {
        return shp;
    }

    public void setShp(MainPortion shp)
    {
        this.shp = shp;
    }

    public DbasePortion getDbf()
    {
        return dbf;
    }

    public void setDbf(DbasePortion dbf)
    {
        this.dbf = dbf;
    }

    public String getFilePath()
    {
        return mFilePath;
    }

    public int getShapeType()
    {
        return shp.getHeader().shapeType;
    }

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

    /**
     * Sets the file path. Only use this if you know what you are doing. This
     * shapefile implementation uses lots of file I/O and switching the path can
     * wreck havoc.
     *
     * @param mFilePath
     */
    protected void setFilePath(String filePath)
    {
        mFilePath = filePath;
    }

    /**
     * Gets the current Mode of the file. Check the flipMode method to change
     * from WRITE to READ.
     *
     * @see io.opensphere.core.common.shapefile.ESRIShapefile.flipMode
     * @return
     */
    public Mode getMode()
    {
        return mode;
    }

    /**
     * Returns the state. If true, then the file streams are closed.
     */
    public boolean isClosed()
    {
        return closed;
    }

    /**
     * Returns the prj portion of the shapefile.
     *
     * @return prj, may be null.
     */
    public ProjectionPortion getPrj()
    {
        return prj;
    }

}
