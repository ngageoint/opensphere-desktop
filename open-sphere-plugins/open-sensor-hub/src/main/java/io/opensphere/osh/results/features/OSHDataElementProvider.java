package io.opensphere.osh.results.features;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.mantle.crust.AbstractSpaceTimeDataElementProvider;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/** OpenSensorHub DataElementProvider. */
class OSHDataElementProvider extends AbstractSpaceTimeDataElementProvider<List<? extends Serializable>>
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(OSHDataElementProvider.class);

    /** The ID counter. */
    private static final AtomicInteger ourIDCounter = new AtomicInteger(0);

    /**
     * Constructor.
     *
     * @param dataType the data type
     * @param results the results
     */
    public OSHDataElementProvider(DefaultDataTypeInfo dataType, Collection<? extends List<? extends Serializable>> results)
    {
        super(dataType, results);
    }

    @Override
    protected MapDataElement createDataElement(List<? extends Serializable> values)
    {
        DataTypeInfo dataType = getDataTypeInfo();
        MetaDataInfo metaDataInfo = dataType.getMetaDataInfo();
        MetaDataProvider metaDataProvider = createMetaDataProvider(metaDataInfo, values);
        LocationTimeSpan locationAndTime = getLocationAndTime(metaDataInfo, metaDataProvider);

        SimpleMapPointGeometrySupport geom = new SimpleMapPointGeometrySupport(locationAndTime.myLocation);
        geom.setTimeSpan(locationAndTime.mySpan);
        DefaultMapDataElement element = new DefaultMapDataElement(ourIDCounter.incrementAndGet(), locationAndTime.mySpan,
                dataType, metaDataProvider, geom);
        element.getVisualizationState().setColor(dataType.getBasicVisualizationInfo().getTypeColor());
        return element;
    }

    /**
     * Gets the location and time span.
     *
     * @param metaDataInfo the meta data info
     * @param metaDataProvider the meta data provider
     * @return the location and time span
     */
    private static LocationTimeSpan getLocationAndTime(MetaDataInfo metaDataInfo, MetaDataProvider metaDataProvider)
    {
        TimeSpan span = TimeSpan.TIMELESS;
        double lat = 0;
        double lon = 0;
        double alt = 0;

        List<String> keys = metaDataProvider.getKeys();
        List<Object> values = metaDataProvider.getValues();
        for (int i = 0; i < keys.size(); i++)
        {
            String key = keys.get(i);
            Object value = values.get(i);

            SpecialKey specialType = metaDataInfo.getSpecialTypeForKey(key);
            if (specialType == TimeKey.DEFAULT)
            {
                if (value instanceof String)
                {
                    try
                    {
                        span = TimeSpan.get(DateTimeUtilities.parseISO8601Date((String)value));
                    }
                    catch (ParseException e)
                    {
                        LOGGER.error(e);
                    }
                }
                else
                {
                    span = TimeSpan.get(((Number)value).longValue());
                }
            }
            else if (specialType == LatitudeKey.DEFAULT)
            {
                lat = ((Number)value).doubleValue();
            }
            else if (specialType == LongitudeKey.DEFAULT)
            {
                lon = ((Number)value).doubleValue();
            }
//            else if (specialType == AltitudeKey.DEFAULT)
//            {
//                alt = ((Number)value).doubleValue();
//            }
        }
        return new LocationTimeSpan(LatLonAlt.createFromDegreesMeters(lat, lon, alt, ReferenceLevel.TERRAIN), span);
    }

    /**
     * Creates a meta data provider with the given values.
     *
     * @param metaDataInfo the MetaDataInfo
     * @param values the values
     * @return the meta data provider
     */
    private static MetaDataProvider createMetaDataProvider(MetaDataInfo metaDataInfo, List<? extends Serializable> values)
    {
        MetaDataProvider metaDataProvider = new MDILinkedMetaDataProvider(metaDataInfo);
        List<String> keys = metaDataInfo.getKeyNames();
        for (int i = 0; i < keys.size(); i++)
        {
            metaDataProvider.setValue(keys.get(i), values.get(i));
        }
        return metaDataProvider;
    }

    /** Location and time span. */
    private static class LocationTimeSpan
    {
        /** The location. */
        private final LatLonAlt myLocation;

        /** The time span. */
        private final TimeSpan mySpan;

        /**
         * Constructor.
         *
         * @param location The location
         * @param span The time span
         */
        public LocationTimeSpan(LatLonAlt location, TimeSpan span)
        {
            myLocation = location;
            mySpan = span;
        }
    }
}
