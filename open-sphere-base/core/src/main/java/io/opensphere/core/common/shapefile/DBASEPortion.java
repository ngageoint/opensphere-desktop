/**
 *
 */
package io.opensphere.core.common.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.shapefile.ESRIShapefile.MetadataFormat;
import io.opensphere.core.common.shapefile.ESRIShapefile.Mode;
import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;

/**
 * A class that represents the dBASE portion of a shapefile.
 *
 * DBF File format used from
 * http://www.clicketyclick.dk/databases/xbase/format/dbf.html which appears to
 * be identical to
 * http://www.geocities.com/geoff_wass/dBASE/GaryWhite/dBASE/FAQ/qformt.htm
 *
 * @deprecated Deprecated to com.bitsys.common.shapefile.v2.dbase.BasePortion
 * @see com.bitsys.common.shapefile.v2.dbase.BasePortion
 */
@Deprecated
class DBASEPortion implements Collection<Object[]>
{

    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Log logger = LogFactory.getLog(DBASEPortion.class);

    public class DBASEHeader
    {
        /* Byte Position Field Units Type Order Byte 0 Version Version Byte n/a
         * Byte 1 Date 1900 + hex Byte[3] Little Byte 4 NumRecords Count Integer
         * Little Byte 8 HeaderSize Bytes Short Little Byte 10 RecordSize Bytes
         * Short Little Byte 12 reserved Byte[2] n/a Byte 14 Incomplete
         * transaction flag Byte n/a Byte 15 Encryption Flag Byte n/a Byte 16
         * Free record thread (reserved) Byte[4] n/a Byte 20 Reserved for
         * multi-user Byte[8] n/a Byte 28 MDX Flag (reserved) Byte n/a Byte 29
         * Language driver (reserved) Byte n/a Byte 30 Reserved Byte[2] n/a Byte
         * 32 Table Field descriptor Array Byte[32] n/a Byte 64-n repeated for
         * each field Byte[32] n/a Byte n Terminator "0Dh" Byte n/a Byte n+1
         * ASCII Records separated by a "space" (20h) Fields are not separated */
        // Expanding out, java doesn't do unsigned
        long numRecords;

        int headerSize;

        int recordSize;

        private List<DBFColumnInfo> fields = null;

        public DBASEHeader()
        {
        }

        /**
         * @return the fields
         */
        public List<DBFColumnInfo> getFields()
        {
            return fields;
        }

        public void setFields(List<DBFColumnInfo> fields)
        {
            this.fields = fields;
            // Calculate total record size
            // record deleted flag
            recordSize = 1;
            for (DBFColumnInfo field : fields)
            {
                recordSize += field.length;
            }

            headerSize = fields.size() * 32 + 32 + 1;
        }

        private boolean parseHeader(FileChannel channel) throws IOException
        {
            boolean returnValue = true;

            channel.position(0);

            ByteBuffer buffer = ByteBuffer.allocate(32);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            channel.read(buffer);
            buffer.flip();

            try
            {
                @SuppressWarnings("unused")
                int filler = buffer.getInt();
            }
            catch (BufferUnderflowException e)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Shapefile dBase file does not contain a header, file: " + file.getAbsolutePath(), e);
                }
                fields = new ArrayList<>();
                return false;
            }

            // Parse out the unsigned numbers, without moving signed bit
            numRecords = 0xFFFFFFFF & buffer.getInt();
            headerSize = 0xFFFF & buffer.getShort();
            recordSize = 0xFFFF & buffer.getShort();

            int numColumns = (headerSize - 32 - 1) / 32;

            if (numColumns > 0)
            {
                fields = new ArrayList<>(numColumns);

                DBFColumnInfo thisField = null;
                for (int i = 0; i < numColumns; i++)
                {
                    buffer.clear();
                    channel.read(buffer);
                    buffer.flip();
                    thisField = new DBFColumnInfo();
                    thisField.parseFieldDefinition(buffer);
                    fields.add(thisField);
                }
            }

