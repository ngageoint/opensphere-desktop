package io.opensphere.core.common.feature.model;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Wrapper for a {@link Geometry} object. This is mostly a hack to help mark
 * serialized {@link Geometry} objects in a {@link Feature}.
 */
class GeometryWrapper implements Serializable
{

    /**
     * For serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * The wrapper {@link Geometry} object.
     */
    private Geometry geometry;

    public GeometryWrapper(Geometry geometry)
    {
        this.geometry = geometry;
    }

    public GeometryWrapper()
    {
        geometry = null;
    }

    public Geometry getGeometry()
    {
        return geometry;
    }

    public void setGeometry(Geometry geometry)
    {
        this.geometry = geometry;
    }

    /**
     * Overrides the default serialization of this object,
     *
     * @param out The {@link java.io.ObjectOutputStream} to write to.
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        // Serialize the Geometry to a String and write that to the stream
        // instead.
        String serializedGeometryStr = GeometryHelper.serializeGeometryToStr(geometry);
        out.writeObject(serializedGeometryStr);
    }

    /**
     * Overrides the default deserialization of this object.
     *
     * @param in The {@link java.io.ObjectInputStream} to read from.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        // Read the String containing the serialized Geometry from the stream
        // and deserialize it
        String serializedGeometryStr = (String)in.readObject();
        geometry = GeometryHelper.deserializeGeometry(serializedGeometryStr);
    }

    @SuppressWarnings("unused")
    private void readObjectNoData() throws ObjectStreamException
    {
        new GeometryWrapper();
    }

}
