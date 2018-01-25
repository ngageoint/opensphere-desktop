package io.opensphere.core.common.feature.model;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

/**
 * Helper methods for working with {@link Geometry} objects.
 */
public class GeometryHelper
{
    /**
     * Convenience method for serializing a {@link Geometry} to a String using
     * the Well Known Binary (WKB) format
     *
     * @param geometry The Geometry to be serialized
     * @return The Geometry provided serialized to a String using WKB
     */
    public static String serializeGeometryToStr(Geometry geometry)
    {
        WKBWriter writer = new WKBWriter();
        byte[] serializedGeometry = writer.write(geometry);
        String serializedGeometryStr = WKBWriter.toHex(serializedGeometry);
        return serializedGeometryStr;
    }

    /**
     * Convenience method for deserializing a {@link Geometry} from a String
     * using the Well Known Binary (WKB) format
     *
     * @param serializedGeometryStr Serialized {@link Geometry} object.
     * @return Deserialized {@link Geometry} object,
     */
    public static Geometry deserializeGeometry(String serializedGeometryStr)
    {
        Geometry geometry = null;
        WKBReader wkbReader = new WKBReader();
        try
        {
            byte[] serializedGeometry = WKBReader.hexToBytes(serializedGeometryStr);
            geometry = wkbReader.read(serializedGeometry);
        }
        catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return geometry;
    }
}
