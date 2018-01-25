package io.opensphere.osh.model;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ToStringHelper;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.specialkey.AltitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/** Bean that stores property info. */
@Immutable
public class PropertyInfo
{
    /** The map of property to property info. */
    private static final Map<String, PropertyInfo> OUR_PROPERTY_INFO_MAP = createPropertyMap();

    /**
     * Creates the property map.
     *
     * @return the property map
     */
    private static Map<String, PropertyInfo> createPropertyMap()
    {
        Map<String, PropertyInfo> map = New.map();
        PropertyInfo samplingTime = new PropertyInfo("http://www.opengis.net/def/property/OGC/0/SamplingTime", Date.class,
                TimeKey.DEFAULT);
        PropertyInfo lat = new PropertyInfo("http://sensorml.com/ont/swe/property/Latitude", Float.class, LatitudeKey.DEFAULT);
        PropertyInfo lon = new PropertyInfo("http://sensorml.com/ont/swe/property/Longitude", Float.class, LongitudeKey.DEFAULT);
        PropertyInfo alt = new PropertyInfo("http://sensorml.com/ont/swe/property/Altitude", Float.class, AltitudeKey.DEFAULT);
        map.put(samplingTime.getProperty(), samplingTime);
        map.put(lat.getProperty(), lat);
        map.put(lon.getProperty(), lon);
        map.put(alt.getProperty(), alt);
        map.put("lat", lat);
        map.put("lon", lon);
        map.put("alt", alt);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Gets the property for the given field, if there is one.
     *
     * @param field the field
     * @return the property, or null
     */
    public static PropertyInfo getProperty(Field field)
    {
        String mapKey = field.getProperty() != null ? field.getProperty() : field.getName();
        return PropertyInfo.OUR_PROPERTY_INFO_MAP.get(mapKey);
    }

    /**
     * Determines if a given field should be excluded from the metadata and
     * being read from the data.
     *
     * @param field the field
     * @return whether it's excluded
     */
    public static boolean isExcluded(Field field)
    {
        /* Hack: filter out 'number of points' for plume layer as it's not
         * supplied in the data. */
        return "num_pos".equals(field.getName());
    }

    /** The property. */
    private final String myProperty;

    /** The class. */
    private final Class<?> myClass;

    /** The special key. */
    private final SpecialKey mySpecialKey;

    /**
     * Constructor.
     *
     * @param property the property
     * @param class1 the class
     * @param specialKey the special key (or null)
     */
    public PropertyInfo(String property, Class<?> class1, SpecialKey specialKey)
    {
        myProperty = property;
        myClass = class1;
        mySpecialKey = specialKey;
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public String getProperty()
    {
        return myProperty;
    }

    /**
     * Gets the class.
     *
     * @return the class
     */
    public Class<?> getPropertyClass()
    {
        return myClass;
    }

    /**
     * Gets the specialKey.
     *
     * @return the specialKey
     */
    public SpecialKey getSpecialKey()
    {
        return mySpecialKey;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("property", myProperty);
        helper.add("class", myClass);
        helper.add("specialKey", mySpecialKey);
        return helper.toString();
    }
}
