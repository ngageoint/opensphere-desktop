package io.opensphere.mantle.data.impl.specialkey;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.core.units.length.StatuteMiles;
import io.opensphere.mantle.data.MetaDataInfo;

/**
 * The Class EllipseSemiMajorAxisKey.
 */
public class EllipseSemiMajorAxisKey extends AbstractSpecialKey
{
    /** The Default EllipseSemiMajorAxis. */
    public static final EllipseSemiMajorAxisKey DEFAULT = new EllipseSemiMajorAxisKey();

    /** The Constant ELLIPS_SEMI_MAJOR_AXIS_KEY_NAME. */
    public static final String ELLIPS_SEMI_MAJOR_AXIS_KEY_NAME = "EllipseSemiMajorAxis";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new EllipseSemiMajorAxisKey, defaulted to nautical miles
     * unit.
     */
    public EllipseSemiMajorAxisKey()
    {
        this(NauticalMiles.class);
    }

    /**
     * Instantiates a new EllipseSemiMajorAxisKey.
     *
     * @param semiMajorUnit the semi major unit
     */
    public EllipseSemiMajorAxisKey(Class<? extends Length> semiMajorUnit)
    {
        super(ELLIPS_SEMI_MAJOR_AXIS_KEY_NAME, semiMajorUnit);
    }

    /**
     * Gets the length units.
     *
     * @return the semi major unit
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Length> getSemiMajorUnit()
    {
        return (Class<? extends Length>)getKeyUnit();
    }

    /**
     * Inspects the supplied column name, to determine if it represents a semi-major axis. If the column name represents a
     * semi-major axis, the metadata is updated to reflect this.
     *
     * @param metaData the meta data to update if the supplied column represents a semi-major axis.
     * @param columnName the name of the column to inspect
     * @return whether the column was detected
     */
    public static boolean detectSemiMajor(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        if (!metaData.hasTypeForSpecialKey(EllipseSemiMajorAxisKey.DEFAULT)) // TODO
        {
            Class<? extends Length> unit = detectUnit(columnName);
            EllipseSemiMajorAxisKey ellipseKey = unit != null ? new EllipseSemiMajorAxisKey(unit)
                    : EllipseSemiMajorAxisKey.DEFAULT;
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
        Class<? extends Length> lengthClass = null;
        if (StringUtils.containsIgnoreCase(columnName, "km)") || StringUtils.containsIgnoreCase(columnName, "kilometers)"))
        {
            lengthClass = Kilometers.class;
        }
        else if (StringUtils.containsIgnoreCase(columnName, "nm)") || StringUtils.containsIgnoreCase(columnName, "nmi)")
                || StringUtils.containsIgnoreCase(columnName, "nautical miles)"))
        {
            lengthClass = NauticalMiles.class;
        }
        else if (StringUtils.containsIgnoreCase(columnName, "mi)") || StringUtils.containsIgnoreCase(columnName, "miles)"))
        {
            lengthClass = StatuteMiles.class;
        }
        else if (StringUtils.containsIgnoreCase(columnName, "m)") || StringUtils.containsIgnoreCase(columnName, "meters)"))
        {
            lengthClass = Meters.class;
        }
        return lengthClass;
    }
}
