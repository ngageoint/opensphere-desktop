package io.opensphere.mantle.data.impl.specialkey;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialColumnDetector;
import io.opensphere.mantle.data.SpecialKey;

/**
 * A {@link SpecialKey} for heading / bearing / course error.
 */
public class HeadingErrorKey extends AbstractSpecialKey implements SpecialColumnDetector
{
    /** The default HeadingErrorKey. */
    public static final HeadingErrorKey DEFAULT = new HeadingErrorKey();

    /** The name of this key. */
    private static final String NAME = "Heading Error";

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new heading error key with the DEGREES_CLOCKWISE_FROM_NORTH unit.
     */
    public HeadingErrorKey()
    {
        super(NAME, HeadingKey.HeadingUnit.DEGREES_CLOCKWISE_FROM_NORTH);
    }

    /**
     * Instantiates a new heading error key.
     *
     * @param unit the orientation unit
     */
    public HeadingErrorKey(HeadingKey.HeadingUnit unit)
    {
        super(NAME, unit);
    }

    @Override
    public HeadingKey.HeadingUnit getKeyUnit()
    {
        return (HeadingKey.HeadingUnit)super.getKeyUnit();
    }

    @Override
    public boolean markSpecialColumn(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        if (!metaData.hasTypeForSpecialKey(HeadingErrorKey.DEFAULT))
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
        return HeadingKey.isHeading(columnName) && StringUtils.containsIgnoreCase(columnName, "error") ? HeadingErrorKey.DEFAULT
                : null;
    }
}
