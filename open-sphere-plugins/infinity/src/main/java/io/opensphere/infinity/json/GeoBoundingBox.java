package io.opensphere.infinity.json;

/** Elasticsearch geo bounding box JSON bean. */
public class GeoBoundingBox extends Any
{
    /** Ignore unmapped. */
    private final boolean myIgnoreUnmapped = true;

    /** Constructor. */
    public GeoBoundingBox()
    {
    }

    /**
     * Constructor.
     *
     * @param propertyName The property name
     * @param propertyValue The property value
     */
    public GeoBoundingBox(String propertyName, Object propertyValue)
    {
        super(propertyName, propertyValue);
    }

    /**
     * Gets the ignoreUnmapped.
     *
     * @return the ignoreUnmapped
     */
    public boolean isIgnore_unmapped()
    {
        return myIgnoreUnmapped;
    }
}
