package io.opensphere.mantle.data.impl.encoder;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.lang.ByteString;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.geom.impl.AbstractDefaultMapGeometrySupport;
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
import io.opensphere.mantle.util.dynenum.util.DynamicEnumerationLongKeyUtility;

/** The Class DiskEncodeHelper. */
@SuppressWarnings({ "PMD.AvoidUsingShortType", "PMD.GodClass" })
public final class DiskEncodeHelper
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(DiskEncodeHelper.class);

    /**
     * Encode byte.
     *
     * @param oos the oos
     * @param o the o
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeByte(ObjectOutputStream oos, Byte o) throws IOException
    {
        EncodeType.BYTE_OBJ.encode(oos);
        oos.writeByte(o.byteValue());
    }

    /**
     * Encode byte string.
     *
     * @param oos the oos
     * @param val the val
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeByteString(ObjectOutputStream oos, ByteString val) throws IOException
    {
        EncodeType.BYTE_STRING.encode(oos);
        String str = val.toString();
        byte[] strB = str.getBytes(StringUtilities.DEFAULT_CHARSET);
        oos.writeShort(strB.length);
        oos.write(strB);
    }

    /**
     * Encode byte.
     *
     * @param oos the oos
     * @param val the val
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeByteValue(ObjectOutputStream oos, byte val) throws IOException
    {
        EncodeType.BYTE_VALUE.encode(oos);
        oos.writeByte(val);
    }

    /**
     * Encode.
     *
     * @param oos the oos
     * @param val the val
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeColor(ObjectOutputStream oos, Color val) throws IOException
    {
        EncodeType.COLOR.encode(oos);
        oos.writeInt(val.getRGB());
    }

    /**
     * Encode.
     *
     * @param oos the oos
     * @param val the val
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeDate(ObjectOutputStream oos, Date val) throws IOException
    {
        EncodeType.DATE.encode(oos);
        oos.writeLong(val.getTime());
    }

    /**
     * Encode double.
     *
     * @param oos the oos
     * @param o the o
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeDouble(ObjectOutputStream oos, Double o) throws IOException
    {
        EncodeType.DOUBLE_OBJ.encode(oos);
        oos.writeDouble(o.doubleValue());
    }

    /**
     * Encode double.
     *
     * @param oos the oos
     * @param val the val
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeDoubleValue(ObjectOutputStream oos, double val) throws IOException
    {
        EncodeType.DOUBLE_VALUE.encode(oos);
        oos.writeDouble(val);
    }

    /**
     * Encode dynamic enum key.
     *
     * @param oos the oos
     * @param val the val
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeDynamicEnumKey(ObjectOutputStream oos, DynamicEnumerationKey val) throws IOException
    {
        if (val instanceof DynamicEnumerationCombinedIntKey)
        {
            EncodeType.DYNAMIC_ENUM_INT_KEY.encode(oos);
            oos.writeInt(((DynamicEnumerationCombinedIntKey)val).getCompositeKey());
        }
        else if (val instanceof DynamicEnumerationCombinedLongKey)
        {
            EncodeType.DYNAMIC_ENUM_LONG_KEY.encode(oos);
            oos.writeLong(((DynamicEnumerationCombinedLongKey)val).getCompositeKey());
        }
        else
        {
            long combinedKey = DynamicEnumerationLongKeyUtility.createCombinedLongKeyValue(val.getTypeId(),
                    val.getMetaDataKeyId(), val.getValueId());
            EncodeType.DYNAMIC_ENUM_LONG_KEY.encode(oos);
            oos.writeLong(combinedKey);
        }
    }

    /**
     * Encode float.
     *
     * @param oos the oos
     * @param o the o
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeFloat(ObjectOutputStream oos, Float o) throws IOException
    {
        EncodeType.FLOAT_OBJ.encode(oos);
        oos.writeFloat(o.floatValue());
    }

    /**
     * Encode float.
     *
     * @param oos the oos
     * @param val the val
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeFloatValue(ObjectOutputStream oos, float val) throws IOException
    {
        EncodeType.FLOAT_VALUE.encode(oos);
        oos.writeFloat(val);
    }

    /**
     * Encode integer.
     *
     * @param oos the oos
     * @param o the o
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeInteger(ObjectOutputStream oos, Integer o) throws IOException
    {
        EncodeType.INTEGER_OBJ.encode(oos);
        oos.writeInt(o.intValue());
    }

    /**
     * Encode int.
     *
     * @param oos the oos
     * @param val the value
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeIntValue(ObjectOutputStream oos, int val) throws IOException
    {
        EncodeType.INT_VALUE.encode(oos);
        oos.writeInt(val);
    }

    /**
     * Encode lat lon alt.
     *
     * @param oos the oos
     * @param lla the lla
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeLatLonAlt(ObjectOutputStream oos, LatLonAlt lla) throws IOException
    {
        EncodeType.LAT_LON_ALT.encode(oos);
        oos.writeDouble(lla.getLatD());
        oos.writeDouble(lla.getLonD());
        oos.writeDouble(lla.getAltM());
        oos.writeInt(lla.getAltitudeReference().ordinal());
    }

    /**
     * Encode long.
     *
     * @param oos the oos
     * @param o the o
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeLong(ObjectOutputStream oos, Long o) throws IOException
    {
        EncodeType.LONG_OBJ.encode(oos);
        oos.writeLong(o.longValue());
    }

    /**
     * Encode long.
     *
     * @param oos the oos
     * @param val the long
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeLongValue(ObjectOutputStream oos, long val) throws IOException
    {
        EncodeType.LONG_VALUE.encode(oos);
        oos.writeLong(val);
    }

    /**
     * Encode map geometry support as a stand-alone item.
     *
     * @param oos the oos
     * @param support the {@link MapGeometrySupport}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeMapGeometrySupport(ObjectOutputStream oos, MapGeometrySupport support) throws IOException
    {
        if (support == null)
        {
            encodeNull(oos);
        }
        else
        {
            encodeMapGeometrySupportInternal(oos, support);
        }
    }

    /**
     * Encode meta data list as a stand-alone item.
     *
     * @param oos the oos
     * @param aList the a list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeMetaDataList(ObjectOutputStream oos, List<Object> aList) throws IOException
    {
        if (aList == null)
        {
            encodeNull(oos);
        }
        else
        {
            encodeMetaDataListInternal(oos, aList);
        }
    }

    /**
     * Meta data list encode.
     *
     * @param oos the oos
     * @param aList the a list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeMetaDataListInternal(ObjectOutputStream oos, List<Object> aList) throws IOException
    {
        EncodeType.LIST.encode(oos);
        oos.writeInt(aList.size());
        for (Object o : aList)
        {
            encodeMetaDataValue(oos, o);
        }
    }

    /**
     * Meta data value encode.
     *
     * @param oos the oos
     * @param o the o
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeMetaDataValue(ObjectOutputStream oos, Object o) throws IOException
    {
        if (o == null)
        {
            encodeNull(oos);
        }
        else if (o instanceof String)
        {
            encodeString(oos, (String)o);
        }
        else if (o instanceof ByteString)
        {
            encodeByteString(oos, (ByteString)o);
        }
        else if (o instanceof Number)
        {
            encodeNumber(oos, (Number)o);
        }
        else if (o instanceof Date)
        {
            encodeDate(oos, (Date)o);
        }
        else if (o instanceof TimeSpan)
        {
            encodeTimeSpan(oos, (TimeSpan)o);
        }
        else if (o instanceof DynamicEnumerationKey)
        {
            encodeDynamicEnumKey(oos, (DynamicEnumerationKey)o);
        }
        else
        {
            encodeObject(oos, o);
        }
    }

    /**
     * Encode null.
     *
     * @param oos the oos
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeNull(ObjectOutputStream oos) throws IOException
    {
        EncodeType.NULL.encode(oos);
    }

    /**
     * Encode number.
     *
     * @param oos the oos
     * @param o the o
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeNumber(ObjectOutputStream oos, Number o) throws IOException
    {
        if (o instanceof Double)
        {
            encodeDouble(oos, (Double)o);
        }
        else if (o instanceof Float)
        {
            encodeFloat(oos, (Float)o);
        }
        else if (o instanceof Integer)
        {
            encodeInteger(oos, (Integer)o);
        }
        else if (o instanceof Long)
        {
            encodeLong(oos, (Long)o);
        }
        else if (o instanceof Byte)
        {
            encodeByte(oos, (Byte)o);
        }
        else
        {
            EncodeType.OBJECT.encode(oos);
            oos.writeObject(o);
        }
    }

    /**
     * Encode object.
     *
     * @param oos the oos
     * @param o the o
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeObject(ObjectOutputStream oos, Object o) throws IOException
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Encoding Object: " + o.getClass().getSimpleName());
        }
        EncodeType.OBJECT.encode(oos);
        oos.writeObject(o);
    }

    /**
     * Encode origin id as a stand alone item.
     *
     * @param oos the oos
     * @param originId the origin id
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeOriginId(ObjectOutputStream oos, Long originId) throws IOException
    {
        if (originId == null)
        {
            encodeNull(oos);
        }
        else
        {
            EncodeType.LONG_OBJ.encode(oos);
            oos.writeLong(originId.longValue());
        }
    }

    /**
     * Encode string.
     *
     * @param oos the oos
     * @param val the val
     * @return the int number of bytes written.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static int encodeString(ObjectOutputStream oos, String val) throws IOException
    {
        EncodeType.STRING.encode(oos);
        byte[] strB = val.getBytes(StringUtilities.DEFAULT_CHARSET);
        oos.writeShort(strB.length);
        oos.write(strB);
        return 3 + strB.length;
    }

    /**
     * Encode time span.
     *
     * @param oos the oos
     * @param ts the ts
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void encodeTimeSpan(ObjectOutputStream oos, TimeSpan ts) throws IOException
    {
        if (ts.isTimeless())
        {
            EncodeType.TIMESPAN_TIMELESS.encode(oos);
        }
        else if (ts.isInstantaneous())
        {
            EncodeType.TIMESPAN_INSTANT.encode(oos);
            oos.writeLong(ts.getStart());
        }
        else if (ts.isUnboundedStart())
        {
            EncodeType.TIMESPAN_UNBOUNDED_START.encode(oos);
            oos.writeLong(ts.getEnd());
        }
        else if (ts.isUnboundedEnd())
        {
            EncodeType.TIMESPAN_UNBOUNDED_END.encode(oos);
            oos.writeLong(ts.getStart());
        }
        else
        {
            EncodeType.TIMESPAN_SPAN.encode(oos);
            oos.writeLong(ts.getStart());
            oos.writeLong(ts.getEnd());
        }
    }

    /**
     * Encode default map geometry support internal.
     *
     * @param oos the oos
     * @param support the {@link MapGeometrySupport}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void encodeDefaultMapGeometrySupportInternal(ObjectOutputStream oos, MapGeometrySupport support)
        throws IOException
    {
        if (support instanceof DefaultMapPointGeometrySupport)
        {
            EncodeType.DEFAULT_MAP_POINT_GEOMETRY_SUPPORT.encode(oos);
            encodeMapLocationGeometrySupport(oos, (DefaultMapPointGeometrySupport)support);
            encodeMapGeometrySupportPortion(oos, support);
        }
        else if (support instanceof DefaultMapEllipseGeometrySupport)
        {
            EncodeType.DEFAULT_MAP_ELLIPSE_GEOMETRY_SUPPORT.encode(oos);
            DefaultMapEllipseGeometrySupport ellipse = (DefaultMapEllipseGeometrySupport)support;
            oos.writeFloat(ellipse.getSemiMajorAxis());
            oos.writeFloat(ellipse.getSemiMinorAxis());
            oos.writeFloat(ellipse.getOrientation());
            encodeMapLocationGeometrySupport(oos, (DefaultMapEllipseGeometrySupport)support);
            encodeMapGeometrySupportPortion(oos, support);
        }
        else if (support instanceof DefaultMapCircleGeometrySupport)
        {
            EncodeType.DEFAULT_MAP_ELLIPSE_GEOMETRY_SUPPORT.encode(oos);
            DefaultMapCircleGeometrySupport circle = (DefaultMapCircleGeometrySupport)support;
            oos.writeFloat(circle.getRadius());
            encodeMapLocationGeometrySupport(oos, (DefaultMapCircleGeometrySupport)support);
            encodeMapGeometrySupportPortion(oos, support);
        }
        else if (support instanceof DefaultMapLineOfBearingGeometrySupport)
        {
            EncodeType.DEFAULT_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT.encode(oos);
            DefaultMapLineOfBearingGeometrySupport lineOfBearing = (DefaultMapLineOfBearingGeometrySupport)support;
            oos.writeFloat(lineOfBearing.getLength());
            oos.writeFloat(lineOfBearing.getOrientation());
            encodeMapLocationGeometrySupport(oos, (DefaultMapPointGeometrySupport)support);
            encodeMapGeometrySupportPortion(oos, support);
        }
        else if (support instanceof DefaultMapPolygonGeometrySupport)
        {
            EncodeType.DEFAULT_MAP_POLYGON_GEOMETRY_SUPPORT.encode(oos);
            DefaultMapPolygonGeometrySupport poly = (DefaultMapPolygonGeometrySupport)support;
            oos.writeBoolean(poly.isFilled());
            oos.writeBoolean(poly.isLineDrawn());
            encodeMapPathGeometrySupport(oos, poly);
            encodeMapGeometrySupportPortion(oos, support);
        }
        else if (support instanceof DefaultMapPolylineGeometrySupport)
        {
            EncodeType.DEFAULT_MAP_POLYLINE_GEOMETRY_SUPPORT.encode(oos);
            DefaultMapPolylineGeometrySupport poly = (DefaultMapPolylineGeometrySupport)support;
            encodeMapPathGeometrySupport(oos, poly);
            encodeMapGeometrySupportPortion(oos, support);
        }
        else if (support instanceof DefaultMapIconGeometrySupport)
        {
            EncodeType.DEFAULT_MAP_ICON_GEOMETRY_SUPPORT.encode(oos);
            DefaultMapIconGeometrySupport dmigs = (DefaultMapIconGeometrySupport)support;
            encodeMapLocationGeometrySupport(oos, dmigs);
            encodeString(oos, dmigs.getIconURL());
            encodeFloatValue(oos, dmigs.getIconSize());
            encodeMapGeometrySupportPortion(oos, support);
        }
        else
        {
            EncodeType.MAP_GEOMETRY_TYPE_UNKNOWN.encode(oos);
            encodeObject(oos, support);
        }
    }

    /**
     * Encode map geometry support.
     *
     * @param oos the oos
     * @param support the {@link MapGeometrySupport}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void encodeMapGeometrySupportInternal(ObjectOutputStream oos, MapGeometrySupport support) throws IOException
    {
        EncodeType.MAP_GEOMETRY_SUPPORT.encode(oos);

        if (support instanceof AbstractDefaultMapGeometrySupport)
        {
            encodeDefaultMapGeometrySupportInternal(oos, support);
        }
        else if (support instanceof AbstractSimpleGeometrySupport)
        {
            encodeSimpleMapGeometrySupportInternal(oos, support);
        }
        else
        {
            EncodeType.MAP_GEOMETRY_TYPE_UNKNOWN.encode(oos);
            encodeObject(oos, support);
        }
    }

    /**
     * Encode MapGeometrySupport portion.
     *
     * @param oos the oos
     * @param support the {@link MapGeometrySupport}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void encodeMapGeometrySupportPortion(ObjectOutputStream oos, MapGeometrySupport support) throws IOException
    {
        if (support.getTimeSpan() == null)
        {
            encodeNull(oos);
        }
        else
        {
            encodeTimeSpan(oos, support.getTimeSpan());
        }

        if (support.getColor() == null)
        {
            encodeNull(oos);
        }
        else
        {
            encodeColor(oos, support.getColor());
        }

        if (support.getCallOutSupport() == null)
        {
            encodeNull(oos);
        }
        else
        {
            encodeObject(oos, support.getCallOutSupport());
        }

        if (support.getToolTip() == null)
        {
            encodeNull(oos);
        }
        else
        {
            encodeString(oos, support.getToolTip());
        }

        oos.writeBoolean(support.followTerrain());

        if (support.getChildren() == null)
        {
            encodeNull(oos);
        }
        else
        {
            EncodeType.LIST.encode(oos);
            oos.writeInt(support.getChildren().size());
            for (MapGeometrySupport aChild : support.getChildren())
            {
                if (aChild == null)
                {
                    encodeNull(oos);
                }
                else
                {
                    encodeMapGeometrySupportInternal(oos, aChild);
                }
            }
        }
    }

    /**
     * Encode map location geometry support.
     *
     * @param oos the oos
     * @param mlgs the mlgs
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void encodeMapLocationGeometrySupport(ObjectOutputStream oos, MapLocationGeometrySupport mlgs)
        throws IOException
    {
        if (mlgs.getLocation() == null)
        {
            encodeNull(oos);
        }
        else
        {
            encodeLatLonAlt(oos, mlgs.getLocation());
        }
    }

    /**
     * Encode map path geometry support.
     *
     * @param oos the oos
     * @param support the {@link MapPathGeometrySupport}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void encodeMapPathGeometrySupport(ObjectOutputStream oos, MapPathGeometrySupport support) throws IOException
    {
        if (support.getLineType() == null)
        {
            encodeNull(oos);
        }
        else
        {
            encodeIntValue(oos, support.getLineType().ordinal());
        }
        if (support.getLocations() == null)
        {
            encodeNull(oos);
        }
        else
        {
            EncodeType.LIST.encode(oos);
            oos.writeInt(support.getLocations().size());
            for (LatLonAlt lla : support.getLocations())
            {
                if (lla == null)
                {
                    encodeNull(oos);
                }
                else
                {
                    encodeLatLonAlt(oos, lla);
                }
            }
        }
    }

    /**
     * Encode simple map geometry support internal.
     *
     * @param oos the oos
     * @param support the MapGeometrySupport
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void encodeSimpleMapGeometrySupportInternal(ObjectOutputStream oos, MapGeometrySupport support)
        throws IOException
    {
        if (support instanceof SimpleMapPointGeometrySupport)
        {
            EncodeType.SIMPLE_MAP_POINT_GEOMETRY_SUPPORT.encode(oos);
            SimpleMapPointGeometrySupport point = (SimpleMapPointGeometrySupport)support;
            encodeMapLocationGeometrySupport(oos, point);
            encodeSimpleMapGeometrySupportPortion(oos, point);
        }
        else if (support instanceof SimpleMapEllipseGeometrySupport)
        {
            EncodeType.SIMPLE_MAP_ELLIPSE_GEOMETRY_SUPPORT.encode(oos);
            SimpleMapEllipseGeometrySupport ellipse = (SimpleMapEllipseGeometrySupport)support;
            oos.writeFloat(ellipse.getSemiMajorAxis());
            oos.writeFloat(ellipse.getSemiMinorAxis());
            oos.writeFloat(ellipse.getOrientation());
            encodeMapLocationGeometrySupport(oos, ellipse);
            encodeSimpleMapGeometrySupportPortion(oos, ellipse);
        }
        else if (support instanceof SimpleMapCircleGeometrySupport)
        {
            EncodeType.SIMPLE_MAP_ELLIPSE_GEOMETRY_SUPPORT.encode(oos);
            SimpleMapCircleGeometrySupport circle = (SimpleMapCircleGeometrySupport)support;
            oos.writeFloat(circle.getRadius());
            encodeMapLocationGeometrySupport(oos, circle);
            encodeSimpleMapGeometrySupportPortion(oos, circle);
        }
        else if (support instanceof SimpleMapLineOfBearingGeometrySupport)
        {
            EncodeType.SIMPLE_MAP_LINE_OF_BEARING_GEOMETRY_SUPPORT.encode(oos);
            SimpleMapLineOfBearingGeometrySupport lineOfBearing = (SimpleMapLineOfBearingGeometrySupport)support;
            oos.writeFloat(lineOfBearing.getLength());
            oos.writeFloat(lineOfBearing.getOrientation());
            encodeMapLocationGeometrySupport(oos, lineOfBearing);
            encodeSimpleMapGeometrySupportPortion(oos, lineOfBearing);
        }
        else if (support instanceof SimpleMapPolygonGeometrySupport)
        {
            EncodeType.SIMPLE_MAP_POLYGON_GEOMETRY_SUPPORT.encode(oos);
            SimpleMapPolygonGeometrySupport poly = (SimpleMapPolygonGeometrySupport)support;
            oos.writeBoolean(poly.isFilled());
            oos.writeBoolean(poly.isLineDrawn());
            if (poly.getFillColor() == null)
            {
                encodeNull(oos);
            }
            else
            {
                encodeColor(oos, poly.getFillColor());
            }
            encodeMapPathGeometrySupport(oos, poly);
            encodeSimpleMapGeometrySupportPortion(oos, poly);
        }
        else if (support instanceof SimpleMapPolylineGeometrySupport)
        {
            EncodeType.SIMPLE_MAP_POLYLINE_GEOMETRY_SUPPORT.encode(oos);
            SimpleMapPolylineGeometrySupport poly = (SimpleMapPolylineGeometrySupport)support;
            encodeMapPathGeometrySupport(oos, poly);
            encodeSimpleMapGeometrySupportPortion(oos, poly);
        }
        else
        {
            EncodeType.MAP_GEOMETRY_TYPE_UNKNOWN.encode(oos);
            encodeObject(oos, support);
        }
    }

    /**
     * Encode AbstractSimpleGeometrySupport portion.
     *
     * @param oos the oos
     * @param simpleGeomSupport the AbstractSimpleGeometrySupport
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void encodeSimpleMapGeometrySupportPortion(ObjectOutputStream oos,
            AbstractSimpleGeometrySupport simpleGeomSupport) throws IOException
    {
        if (simpleGeomSupport.getTimeSpan() == null)
        {
            encodeNull(oos);
        }
        else
        {
            encodeTimeSpan(oos, simpleGeomSupport.getTimeSpan());
        }

        encodeColor(oos, simpleGeomSupport.getColor());

        oos.writeBoolean(simpleGeomSupport.followTerrain());
    }

    /** Instantiates a new disk encode helper. */
    private DiskEncodeHelper()
    {
    }
}
