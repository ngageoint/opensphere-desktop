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
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import io.opensphere.core.common.shapefile.ESRIShapefile.Mode;
import io.opensphere.core.common.shapefile.shapes.MeasureMinMax;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord.ShapeType;
import io.opensphere.core.common.shapefile.shapes.ZMinMax;
import io.opensphere.core.common.shapefile.utils.ShapeHeader;

/**
 * This class represents the "main" (shp) portion of an ESRI Shapefile.
 *
 * @deprecated Deprecated to com.bitsys.common.shapefile.v2.MainPortion
 * @see io.opensphere.core.common.shapefile.v2.main.MainPortion
 */
@Deprecated
class MainPortion implements Collection<ShapeRecord>
{

    // Contents

    public class MainPortionRecord
    {
        /* Byte Position Field Value Type Order Byte 0 Record Number Record
         * Number Integer Big Byte 4 Content Length Content Length Integer Big
         *
         * Content Length is # of 16bit words. */

        private int recordNumber;

        private int contentLength;

        private ShapeRecord record;

        /**
         * Constructor. Intentionally does nothing. Use parse() functions to
         * force the file to be read.
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
        public boolean parseRecord(ByteBuffer buffer) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
        {
            boolean returnValue = true;

            buffer.order(ByteOrder.BIG_ENDIAN);
            recordNumber = buffer.getInt();
            contentLength = buffer.getInt();

            ShapeType type = ShapeType.getInstance(header.shapeType);
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

//            System.out.println("Starting Record " + recNumber + " At Pos: " + channel.position() );

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

    public class MainPortionIterator implements Iterator<ShapeRecord>
    {
        MainPortion parent = null;

        FileChannel thisFile = null;

        int nextRecord = 0;

        public MainPortionIterator(MainPortion mp)
        {
            try
            {
                parent = mp;
                thisFile = mp.inputStream.getChannel();
                // reset position
                if (!index.records.isEmpty())
                {
                    thisFile.position(index.records.get(0).offset);
                }
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
        public ShapeRecord next()
        {
            MainPortionRecord record = new MainPortionRecord();
            try
            {
                thisFile.position(2 * parent.index.records.get(nextRecord).offset);
                int bytesToAllocate = 2 * (index.records.get(nextRecord).contentLength + 4);
                // Offset units is # of 16bit words
                // Extra 4 words is for the mainportionrecord headers around the
                // shaperecord

                ByteBuffer buffer = ByteBuffer.allocate(bytesToAllocate);
                thisFile.read(buffer);
                buffer.flip();
                record.parseRecord(buffer);
                nextRecord++;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (ReflectiveOperationException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return record.getRecord();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }

    private final ESRIShapefile.Mode mode;

    private File file = null;

    private FileInputStream inputStream = null;

    private FileOutputStream outputStream = null;

    private ShapeHeader header = null;

    private IndexPortion index = null;

    public MainPortion(ESRIShapefile.Mode mode, String filePath) throws FileNotFoundException
    {
        this.mode = mode;
        file = new File(filePath);
        if (mode == ESRIShapefile.Mode.READ)
        {
            inputStream = new FileInputStream(file);
            header = new ShapeHeader();
        }
        else // if (mode == ESRIShapefile.Mode.WRITE)
        {
            outputStream = new FileOutputStream(file);
            header = new ShapeHeader();
        }
    }

    public ShapeHeader getHeader()
    {
        return header;
    }

    /**
     * Creates the main portion for this ShapeFile, an IndexPortion and Header
     * is auto-generated to match the MainPortionRecord[] and Header.
     *
     * @param header
     * @param records
     * @throws UnsupportedDataTypeException if the record types in the array are
     *             not yet supported
     * @throws IllegalArgumentException if records does not contain at least one
     *             record or if the list of records does not contain all of the
     *             same type of shape.
     * @throws NullPointerException if records is null or any value in records
     *             is null
     */
    /* public MainPortion( List<ShapeRecord> records ) { setShapes(records);
     * } */

    public void addIndex(IndexPortion index)
    {
        this.index = index;
    }

