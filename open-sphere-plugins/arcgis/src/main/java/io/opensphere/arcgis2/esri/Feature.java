package io.opensphere.arcgis2.esri;

import java.util.Map;

/** A feature. */
public class Feature
{
    /** The geometry. */
    private Geometry myGeometry;

    /** The attributes. */
    private Map<String, Object> myAttributes;

    /**
     * Gets the geometry.
     *
     * @return the geometry
     */
    public Geometry getGeometry()
    {
        return myGeometry;
    }

    /**
     * Sets the geometry.
     *
     * @param geometry the geometry
     */
    public void setGeometry(Geometry geometry)
    {
        myGeometry = geometry;
    }

    /**
     * Gets the attributes.
     *
     * @return the attributes
     */
    public Map<String, Object> getAttributes()
    {
        return myAttributes;
    }

    /**
     * Sets the attributes.
     *
     * @param attributes the attributes
     */
    public void setAttributes(Map<String, Object> attributes)
    {
        myAttributes = attributes;
    }

    @Override
    public String toString()
    {
        return "Feature [myGeometry=" + myGeometry + ", myAttributes=" + myAttributes + "]";
    }
}
