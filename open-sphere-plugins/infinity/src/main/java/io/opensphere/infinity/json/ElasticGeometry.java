package io.opensphere.infinity.json;

/** Elasticsearch geometry JSON bean. */
public class ElasticGeometry extends Any
{
    /** Ignore unmapped. */
    private final boolean myIgnoreUnmapped = true;

    /** Constructor. */
    public ElasticGeometry()
    {
    }

    /**
     * Constructor.
     *
     * @param propertyName The property name
     * @param propertyValue The property value
     */
    public ElasticGeometry(String propertyName, Object propertyValue)
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