    /* /** Writes the main portion to the two associated files .shp and .shx
     * written as filePath/fileNamePrefix.shp and filePath/fileNamePrefix.shx
     *
     * @param filePath - path for the file
     *
     * @param fileNamePrefix - the file name prefix
     *
     * @return true if successful
     *
     * @throws IOException
     *
     * @throws FileNotFoundException *//* public boolean writeToFiles( String
                                        * filePath, String fileNamePrefix )
                                        * throws IOException,
                                        * FileNotFoundException { boolean
                                        * returnValue = true;
                                        *
                                        * if ( this.index == null && records !=
                                        * null ) setShapes(records);
                                        *
                                        * File shpFile = new File( filePath +
                                        * File.separator + fileNamePrefix +
                                        * ".shp"); File shxFile = new File(
                                        * filePath + File.separator +
                                        * fileNamePrefix + ".shx");
                                        *
                                        * // TODO: Write the main file.
                                        * FileOutputStream shpFOS = new
                                        * FileOutputStream(shpFile); FileChannel
                                        * shpChannel = shpFOS.getChannel(); try
                                        * { // System.out.
                                        * println("Writing Main File: ");
                                        * this.header.writeHeader(shpChannel);
                                        * // TODO clean up inefficiencies for (
                                        * int i = 0; i < records.size(); i++ )
                                        * records.get(i).writeRecord(i+1,
                                        * shpChannel); } finally {
                                        * shpChannel.close(); }
                                        *
                                        * // Write out the index file
                                        * FileOutputStream shxFOS = new
                                        * FileOutputStream(shxFile);
                                        * this.index.writeFile(shxFOS);
                                        *
                                        * return returnValue; }
                                        *
                                        * /** Gets an array of byte buffers.
                                        *
                                        * @return ByteBuffer[] always length of
                                        * 2 Index 0: shp ByteBuffer Index 1: shx
                                        * ByteBuffer *//* public ByteBuffer[]
                                                        * getAsByteBuffer() {
                                                        * ByteBuffer[] buffArray
                                                        * = new ByteBuffer[2];
                                                        * int allocateSize =
                                                        * ShapeHeader.
                                                        * HEADER_SIZE;
                                                        * List<ByteBuffer>
                                                        * recordBuffs = new
                                                        * ArrayList<ByteBuffer>(
                                                        * ); //Capture all of
                                                        * the record byte
                                                        * buffers so that we can
                                                        * //calculate an
                                                        * allocation size, and
                                                        * add to the overall
                                                        * //shpBuffer
                                                        *
                                                        * // TODO Do we really
                                                        * need all this copying?
                                                        * for ( int i = 0; i <
                                                        * records.size(); i++ )
                                                        * { ByteBuffer
                                                        * recordBuff =
                                                        * records.get(i).
                                                        * getAsByteBuffer(i+1);
                                                        * recordBuffs.add(
                                                        * recordBuff);
                                                        * allocateSize +=
                                                        * recordBuff.limit(); }
                                                        * ByteBuffer shpBuffer =
                                                        * ByteBuffer.allocate(
                                                        * allocateSize);
                                                        * shpBuffer.put(this.
                                                        * header.getAsByteBuffer
                                                        * ()); for (ByteBuffer
                                                        * recordBuff:
                                                        * recordBuffs) {
                                                        * shpBuffer.put(
                                                        * recordBuff); }
                                                        * shpBuffer.flip();
                                                        * buffArray[0] =
                                                        * shpBuffer;
                                                        *
                                                        * //Add the shx byte
                                                        * buffer buffArray[1] =
                                                        * this.index.
                                                        * getAsByteBuffer();
                                                        * return buffArray; } */
    boolean doPrep() throws IOException
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
            // stuff
        }

        // System.out.println("Header File Length: " + header.fileLength);
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

    InputStream getIndexAsInputStream() throws IOException
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

