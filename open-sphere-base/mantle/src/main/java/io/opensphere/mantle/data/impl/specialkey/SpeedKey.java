package io.opensphere.mantle.data.impl.specialkey;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;

/**
 * A {@link SpecialKey} for speed.
 */
public class SpeedKey extends AbstractSpecialKey
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

    /**
     * Inspects the supplied column name, to determine if it represents a speed. If the column name represents a speed, the
     * metadata is updated to reflect this.
     *
     * @param metaData the meta data to update if the supplied column represents a speed.
     * @param columnName the name of the column to inspect
     * @return whether the column was detected
     */
    public static boolean detectSpeed(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        if (!metaData.hasTypeForSpecialKey(SpeedKey.DEFAULT) && isSpeed(columnName))
        {
            SpeedUnit unit = SpeedUnit.detectUnit(columnName);
            SpeedKey speedKey = unit != null ? new SpeedKey(unit) : SpeedKey.DEFAULT;
            metaData.setSpecialKey(columnName, speedKey, metaData);
            wasDetected = true;
        }
        return wasDetected;
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
