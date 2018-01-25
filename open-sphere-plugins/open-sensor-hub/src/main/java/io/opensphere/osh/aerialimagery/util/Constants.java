package io.opensphere.osh.aerialimagery.util;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Contains some constaints.
 */
public final class Constants
{
    /** The descriptor for historical feed metadata. */
    public static final PropertyDescriptor<Long> METADATA_TIMESTAMP_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>("timestamp",
            Long.class);

    /**
     * The platform metadata property descriptor.
     */
    public static final PropertyDescriptor<PlatformMetadata> PLATFORM_METADATA_DESCRIPTOR = new PropertyDescriptor<>("metadata",
            PlatformMetadata.class);

    /**
     * The platform metadata family.
     */
    public static final String PLATFORM_METADATA_FAMILY = PlatformMetadata.class.getName();

    /**
     * Not constructible.
     */
    private Constants()
    {
    }
}
