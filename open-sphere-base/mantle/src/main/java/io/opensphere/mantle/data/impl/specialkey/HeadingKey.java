package io.opensphere.mantle.data.impl.specialkey;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialColumnDetector;
import io.opensphere.mantle.data.SpecialKey;

/**
 * A {@link SpecialKey} for heading / bearing / course.
 */
public class HeadingKey extends AbstractSpecialKey implements SpecialColumnDetector
{
    /** The default HeadingKey. */
    public static final HeadingKey DEFAULT = new HeadingKey();

    /** The name of this key. */
    private static final String NAME = "Heading";

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new heading key with the DEGREES_CLOCKWISE_FROM_NORTH
     * unit.
     */
    public HeadingKey()
    {
        super(NAME, HeadingUnit.DEGREES_CLOCKWISE_FROM_NORTH);
    }

    /**
     * Instantiates a new heading key.
     *
     * @param unit the orientation unit
     */
    public HeadingKey(HeadingUnit unit)
    {
        super(NAME, unit);
    }

    @Override
    public HeadingUnit getKeyUnit()
    {
        return (HeadingUnit)super.getKeyUnit();
    }

    @Override
    public boolean markSpecialColumn(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        if (!metaData.hasTypeForSpecialKey(HeadingKey.DEFAULT))
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
        return isHeading(columnName) ? HeadingKey.DEFAULT : null;
    }

    /**
     * Inspects the supplied column name, to determine if it represents a heading.
     *
     * @param columnName the name of the column to inspect
     * @return whether the column is determined to be a heading column
     */
    public static boolean isHeading(String columnName)
    {
        return StringUtils.containsIgnoreCase(columnName, "course") || StringUtils.containsIgnoreCase(columnName, "heading")
                || StringUtils.containsIgnoreCase(columnName, "bearing");
    }

    /** An enumeration of heading units. */
    public enum HeadingUnit
    {
        /** DEGREES_CLOCKWISE_FROM_NORTH. */
        DEGREES_CLOCKWISE_FROM_NORTH
    }
}
