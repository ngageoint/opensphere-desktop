package io.opensphere.core.common.shapefile.v2.dbase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.shapefile.v2.ESRIShapefile.MetadataFormat;

public class DbasePortionIterator implements Iterator<Object[]>
{

    public static final String ERR_PARSING_DATA = "Error parsing data";

    public static final String ERR_WORKING_WITH_CHANNEL = "Error working with channel";

    /** The logger */
    private static Log LOGGER = LogFactory.getLog(DbasePortionIterator.class);

    DbasePortion parent = null;

    FileChannel thisFile = null;

    int nextRecord = 0;

    public DbasePortionIterator(DbasePortion dp)
    {
        try
        {
            parent = dp;
            thisFile = dp.getInChannel();
            // reset position
            thisFile.position(dp.getHeader().mHeaderSize);
        }
        catch (IOException e)
        {
            LOGGER.error(ERR_WORKING_WITH_CHANNEL, e);
        }
    }

    public boolean hasNext()
    {
        return (nextRecord < parent.size());
    }

    public Object[] next()
    {
        Object[] metadataRow = new Object[parent.getHeader().getFields().size()];
        try
        {
            thisFile.position(parent.getHeader().mHeaderSize + nextRecord * parent.getHeader().mRecordSize);
            // Offset units is # of 16bit words
            // Extra 4 words is for the mainportionrecord headers around the
            // shaperecord

            ByteBuffer buffer = ByteBuffer.allocate(parent.getHeader().mRecordSize);
            thisFile.read(buffer);
            buffer.flip();
            if (parent.getFormat() == MetadataFormat.STRING)
            {
                parent.parseDbaseRecord(buffer, metadataRow, null);
            }
            else // format == ACTUAL
            {
                parent.parseDbaseRecord(buffer, null, metadataRow);
            }
            nextRecord++;
        }
        catch (IOException e)
        {
            LOGGER.error(ERR_WORKING_WITH_CHANNEL, e);
        }
        catch (ParseException e)
        {
            LOGGER.error(ERR_PARSING_DATA, e);
        }
        return metadataRow;
    }

    public void remove()
    {
        throw (new UnsupportedOperationException());
    }
}
