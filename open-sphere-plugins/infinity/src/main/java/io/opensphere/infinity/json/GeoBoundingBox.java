package io.opensphere.infinity.json;

public class GeoBoundingBox extends Any
{
    private final boolean myIgnoreUnmapped = true;

    public GeoBoundingBox()
    {
    }

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
