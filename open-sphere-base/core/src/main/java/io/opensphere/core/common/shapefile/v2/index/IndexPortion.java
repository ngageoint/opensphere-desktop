/**
 *
 */
package io.opensphere.core.common.shapefile.v2.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.common.shapefile.utils.ShapeHeader;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile.Mode;

/**
 * Class for handling the Index (shx) portion of the shapefile.
 */
public class IndexPortion
{

    private ESRIShapefile.Mode mMode;

    private File mFile = null;

    private FileInputStream mInputStream = null;

    private FileOutputStream mOutputStream = null;

    private ShapeHeader mHeader = null;

    private List<IndexPortionRecord> mRecords = null;

    /**
     * @throws FileNotFoundException
     *
     */
    public IndexPortion(ESRIShapefile.Mode mode, String filePath) throws FileNotFoundException
    {
        mFile = new File(filePath);
        mMode = mode;
        open();
        mHeader = new ShapeHeader();
    }

    public boolean doPrep() throws IOException
    {
        boolean returnValue = true;
        if (mMode == ESRIShapefile.Mode.READ)
        {
            returnValue = parseRecords();
        }
        else // if mMode == WRITE
        {
            // Linked list for writing
            mRecords = new LinkedList<>();
        }
        return returnValue;
    }

    public InputStream getAsInputStream() throws IOException
    {
        if (mMode == Mode.READ)
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
    void inflateFromStream(InputStream is, long bytesToRead) throws IOException
    {
        if (mMode == Mode.WRITE)
        {
            mOutputStream.getChannel().position(0);

            for (long i = 0; i < bytesToRead; i++)
            {
                mOutputStream.write(is.read());
            }
        }
    }

    /**
     * Deletes the mFile managed by this class.
     *
     * @throws IOException if an error occurs while closing the input/output
     *             streams.
     */
    public void delete() throws IOException
    {
        if (mFile != null && mFile.exists())
        {
            // Close the mFile descriptors first.
            try
            {
                close();
            }
            finally
            {
                // If unable to delete the mFile, delete it on exit.
                if (!mFile.delete())
                {
                    mFile.deleteOnExit();
                }
            }
        }
    }

    /**
     * Opens this IndexPortion mFile for input/output
     *
     * @throws IOException
     */
    public void open() throws FileNotFoundException
    {
        if (mMode == ESRIShapefile.Mode.READ)
        {
            mInputStream = new FileInputStream(mFile);
        }
        else
        {
            mOutputStream = new FileOutputStream(mFile);
        }
    }

    /**
     * Closes the streams
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

    /**
     * Parse the record into memory from the backing mFile.
     *
     * @return
     * @throws IOException
     */
    public boolean parseRecords() throws IOException
    {
        boolean returnValue = true;
        FileChannel channel = mInputStream.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(ShapeHeader.HEADER_SIZE);
        channel.read(buffer);
        buffer.flip();
        if (!mHeader.parseHeader(buffer))
        {
            // Log
            returnValue = false;
        }
        else
        {
            // Whole point of an index mFile is to be able to quickly read the
            // entire
            // mFile into memory. Not bothering with any fancy partial read
            // stuff.
            int fileSize = mHeader.fileLength - ShapeHeader.HEADER_SIZE;
            /* record size */
            int numRecords = fileSize / 8;

            buffer = ByteBuffer.allocate(fileSize);
            channel.read(buffer);
            buffer.flip();

            mRecords = new ArrayList<>(numRecords);

            for (int i = 0; i < numRecords; i++)
            {
                IndexPortionRecord record = new IndexPortionRecord();
                record.parseRecord(buffer);
                mRecords.add(record);
            }
        }

        return returnValue;
    }

    /**
     * Write the records to the backing mFile.
     *
     * @param mHeader
     * @return
     * @throws IOException
     */
    public boolean writeRecords(ShapeHeader header) throws IOException
    {
        boolean returnValue = true;
        FileChannel channel = mOutputStream.getChannel();

        // SHP and SHX headers need to be guaranteed to be the same (except for
        // filesize).
        // Using SHP mHeader to do write ensures this.
        mHeader = header;
        mHeader.fileLength = ShapeHeader.HEADER_SIZE + mRecords.size() * 8;

        ByteBuffer buffer = ByteBuffer.allocate(ShapeHeader.HEADER_SIZE);
        header.writeHeader(buffer);
        buffer.flip();
        channel.write(buffer);

        buffer = ByteBuffer.allocate(8);

        for (IndexPortionRecord record : mRecords)
        {
            record.writeRecord(buffer);
            buffer.flip();
            channel.write(buffer);
            buffer.flip();
        }

        return returnValue;
    }

    public List<IndexPortionRecord> getRecords()
    {
        return mRecords;
    }

}
