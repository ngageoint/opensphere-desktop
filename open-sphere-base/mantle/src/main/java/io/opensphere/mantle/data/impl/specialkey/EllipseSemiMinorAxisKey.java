package io.opensphere.mantle.data.impl.specialkey;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * The Class EllipseSemiMinorAxisKey.
 */
public class EllipseSemiMinorAxisKey extends AbstractSpecialKey
{
    /** The Default EllipseSemiMinorAxisKey. */
    public static final EllipseSemiMinorAxisKey DEFAULT = new EllipseSemiMinorAxisKey();

    /** The Constant ELLIPS_SEMI_MINOR_AXIS_KEY_NAME. */
    public static final String ELLIPS_SEMI_MINOR_AXIS_KEY_NAME = "EllipseSemiMinorAxis";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new EllipseSemiMinorAxisKey key. Defaulted to nautical
     * miles unit.
     */
    public EllipseSemiMinorAxisKey()
    {
        this(NauticalMiles.class);
    }

    /**
     * Instantiates a new EllipseSemiMinorAxisKey key.
     *
     * @param semiMinorUnit the semi minor unit
     */
    public EllipseSemiMinorAxisKey(Class<? extends Length> semiMinorUnit)
    {
        super(ELLIPS_SEMI_MINOR_AXIS_KEY_NAME, semiMinorUnit);
    }

    /**
     * Gets the length units.
     *
     * @return the semi minor unit
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Length> getAltitudeUnit()
    {
        return (Class<? extends Length>)getKeyUnit();
    }

    /**
     * Inspects the supplied column name, to determine if it represents a semi-minor axis. If the column name represents a
     * semi-minor axis, the metadata is updated to reflect this.
     *
     * @param metaData the meta data to update if the supplied column represents a semi-minor axis.
     * @param columnName the name of the column to inspect
     * @return whether the column was detected
     */
    public static boolean detectSemiMinor(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        if (!metaData.hasTypeForSpecialKey(EllipseSemiMinorAxisKey.DEFAULT)) // TODO
        {
            Class<? extends Length> unit = detectUnit(columnName);
            EllipseSemiMinorAxisKey ellipseKey = unit != null ? new EllipseSemiMinorAxisKey(unit)
                    : EllipseSemiMinorAxisKey.DEFAULT;
            metaData.setSpecialKey(columnName, ellipseKey, metaData);
            wasDetected = true;
        }
        return wasDetected;
    }

    /**
     * Attempts to detect the length unit from the column name.
     *
     * @param columnName the name of the column to inspect
     * @return the detected length unit, or null if it couldn't be detected
     */
    public static Class<? extends Length> detectUnit(String columnName)
    {
        return EllipseSemiMajorAxisKey.detectUnit(columnName);
    }
}