    /* public List<ShapeRecord> parseRecords() throws IOException,
     * InstantiationException, IllegalAccessException { List<ShapeRecord>
     * returnList = new LinkedList<ShapeRecord>();
     * parseRecords(Integer.MAX_VALUE, returnList ); return returnList; }
     *
     * public void parseRecords(int numRecords, List<ShapeRecord> returnList )
     * throws IOException, InstantiationException, IllegalAccessException {
     * FileChannel channel = inputStream.getChannel();
     *
     * if (index != null && index.records != null ) { // I have an index, read
     * optimally. // This is only going to be important for HUGE shapefiles. //
     * For most shapefiles, might be able to get away with loading the whole
     * file. int recordsLeft = index.records.length - readPointer;
     *
     * System.out.println("records: " + (index.records.length - readPointer));
     * while ( recordsLeft > 0 && returnList.size() < numRecords ) { int
     * recordsThisRead = Math.min(Math.min(numRecords, recordsLeft),
     * arbitraryReadSize);
     *
     * System.out.println("recordsThisRead:" + recordsThisRead); int
     * bytesToAllocate = 2*(
     * (index.records[(readPointer+recordsThisRead)-1].offset -
     * index.records[readPointer].offset) +
     * index.records[(readPointer+recordsThisRead)-1].contentLength+4); //Offset
     * units is # of 16bit words
     *
     * ByteBuffer buffer = ByteBuffer.allocate(bytesToAllocate);
     * channel.read(buffer); buffer.flip(); for (int i = 0; i < recordsThisRead;
     * i++) { MainPortionRecord record = new MainPortionRecord(); if (
     * record.parseRecord(buffer) ) returnList.add(record.getRecord()); }
     * recordsLeft = recordsLeft - recordsThisRead; readPointer = readPointer +
     * recordsThisRead; System.out.println("recordsLeft: " + recordsLeft +
     * " readPointer: " + readPointer); } }
     *
     *
     * // System.out.println("Records: " + records.length ); } */

    // Convenience function for any case where we have the shapes, and not the
    // Full shape record. Shape record has the extra 8 bytes for record number
    // and
    // content length
    /* public void setShapes( List<ShapeRecord> shapeList ) { if ( shapeList ==
     * null ) throw new NullPointerException();
     *
     * if ( shapeList.size() <= 0 ) throw new IllegalArgumentException();
     *
     * // Explode out shapes into shape records LinkedList<MainPortionRecord>
     * shapeRecords = new LinkedList<MainPortionRecord>(); for ( int i = 0; i <
     * shapeList.size(); i++ ) shapeRecords.add( new
     * MainPortionRecord(shapeList.get(i)) );
     *
     * setShapes(shapeRecords); } */

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
            throw new IllegalArgumentException();
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

