package io.opensphere.osh.util;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.osh.model.Offering;
import io.opensphere.osh.model.Output;

/** OpenSensorHub data registry utilities. */
public final class OSHRegistryUtils
{
    /** The {@link PropertyDescriptor} for the OSH get capabilities class. */
    public static final PropertyDescriptor<Offering> GET_CAPABILITIES_DESCRIPTOR = new PropertyDescriptor<>("GetCapabilities",
            Offering.class);

    /** The get capabilities data model category family. */
    public static final String GET_CAPABILITIES_FAMILY = "OSH.GetCapabilities";

    /** The {@link PropertyDescriptor} for the OSH describe sensor class. */
    public static final PropertyDescriptor<Output> DESCRIBE_SENSOR_DESCRIPTOR = new PropertyDescriptor<>("DescribeSensor",
            Output.class);

    /** The describe sensor data model category family. */
    public static final String DESCRIBE_SENSOR_FAMILY = "OSH.DescribeSensor";

    /** The {@link PropertyDescriptor} for the OSH get result template class. */
    public static final PropertyDescriptor<Output> GET_RESULT_TEMPLATE_DESCRIPTOR = new PropertyDescriptor<>("GetResultTemplate",
            Output.class);

    /** The get result template data model category family. */
    public static final String GET_RESULT_TEMPLATE_FAMILY = "OSH.GetResultTemplate";

    /** The {@link PropertyDescriptor} for the OSH get result class. */
    public static final PropertyDescriptor<CancellableInputStream> GET_RESULT_DESCRIPTOR = new PropertyDescriptor<>("GetResult",
            CancellableInputStream.class);

    /** The get result data model category family. */
    public static final String GET_RESULT_FAMILY = "OSH.GetResult";

    /** The {@link PropertyDescriptor} for OSH video results. */
    public static final PropertyDescriptor<byte[]> VIDEO_RESULT_DESCRIPTOR = new PropertyDescriptor<>("VideoResult",
            byte[].class);

    /**
     * Creates a get capabilities data model category.
     *
     * @param url the URL
     * @return the data model category
     */
    public static DataModelCategory newGetCapabilitiesCategory(String url)
    {
        return new DataModelCategory(url, GET_CAPABILITIES_FAMILY, "");
    }

    /**
     * Creates a describe sensor data model category.
     *
     * @param url the URL
     * @param procedure the procedure
     * @return the data model category
     */
    public static DataModelCategory newDescribeSensorCategory(String url, String procedure)
    {
        return new DataModelCategory(url, DESCRIBE_SENSOR_FAMILY, procedure);
    }

    /**
     * Creates a get result template data model category.
     *
     * @param url the URL
     * @param offeringId the offering ID
     * @param property the property
     * @return the data model category
     */
    public static DataModelCategory newGetResultTemplateCategory(String url, String offeringId, String property)
    {
        return new DataModelCategory(url, GET_RESULT_TEMPLATE_FAMILY, StringUtilities.concat(offeringId, "|", property));
    }

    /**
     * Creates a get result data model category.
     *
     * @param url the URL
     * @param offeringId the offering ID
     * @param property the property
     * @return the data model category
     */
    public static DataModelCategory newGetResultCategory(String url, String offeringId, String property)
    {
        return new DataModelCategory(url, GET_RESULT_FAMILY, StringUtilities.concat(offeringId, "|", property));
    }

    /**
     * Creates a video result data model category.
     *
     * @param typeKey the type key
     * @return the data model category
     */
    public static DataModelCategory newVideoResultCategory(String typeKey)
    {
        return new DataModelCategory(typeKey, "VideoResult", "");
    }

    /**
     * Gets the URL string from the data model category.
     *
     * @param category the data model category
     * @return the URL string
     */
    public static String getUrl(DataModelCategory category)
    {
        return category.getSource();
    }

    /**
     * Gets the procedure from the data model category.
     *
     * @param category the data model category
     * @return the procedure
     */
    public static String getProcedure(DataModelCategory category)
    {
        return category.getCategory();
    }

    /**
     * Gets the offering ID from the data model category.
     *
     * @param category the data model category
     * @return the offering ID
     */
    public static String getOfferingId(DataModelCategory category)
    {
        return category.getCategory().split("\\|")[0];
    }

    /**
     * Gets the property from the data model category.
     *
     * @param category the data model category
     * @return the property
     */
    public static String getProperty(DataModelCategory category)
    {
        return category.getCategory().split("\\|")[1];
    }

    /** Disallow instantiation. */
    private OSHRegistryUtils()
    {
    }
}
