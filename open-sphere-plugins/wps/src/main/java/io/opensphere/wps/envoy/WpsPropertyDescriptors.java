package io.opensphere.wps.envoy;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.wps.request.WpsProcessConfiguration;
import net.opengis.wps._100.ProcessDescriptionType;
import net.opengis.wps._100.WPSCapabilitiesType;

/**
 * The set of property descriptors used in WPS server interactions.
 */
public final class WpsPropertyDescriptors
{
    /**
     * The descriptor for the WPS Get Capabilities property.
     */
    public static final PropertyDescriptor<WPSCapabilitiesType> WPS_GET_CAPABILITIES = new PropertyDescriptor<>(
            "wpsGetCapabilties", WPSCapabilitiesType.class);

    /**
     * The descriptor for the WPS Describe Process Type property.
     */
    public static final PropertyDescriptor<ProcessDescriptionType> WPS_DESCRIBE_PROCESS = new PropertyDescriptor<>(
            "wpsDescribeProcessType", ProcessDescriptionType.class);

    /**
     * The descriptor for the WPS Save Process Configuration property.
     */
    public static final PropertyDescriptor<WpsProcessConfiguration> WPS_SAVE_PROCESS_CONFIGURATION = new PropertyDescriptor<>(
            "wpsSaveProcessConfiguration", WpsProcessConfiguration.class);

    /**
     * The descriptor for the WPS process ID field.
     */
    public static final PropertyDescriptor<String> PROCESS_ID_DESCRIPTOR = PropertyDescriptor
            .create(WpsParameter.PROCESS_ID.getVariableName(), String.class);

    /**
     * Default constructor, hidden to prevent instantiation.
     */
    private WpsPropertyDescriptors()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }
}
