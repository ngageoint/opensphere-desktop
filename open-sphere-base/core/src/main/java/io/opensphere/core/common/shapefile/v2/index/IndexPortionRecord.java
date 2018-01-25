package io.opensphere.core.common.shapefile.v2.index;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* 100 Byte header, identical to MainPortion's header, followed by "n" 8 byte
 * records. */
public class IndexPortionRecord
{
    /* Byte Position Field Value Type Order Byte 0 Offset Offset Integer Big
     * Byte 4 Content Length Content Length Integer Big */
    int mOffset;

    int mContentLength;

    public IndexPortionRecord()
    {
    }

    public IndexPortionRecord(int offset, int contentLength)
    {
        this.mOffset = offset;
        this.mContentLength = contentLength;
    }

    public boolean parseRecord(ByteBuffer buffer)
    {
        boolean returnValue = true;
        buffer.order(ByteOrder.BIG_ENDIAN);
        mOffset = buffer.getInt();
        mContentLength = buffer.getInt();
        return returnValue;
    }

    public boolean writeRecord(ByteBuffer buffer)
    {
        boolean returnValue = true;
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(mOffset);
        buffer.putInt(mContentLength);
        return returnValue;
    }

    public int getOffset()
    {
        return mOffset;
    }

    public int getContentLength()
    {
        return mContentLength;
    }

}
