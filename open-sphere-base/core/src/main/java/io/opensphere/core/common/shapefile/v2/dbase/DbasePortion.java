package io.opensphere.core.common.shapefile.v2.dbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile.MetadataFormat;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile.Mode;

/**
 * A class that represents the dBASE portion of a shapefile.
 *
 * DBF File mFormat used from
 * http://www.clicketyclick.dk/databases/xbase/mFormat/dbf.html which appears to
 * be identical to
 * http://www.geocities.com/geoff_wass/dBASE/GaryWhite/dBASE/FAQ/qformt.htm
 */
public class DbasePortion implements Collection<Object[]>
{

    private static final String ERR_COULD_NOT_SET_POSITION = "Could not set position in the input Channel.";

    public static final String ERR_NO_HEADER_FIELDS_PRESENT = "No Header fields are present";

    public static final String ERR_FLIPPING_OR_WRITING_THE_BUFFER = "Error flipping or writing the buffer.";

    public static final String ERR_NOT_SETUP_FOR_WRITING = "Not setup for writing.";

    public static final String ERR_FAILED_TO_PARSE_THE_HEADER = "Failed to parse the mHeader";

    /** The logger */
    private static Log LOGGER = LogFactory.getLog(DbasePortion.class);

    private ESRIShapefile.Mode mode;

    private File file = null;

    private FileInputStream mInputStream = null;

    private FileOutputStream mOutputStream = null;

    private FileChannel mInChannel = null;

    private FileChannel mOutChannel = null;

    private MetadataFormat mFormat = ESRIShapefile.MetadataFormat.STRING;

    private DbaseHeader mHeader = null;

    /**
     * Constructor
     *
     * @param mode
     * @param filePath
     * @throws FileNotFoundException
     */
    public DbasePortion(ESRIShapefile.Mode mode, String filePath) throws FileNotFoundException
    {
        file = new File(filePath);
        if (mode == ESRIShapefile.Mode.READ)
        {
            mInputStream = new FileInputStream(file);
            mInChannel = mInputStream.getChannel();
        }
        else // if mode == WRITE
        {
            mOutputStream = new FileOutputStream(file);
            mOutChannel = mOutputStream.getChannel();
        }
        mHeader = new DbaseHeader();
        this.mode = mode;
    }

    private boolean checkHeader(Object[] metadata)
    {
        boolean returnValue = true;

        // Check to see that we've been initialized enough to accept records
        if (mHeader == null || mHeader.getFields() == null || mHeader.getFields().isEmpty())
        {
            throw new IllegalStateException(ERR_NO_HEADER_FIELDS_PRESENT);
        }

        // check object[] vs. known fields. Size should do for now, might want
        // to be more thorough later.
        if (mHeader.getFields().size() != metadata.length)
        {
            throw new IllegalArgumentException("The number of mHeader fields (" + mHeader.getFields().size()
                    + ") does not match the number of data fields (" + metadata.length + ")");
        }

        return returnValue;
    }

    private boolean updateHeader(Object[] metadata)
    {
        mHeader.mNumRecords++;
        return true;
    }

    public boolean doPrep() throws IOException
    {
        boolean returnValue = true;
        if (mode == ESRIShapefile.Mode.READ)
        {
            returnValue = mHeader.parseHeader(mInChannel);
        }
        return returnValue;
    }

    public InputStream getAsInputStream() throws IOException
    {
        if (mode == Mode.READ)
        {
            mInputStream.getChannel().position(0);
            return mInputStream;
        }

        return null;
    }

    /**
     * Provides a mechanism for inflating this object from a stream
     *
     * @param os InputStream
     * @param bytesToRead long
     * @throws IOException
     */
    public void inflateFromStream(InputStream is, long bytesToRead) throws IOException
    {
        if (mode == Mode.WRITE)
        {

            // Writes the bytes to the backing file.
            for (long i = 0; i < bytesToRead; i++)
            {
                mOutputStream.write(is.read());
            }

            // Parse the header info into the object.
            mHeader.parseHeader(mOutputStream.getChannel().position(0));

        }

    }

