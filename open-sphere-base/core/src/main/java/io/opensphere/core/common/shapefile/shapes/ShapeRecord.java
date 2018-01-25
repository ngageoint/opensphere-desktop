/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public abstract class ShapeRecord
{
    private static final Map<Integer, ShapeType> VALUE_SHAPE_MAP = new HashMap<>();

    public enum ShapeType
    {
        NULLSHAPE(0, NullRecord.class), POINT(1, PointRecord.class), POLYLINE(3, PolyLineRecord.class), POLYGON(5,
                PolygonRecord.class), MULTIPOINT(8, MultiPointRecord.class), POINTZ(11, PointZRecord.class), POLYLINEZ(13,
                        PolyLineZRecord.class), POLYGONZ(15, PolygonZRecord.class), MULTIPOINTZ(18,
                                MultiPointZRecord.class), POINTM(21, PointMRecord.class), POLYLINEM(23,
                                        PolyLineMRecord.class), POLYGONM(25, PolygonMRecord.class), MULTIPOINTM(28,
                                                MultiPointMRecord.class), MULTIPATCH(31, MultiPatchRecord.class);

        private int value;

        private Class<? extends ShapeRecord> shapeRecord;

        private ShapeType(int value, Class<? extends ShapeRecord> shapeRecord)
        {
            this.value = value;
            this.shapeRecord = shapeRecord;
            VALUE_SHAPE_MAP.put(value, this);
        }

        /**
         * The value.
         *
         * @return the value
         */
        public int getValue()
        {
            return value;
        }

        /**
         * @throws IllegalAccessException
         * @throws InstantiationException
         *
         */
        public ShapeRecord getShapeRecordInstance() throws InstantiationException, IllegalAccessException
        {
            return shapeRecord.newInstance();
        }

        /**
         *
         */
        public static ShapeType getInstance(int type)
        {
            return VALUE_SHAPE_MAP.get(type);
        }
    };

    /* Byte Position Field Value Type Order Byte 0 Shape Type Shape Type Integer
     * Little */
    protected int shapeType;

    public ShapeRecord()
    {
    }

    /**
     * @return the shapeType
     */
    public int getShapeType()
    {
        return shapeType;
    }

    /**
     * Returns the array of min x, y and max x, y.
     *
     * @return the box, null if no bounding box is possible
     */
    public abstract double[] getBox();

    public abstract boolean parseRecord(ByteBuffer buffer);

    public abstract boolean writeRecord(ByteBuffer buffer);

    /**
     * Parses the first integer from the buffer to determine the shape type.
     *
     * @param buffer the buffer containing the shape data.
     * @return <code>true</code> if the shape was successfully read.
     */
    public boolean parseRecordType(ByteBuffer buffer)
    {
        boolean returnValue = true;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        shapeType = buffer.getInt();
        return returnValue;
    }

    public boolean writeRecordType(ByteBuffer buffer)
    {
        boolean returnValue = true;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(shapeType);
        return returnValue;
    }

    /**
     * Returns the content length of the record in Words ( 16 bit words )
     *
     * @return the content length of the record
     */
    public abstract int getContentLengthInWords();

    public int getLengthInBytes()
    {
        return getContentLengthInWords() * 2;
    }

    @Override
    public String toString()
    {
        return ShapeType.getInstance(shapeType).toString();
    }

}
