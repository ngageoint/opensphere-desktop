package io.opensphere.core.cache;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Helper for working with JTS.
 */
public class JTSHelper
{
    /** The well-known-binary reader for JTS geometries. */
    private final WKBReader myWKBReader;

    /** The well-known-text writer for JTS geometries. */
    private final WKTWriter myWKTWriter;

    /**
     * Constructor.
     */
    public JTSHelper()
    {
        GeometryFactory factory = new GeometryFactory();
        myWKBReader = new WKBReader(factory);

        myWKTWriter = new WKTWriter();
    }

    /**
     * Read a JTS geometry from well-known-bytes.
     *
     * @param bytes The binary data.
     * @return The geometry.
     * @throws CacheException If the geometry cannot be read.
     */
    public Geometry readGeometry(byte[] bytes) throws CacheException
    {
        try
        {
            return myWKBReader.read(bytes);
        }
        catch (ParseException e)
        {
            throw new CacheException("Binary geometry representation was corrupt: " + e, e);
        }
    }

    /**
     * Write a JTS geometry to well-known-text.
     *
     * @param geom The geometry.
     * @return The well-known-text string.
     */
    public String writeGeometry(Geometry geom)
    {
        return myWKTWriter.write(geom);
    }
}
