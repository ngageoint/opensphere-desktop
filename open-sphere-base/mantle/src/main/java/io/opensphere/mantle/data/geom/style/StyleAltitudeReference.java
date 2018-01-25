package io.opensphere.mantle.data.geom.style;

import io.opensphere.core.model.Altitude;

/**
 * The Enum StyleAltitudeReference.
 */
public enum StyleAltitudeReference
{
    /** Altitude is provided by the data source. */
    AUTOMATIC(null, "Automatic: Provided by source data"),

    /** Altitude is relative to the configured ellipsoid. */
    ELLIPSOID(Altitude.ReferenceLevel.ELLIPSOID, "Ellipsoid: Relative to WGS84 ellipsoid"),

    /** Altitude is relative to the center of the model. */
    ORIGIN(Altitude.ReferenceLevel.ORIGIN, "Origin: Relative to the center of the earth"),

    /** Altitude is relative to local elevation. */
    TERRAIN(Altitude.ReferenceLevel.TERRAIN, "Terrain: Relative to local elevation");

    /** The label. */
    private final String myLabel;

    /** The Reference level. */
    private Altitude.ReferenceLevel myReferenceLevel;

    /**
     * Instantiates a new reference level.
     *
     * @param ref the ref
     * @param label the label
     */
    StyleAltitudeReference(Altitude.ReferenceLevel ref, String label)
    {
        myLabel = label;
        myReferenceLevel = ref;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Gets the reference.
     *
     * @return the reference
     */
    public Altitude.ReferenceLevel getReference()
    {
        return myReferenceLevel;
    }

    /**
     * Checks if is automatic.
     *
     * @return true, if is automatic
     */
    public boolean isAutomatic()
    {
        return AUTOMATIC.equals(this);
    }

    @Override
    public String toString()
    {
        return myLabel;
    }
}
