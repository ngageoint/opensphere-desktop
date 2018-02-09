package io.opensphere.mantle.data.impl.specialkey;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialColumnDetector;
import io.opensphere.mantle.data.SpecialKey;

/**
 * A {@link SpecialKey} for speed.
 */
public class SpeedKey extends AbstractSpecialKey implements SpecialColumnDetector
{
    /** The default SpeedKey. */
    public static final SpeedKey DEFAULT = new SpeedKey();

    /** The name of this key. */
    private static final String NAME = "Speed";

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new speed key with the METERS_PER_SECOND unit.
     */
    public SpeedKey()
    {
        super(NAME, SpeedUnit.METERS_PER_SECOND);
    }

    /**
     * Instantiates a new speed key.
     *
     * @param unit the orientation unit
     */
    public SpeedKey(SpeedUnit unit)
    {
        super(NAME, unit);
    }

    @Override
    public SpeedUnit getKeyUnit()
    {
        return (SpeedUnit)super.getKeyUnit();
    }

    @Override
    public boolean markSpecialColumn(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        if (!metaData.hasTypeForSpecialKey(SpeedKey.DEFAULT))
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
        if (isSpeed(columnName) && !StringUtils.containsIgnoreCase(columnName, "error"))
        {
            SpeedUnit unit = SpeedUnit.detectUnit(columnName);
            specialKey = unit != null ? new SpeedKey(unit) : SpeedKey.DEFAULT;
        }
        return specialKey;
    }

    /**
     * Inspects the supplied column name, to determine if it represents a heading.
     *
     * @param columnName the name of the column to inspect
     * @return whether the column is determined to be a heading column
     */
    public static boolean isSpeed(String columnName)
    {
        return StringUtils.containsIgnoreCase(columnName, "speed");
    }
}