    /*    *//**
             * Sets the main portion for this ShapeFile, an IndexPortion and
             * Header are auto-generated to match the MainPortionRecord list and
             * Header.
             *
             * @param header
             * @param records
             * @throws UnsupportedDataTypeException if the record types in the
             *             array are not yet supported
             * @throws IllegalArgumentException if records does not contain at
             *             least one record or if the list of records does not
             *             contain all of the same type of shape.
             * @throws NullPointerException if records is null or any value in
             *             records is null
             *//* public void setShapes( LinkedList<MainPortionRecord> shapes )
                * { if ( shapes == null ) throw new NullPointerException();
                *
                * if ( shapes.size() <= 0 ) throw new
                * IllegalArgumentException();
                *
                * records = (LinkedList<MainPortionRecord>) shapes.clone();
                *
                * int shapeType =
                * records.peekFirst().getRecord().getShapeType();
                *
                * // Assume all shapeType are the same since that is the
                * standard. // To store the bounding box for the header double[]
                * bbox = new double[8];
                *
                * // Prep the bounding box values // Xmin bbox[0] =
                * Double.MAX_VALUE; // Ymin bbox[1] = Double.MAX_VALUE; // Xmax
                * bbox[2] = Double.MIN_VALUE; // Ymax bbox[3] =
                * Double.MIN_VALUE; bbox[4] = 0.0; bbox[5] = 0.0; bbox[6] = 0.0;
                * bbox[7] = 0.0;
                *
                * // Prep Z Min and Max only if we're using a Z type shape. if (
                * shapeType == 11 || shapeType == 13 || shapeType == 15 ||
                * shapeType == 18) { // Zmin bbox[4] = Double.MAX_VALUE; // Zmax
                * bbox[5] = Double.MIN_VALUE; }
                *
                * // Prep Do M Min and Max only if we're using a M type shape.
                * if ( shapeType == 23 || shapeType == 21 || shapeType == 25 ||
                * shapeType == 28) { // Mmin bbox[6] = Double.MAX_VALUE; // Mmax
                * bbox[7] = Double.MIN_VALUE; }
                *
                * // Inital 50 for header. int fileLength = 50;
                *
                * //for ( int i = 0; i < this.records.length; i++ ) // Last
                * chance before writing, make sure all the right components are
                * // correctly populated. This method is intentionally
                * inefficient int numRecords = 0; for ( MainPortionRecord record
                * : records ) { if ( record.getRecord().getShapeType() !=
                * shapeType ) throw new
                * IllegalArgumentException("Mixed Shape Types Detected, This is not allowed"
                * );
                *
                * ShapeRecord rec = record.getRecord();
                *
                * //Ensure mainportion record has the size component
                * record.contentLength = rec.getContentLengthInWords();
                *
                * // Make sure record numbers and content length are set
                * properly. record.recordNumber = ++numRecords;
                * record.contentLength =
                * record.getRecord().getContentLengthInWords(); fileLength += 4
                * + record.contentLength;
                *
                * double[] shapeBBox = rec.getBox();
                *
                * // Modify summary bounding boxes if necessary if ( shapeBBox
                * != null ) { // Adjust bounding region if necessary if (
                * shapeBBox[0] < bbox[0] ) // Xmin bbox[0] = shapeBBox[0];
                *
                * if ( shapeBBox[2] > bbox[2] ) // Xmax bbox[2] = shapeBBox[2];
                *
                * if ( shapeBBox[1] < bbox[1] ) // Ymin bbox[1] = shapeBBox[1];
                *
                * if ( shapeBBox[3] > bbox[3] ) // Ymax bbox[3] = shapeBBox[3];
                * }
                *
                * // Modify measurment bounds if necessary if ( rec instanceof
                * MeasureMinMax ) { MeasureMinMax recM = (MeasureMinMax)rec;
                *
                * if ( recM.getMeasurementMin() < bbox[4] ) bbox[4] =
                * recM.getMeasurementMin();
                *
                * if ( recM.getMeasurementMax() > bbox[5] ) bbox[5] =
                * recM.getMeasurementMax(); }
                *
                * // Modify Z bounds if necessary if ( rec instanceof ZMinMax )
                * { ZMinMax recM = (ZMinMax)rec;
                *
                * if ( recM.getZMin() < bbox[4] ) bbox[4] = recM.getZMin();
                *
                * if ( recM.getZMax() > bbox[5] ) bbox[5] = recM.getZMax(); } }
                *
                * this.header = new ShapeHeader(fileLength,shapeType, bbox);
                * //this.index = new IndexPortion(this.header,this.records);
                * } */

    /**
     * Retrieves all of the shapes in the main portion
     *
     * @return Array of shape records
     *
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    /* public MainPortionRecord[] getShapes() throws IOException,
     * InstantiationException, IllegalAccessException { boolean progress = true;
     * MainPortionRecord[] returnArray = null;
     *
     * if ( records == null ) { if ( header == null ) { progress =
     * parseHeader(); } if ( progress ) { progress = parseRecords(); } }
     *
     * // just to be sure returnArray = (MainPortionRecord[]) (( progress ) ?
     * records.toArray(new MainPortionRecord[0]) : null);
     *
     * return returnArray; } */

    @Override
    public boolean add(ShapeRecord e)
    {
        if (mode == ESRIShapefile.Mode.WRITE)
        {
            checkHeader(e);

            int offset = header.fileLength / 2;
            int contentLength = e.getContentLengthInWords();
            index.records.add(index.new IndexPortionRecord(offset, contentLength));
            int bytesToAllocate = contentLength * 2 + 8;
            // MainPortionRecord has 8 extra bytes
            ByteBuffer buffer = ByteBuffer.allocate(bytesToAllocate);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(index.records.size());
            buffer.putInt(contentLength);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            e.writeRecord(buffer);

            FileChannel thisFile = outputStream.getChannel();
            buffer.flip();

            try
            {
                thisFile.position(offset * 2);
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
            inputStream.getChannel().position(index.records.get(0).offset);
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
        return index.records.size() == 0;
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
        return index.records.size();
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
}
