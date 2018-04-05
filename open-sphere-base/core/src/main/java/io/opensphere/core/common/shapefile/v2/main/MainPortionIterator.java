package io.opensphere.core.common.shapefile.v2.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.shapefile.shapes.ShapeRecord;

public class MainPortionIterator implements Iterator<ShapeRecord>
{

    /** The logger */
    private static Log LOGGER = LogFactory.getLog(MainPortionIterator.class);

    protected MainPortion parent = null;

    protected FileChannel thisFile = null;

    protected int nextRecord = 0;

    /**
     * Constructor that takes a main portion.
     *
     * @param mp
     */
    public MainPortionIterator(MainPortion mp)
    {
        try
        {
            parent = mp;
            thisFile = mp.getInputStream().getChannel();
            // reset position
            thisFile.position(mp.getIndex().getRecords().get(0).getOffset());

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
        return (nextRecord < parent.size());
    }

    @Override
    public ShapeRecord next()
    {
        MainPortionRecord record = new MainPortionRecord();
        try
        {
            thisFile.position(2 * parent.getIndex().getRecords().get(nextRecord).getOffset());
            int bytesToAllocate = 2 * (parent.getIndex().getRecords().get(nextRecord).getContentLength() + 4);
            // Offset units is # of 16bit words
            // Extra 4 words is for the mainportionrecord headers around the
            // shaperecord

            ByteBuffer buffer = ByteBuffer.allocate(bytesToAllocate);
            thisFile.read(buffer);
            buffer.flip();
            record.parseRecord(buffer, parent.getHeader());
            nextRecord++;
        }
        catch (IOException e)
        {
            LOGGER.error(e);
        }
        catch (InstantiationException e)
        {
            LOGGER.error(e);
        }
        catch (IllegalAccessException e)
        {
            LOGGER.error(e);
        }
        return record.getRecord();
    }

    @Override
    public void remove()
    {
        throw (new UnsupportedOperationException());
    }

}
