package io.opensphere.core.common.shapefile.v2.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.shapefile.shapes.MeasureMinMax;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.shapes.ZMinMax;
import io.opensphere.core.common.shapefile.utils.ShapeHeader;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile.Mode;
import io.opensphere.core.common.shapefile.v2.index.IndexPortion;
import io.opensphere.core.common.shapefile.v2.index.IndexPortionRecord;

/**
 * This class represents the "main" (shp) portion of an ESRI Shapefile.
 */
public class MainPortion implements Collection<ShapeRecord>
{

    public static final String ERR_WRITING_TO_FILE = "Error writing to file.";

    public static final String ERR_RECORD_TYPE_MISMATCH = "Record type much match header type";

    /** The logger */
    private static Log LOGGER = LogFactory.getLog(MainPortion.class);

    private ESRIShapefile.Mode mode;

    private File file = null;

    private FileInputStream inputStream = null;

    private FileOutputStream outputStream = null;

    private ShapeHeader header = null;

    private IndexPortion index = null;

    /**
     * Constructor
     *
     * @param mode
     * @param filePath
     * @throws FileNotFoundException
     */
    public MainPortion(ESRIShapefile.Mode mode, String filePath) throws FileNotFoundException
    {
        this.mode = mode;
        file = new File(filePath);
        open();
        header = new ShapeHeader();

    }

    public ShapeHeader getHeader()
    {
        return header;
    }

    public void addIndex(IndexPortion index)
    {
        this.index = index;
    }

    public boolean doPrep() throws IOException
    {
        boolean returnValue = true;
        if (mode == ESRIShapefile.Mode.READ)
        {
            ByteBuffer buffer = ByteBuffer.allocate(ShapeHeader.HEADER_SIZE);
            inputStream.getChannel().read(buffer);
            buffer.flip();
            returnValue = header.parseHeader(buffer);
        }
        else // if mode == WRITE
        {
            LOGGER.debug("Unimplemented");
        }

        LOGGER.debug("Header File Length: " + header.fileLength);
        return returnValue;
    }

