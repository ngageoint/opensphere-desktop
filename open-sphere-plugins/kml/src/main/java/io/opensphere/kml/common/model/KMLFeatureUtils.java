package io.opensphere.kml.common.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Link;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.SchemaData;
import de.micromata.opengis.kml.v_2_2_0.SimpleData;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Feature utility methods.
 */
public final class KMLFeatureUtils
{
    /**
     * Gets the extended data map for the given feature.
     *
     * @param feature The feature
     * @return The extended data map
     */
    public static Map<String, String> getExtendedDataMap(Feature feature)
    {
        Map<String, String> nameToValueMap = new LinkedHashMap<>();
        if (feature.getExtendedData() != null)
        {
            for (Data data : feature.getExtendedData().getData())
            {
                String displayName = !StringUtils.isBlank(data.getDisplayName()) ? data.getDisplayName() : data.getName();
                nameToValueMap.put(StringUtilities.safeTrim(displayName), StringUtilities.safeTrim(data.getValue()));
            }
            for (SchemaData schemaData : feature.getExtendedData().getSchemaData())
            {
                for (SimpleData simpleData : schemaData.getSimpleData())
                {
                    nameToValueMap.put(StringUtilities.safeTrim(simpleData.getName()),
                            StringUtilities.safeTrim(simpleData.getValue()));
                }
            }
        }
        return nameToValueMap;
    }

    /**
     * Convenience method to get the Link or Url from a NetworkLink.
     *
     * @param networkLink The NetworkLink
     * @return The Link or Url
     */
    public static Link getLink(NetworkLink networkLink)
    {
        return networkLink.getLink() != null ? networkLink.getLink() : networkLink.getUrl();
    }

    /**
     * Private constructor.
     */
    private KMLFeatureUtils()
    {
    }
}
