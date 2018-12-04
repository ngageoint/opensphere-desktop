package io.opensphere.mantle.data.impl.specialkey;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialColumnDetector;
import io.opensphere.mantle.data.SpecialKey;

/**
 * A key used to handle special fields for color purposes.
 */
public class ColorKey extends AbstractSpecialKey implements SpecialColumnDetector
{
    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = -4181277708742108590L;

    /** The constant containing the name of the special key. */
    public static final String COLOR_SPECIAL_KEY_NAME = "Color";

    /** The default ColorKey. */
    public static final ColorKey DEFAULT = new ColorKey();

    /**
     * Creates a new color key.
     */
    public ColorKey()
    {
        super(COLOR_SPECIAL_KEY_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.SpecialColumnDetector#markSpecialColumn(io.opensphere.mantle.data.MetaDataInfo,
     *      java.lang.String)
     */
    @Override
    public boolean markSpecialColumn(MetaDataInfo layerInfo, String columnName)
    {
        boolean wasDetected = false;
        if (!layerInfo.hasTypeForSpecialKey(ColorKey.DEFAULT))
        {
            SpecialKey specialKey = detectColumn(columnName);
            if (specialKey != null)
            {
                layerInfo.setSpecialKey(columnName, specialKey, layerInfo);
                wasDetected = true;
            }
        }
        return wasDetected;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.SpecialColumnDetector#detectColumn(java.lang.String)
     */
    @Override
    public SpecialKey detectColumn(String columnName)
    {
        SpecialKey specialKey = null;
        if (StringUtils.containsIgnoreCase("color", columnName) && !StringUtils.containsIgnoreCase(columnName, "error"))
        {
            specialKey = ColorKey.DEFAULT;
        }
        return specialKey;
    }
}
