package io.opensphere.mantle.data.impl.specialkey;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;

/**
 * A {@link SpecialKey} for heading / bearing / course.
 */
public class HeadingKey extends AbstractSpecialKey
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

    /** An enumeration of heading units. */
    public enum HeadingUnit
    {
        /** DEGREES_CLOCKWISE_FROM_NORTH. */
        DEGREES_CLOCKWISE_FROM_NORTH
    }

    /**
     * Inspects the supplied column name, to determine if it represents a
     * heading. If the column name represents a heading, the metadata is updated
     * to reflect this.
     *
     * @param metaData the meta data to update if the supplied column represents
     *            a heading.
     * @param columnName the name of the column to inspect
     * @return whether the column was detected
     */
    public static boolean detectHeading(MetaDataInfo metaData, String columnName)
    {
        boolean wasDetected = false;
        if (!metaData.hasTypeForSpecialKey(HeadingKey.DEFAULT) && isHeading(columnName))
        {
            metaData.setSpecialKey(columnName, HeadingKey.DEFAULT, metaData);
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
    public static boolean isHeading(String columnName)
    {
        return StringUtils.containsIgnoreCase(columnName, "course") || StringUtils.containsIgnoreCase(columnName, "heading")
                || StringUtils.containsIgnoreCase(columnName, "bearing");
    }
}
