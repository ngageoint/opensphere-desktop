package io.opensphere.core.common.shapefile.v2.main;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord.ShapeType;
import io.opensphere.core.common.shapefile.utils.ShapeHeader;

/**
 * Represents a record in the 'shp' file.
 */
public class MainPortionRecord
{
    /* Byte Position Field Value Type Order Byte 0 Record Number Record Number
     * Integer Big Byte 4 Content Length Content Length Integer Big
     *
     * Content Length is # of 16bit words. */

    private int recordNumber;

    private int contentLength;

    private ShapeRecord record;

    /**
     * Constructor. Intentionally does nothing. Use parse() functions to force
     * the file to be read.
     */
    public MainPortionRecord()
    {
    }

    /**
     * Constructor. Setting record
     **/
    public MainPortionRecord(ShapeRecord rec)
    {
        record = rec;
    }

    /**
     * Returns the record number.
     *
     * @return the recordNumber.
     */
    public int getRecordNumber()
    {
        return recordNumber;
    }

    /**
     * Returns the content length.
     *
     * @return the contentLength.
     */
    public int getContentLength()
    {
        return contentLength;
    }

    /**
     * Returns the <code>ShapeRecord</code> instance.
     *
     * @return the shape record.
     */
    public ShapeRecord getRecord()
    {
        return record;
    }

    public void setRecord(ShapeRecord rec)
    {
        record = rec;
    }

    /**
     * Forces the parsing of the file.
     *
     * @return Returns false if parsing fails in a way that doesn't throw an
     *         exception.
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    public boolean parseRecord(ByteBuffer buffer, ShapeHeader pHeader) throws InstantiationException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {

        boolean returnValue = true;

        buffer.order(ByteOrder.BIG_ENDIAN);
        recordNumber = buffer.getInt();
        contentLength = buffer.getInt();

        ShapeType type = ShapeType.getInstance(pHeader.shapeType);
        if (type != null)
        {
            record = type.getShapeRecordInstance();
            returnValue = record.parseRecord(buffer);
        }
        else
        {
            returnValue = false;
        }

        return returnValue;
    }

    public boolean writeRecord(int recNumber, FileChannel channel) throws IOException
    {
        boolean returnValue = true;

        channel.write(getAsByteBuffer(recNumber));

        return returnValue;
    }

    public ByteBuffer getAsByteBuffer(int recNumber)
    {
        ByteBuffer buff = ByteBuffer.allocate(record.getLengthInBytes() + 8);
        buff.order(ByteOrder.BIG_ENDIAN);
        buff.putInt(recNumber);
        buff.putInt(record.getContentLengthInWords());

        record.writeRecord(buff);

        buff.flip();
        return buff;
    }
}
