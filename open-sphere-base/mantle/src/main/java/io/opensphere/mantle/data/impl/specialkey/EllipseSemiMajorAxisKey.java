package io.opensphere.mantle.data.impl.specialkey;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.core.units.length.StatuteMiles;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialColumnDetector;
import io.opensphere.mantle.data.SpecialKey;

/**
 * The Class EllipseSemiMajorAxisKey.
 */
public class EllipseSemiMajorAxisKey extends AbstractSpecialKey implements SpecialColumnDetector
{
    /** The Default EllipseSemiMajorAxis. */
    public static final EllipseSemiMajorAxisKey DEFAULT = new EllipseSemiMajorAxisKey();

    /** The pattern. */
    private static final Pattern COLUMN_PATTERN = Pattern.compile(".*(semi.?major|smj_nm|smaj?).*", Pattern.CASE_INSENSITIVE);

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new EllipseSemiMajorAxisKey, defaulted to nautical miles unit.
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
        super("EllipseSemiMajorAxis", semiMajorUnit);
    }

    @Override
    public boolean markSpecialColumn(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        if (!metaData.hasTypeForSpecialKey(EllipseSemiMajorAxisKey.DEFAULT))
        {
            SpecialKey specialKey = detectColumn(columnName);
            if (specialKey != null)
            {
                metaData.setSpecialKey(columnName, specialKey, metaData);
                wasDetected = true;
            }
        }
        return wasDetected;
    }

    @Override
    public SpecialKey detectColumn(String columnName)
    {
        SpecialKey specialKey = null;
        if (isSemiMajor(columnName))
        {
            Class<? extends Length> unit = detectUnit(columnName);
            specialKey = unit != null ? new EllipseSemiMajorAxisKey(unit) : EllipseSemiMajorAxisKey.DEFAULT;
        }
        return specialKey;
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

    /**
     * Inspects the supplied column name, to determine if it represents a semi-major axis.
     *
     * @param columnName the name of the column to inspect
     * @return whether the column is determined to be a semi-major axis column
     */
    static boolean isSemiMajor(String columnName)
    {
        return COLUMN_PATTERN.matcher(columnName).matches();
    }
}