    /**
     * @return the mHeader
     * @throws ParseException
     * @throws IOException
     */
    public DbaseHeader getHeader()
    {
        if (mHeader == null)
        {
            mHeader = new DbaseHeader();
            try
            {
                // DBASE Header is variable size, so this function takes the
                // file,
                // rather than the buffer that the other parse() functions take.
                if (!mHeader.parseHeader(mInChannel))
                {
                    LOGGER.error(ERR_FAILED_TO_PARSE_THE_HEADER);
                    throw new RuntimeException(ERR_FAILED_TO_PARSE_THE_HEADER);
                }
            }
            catch (IOException e)
            {
                LOGGER.error(ERR_FAILED_TO_PARSE_THE_HEADER);
                throw new RuntimeException(ERR_FAILED_TO_PARSE_THE_HEADER, e);
            }
        }
        return mHeader;
    }

    public boolean setFormat(MetadataFormat newFormat)
    {
        mFormat = newFormat;
        return mode == ESRIShapefile.Mode.READ;
    }

    /**
     * Deletes the file managed by this class.
     *
     * @throws IOException if an error occurs while closing the input/output
     *             streams.
     */
    public void delete() throws IOException
    {
        if (file != null && file.exists())
        {
            try
            {
                // Close the file descriptors first.
                close();
            }
            finally
            {
                // If unable to delete the file, delete it on exit.
                if (!file.delete())
                {
                    file.deleteOnExit();
                }
            }
        }
    }

    /**
     * Opens the DbasePortion file for input/output
     *
     * @throws IOException
     */
    public void open() throws FileNotFoundException
    {
        if (mode == ESRIShapefile.Mode.READ)
        {
            mInputStream = new FileInputStream(file);
        }
        else // if (mode == ESRIShapefile.Mode.WRITE)
        {
            mOutputStream = new FileOutputStream(file);
        }
    }

    /**
     * Closes the DbasePortion streams.
     *
     * @throws IOException
     */
    public void close() throws IOException
    {
        if (mInputStream != null)
        {
            mInputStream.close();
        }
        if (mOutputStream != null)
        {
            mOutputStream.close();
        }
    }

    public boolean writeHeader() throws IOException
    {
        return mHeader.writeHeader(mOutputStream.getChannel());
    }

    public boolean parseDbaseRecord(ByteBuffer buffer, Object[] stringArray, Object[] actualArray)
        throws IOException, ParseException
    {
        boolean returnValue = false;

        int size = mHeader.getFields().size();

        // pull one byte off of the beginning of each record.
        @SuppressWarnings("unused")
        byte RecordDeletedFlag = buffer.get();
        boolean doStringArray = stringArray != null && stringArray.length == size;
        boolean doActualArray = actualArray != null && actualArray.length == size;
        for (int j = 0; j < size; j++)
        {
            DBFColumnInfo field = mHeader.getFields().get(j);
            // System.out.println(" C " + j + " of " + size + " Length: " +
            // field.length );
            Object fieldRecord = field.parseFieldRecord(buffer, mFormat == MetadataFormat.ACTUAL);
            if (fieldRecord instanceof String)
            {
                fieldRecord = ((String)fieldRecord).trim();
            }
            returnValue = fieldRecord != null;
            if (returnValue && doStringArray)
            {
                stringArray[j] = fieldRecord;
            }
            if (returnValue && doActualArray)
            {
                actualArray[j] = field.getType().getDBFFieldActual(fieldRecord);
            }
        }

        return returnValue;
    }