            // Read terminator
            // TODO this is a bit excessive, could do some extra work to read
            // the whole header + a healthy amount and then transfer the
            // extra bytes to a new buffer.
            buffer = ByteBuffer.allocate(1);
            channel.read(buffer);
            buffer.flip();
            byte terminator = buffer.get();
            if (terminator != 0x0D)
            {
                // Log
                // returnValue = false;
            }
            return returnValue;
        }

        private boolean writeHeader(FileChannel channel) throws IOException
        {
            boolean returnValue = true;

            // start at beginning!
            channel.position(0);

            ByteBuffer buffer = ByteBuffer.allocate(32);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            // DBASE II Version Code
            buffer.put((byte)3);

            GregorianCalendar calendar = new GregorianCalendar();
            // Year
            buffer.put((byte)(calendar.get(1) - 1900));
            // Month
            buffer.put((byte)(calendar.get(2) + 1));
            // Day
            buffer.put((byte)calendar.get(5));

            // Downsample back into unsigned binary of appropriate bytelength
            buffer.putInt((int)(numRecords & 0xFFFFFFFFL));
            buffer.putShort((short)(headerSize & 0xFFFF));
            buffer.putShort((short)(recordSize & 0xFFFF));

            byte[] reservedVals = new byte[20];
            buffer.put(reservedVals);
            buffer.flip();

            channel.write(buffer);

            if (fields == null)
            {
                // should throw something here
                returnValue = false;
            }

            for (int i = 0; i < fields.size(); i++)
            {
                buffer.clear();
                fields.get(i).writeFieldDefinition(buffer);
                buffer.flip();
                channel.write(buffer);
            }

            // Put in the terminator
            buffer = ByteBuffer.allocate(1);
            // Terminator
            buffer.put((byte)13);
            buffer.flip();

            channel.write(buffer);

            return returnValue;
        }

    }
    // END DBASEHeader

    class DBASEPortionIterator implements Iterator<Object[]>
    {
        DBASEPortion parent = null;

        FileChannel thisFile = null;

        int nextRecord = 0;

        public DBASEPortionIterator(DBASEPortion dp)
        {
            try
            {
                parent = dp;
                thisFile = dp.inputStream.getChannel();
                // reset position
                thisFile.position(header.headerSize);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public boolean hasNext()
        {
            return nextRecord < parent.size();
        }

        @Override
        public Object[] next()
        {
            Object[] metadataRow = new Object[header.fields.size()];
            try
            {
                thisFile.position(header.headerSize + nextRecord * header.recordSize);
                // Offset units is # of 16bit words
                // Extra 4 words is for the mainportionrecord headers around the
                // shaperecord

                ByteBuffer buffer = ByteBuffer.allocate(header.recordSize);
                thisFile.read(buffer);
                buffer.flip();
                if (format == MetadataFormat.STRING)
                {
                    parent.parseDBASERecord(buffer, metadataRow, null);
                }
                else // format == ACTUAL
                {
                    parent.parseDBASERecord(buffer, null, metadataRow);
                }
                nextRecord++;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (ParseException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return metadataRow;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
    // END DBASEPortionIterator

    private final ESRIShapefile.Mode mode;

    private File file = null;

    private FileInputStream inputStream = null;

    private FileOutputStream outputStream = null;

    private MetadataFormat format = ESRIShapefile.MetadataFormat.STRING;

    private DBASEHeader header = null;

    DBASEPortion(ESRIShapefile.Mode mode, String filePath) throws FileNotFoundException
    {
        file = new File(filePath);
        if (mode == ESRIShapefile.Mode.READ)
        {
            inputStream = new FileInputStream(file);
            header = new DBASEHeader();
        }
        else // if mode == WRITE
        {
            outputStream = new FileOutputStream(file);
            header = new DBASEHeader();
        }

        this.mode = mode;
    }

    private boolean checkHeader(Object[] metadata)
    {
        boolean returnValue = true;

        // Check to see that we've been initialized enough to accept records
        if (header == null || header.fields == null || header.fields.isEmpty())
        {
            throw new IllegalStateException("No header fields are present");
        }

        // check object[] vs. known fields. Size should do for now, might want
        // to be more thorough later.
        if (header.fields.size() != metadata.length)
        {
            throw new IllegalArgumentException("The number of header fields (" + header.fields.size()
            + ") does not match the number of data fields (" + metadata.length + ")");
        }

        return returnValue;
    }

    private boolean updateHeader(Object[] metadata)
    {
        boolean returnValue = true;

        header.numRecords++;

        return returnValue;
    }

    boolean doPrep() throws IOException
    {
        boolean returnValue = true;
        if (mode == ESRIShapefile.Mode.READ)
        {
            returnValue = header.parseHeader(inputStream.getChannel());
        }
        return returnValue;
    }

    InputStream getAsInputStream() throws IOException
    {
        if (mode == Mode.READ)
        {
            inputStream.getChannel().position(0);
            return inputStream;
        }

        return null;
    }

    /**
     * @return the header
     * @throws ParseException
     * @throws IOException
     */
    DBASEHeader getHeader()
    {
        if (header == null)
        {
            FileChannel channel = inputStream.getChannel();
            header = new DBASEHeader();

            try
            {
                // DBASE Header is variable size, so this function takes the
                // file,
                // rather than the buffer that the other parse() functions take.
                if (!header.parseHeader(channel))
                {
                    throw new RuntimeException("Failed to parse the header");
                }

            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to parse the header", e);
            }
        }
        return header;
    }

    boolean setFormat(MetadataFormat newFormat)
    {
        format = newFormat;
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

    public void close() throws IOException
    {
        if (inputStream != null)
        {
            inputStream.close();
        }
        if (outputStream != null)
        {
            outputStream.close();
        }
    }

    public boolean writeHeader() throws IOException
    {
        return header.writeHeader(outputStream.getChannel());
    }

    public boolean parseDBASERecord(ByteBuffer buffer, Object[] stringArray, Object[] actualArray)
        throws ParseException
    {
        boolean returnValue = false;

        int size = header.fields.size();

        // pull one byte off of the beginning of each record.
        @SuppressWarnings("unused")
        byte RecordDeletedFlag = buffer.get();
        boolean doStringArray = stringArray != null && stringArray.length == size;
        boolean doActualArray = actualArray != null && actualArray.length == size;
        for (int j = 0; j < size; j++)
        {
            DBFColumnInfo field = header.fields.get(j);
            // System.out.println(" C " + j + " of " + size + " Length: " +
            // field.length );
            Object fieldRecord = field.parseFieldRecord(buffer, false);
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
    public boolean add(Object[] e)
    {
        if (mode == ESRIShapefile.Mode.WRITE)
        {
            checkHeader(e);

            long position = header.headerSize + header.numRecords * header.recordSize;
            FileChannel thisFile = outputStream.getChannel();

            int bytesToAllocate = header.recordSize;
            ByteBuffer buffer = ByteBuffer.allocate(bytesToAllocate);

            // Record deleted flag
            buffer.put((byte)' ');
            int i = 0;
            for (DBFColumnInfo field : header.fields)
            {
                field.writeFieldRecord(e[i++], buffer);
            }

            try
            {
                thisFile.position(position);
                buffer.flip();
                thisFile.write(buffer);
            }
            catch (IOException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                // RETHROW in something that matches the interface
                throw new IllegalStateException(e1);
            }

            updateHeader(e);
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
            inputStream.getChannel().position(header.headerSize);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        return header.numRecords == 0;
    }

    @Override
    public Iterator<Object[]> iterator()
    {
        return new DBASEPortionIterator(this);
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
        return (int)header.numRecords;
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
            a = (T[])new Object[header.fields.size()][size()];
        }

        int i = 0;
        for (Object[] metadata : this)
        {
            a[i++] = (T)metadata;
        }

        return a;
    }
}
