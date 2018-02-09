package io.opensphere.mantle.data.impl.specialkey;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialColumnDetector;
import io.opensphere.mantle.data.SpecialKey;

/**
 * A {@link SpecialKey} for speed error.
 */
public class SpeedErrorKey extends AbstractSpecialKey implements SpecialColumnDetector
{
    /** The default SpeedErrorKey. */
    public static final SpeedErrorKey DEFAULT = new SpeedErrorKey();

    /** The name of this key. */
    private static final String NAME = "Speed Error";

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new speed error key with the METERS_PER_SECOND unit.
     */
    public SpeedErrorKey()
    {
        super(NAME, SpeedUnit.METERS_PER_SECOND);
    }

    /**
     * Instantiates a new speed error key.
     *
     * @param unit the orientation unit
     */
    public SpeedErrorKey(SpeedUnit unit)
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
        if (!metaData.hasTypeForSpecialKey(SpeedErrorKey.DEFAULT))
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
        if (SpeedKey.isSpeed(columnName) && StringUtils.containsIgnoreCase(columnName, "error"))
        {
            SpeedUnit unit = SpeedUnit.detectUnit(columnName);
            specialKey = unit != null ? new SpeedErrorKey(unit) : SpeedErrorKey.DEFAULT;
        }
        return specialKey;
    }
}
