package io.opensphere.mantle.data.impl.specialkey;

import java.util.regex.Pattern;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialColumnDetector;
import io.opensphere.mantle.data.SpecialKey;

/**
 * The Class EllipseSemiMinorAxisKey.
 */
public class EllipseSemiMinorAxisKey extends AbstractSpecialKey implements SpecialColumnDetector
{
    /** The Default EllipseSemiMinorAxisKey. */
    public static final EllipseSemiMinorAxisKey DEFAULT = new EllipseSemiMinorAxisKey();

    /** The pattern. */
    private static final Pattern COLUMN_PATTERN = Pattern.compile(".*(semi.?minor|smi_nm|smin?).*", Pattern.CASE_INSENSITIVE);

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new EllipseSemiMinorAxisKey key. Defaulted to nautical miles unit.
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
        super("EllipseSemiMinorAxis", semiMinorUnit);
    }

    @Override
    public boolean markSpecialColumn(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        if (!metaData.hasTypeForSpecialKey(EllipseSemiMinorAxisKey.DEFAULT))
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
        if (isSemiMinor(columnName))
        {
            Class<? extends Length> unit = detectUnit(columnName);
            specialKey = unit != null ? new EllipseSemiMinorAxisKey(unit) : EllipseSemiMinorAxisKey.DEFAULT;
        }
        return specialKey;
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
     * Attempts to detect the length unit from the column name.
     *
     * @param columnName the name of the column to inspect
     * @return the detected length unit, or null if it couldn't be detected
     */
    public static Class<? extends Length> detectUnit(String columnName)
    {
        return EllipseSemiMajorAxisKey.detectUnit(columnName);
    }

    /**
     * Inspects the supplied column name, to determine if it represents a semi-minor axis.
     *
     * @param columnName the name of the column to inspect
     * @return whether the column is determined to be a semi-minor axis column
     */
    static boolean isSemiMinor(String columnName)
    {
        return COLUMN_PATTERN.matcher(columnName).matches();
    }
}
