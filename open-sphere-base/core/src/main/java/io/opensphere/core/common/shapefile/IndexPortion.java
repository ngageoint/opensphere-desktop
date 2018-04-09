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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.common.shapefile.ESRIShapefile.Mode;
import io.opensphere.core.common.shapefile.utils.ShapeHeader;

/**
 * Class for handling the Index (shx) portion of the shapefile.
 *
 * @deprecated Deprecated to com.bitsys.common.shapefile.v2.IndexPortion
 * @see io.opensphere.core.common.shapefile.v2.index.IndexPortion
 */
@Deprecated
class IndexPortion
{
    /* 100 Byte header, identical to MainPortion's header, followed by "n" 8
     * byte records. */
    public class IndexPortionRecord
    {
        /* Byte Position Field Value Type Order Byte 0 Offset Offset Integer Big
         * Byte 4 Content Length Content Length Integer Big */
        int offset;

        int contentLength;

        public IndexPortionRecord()
        {
        }

        public IndexPortionRecord(int offset, int contentLength)
        {
            this.offset = offset;
            this.contentLength = contentLength;
        }

        public boolean parseRecord(ByteBuffer buffer)
        {
            boolean returnValue = true;
            buffer.order(ByteOrder.BIG_ENDIAN);
            offset = buffer.getInt();
            contentLength = buffer.getInt();
            return returnValue;
        }

        public boolean writeRecord(ByteBuffer buffer)
        {
            boolean returnValue = true;
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(offset);
            buffer.putInt(contentLength);
            return returnValue;
        }

    }

    private ESRIShapefile.Mode mode;

    private File file = null;

    private FileInputStream inputStream = null;

    private FileOutputStream outputStream = null;

    private ShapeHeader header = null;

    public List<IndexPortionRecord> records = null;

    /**
     * @throws FileNotFoundException
     *
     */
    IndexPortion(ESRIShapefile.Mode mode, String filePath) throws FileNotFoundException
    {
        file = new File(filePath);
        this.mode = mode;
        if (mode == ESRIShapefile.Mode.READ)
        {
            inputStream = new FileInputStream(file);
            header = new ShapeHeader();
        }
        else
        {
            outputStream = new FileOutputStream(file);
            header = new ShapeHeader();
        }
    }

    /**
     * Creates the IndexPortionRecord[] given an array of MainPortionRecords
     *
     * @param records
     */
    /* public IndexPortion( ShapeHeader mainRecHeader,
     * LinkedList<MainPortionRecord> mpRecs ) { if ( mpRecs == null ) throw new
     * NullPointerException();
     *
     * if ( mpRecs.size() == 0 ) throw new IllegalArgumentException(
     * "Records length cannot be < 1");
     *
     * records = new IndexPortionRecord[mpRecs.size()];
     *
     * int offset = 50; for ( int i = 0; i < mpRecs.size(); i++ ) { records[i] =
     * new IndexPortionRecord(); records[i].contentLength =
     * mpRecs.get(i).getContentLength(); records[i].offset = offset; offset += 4
     * + records[i].contentLength; }
     *
     * this.header = new
     * ShapeHeader((50+records.length*4)*2,mainRecHeader.shapeType,mainRecHeader
     * .bbox); } */

    boolean doPrep() throws IOException
    {
        boolean returnValue = true;
        if (mode == ESRIShapefile.Mode.READ)
        {
            returnValue = parseRecords();
        }
        else // if mode == WRITE
        {
            // Linked list for writing
            records = new LinkedList<>();
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
     * Deletes the file managed by this class.
     *
     * @throws IOException if an  error occurs while closing the input/output
     *             streams.
     */
    public void delete() throws IOException
    {
        if (file != null && file.exists())
        {
            // Close the file descriptors first.
            try
            {
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

    public boolean parseRecords() throws IOException
    {
        boolean returnValue = true;
        FileChannel channel = inputStream.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(ShapeHeader.HEADER_SIZE);
        channel.read(buffer);
        buffer.flip();
        if (!header.parseHeader(buffer))
        {
            // Log
            returnValue = false;
        }
        else
        {
            // Whole point of an index file is to be able to quickly read the
            // entire
            // file into memory. Not bothering with any fancy partial read
            // stuff.
            int fileSize = header.fileLength - ShapeHeader.HEADER_SIZE;
            /* record size */
            int numRecords = fileSize / 8;

            buffer = ByteBuffer.allocate(fileSize);
            channel.read(buffer);
            buffer.flip();

            records = new ArrayList<>(numRecords);

            for (int i = 0; i < numRecords; i++)
            {
                IndexPortionRecord record = new IndexPortionRecord();
                record.parseRecord(buffer);
                records.add(record);
            }
        }

        return returnValue;
    }

    public boolean writeRecords(ShapeHeader header) throws IOException
    {
        boolean returnValue = true;
        FileChannel channel = outputStream.getChannel();

        // SHP and SHX headers need to be guaranteed to be the same (except for
        // filesize).
        // Using SHP header to do write ensures this.
        this.header = header;
        this.header.fileLength = ShapeHeader.HEADER_SIZE + records.size() * 8;

        ByteBuffer buffer = ByteBuffer.allocate(ShapeHeader.HEADER_SIZE);
        header.writeHeader(buffer);
        buffer.flip();
        channel.write(buffer);

        buffer = ByteBuffer.allocate(8);

        for (IndexPortionRecord record : records)
        {
            record.writeRecord(buffer);
            buffer.flip();
            channel.write(buffer);
            buffer.flip();
        }

        return returnValue;
    }

    /* public ByteBuffer getAsByteBuffer() { int allocationSize =
     * ShapeHeader.HEADER_SIZE + (records.size() * 8); ByteBuffer byteBuffer =
     * ByteBuffer.allocate(allocationSize);
     *
     * byteBuffer.put(header.getAsByteBuffer()); for ( int i = 0; i <
     * records.size(); i++ ) { byteBuffer.put(records.get(i).getAsByteBuffer());
     * } byteBuffer.flip(); return byteBuffer; }
     *
     * public boolean writeFile( FileOutputStream outputStream ) throws
     * IOException { boolean returnValue = true;
     *
     * FileChannel channel = outputStream.getChannel();
     *
     * try { if(!header.writeHeader(channel)) { returnValue = false; } else {
     * for ( int i = 0; i < records.size(); i++ ) {
     * records.get(i).writeRecord(channel); } } } finally { channel.close(); }
     *
     * return returnValue; } */
}
