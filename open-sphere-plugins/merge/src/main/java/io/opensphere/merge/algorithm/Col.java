package io.opensphere.merge.algorithm;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.SpecialKey;

/**
 * Struct for holding data column parameters. They are a pain in the butt to
 * acquire, so it is convenient to do so once and store them where they can be
 * used easily.
 */
public class Col
{
    /** Reference to the type to which this column belongs. */
    public DataTypeInfo owner;

    /** The name of the column. */
    public String name;

    /**
     * If this column is mapped with other columns, this is the name the user
     * would like to see.
     */
    public String definedName;

    /** The value type for the column. */
    public Class<?> type;

    /** Special designation, if any. */
    public SpecialKey special;

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Col:\n\tName: '" + name + "'\n\tDefined Name: '" + definedName + "'\n\tType: '" + type.getName()
                + "'\n\tSpecial Key: " + ToStringBuilder.reflectionToString(special) + "\n\tOwner: '" + owner.getTypeKey() + "'";
    }
}
