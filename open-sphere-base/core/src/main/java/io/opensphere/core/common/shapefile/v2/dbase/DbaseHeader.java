package io.opensphere.core.common.shapefile.v2.dbase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;

/**
 * The DBASE header for the .dbf file.
 */
public class DbaseHeader
{

    /* Byte Position Field Units Type Order Byte 0 Version Version Byte n/a Byte
     * 1 Date 1900 + hex Byte[3] Little Byte 4 NumRecords Count Integer Little
     * Byte 8 HeaderSize Bytes Short Little Byte 10 RecordSize Bytes Short
     * Little Byte 12 reserved Byte[2] n/a Byte 14 Incomplete transaction flag
     * Byte n/a Byte 15 Encryption Flag Byte n/a Byte 16 Free record thread
     * (reserved) Byte[4] n/a Byte 20 Reserved for multi-user Byte[8] n/a Byte
     * 28 MDX Flag (reserved) Byte n/a Byte 29 Language driver (reserved) Byte
     * n/a Byte 30 Reserved Byte[2] n/a Byte 32 Table Field descriptor Array
     * Byte[32] n/a Byte 64-n repeated for each field Byte[32] n/a Byte n
     * Terminator "0Dh" Byte n/a Byte n+1 ASCII Records separated by a "space"
     * (20h) Fields are not separated */
    // Expanding out, java doesn't do unsigned
    long mNumRecords;

    int mHeaderSize;

    int mRecordSize;

    private List<DBFColumnInfo> mFields = null;

    /**
     * @return the mFields
     */
    public List<DBFColumnInfo> getFields()
    {
        return mFields;
    }

    public void setFields(List<DBFColumnInfo> fields)
    {
        mFields = fields;
        // Calculate total record size
        // record deleted flag
        mRecordSize = 1;
        for (DBFColumnInfo field : fields)
        {
            mRecordSize += field.length;
        }

        mHeaderSize = fields.size() * 32 + 32 + 1;
    }

    /**
     * Read the bytes from the channel and unmarshall them into this object.
     *
     * @param channel
     * @return
     * @throws IOException
     */
    protected boolean parseHeader(FileChannel channel) throws IOException
    {
        boolean returnValue = true;

        channel.position(0);

        ByteBuffer buffer = ByteBuffer.allocate(32);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        channel.read(buffer);
        buffer.flip();

        @SuppressWarnings("unused")
        int filler = buffer.getInt();

        // Parse out the unsigned numbers, without moving signed bit
        mNumRecords = 0xFFFFFFFF & buffer.getInt();
        mHeaderSize = 0xFFFF & buffer.getShort();
        mRecordSize = 0xFFFF & buffer.getShort();

        int numColumns = (mHeaderSize - 32 - 1) / 32;

        if (numColumns > 0)
        {
            mFields = new ArrayList<>(numColumns);

            DBFColumnInfo thisField = null;
            for (int i = 0; i < numColumns; i++)
            {
                buffer.clear();
                channel.read(buffer);
                buffer.flip();
                thisField = new DBFColumnInfo();
                thisField.parseFieldDefinition(buffer);
                mFields.add(thisField);
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

    /**
     * Marshalls the bytes of this object into the file
     *
     * @param channel
     * @return
     * @throws IOException
     */
    protected boolean writeHeader(FileChannel channel) throws IOException
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
        buffer.putInt((int)(mNumRecords & 0xFFFFFFFFL));
        buffer.putShort((short)(mHeaderSize & 0xFFFF));
        buffer.putShort((short)(mRecordSize & 0xFFFF));

        byte[] reservedVals = new byte[20];
        buffer.put(reservedVals);
        buffer.flip();

        channel.write(buffer);

        if (mFields == null)
        {
            // should throw something here
            returnValue = false;
        }

        for (int i = 0; i < mFields.size(); i++)
        {
            buffer.clear();
            mFields.get(i).writeFieldDefinition(buffer);
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

    public long getNumRecords()
    {
        return mNumRecords;
    }

    public void setNumRecords(long numRecords)
    {
        mNumRecords = numRecords;
    }

    public int getHeaderSize()
    {
        return mHeaderSize;
    }

    public void setHeaderSize(int headerSize)
    {
        mHeaderSize = headerSize;
    }

    public int getRecordSize()
    {
        return mRecordSize;
    }

    public void setRecordSize(int recordSize)
    {
        mRecordSize = recordSize;
    }

}
