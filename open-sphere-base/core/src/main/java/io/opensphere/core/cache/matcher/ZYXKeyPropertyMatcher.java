package io.opensphere.core.cache.matcher;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.ZYXImageKey;

/**
 * A property matcher that matches a {@link ZYXImageKey}.
 */
public class ZYXKeyPropertyMatcher extends GeneralPropertyMatcher<String>
{
    /** The key for the image. */
    private final ZYXImageKey myImageKey;

    /**
     * Constructor.
     *
     * @param propertyDescriptor The {@link PropertyDescriptor} describing the
     *            column for the key.
     * @param imageKey The key for the image.
     */
    public ZYXKeyPropertyMatcher(PropertyDescriptor<String> propertyDescriptor, ZYXImageKey imageKey)
    {
        super(propertyDescriptor, imageKey.toString());
        myImageKey = imageKey;
    }

    @Override
    @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    /**
     * Accessor for the image key.
     *
     * @return The image key.
     */
    public ZYXImageKey getImageKey()
    {
        return myImageKey;
    }

    @Override
    @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
    public int hashCode()
    {
        return super.hashCode();
    }
}