    @Override
    public boolean add(Object[] obj)
    {
        if (mode == ESRIShapefile.Mode.WRITE)
        {
            checkHeader(obj);

            long position = mHeader.mHeaderSize + mHeader.mNumRecords * mHeader.mRecordSize;
            FileChannel thisFile = mOutputStream.getChannel();

            int bytesToAllocate = mHeader.mRecordSize;
            ByteBuffer buffer = ByteBuffer.allocate(bytesToAllocate);

            // Record deleted flag
            buffer.put((byte)' ');
            int i = 0;
            for (DBFColumnInfo field : mHeader.getFields())
            {
                field.writeFieldRecord(obj[i++], buffer);
            }

            try
            {
                thisFile.position(position);
                buffer.flip();
                thisFile.write(buffer);
            }
            catch (IOException e)
            {

                LOGGER.error(ERR_FLIPPING_OR_WRITING_THE_BUFFER, e);
                // RETHROW in something that matches the interface
                throw new IllegalStateException(e);
            }

            updateHeader(obj);
        }
        else
        {
            // Not setup for writing
            throw new IllegalStateException(ERR_NOT_SETUP_FOR_WRITING);
        }

        // Per spec, in this case we throw exceptions on fail, not return false
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Object[]> c)
    {
        if (mode == ESRIShapefile.Mode.WRITE)
        {
            for (Object[] obj : c)
            {
                add(obj);
            }
        }
        else
        {
            // Not setup for writing
            throw new IllegalStateException();
        }

        // Per spec, in this case we throw exceptions on fail, not return false
        return true;
    }

    @Override
    public void clear()
    {
        try
        {
            mInputStream.getChannel().position(mHeader.mHeaderSize);
        }
        catch (IOException e)
        {
            LOGGER.equals(ERR_COULD_NOT_SET_POSITION);
        }
    }

    @Override
    public boolean contains(Object o)
    {
        // Will be exceptionally slow on large files
        boolean returnValue = false;
        for (Object[] row : this)
        {
            // Almost guaranteed to not work. Don't care for now.
            returnValue |= row.equals(o);
        }

        return returnValue;
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        // Will be ridiculously slow on large files
        boolean returnValue = true;
        for (Object o : c)
        {
            returnValue &= contains(o);
            if (!returnValue)
            {
                break;
            }
        }
        return returnValue;
    }

    @Override
    public boolean isEmpty()
    {
        return mHeader.mNumRecords == 0;
    }

    @Override
    public Iterator<Object[]> iterator()
    {
        return new DbasePortionIterator(this);
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
        return (int)mHeader.mNumRecords;
    }

    @Override
    public Object[] toArray()
    {
        Object[] returnArray = null;

        List<Object[]> list = new LinkedList<>();
        for (Object[] metadata : this)
        {
            list.add(metadata);
        }
        returnArray = list.toArray();

        return returnArray;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a)
    {
        if (a.length < size())
        {
            a = (T[])new Object[mHeader.getFields().size()][size()];
        }

        int i = 0;
        for (Object[] metadata : this)
        {
            a[i++] = (T)metadata;
        }

        return a;
    }

    public ESRIShapefile.Mode getMode()
    {
        return mode;
    }

    public void setMode(ESRIShapefile.Mode mode)
    {
        this.mode = mode;
    }

    public InputStream getInputStream()
    {
        return mInputStream;
    }

    public void setInputStream(FileInputStream inputStream)
    {
        mInputStream = inputStream;
    }

    public OutputStream getOutputStream()
    {
        return mOutputStream;
    }

    public void setOutputStream(FileOutputStream outputStream)
    {
        mOutputStream = outputStream;
    }

    public MetadataFormat getFormat()
    {
        return mFormat;
    }

    public void setHeader(DbaseHeader header)
    {
        mHeader = header;
    }

    public FileChannel getInChannel()
    {
        return mInChannel;
    }

    public void setInChannel(FileChannel inChannel)
    {
        mInChannel = inChannel;
    }

    public FileChannel getOutChannel()
    {
        return mOutChannel;
    }

    public void setOutChannel(FileChannel outChannel)
    {
        mOutChannel = outChannel;
    }

}