    public InputStream getAsInputStream() throws IOException
    {
        if (mode == Mode.READ)
        {
            inputStream.getChannel().position(0);
            return inputStream;
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
        if (mode == Mode.WRITE)
        {
            outputStream.getChannel().position(0);

            for (long i = 0; i < bytesToRead; i++)
            {
                outputStream.write(is.read());
            }
        }
    }

    public InputStream getIndexAsInputStream() throws IOException
    {
        if (mode == Mode.READ && index != null)
        {
            return index.getAsInputStream();
        }

        return null;
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
            // Close the file descriptors first.
            try
            {
                close();
            }
            finally
            {
                try
                {
                    index.delete();
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
    }

    /**
     * Opens the shapefile file for input/output
     *
     * @throws IOException
     */
    public void open() throws FileNotFoundException
    {
        if (mode == ESRIShapefile.Mode.READ)
        {
            inputStream = new FileInputStream(file);
            if (null != index)
            {
                index.open();
            }
        }
        else // if (mode == ESRIShapefile.Mode.WRITE)
        {
            outputStream = new FileOutputStream(file);
        }
    }

    public void close() throws IOException
    {
        if (index != null)
        {
            index.close();
        }
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
        boolean returnValue = true;

        if (mode == ESRIShapefile.Mode.WRITE)
        {
            FileChannel channel = outputStream.getChannel();
            channel.position(0);
            ByteBuffer buffer = ByteBuffer.allocate(ShapeHeader.HEADER_SIZE);
            returnValue = header.writeHeader(buffer);
            buffer.flip();
            channel.write(buffer);
        }

        return returnValue;
    }

    public boolean writeIndex() throws IOException
    {
        return index.writeRecords(header);
    }

    public void checkHeader(ShapeRecord record)
    {
        if (header == null)
        {
            header = new ShapeHeader();
        }

        if (header.shapeType == 0)
        {
            header.shapeType = record.getShapeType();
        }

        // Must be same type throughout file. If it doesn't match,
        // throw something compatible with Collection.add()
        if (header.shapeType != record.getShapeType())
        {
            throw new IllegalArgumentException(ERR_RECORD_TYPE_MISMATCH);
        }
    }

    /**
     * Update the header based upon this record
     *
     * @param record The record to update from
     */
    public void updateHeader(ShapeRecord record)
    {
        // Shape header is functioning in bytes
        header.fileLength += 8 + record.getLengthInBytes();

        // Adjust bbox
        double[] shapeBBox = record.getBox();

        // Modify summary bounding boxes if necessary
        if (shapeBBox != null)
        {
            // Adjust bounding region if necessary
            if (shapeBBox[0] < header.bbox[0])
            {
                header.bbox[0] = shapeBBox[0];
            }

            if (shapeBBox[2] > header.bbox[2])
            {
                header.bbox[2] = shapeBBox[2];
            }

            if (shapeBBox[1] < header.bbox[1])
            {
                header.bbox[1] = shapeBBox[1];
            }

            if (shapeBBox[3] > header.bbox[3])
            {
                header.bbox[3] = shapeBBox[3];
            }
        }

        // Modify Z bounds if necessary
        if (record instanceof ZMinMax)
        {
            ZMinMax recZ = (ZMinMax)record;

            if (recZ.getZMin() < header.bbox[4])
            {
                header.bbox[4] = recZ.getZMin();
            }

            if (recZ.getZMax() > header.bbox[5])
            {
                header.bbox[5] = recZ.getZMax();
            }
        }
        else
        {
            if (header.bbox[4] != 0)
            {
                header.bbox[4] = 0;
            }
            if (header.bbox[5] != 0)
            {
                header.bbox[5] = 0;
            }
        }

        // Modify measurment bounds if necessary
        if (record instanceof MeasureMinMax)
        {
            MeasureMinMax recM = (MeasureMinMax)record;

            if (recM.getMeasurementMin() < header.bbox[6])
            {
                header.bbox[6] = recM.getMeasurementMin();
            }

            if (recM.getMeasurementMax() > header.bbox[7])
            {
                header.bbox[7] = recM.getMeasurementMax();
            }
        }
        else
        {
            if (header.bbox[6] != 0)
            {
                header.bbox[6] = 0;
            }
            if (header.bbox[7] != 0)
            {
                header.bbox[7] = 0;
            }
        }

    }

    @Override
    public boolean add(ShapeRecord pRecordShape)
    {
        if (mode == ESRIShapefile.Mode.WRITE)
        {
            checkHeader(pRecordShape);

            int offset = header.fileLength / 2;
            int contentLength = pRecordShape.getContentLengthInWords();
            index.getRecords().add(new IndexPortionRecord(offset, contentLength));
            int bytesToAllocate = contentLength * 2 + 8;
            // MainPortionRecord has 8 extra bytes
            ByteBuffer buffer = ByteBuffer.allocate(bytesToAllocate);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(index.getRecords().size());
            buffer.putInt(contentLength);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            pRecordShape.writeRecord(buffer);

            FileChannel thisFile = outputStream.getChannel();
            buffer.flip();

            try
            {
                thisFile.position(offset * 2);
                thisFile.write(buffer);
            }
            catch (IOException e)
            {
                LOGGER.error(ERR_WRITING_TO_FILE, e);
                // RETHROW in something that matches the interface
                throw new IllegalStateException(ERR_WRITING_TO_FILE, e);
            }

            updateHeader(pRecordShape);
        }
        else
        {
            // Not setup for writing
            throw new NonWritableChannelException();
        }

        // Per spec, in this case we throw exceptions on fail, not return false
        // Returning false is reserved for only the 'set' case where duplicates
        // are
        // not allowed to be added.
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends ShapeRecord> c)
    {
        if (mode == ESRIShapefile.Mode.WRITE)
        {
            for (ShapeRecord shape : c)
            {
                add(shape);
            }
        }
        else
        {
            // Not setup for writing
            throw new NonWritableChannelException();
        }

        // Per spec, in this case we throw exceptions on fail, not return false
        return true;
    }

    @Override
    public void clear()
    {
        try
        {
            inputStream.getChannel().position(index.getRecords().get(0).getOffset());
        }
        catch (IOException e)
        {
            LOGGER.error(ERR_WRITING_TO_FILE, e);
        }
    }

    @Override
    public boolean contains(Object o)
    {
        // Will be exceptionally slow on large files
        boolean returnValue = false;
        for (ShapeRecord shape : this)
        {
            returnValue |= shape.equals(o);
            if (returnValue)
            {
                break;
            }
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
        return index.getRecords().size() == 0;
    }

    @Override
    public Iterator<ShapeRecord> iterator()
    {
        return new MainPortionIterator(this);
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
        return index.getRecords().size();
    }

    @Override
    public Object[] toArray()
    {
        Object[] returnArray = null;

        List<ShapeRecord> list = new LinkedList<>();
        for (ShapeRecord shape : this)
        {
            list.add(shape);
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
            a = (T[])new ShapeRecord[size()];
        }

        int i = 0;
        for (ShapeRecord sr : this)
        {
            a[i++] = (T)sr;
        }

        return a;
    }

    protected FileInputStream getInputStream()
    {
        return inputStream;
    }

    protected FileOutputStream getOutputStream()
    {
        return outputStream;
    }

    public IndexPortion getIndex()
    {
        return index;
    }
}
