package io.opensphere.mantle.data.impl.encoder;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.lang.ByteString;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.CallOutSupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.geom.impl.AbstractSimpleGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapIconGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPolylineGeometrySupport;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.impl.DynamicEnumerationCombinedIntKey;
import io.opensphere.mantle.util.dynenum.impl.DynamicEnumerationCombinedLongKey;

/**
 * The Class DiskDecodeHelper.
 */
@SuppressWarnings({ "PMD.AvoidUsingShortType", "PMD.GodClass" })
public final class DiskDecodeHelper
{
    /**
     * Decode color.
     *
     * @param oos the oos
     * @return the color
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Color decodeColor(ObjectInputStream oos) throws IOException
    {
        return new Color(oos.readInt());
    }

    /**
     * Decode dynamic enum key.
     *
     * @param oos the oos
     * @param et the et
     * @return the dynamic enumeration key
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static DynamicEnumerationKey decodeDynamicEnumKey(ObjectInputStream oos, EncodeType et) throws IOException
    {
        DynamicEnumerationKey result = null;

        if (et == EncodeType.DYNAMIC_ENUM_INT_KEY)
        {
            result = new DynamicEnumerationCombinedIntKey(oos.readInt());
        }
        else if (et == EncodeType.DYNAMIC_ENUM_LONG_KEY)
        {
            result = new DynamicEnumerationCombinedLongKey(oos.readLong());
        }
        return result;
    }

    /**
     * Decode map geometry support.
     *
     * @param oos the oos
     * @return the map geometry support
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static MapGeometrySupport decodeMapGeometrySupport(ObjectInputStream oos) throws IOException
    {
        MapGeometrySupport support = null;
        EncodeType et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            support = decodeMapGeometrySupportInternal(oos);
        }
        return support;
    }

    /**
     * Decode map geometry support.
     *
     * @param oos the oos
     * @return the map geometry support
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static MapGeometrySupport decodeMapGeometrySupportInternal(ObjectInputStream oos) throws IOException
    {
        EncodeType et = EncodeType.decode(oos);
        if (et == EncodeType.SIMPLE_MAP_POINT_GEOMETRY_SUPPORT || et == EncodeType.SIMPLE_MAP_ELLIPSE_GEOMETRY_SUPPORT
                || et == EncodeType.SIMPLE_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT
                || et == EncodeType.SIMPLE_MAP_POLYGON_GEOMETRY_SUPPORT || et == EncodeType.SIMPLE_MAP_POLYLINE_GEOMETRY_SUPPORT)
        {
            return decodeSimpleMapGeometrySupportInternal(et, oos);
        }
        else if (et == EncodeType.DEFAULT_MAP_POINT_GEOMETRY_SUPPORT || et == EncodeType.DEFAULT_MAP_ELLIPSE_GEOMETRY_SUPPORT
                || et == EncodeType.DEFAULT_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT)
        {
            return decodeDefaultLocationMapGeometrySupportInternal(et, oos);
        }
        else if (et == EncodeType.DEFAULT_MAP_POLYGON_GEOMETRY_SUPPORT)
        {
            DefaultMapPolygonGeometrySupport poly = new DefaultMapPolygonGeometrySupport();
            poly.setFilled(oos.readBoolean());
            poly.setLineDrawn(oos.readBoolean());
            decodeMapPathGeometrySupport(oos, poly);
            decodeMapGeometrySupportPortion(oos, poly);
            return poly;
        }
        else if (et == EncodeType.DEFAULT_MAP_POLYLINE_GEOMETRY_SUPPORT)
        {
            DefaultMapPolylineGeometrySupport poly = new DefaultMapPolylineGeometrySupport();
            decodeMapPathGeometrySupport(oos, poly);
            decodeMapGeometrySupportPortion(oos, poly);
            return poly;
        }
        else if (et == EncodeType.DEFAULT_MAP_ICON_GEOMETRY_SUPPORT)
        {
            DefaultMapIconGeometrySupport icon = new DefaultMapIconGeometrySupport();
            decodeMapLocationGeometrySupport(oos, icon);
            icon.setIconURL(decodeString(oos));
            icon.setIconSize(decodeFloat(oos));
            decodeMapGeometrySupportPortion(oos, icon);
            return icon;
        }
        else
        {
            Object o = null;
            try
            {
                o = oos.readObject();
            }
            catch (ClassNotFoundException e)
            {
                throw new IOException("Unknown class found while decoding MapGeometrySupport element.", e);
            }
            return (MapGeometrySupport)o;
        }
    }

    /**
     * Decode meta data list as a stand-alone object from the input stream.
     * (i.e. reads off header encode type byte)
     *
     * @param oos the oos
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static List<Object> decodeMetaDataList(ObjectInputStream oos) throws IOException
    {
        EncodeType et = EncodeType.decode(oos);
        if (!et.isNullType() && et == EncodeType.LIST)
        {
            return DiskDecodeHelper.decodeMetaDataListInternal(oos);
        }
        return null;
    }

    /**
     * Decode meta data list.
     *
     * @param oos the oos
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static List<Object> decodeMetaDataListInternal(ObjectInputStream oos) throws IOException
    {
        int length = oos.readInt();
        List<Object> mdList = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
        {
            Object val = decodeMetaDataValue(oos);
            mdList.add(val);
        }
        return mdList;
    }

    /**
     * Decode origin id.as a stand alone item.
     *
     * @param oos the oos
     * @return the long
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Long decodeOriginId(ObjectInputStream oos) throws IOException
    {
        Long result = null;
        EncodeType et = EncodeType.decode(oos);
        if (!et.isNullType() && et == EncodeType.LONG_OBJ)
        {
            result = Long.valueOf(oos.readLong());
        }
        return result;
    }

    /**
     * Decode string.
     *
     * @param oos the oos
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String decodeString(ObjectInputStream oos) throws IOException
    {
        short length = oos.readShort();
        byte[] value = new byte[length];
        int totalRead = oos.read(value);
        while (totalRead < value.length)
        {
            int read = oos.read(value, totalRead, length - totalRead);
            if (read == -1)
            {
                throw new IOException("End of Stream reached while trying after reading string value " + totalRead + " of "
                        + value.length + " bytes.");
            }
            else
            {
                totalRead += read;
            }
        }
        return new String(value, StringUtilities.DEFAULT_CHARSET);
    }

    /**
     * Decode time span.
     *
     * @param oos the oos
     * @param et the et
     * @return the time span
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static TimeSpan decodeTimeSpan(ObjectInputStream oos, EncodeType et) throws IOException
    {
        if (et == EncodeType.TIMESPAN_TIMELESS)
        {
            return TimeSpan.TIMELESS;
        }
        else if (et == EncodeType.TIMESPAN_INSTANT)
        {
            long time = oos.readLong();
            return TimeSpan.get(time, time);
        }
        else if (et == EncodeType.TIMESPAN_SPAN)
        {
            return TimeSpan.get(oos.readLong(), oos.readLong());
        }
        else if (et == EncodeType.TIMESPAN_UNBOUNDED_START)
        {
            return TimeSpan.newUnboundedStartTimeSpan(oos.readLong());
        }
        else if (et == EncodeType.TIMESPAN_UNBOUNDED_END)
        {
            return TimeSpan.newUnboundedEndTimeSpan(oos.readLong());
        }
        else
        {
            throw new IOException("Unknown EncodeType " + et.toString() + " where TIMESPAN sub type expected");
        }
    }

    /**
     * Decode byte.
     *
     * @param oos the oos
     * @return the byte
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Byte decodeByte(ObjectInputStream oos) throws IOException
    {
        return Byte.valueOf(oos.readByte());
    }

    /**
     * Decode byte string.
     *
     * @param oos the oos
     * @return the byte string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static ByteString decodeByteString(ObjectInputStream oos) throws IOException
    {
        short length = oos.readShort();
        byte[] value = new byte[length];
        int totalRead = oos.read(value);
        while (totalRead < value.length)
        {
            int read = oos.read(value, totalRead, length - totalRead);
            if (read == -1)
            {
                throw new IOException("End of Stream reached while trying after reading ByteString value " + totalRead + " of "
                        + value.length + " bytes.");
            }
            else
            {
                totalRead += read;
            }
        }
        return new ByteString(value);
    }

    /**
     * Decode date.
     *
     * @param oos the oos
     * @return the date
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Date decodeDate(ObjectInputStream oos) throws IOException
    {
        return new Date(oos.readLong());
    }

    /**
     * Decode default location map geometry support internal.
     *
     * @param et the et
     * @param oos the oos
     * @return the map geometry support
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static MapGeometrySupport decodeDefaultLocationMapGeometrySupportInternal(EncodeType et, ObjectInputStream oos)
        throws IOException
    {
        if (et == EncodeType.DEFAULT_MAP_POINT_GEOMETRY_SUPPORT)
        {
            DefaultMapPointGeometrySupport point = new DefaultMapPointGeometrySupport();
            decodeMapLocationGeometrySupport(oos, point);
            decodeMapGeometrySupportPortion(oos, point);
            return point;
        }
        else if (et == EncodeType.DEFAULT_MAP_ELLIPSE_GEOMETRY_SUPPORT)
        {
            DefaultMapEllipseGeometrySupport ellipse = new DefaultMapEllipseGeometrySupport();
            ellipse.setSemiMajorAxis(oos.readFloat());
            ellipse.setSemiMinorAxis(oos.readFloat());
            ellipse.setOrientation(oos.readFloat());
            decodeMapLocationGeometrySupport(oos, ellipse);
            decodeMapGeometrySupportPortion(oos, ellipse);
            return ellipse;
        }
        else if (et == EncodeType.DEFAULT_MAP_CIRCLE_GEOMETRY_SUPPORT)
        {
            DefaultMapCircleGeometrySupport circle = new DefaultMapCircleGeometrySupport();
            circle.setRadius(oos.readFloat());
            decodeMapLocationGeometrySupport(oos, circle);
            decodeMapGeometrySupportPortion(oos, circle);
            return circle;
        }
        else if (et == EncodeType.DEFAULT_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT)
        {
            DefaultMapLineOfBearingGeometrySupport lineOfBearing = new DefaultMapLineOfBearingGeometrySupport();
            lineOfBearing.setLength(oos.readFloat());
            lineOfBearing.setOrientation(oos.readFloat());
            decodeMapLocationGeometrySupport(oos, lineOfBearing);
            decodeMapGeometrySupportPortion(oos, lineOfBearing);
            return lineOfBearing;
        }
        return null;
    }

    /**
     * Decode double.
     *
     * @param oos the oos
     * @return the double
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Double decodeDouble(ObjectInputStream oos) throws IOException
    {
        return Double.valueOf(oos.readDouble());
    }

    /**
     * Decode float.
     *
     * @param oos the oos
     * @return the float
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Float decodeFloat(ObjectInputStream oos) throws IOException
    {
        return Float.valueOf(oos.readFloat());
    }

    /**
     * Decode integer.
     *
     * @param oos the oos
     * @return the integer
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Integer decodeInteger(ObjectInputStream oos) throws IOException
    {
        return Integer.valueOf(oos.readInt());
    }

    /**
     * Decode lat lon alt.
     *
     * @param oos the oos
     * @return the lat lon alt
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static LatLonAlt decodeLatLonAlt(ObjectInputStream oos) throws IOException
    {
        double lat = oos.readDouble();
        double lon = oos.readDouble();
        double alt = oos.readDouble();
        int arType = oos.readInt();
        Altitude.ReferenceLevel ar = Altitude.ReferenceLevel.values()[arType];
        return LatLonAlt.createFromDegreesMeters(lat, lon, alt, ar);
    }

    /**
     * Decode long.
     *
     * @param oos the oos
     * @return the long
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Long decodeLong(ObjectInputStream oos) throws IOException
    {
        return Long.valueOf(oos.readLong());
    }

    /**
     * Decode AbstractMapGeometrySupport portion.
     *
     * @param oos the oos
     * @param support the {@link AbstractMapGeometrySupport}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void decodeMapGeometrySupportPortion(ObjectInputStream oos, AbstractMapGeometrySupport support)
        throws IOException
    {
        EncodeType et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            support.setTimeSpan(decodeTimeSpan(oos, et));
        }

        et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            support.setColor(decodeColor(oos), null);
        }

        et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            Object o = null;
            try
            {
                o = oos.readObject();
            }
            catch (ClassNotFoundException e)
            {
                throw new IOException("Unknown class found while decoding CallOutSupport element of MapGeometrySupport.", e);
            }
            support.setCallOutSupport((CallOutSupport)o);
        }

        et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            support.setToolTip(decodeString(oos));
        }

        support.setFollowTerrain(oos.readBoolean(), null);

        et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            int numChildren = oos.readInt();
            for (int i = 0; i < numChildren; i++)
            {
                EncodeType et2 = EncodeType.decode(oos);
                if (et2.isNullType())
                {
                    support.addChild(null);
                }
                else
                {
                    support.addChild(decodeMapGeometrySupport(oos));
                }
            }
        }
    }

    /**
     * Decode map location geometry support.
     *
     * @param oos the oos
     * @param mlgs the mlgs
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void decodeMapLocationGeometrySupport(ObjectInputStream oos, MapLocationGeometrySupport mlgs)
        throws IOException
    {
        EncodeType et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            mlgs.setLocation(decodeLatLonAlt(oos));
        }
    }

    /**
     * Decode map path geometry support.
     *
     * @param oos the oos
     * @param support the {@link MapPathGeometrySupport}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void decodeMapPathGeometrySupport(ObjectInputStream oos, MapPathGeometrySupport support) throws IOException
    {
        EncodeType et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            int value = oos.readInt();
            support.setLineType(LineType.values()[value]);
        }
        else
        {
            support.setLineType(null);
        }

        et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            int count = oos.readInt();
            for (int i = 0; i < count; i++)
            {
                EncodeType et2 = EncodeType.decode(oos);
                if (et2.isNullType())
                {
                    support.addLocation(null);
                }
                else
                {
                    support.addLocation(decodeLatLonAlt(oos));
                }
            }
        }
    }

    /**
     * Decode meta data value.
     *
     * @param oos the oos
     * @return the object
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Object decodeMetaDataValue(ObjectInputStream oos) throws IOException
    {
        EncodeType et = EncodeType.decode(oos);
        if (et.isNullType())
        {
            return null;
        }
        else if (et == EncodeType.STRING)
        {
            return decodeString(oos);
        }
        else if (et == EncodeType.BYTE_STRING)
        {
            return decodeByteString(oos);
        }
        else if (et == EncodeType.DOUBLE_OBJ || et == EncodeType.FLOAT_OBJ || et == EncodeType.INTEGER_OBJ
                || et == EncodeType.LONG_OBJ || et == EncodeType.BYTE_OBJ)
        {
            return decodeNumber(oos, et);
        }
        else if (et == EncodeType.DATE)
        {
            return decodeDate(oos);
        }
        else if (et == EncodeType.TIMESPAN_INSTANT || et == EncodeType.TIMESPAN_SPAN || et == EncodeType.TIMESPAN_TIMELESS)
        {
            return decodeTimeSpan(oos, et);
        }
        else if (et == EncodeType.DYNAMIC_ENUM_INT_KEY || et == EncodeType.DYNAMIC_ENUM_LONG_KEY)
        {
            return decodeDynamicEnumKey(oos, et);
        }
        else
        {
            Object o = null;
            try
            {
                o = oos.readObject();
            }
            catch (ClassNotFoundException e)
            {
                throw new IOException("Unknown class found decoding meta data list.", e);
            }
            return o;
        }
    }

    /**
     * Decode number.
     *
     * @param oos the oos
     * @param et the et
     * @return the number
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static Number decodeNumber(ObjectInputStream oos, EncodeType et) throws IOException
    {
        if (et == EncodeType.DOUBLE_OBJ)
        {
            return decodeDouble(oos);
        }
        else if (et == EncodeType.FLOAT_OBJ)
        {
            return decodeFloat(oos);
        }
        else if (et == EncodeType.INTEGER_OBJ)
        {
            return decodeInteger(oos);
        }
        else if (et == EncodeType.LONG_OBJ)
        {
            return decodeLong(oos);
        }
        else if (et == EncodeType.BYTE_OBJ)
        {
            return decodeByte(oos);
        }
        else
        {
            Object o = null;
            try
            {
                o = oos.readObject();
            }
            catch (ClassNotFoundException e)
            {
                throw new IOException("Unknown class where Number expected.", e);
            }
            return (Number)o;
        }
    }

    /**
     * Decode simple map geometry support internal.
     *
     * @param et the et
     * @param oos the oos
     * @return the map geometry support
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static MapGeometrySupport decodeSimpleMapGeometrySupportInternal(EncodeType et, ObjectInputStream oos)
        throws IOException
    {
        if (et == EncodeType.SIMPLE_MAP_POINT_GEOMETRY_SUPPORT)
        {
            SimpleMapPointGeometrySupport simplePoint = new SimpleMapPointGeometrySupport();
            decodeMapLocationGeometrySupport(oos, simplePoint);
            decodeSimpleMapGeometrySupportPortion(oos, simplePoint);
            return simplePoint;
        }
        else if (et == EncodeType.SIMPLE_MAP_ELLIPSE_GEOMETRY_SUPPORT)
        {
            SimpleMapEllipseGeometrySupport simpleEllipse = new SimpleMapEllipseGeometrySupport();
            simpleEllipse.setSemiMajorAxis(oos.readFloat());
            simpleEllipse.setSemiMinorAxis(oos.readFloat());
            simpleEllipse.setOrientation(oos.readFloat());
            decodeMapLocationGeometrySupport(oos, simpleEllipse);
            decodeSimpleMapGeometrySupportPortion(oos, simpleEllipse);
            return simpleEllipse;
        }
        else if (et == EncodeType.SIMPLE_MAP_CIRCLE_GEOMETRY_SUPPORT)
        {
            SimpleMapCircleGeometrySupport simpleCircle = new SimpleMapCircleGeometrySupport();
            simpleCircle.setRadius(oos.readFloat());
            decodeMapLocationGeometrySupport(oos, simpleCircle);
            decodeSimpleMapGeometrySupportPortion(oos, simpleCircle);
            return simpleCircle;
        }
        else if (et == EncodeType.SIMPLE_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT)
        {
            SimpleMapLineOfBearingGeometrySupport simpleLob = new SimpleMapLineOfBearingGeometrySupport();
            simpleLob.setLength(oos.readFloat());
            simpleLob.setOrientation(oos.readFloat());
            decodeMapLocationGeometrySupport(oos, simpleLob);
            decodeSimpleMapGeometrySupportPortion(oos, simpleLob);
            return simpleLob;
        }
        else if (et == EncodeType.SIMPLE_MAP_POLYGON_GEOMETRY_SUPPORT)
        {
            SimpleMapPolygonGeometrySupport poly = new SimpleMapPolygonGeometrySupport();
            poly.setFilled(oos.readBoolean());
            poly.setLineDrawn(oos.readBoolean());
            EncodeType et2 = EncodeType.decode(oos);
            if (!et2.isNullType())
            {
                poly.setFillColor(decodeColor(oos));
            }
            decodeMapPathGeometrySupport(oos, poly);
            decodeSimpleMapGeometrySupportPortion(oos, poly);
            return poly;
        }
        else if (et == EncodeType.SIMPLE_MAP_POLYLINE_GEOMETRY_SUPPORT)
        {
            SimpleMapPolylineGeometrySupport poly = new SimpleMapPolylineGeometrySupport();
            decodeMapPathGeometrySupport(oos, poly);
            decodeSimpleMapGeometrySupportPortion(oos, poly);
            return poly;
        }
        return null;
    }

    /**
     * Decode simple geometry support portion.
     *
     * @param oos the oos
     * @param support the support
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void decodeSimpleMapGeometrySupportPortion(ObjectInputStream oos, AbstractSimpleGeometrySupport support)
        throws IOException
    {
        EncodeType et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            support.setTimeSpan(decodeTimeSpan(oos, et));
        }

        et = EncodeType.decode(oos);
        if (!et.isNullType())
        {
            support.setColor(decodeColor(oos), null);
        }

        support.setFollowTerrain(oos.readBoolean(), null);
    }

    /**
     * Instantiates a new disk encode helper.
     */
    private DiskDecodeHelper()
    {
    }
}
