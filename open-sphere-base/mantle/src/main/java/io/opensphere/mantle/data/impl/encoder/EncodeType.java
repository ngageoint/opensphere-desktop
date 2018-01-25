package io.opensphere.mantle.data.impl.encoder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** The Enum EncodeType. */
public enum EncodeType
{
    /** BOOLEAN OBJECT. */
    BOOLEAN_OBJ(15),

    /** BOOLEAN VALUE. */
    BOOLEAN_VALUE(8),

    /** The BYTE. */
    BYTE_OBJ(14),

    /** The BYTE STRING. */
    BYTE_STRING(2),

    /** The BYTE value. */
    BYTE_VALUE(7),

    /** COLOR. */
    COLOR(21),

    /** The DATA_ELEMENT. */
    DATA_ELEMENT(80),

    /** The DATE. */
    DATE(20),

    /** The DEFAULT_MAP_ELLIPSE_GEOMETRY_SUPPORT. */
    DEFAULT_MAP_ELLIPSE_GEOMETRY_SUPPORT(62),

    /** DEFAULT_MAP_ICON_GEOMETRY_SUPPORT. */
    DEFAULT_MAP_ICON_GEOMETRY_SUPPORT(67),

    /** The DEFAULT_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT. */
    DEFAULT_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT(63),

    /** The DEFAULT_MAP_POINT_GEOMETRY_SUPPORT. */
    DEFAULT_MAP_POINT_GEOMETRY_SUPPORT(64),

    /** The DEFAULT_MAP_POLYGON_GEOMETRY_SUPPORT. */
    DEFAULT_MAP_POLYGON_GEOMETRY_SUPPORT(65),

    /** The DEFAULT_MAP_POLYLINE_GEOMETRY_SUPPORT. */
    DEFAULT_MAP_POLYLINE_GEOMETRY_SUPPORT(66),

    /** The DEFAULT_MAP_ELLIPSE_GEOMETRY_SUPPORT. */
    DEFAULT_MAP_CIRCLE_GEOMETRY_SUPPORT(68),

    /** The DOUBLE. */
    DOUBLE_OBJ(10),

    /** The DOUBLE value. */
    DOUBLE_VALUE(3),

    /** DYNAMIC_ENUM_INT_KEY. */
    DYNAMIC_ENUM_INT_KEY(101),

    /** DYNAMIC_ENUM_LONG_KEY. */
    DYNAMIC_ENUM_LONG_KEY(102),

    /** DYNAMIC_METADATALIST. */
    DYNAMIC_METADATALIST(100),

    /** The FLOAT. */
    FLOAT_OBJ(11),

    /** The FLOAT value. */
    FLOAT_VALUE(4),

    /** The INT value. */
    INT_VALUE(6),

    /** The INTEGER. */
    INTEGER_OBJ(13),

    /** LAT_LON_ALT. */
    LAT_LON_ALT(81),

    /** The LIST. */
    LIST(30),

    /** The LIST_ENTRY. */
    LIST_ENTRY(31),

    /** The LONG. */
    LONG_OBJ(12),

    /** The LONG value. */
    LONG_VALUE(5),

    /** MAP_GEOMETRY_CALLOUT_SUPPORT. */
    MAP_GEOMETRY_CALLOUT_SUPPORT(70),

    /** The MAP_GEOMETRY_SUPPORT. */
    MAP_GEOMETRY_SUPPORT(60),

    /** The MAP_GEOMETRY_TYPE_UNKNOWN. */
    MAP_GEOMETRY_TYPE_UNKNOWN(61),

    /** The NULL. */
    NULL(-1),

    /** The OBJECT. */
    OBJECT(-2),

    /** The SIMPLE_MAP_ELLIPSE_GEOMETRY_SUPPORT. */
    SIMPLE_MAP_ELLIPSE_GEOMETRY_SUPPORT(71),

    /** The DEFAULT_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT. */
    SIMPLE_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT(72),

    /** The DEFAULT_MAP_POINT_GEOMETRY_SUPPORT. */
    SIMPLE_MAP_POINT_GEOMETRY_SUPPORT(73),

    /** The SIMPLE_MAP_POLYGON_GEOMETRY_SUPPORT. */
    SIMPLE_MAP_POLYGON_GEOMETRY_SUPPORT(74),

    /** The SIMPLE_MAP_POLYLINE_GEOMETRY_SUPPORT. */
    SIMPLE_MAP_POLYLINE_GEOMETRY_SUPPORT(75),

    /** The SIMPLE_MAP_ELLIPSE_GEOMETRY_SUPPORT. */
    SIMPLE_MAP_CIRCLE_GEOMETRY_SUPPORT(76),

    /** The STRING. */
    STRING(1),

    /** TIMESPAN_INSTANT. */
    TIMESPAN_INSTANT(42),

    /** TIMESPAN_SPAN. */
    TIMESPAN_SPAN(41),

    /** TIMELESS TIMESPAN. */
    TIMESPAN_TIMELESS(40),

    /** UNBOUNDED END TIMESPAN. */
    TIMESPAN_UNBOUNDED_END(44),

    /** UNBOUNDED START TIMESPAN. */
    TIMESPAN_UNBOUNDED_START(43),

    /** The UNKNOWN. */
    UNKNOWN(0);

    /** The code. */
    private final byte myCode;

    /**
     * Decode.
     *
     * @param ois the ois
     * @return the encode type
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static EncodeType decode(ObjectInputStream ois) throws IOException
    {
        byte bVal = ois.readByte();
        EncodeType et = UNKNOWN;
        boolean found = false;
        for (EncodeType type : EncodeType.values())
        {
            if (type.myCode == bVal)
            {
                et = type;
                found = true;
                break;
            }
        }
        if (!found)
        {
            throw new IOException("Invalid encode type " + bVal + " found!");
        }
        return et;
    }

    /**
     * Instantiates a new encode type.
     *
     * @param val the val
     */
    EncodeType(int val)
    {
        myCode = (byte)val;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public byte code()
    {
        return myCode;
    }

    /**
     * Encode.
     *
     * @param oos the oos
     * @return the number of bytes written.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public int encode(ObjectOutputStream oos) throws IOException
    {
        oos.writeByte(myCode);
        return 1;
    }

    /**
     * Checks if is null type.
     *
     * @return true, if is null type
     */
    public boolean isNullType()
    {
        return this == NULL;
    }
}
